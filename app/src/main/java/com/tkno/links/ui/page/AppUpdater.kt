package com.tkno.links.ui.page

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.tkno.links.util.UpdateNotificationHelper
import com.tkno.links.util.UpdateUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background composable that checks for updates on launch (if auto-update is enabled)
 * and sends a system notification when a new version is found.
 */
@Composable
fun AppUpdater(isAutoUpdateEnabled: Boolean) {
    val context = LocalContext.current

    val prefs = remember { context.getSharedPreferences("links_prefs", Context.MODE_PRIVATE) }
    val updateChannel = prefs.getInt("update_channel", 1) // 1: Preview, 0: Stable
    val includePrerelease = updateChannel == 1

    LaunchedEffect(isAutoUpdateEnabled, updateChannel) {
        if (!isAutoUpdateEnabled) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            runCatching {
                val foundRelease = UpdateUtil.checkForUpdate(context, includePrerelease = includePrerelease)
                if (foundRelease != null) {
                    val releaseTag = foundRelease.tagName ?: foundRelease.name ?: ""
                    val lastDownloadedTag = prefs.getString("last_downloaded_update_tag", null)

                    if (releaseTag.isNotEmpty() && releaseTag == lastDownloadedTag) {
                        return@runCatching
                    }

                    UpdateNotificationHelper.createNotificationChannel(context)
                    var lastNotifiedPercent = -1

                    UpdateUtil.downloadApk(context, foundRelease).collect { status ->
                        when (status) {
                            is UpdateUtil.DownloadStatus.Progress -> {
                                if (status.percent - lastNotifiedPercent >= 5 || status.percent == 0 || status.percent == 100) {
                                    lastNotifiedPercent = status.percent
                                    UpdateNotificationHelper.showDownloadProgressNotification(
                                        context,
                                        foundRelease,
                                        status.percent
                                    )
                                }
                            }
                            is UpdateUtil.DownloadStatus.Finished -> {
                                if (releaseTag.isNotEmpty()) {
                                    prefs.edit().putString("last_downloaded_update_tag", releaseTag).apply()
                                }
                                UpdateNotificationHelper.showDownloadCompletedNotification(
                                    context,
                                    foundRelease
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }.onFailure { it.printStackTrace() }
        }
    }
}

