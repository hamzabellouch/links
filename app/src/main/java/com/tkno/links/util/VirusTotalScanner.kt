package com.tkno.links.util

import android.content.Context
import android.util.Base64
import android.util.Log
import com.tkno.links.LinkResolver
import com.tkno.links.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

object VirusTotalScanner {

    private const val TAG = "VirusTotalScanner"
    private const val PREFS_NAME = "links_prefs"
    private const val PREF_API_KEY = "virustotal_api_key"

    enum class SafetyStatus {
        SAFE,
        SUSPICIOUS,
        MALICIOUS,
        UNKNOWN
    }

    data class SecurityReport(
        val sourceUrl: String,
        val destinationUrl: String,
        val harmlessCount: Int,
        val maliciousCount: Int,
        val suspiciousCount: Int,
        val undetectedCount: Int,
        val timeoutCount: Int,
        val reputation: Int,
        val title: String?,
        val categories: List<String>,
        val safetyStatus: SafetyStatus
    ) {
        val totalEngines: Int
            get() = harmlessCount + maliciousCount + suspiciousCount + undetectedCount + timeoutCount

        val detectionCount: Int
            get() = maliciousCount + suspiciousCount
    }

    sealed class ScanResult {
        data class Success(val report: SecurityReport) : ScanResult()
        data class Error(val error: ScanError) : ScanResult()
    }

    sealed class ScanError {
        object MissingApiKey : ScanError()
        object InvalidApiKey : ScanError()
        object QuotaExceeded : ScanError()
        object NoInternet : ScanError()
        object InvalidUrl : ScanError()
        data class Custom(val message: String) : ScanError()

        fun getLocalizedMessage(context: Context): String {
            return when (this) {
                is MissingApiKey -> context.getString(R.string.api_key_required)
                is InvalidApiKey -> context.getString(R.string.invalid_api_key)
                is QuotaExceeded -> context.getString(R.string.quota_exceeded)
                is NoInternet -> context.getString(R.string.no_internet_error)
                is InvalidUrl -> context.getString(R.string.invalid_url_format)
                is Custom -> message
            }
        }
    }

    fun getApiKey(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(PREF_API_KEY, "")?.trim() ?: ""
    }

    fun saveApiKey(context: Context, key: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(PREF_API_KEY, key.trim())
            .apply()
    }

    fun clearApiKey(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(PREF_API_KEY)
            .apply()
    }

    fun hasApiKey(context: Context): Boolean {
        return getApiKey(context).isNotEmpty()
    }

