package com.tkno.links.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Request
import okhttp3.ResponseBody
import java.io.File
import java.util.regex.Pattern

object UpdateUtil {

    private const val OWNER = "hamzabellouch"
    private const val REPO = "links"
    private const val TAG = "UpdateUtil"

    private val client = HttpClient.client
    private val jsonFormat = Json { ignoreUnknownKeys = true }

    private fun getReleases(): List<Release> {
        val request = Request.Builder()
            .url("https://api.github.com/repos/$OWNER/$REPO/releases")
            .header("User-Agent", "Mozilla/5.0 Links")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val bodyString = response.body?.string() ?: return emptyList()
                jsonFormat.decodeFromString<List<Release>>(bodyString)
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "Failed to fetch releases list", e)
            emptyList()
        }
    }

    suspend fun checkForUpdate(context: Context, includePrerelease: Boolean = true): Release? =
        withContext(Dispatchers.IO) {
            val currentVersion = context.getCurrentVersion()
            val releases = getReleases()
            if (releases.isEmpty()) return@withContext null

            val candidate = releases
                .filter { release -> release.draft != true }
                .filter { release -> includePrerelease || release.preRelease != true }
                .filter { release -> release.assets?.any { it.name?.endsWith(".apk", ignoreCase = true) == true } == true }
                .maxByOrNull { release -> (release.tagName ?: release.name).toVersion() }
                ?: return@withContext null

            val latestVersion = (candidate.tagName ?: candidate.name).toVersion()
            if (currentVersion < latestVersion) candidate else null
        }

    private fun Context.getCurrentVersion(): Version =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager
                .getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                .versionName
                ?.toVersion() ?: EMPTY_VERSION
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0).versionName?.toVersion() ?: EMPTY_VERSION
        }

    private fun Context.getLatestApk() = File(getExternalFilesDir("apk"), "latest.apk")

    fun installLatestApk(context: Context) {
        runCatching {
            val file = context.getLatestApk()
            if (!file.exists() || file.length() == 0L) {
                Log.e(TAG, "APK file does not exist or is empty")
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                    return
                }
            }
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(contentUri, "application/vnd.android.package-archive")
            }
            context.startActivity(intent)
        }.onFailure { Log.e(TAG, "Failed to launch package installer", it) }
    }

    suspend fun downloadApk(context: Context, release: Release): Flow<DownloadStatus> =
        withContext(Dispatchers.IO) {
            val saveFile = context.getLatestApk()
            val apks = release.assets?.filter { it.name?.endsWith(".apk", ignoreCase = true) == true }
            if (apks.isNullOrEmpty()) return@withContext emptyFlow()

            val abiList = Build.SUPPORTED_ABIS
            val targetAsset = abiList.firstNotNullOfOrNull { abi ->
                apks.find { it.name?.contains(abi, ignoreCase = true) == true }
            } ?: apks.find { it.name?.contains("universal", ignoreCase = true) == true }
            ?: apks.firstOrNull()

            val targetUrl = targetAsset?.browserDownloadUrl ?: return@withContext emptyFlow()

            try {
                val request = Request.Builder()
                    .url(targetUrl)
                    .header("User-Agent", "Mozilla/5.0 Links")
                    .build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.e(TAG, "Download failed with HTTP ${response.code}")
                    response.close()
                    return@withContext emptyFlow()
                }
                val responseBody = response.body
                if (responseBody == null) {
                    response.close()
                    return@withContext emptyFlow()
                }
                return@withContext responseBody.downloadFileWithProgress(saveFile)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e(TAG, "Download error", e)
            }
            emptyFlow()
        }

    private fun ResponseBody.downloadFileWithProgress(saveFile: File): Flow<DownloadStatus> =
        flow {
            emit(DownloadStatus.Progress(0))
            var deleteFile = true
            try {
                use { body ->
                    body.byteStream().use { inputStream ->
                        saveFile.outputStream().use { outputStream ->
                            val totalBytes = body.contentLength()
                            val data = ByteArray(8_192)
                            var progressBytes = 0L

                            while (true) {
                                val bytes = inputStream.read(data)
                                if (bytes == -1) break
                                outputStream.write(data, 0, bytes)
                                progressBytes += bytes
                                val percent = if (totalBytes > 0) {
                                    ((progressBytes * 100) / totalBytes).toInt()
                                } else 50
                                emit(DownloadStatus.Progress(percent = percent))
                            }

                            when {
                                totalBytes > 0 && progressBytes < totalBytes ->
                                    throw Exception("missing bytes")
                                totalBytes > 0 && progressBytes > totalBytes ->
                                    throw Exception("too many bytes")
                                else -> deleteFile = false
                            }
                        }
                    }
                }
                emit(DownloadStatus.Finished(saveFile))
            } finally {
                if (deleteFile) saveFile.delete()
            }
        }
        .flowOn(Dispatchers.IO)
        .distinctUntilChanged()

    @Serializable
    data class Release(
        @SerialName("html_url") val htmlUrl: String? = null,
        @SerialName("tag_name") val tagName: String? = null,
        val name: String? = null,
        val draft: Boolean? = null,
        @SerialName("prerelease") val preRelease: Boolean? = null,
        @SerialName("created_at") val createdAt: String? = null,
        @SerialName("published_at") val publishedAt: String? = null,
        val assets: List<AssetsItem>? = null,
        val body: String? = null,
    )

    @Serializable
    data class AssetsItem(
        val name: String? = null,
        @SerialName("content_type") val contentType: String? = null,
        val size: Long? = null,
        @SerialName("download_count") val downloadCount: Int? = null,
        @SerialName("created_at") val createdAt: String? = null,
        @SerialName("updated_at") val updatedAt: String? = null,
        @SerialName("browser_download_url") val browserDownloadUrl: String? = null,
    )

    sealed class DownloadStatus {
        object NotYet : DownloadStatus()
        data class Progress(val percent: Int) : DownloadStatus()
        data class Finished(val file: File) : DownloadStatus()
    }

    private val pattern = Pattern.compile("""v?(\d+)\.(\d+)(?:\.(\d+))?(?:[-.](alpha|beta|rc)(?:[\.-]?(\d+))?)?""", Pattern.CASE_INSENSITIVE)
    private val EMPTY_VERSION = Version.Stable()

    fun String?.toVersion(): Version =
        this?.run {
            val matcher = pattern.matcher(this.trim())
            if (matcher.find()) {
                val major = matcher.group(1)?.toIntOrNull() ?: 0
                val minor = matcher.group(2)?.toIntOrNull() ?: 0
                val patch = matcher.group(3)?.toIntOrNull() ?: 0
                val buildNumber = matcher.group(5)?.toIntOrNull() ?: 0
                val tag = matcher.group(4)?.lowercase()
                when (tag) {
                    "alpha" -> Version.Alpha(major, minor, patch, buildNumber)
                    "beta" -> Version.Beta(major, minor, patch, buildNumber)
                    "rc" -> Version.ReleaseCandidate(major, minor, patch, buildNumber)
                    else -> Version.Stable(major, minor, patch)
                }
            } else EMPTY_VERSION
        } ?: EMPTY_VERSION

    sealed class Version(val major: Int, val minor: Int, val patch: Int, val build: Int = 0) :
        Comparable<Version> {
        companion object {
            private const val BUILD = 10L
            private const val VARIANT = 100L
            private const val PATCH = 10_000L
            private const val MINOR = 1_000_000L
            private const val MAJOR = 100_000_000L

            private const val STABLE = VARIANT * 4
            private const val ALPHA = VARIANT * 1
            private const val BETA = VARIANT * 2
            private const val RELEASE_CANDIDATE = VARIANT * 3
        }

        abstract fun toNumber(): Long

        class Alpha(
            versionMajor: Int = 0, versionMinor: Int = 0, versionPatch: Int = 0, versionBuild: Int = 0
        ) : Version(versionMajor, versionMinor, versionPatch, versionBuild) {
            override fun toNumber(): Long =
                major * MAJOR + minor * MINOR + patch * PATCH + build * BUILD + ALPHA
        }

        class Beta(
            versionMajor: Int = 0, versionMinor: Int = 0, versionPatch: Int = 0, versionBuild: Int = 0
        ) : Version(versionMajor, versionMinor, versionPatch, versionBuild) {
            override fun toNumber(): Long =
                major * MAJOR + minor * MINOR + patch * PATCH + build * BUILD + BETA
        }

        class ReleaseCandidate(
            versionMajor: Int = 0, versionMinor: Int = 0, versionPatch: Int = 0, versionBuild: Int = 0
        ) : Version(versionMajor, versionMinor, versionPatch, versionBuild) {
            override fun toNumber(): Long =
                major * MAJOR + minor * MINOR + patch * PATCH + build * BUILD + RELEASE_CANDIDATE
        }

        class Stable(
            versionMajor: Int = 0, versionMinor: Int = 0, versionPatch: Int = 0
        ) : Version(versionMajor, versionMinor, versionPatch) {
            override fun toNumber(): Long =
                major * MAJOR + minor * MINOR + patch * PATCH + build * BUILD + STABLE
        }

        override operator fun compareTo(other: Version): Int =
            this.toNumber().compareTo(other.toNumber())
    }
}