    /**
     * VirusTotal URL identifier is the base64url representation of the URL without padding ('=').
     */
    fun encodeUrlId(url: String): String {
        val trimmed = url.trim()
        val normalizedUrl = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            "https://$trimmed"
        } else {
            trimmed
        }
        return Base64.encodeToString(
            normalizedUrl.toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        ).trim().trimEnd('=')
    }

    /**
     * Scans a given URL by first resolving redirects and querying VirusTotal v3.
     */
    suspend fun scanUrl(context: Context, inputUrl: String): ScanResult = withContext(Dispatchers.IO) {
        val trimmedInput = inputUrl.trim()
        if (trimmedInput.isEmpty() || !LinkResolver.isValidUrl(trimmedInput)) {
            return@withContext ScanResult.Error(ScanError.InvalidUrl)
        }

        val apiKey = getApiKey(context)
        if (apiKey.isEmpty()) {
            return@withContext ScanResult.Error(ScanError.MissingApiKey)
        }

        if (!LinkResolver.isNetworkAvailable(context)) {
            return@withContext ScanResult.Error(ScanError.NoInternet)
        }

        // 1. Resolve source and destination
        var sourceUrl = if (!trimmedInput.startsWith("http://") && !trimmedInput.startsWith("https://")) {
            "https://$trimmedInput"
        } else {
            trimmedInput
        }
        var destinationUrl = sourceUrl

        try {
            when (val res = LinkResolver.resolveDetailed(context, trimmedInput)) {
                is LinkResolver.Result.Success -> {
                    sourceUrl = res.source
                    destinationUrl = res.destination
                }
                is LinkResolver.Result.Error -> {
                    // Fall back to the normalized input URL if redirect resolution fails
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Redirect resolution warning: ${e.message}")
        }

        // 2. Query VirusTotal for the destination URL (or source)
        val targetUrl = destinationUrl
        val urlId = encodeUrlId(targetUrl)
        val client = HttpClient.client

        val getRequest = Request.Builder()
            .url("https://www.virustotal.com/api/v3/urls/$urlId")
            .header("x-apikey", apiKey)
            .header("User-Agent", "Links-Android-App")
            .build()

        try {
            client.newCall(getRequest).execute().use { response ->
                when (response.code) {
                    200 -> {
                        val body = response.body?.string() ?: ""
                        return@withContext parseUrlReport(body, sourceUrl, destinationUrl)
                    }
                    401, 403 -> {
                        return@withContext ScanResult.Error(ScanError.InvalidApiKey)
                    }
                    429 -> {
                        return@withContext ScanResult.Error(ScanError.QuotaExceeded)
                    }
                    404 -> {
                        // URL not found in VT database, submit it for analysis
                        return@withContext submitAndFetchAnalysis(apiKey, targetUrl, sourceUrl, destinationUrl)
                    }
                    else -> {
                        val body = response.body?.string() ?: ""
                        val errMsg = extractErrorMessage(body) ?: "HTTP Error ${response.code}"
                        return@withContext ScanResult.Error(ScanError.Custom(errMsg))
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error during scan", e)
            return@withContext ScanResult.Error(ScanError.NoInternet)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during scan", e)
            return@withContext ScanResult.Error(ScanError.Custom(e.message ?: "Unknown error occurred"))
        }
    }

    private suspend fun submitAndFetchAnalysis(
        apiKey: String,
        targetUrl: String,
        sourceUrl: String,
        destinationUrl: String
    ): ScanResult {
        val client = HttpClient.client
        val formBody = FormBody.Builder()
            .add("url", targetUrl)
            .build()

        val postRequest = Request.Builder()
            .url("https://www.virustotal.com/api/v3/urls")
            .header("x-apikey", apiKey)
            .header("User-Agent", "Links-Android-App")
            .post(formBody)
            .build()

        return try {
            client.newCall(postRequest).execute().use { postResponse ->
                when (postResponse.code) {
                    401, 403 -> ScanResult.Error(ScanError.InvalidApiKey)
                    429 -> ScanResult.Error(ScanError.QuotaExceeded)
                    200, 201 -> {
                        val postBody = postResponse.body?.string() ?: ""
                        val postJson = JSONObject(postBody)
                        val analysisId = postJson.optJSONObject("data")?.optString("id")

                        if (!analysisId.isNullOrEmpty()) {
                            // Wait briefly for analysis to run
                            delay(2000)
                            val analysisRequest = Request.Builder()
                                .url("https://www.virustotal.com/api/v3/analyses/$analysisId")
                                .header("x-apikey", apiKey)
                                .header("User-Agent", "Links-Android-App")
                                .build()

                            client.newCall(analysisRequest).execute().use { analysisResponse ->
                                if (analysisResponse.isSuccessful) {
                                    val aBody = analysisResponse.body?.string() ?: ""
                                    parseAnalysisReport(aBody, sourceUrl, destinationUrl)
                                } else {
                                    // Default fresh clean/unrated report
                                    ScanResult.Success(
                                        SecurityReport(
                                            sourceUrl = sourceUrl,
                                            destinationUrl = destinationUrl,
                                            harmlessCount = 0,
                                            maliciousCount = 0,
                                            suspiciousCount = 0,
                                            undetectedCount = 0,
                                            timeoutCount = 0,
                                            reputation = 0,
                                            title = null,
                                            categories = emptyList(),
                                            safetyStatus = SafetyStatus.UNKNOWN
                                        )
                                    )
                                }
                            }
                        } else {
                            ScanResult.Error(ScanError.Custom("Failed to submit URL for analysis"))
                        }
                    }
                    else -> {
                        val body = postResponse.body?.string() ?: ""
                        val errMsg = extractErrorMessage(body) ?: "Submission error ${postResponse.code}"
                        ScanResult.Error(ScanError.Custom(errMsg))
                    }
                }
            }
        } catch (e: Exception) {
            ScanResult.Error(ScanError.Custom(e.message ?: "Analysis submission failed"))
        }
    }

    private fun parseUrlReport(
        jsonString: String,
        sourceUrl: String,
        destinationUrl: String
    ): ScanResult {
        return try {
            val json = JSONObject(jsonString)
            val dataObj = json.optJSONObject("data") ?: return ScanResult.Error(ScanError.Custom("Invalid API response format"))
            val attributes = dataObj.optJSONObject("attributes") ?: return ScanResult.Error(ScanError.Custom("Missing attributes in response"))

            val stats = attributes.optJSONObject("last_analysis_stats")
            val harmless = stats?.optInt("harmless", 0) ?: 0
            val malicious = stats?.optInt("malicious", 0) ?: 0
            val suspicious = stats?.optInt("suspicious", 0) ?: 0
            val undetected = stats?.optInt("undetected", 0) ?: 0
            val timeout = stats?.optInt("timeout", 0) ?: 0

            val reputation = attributes.optInt("reputation", 0)
            val title = if (attributes.has("title")) attributes.optString("title") else null

            val categoriesList = mutableListOf<String>()
            val catObj = attributes.optJSONObject("categories")
            if (catObj != null) {
                val keys = catObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val catVal = catObj.optString(key)
                    if (catVal.isNotBlank() && !categoriesList.contains(catVal)) {
                        categoriesList.add(catVal)
                    }
                }
            }

            val status = when {
                malicious > 0 -> SafetyStatus.MALICIOUS
                suspicious > 0 -> SafetyStatus.SUSPICIOUS
                harmless > 0 || undetected > 0 -> SafetyStatus.SAFE
                else -> SafetyStatus.UNKNOWN
            }

            ScanResult.Success(
                SecurityReport(
                    sourceUrl = sourceUrl,
                    destinationUrl = destinationUrl,
                    harmlessCount = harmless,
                    maliciousCount = malicious,
                    suspiciousCount = suspicious,
                    undetectedCount = undetected,
                    timeoutCount = timeout,
                    reputation = reputation,
                    title = title,
                    categories = categoriesList.take(5),
                    safetyStatus = status
                )
            )
        } catch (e: Exception) {
            ScanResult.Error(ScanError.Custom("Error parsing response: ${e.message}"))
        }
    }

    private fun parseAnalysisReport(
        jsonString: String,
        sourceUrl: String,
        destinationUrl: String
    ): ScanResult {
        return try {
            val json = JSONObject(jsonString)
            val dataObj = json.optJSONObject("data") ?: return ScanResult.Error(ScanError.Custom("Invalid API response format"))
            val attributes = dataObj.optJSONObject("attributes") ?: return ScanResult.Error(ScanError.Custom("Missing attributes in response"))

            val stats = attributes.optJSONObject("stats")
            val harmless = stats?.optInt("harmless", 0) ?: 0
            val malicious = stats?.optInt("malicious", 0) ?: 0
            val suspicious = stats?.optInt("suspicious", 0) ?: 0
            val undetected = stats?.optInt("undetected", 0) ?: 0
            val timeout = stats?.optInt("timeout", 0) ?: 0

            val status = when {
                malicious > 0 -> SafetyStatus.MALICIOUS
                suspicious > 0 -> SafetyStatus.SUSPICIOUS
                harmless > 0 || undetected > 0 -> SafetyStatus.SAFE
                else -> SafetyStatus.UNKNOWN
            }

            ScanResult.Success(
                SecurityReport(
                    sourceUrl = sourceUrl,
                    destinationUrl = destinationUrl,
                    harmlessCount = harmless,
                    maliciousCount = malicious,
                    suspiciousCount = suspicious,
                    undetectedCount = undetected,
                    timeoutCount = timeout,
                    reputation = 0,
                    title = null,
                    categories = emptyList(),
                    safetyStatus = status
                )
            )
        } catch (e: Exception) {
            ScanResult.Error(ScanError.Custom("Error parsing analysis: ${e.message}"))
        }
    }

    private fun extractErrorMessage(bodyString: String): String? {
        return try {
            val json = JSONObject(bodyString)
            json.optJSONObject("error")?.optString("message")
        } catch (e: Exception) {
            null
        }
    }
}
