package com.tkno.links.ui.page

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.tkno.links.util.UpdateUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Background composable that checks for updates on launch (if auto-update is enabled)
 * and shows an [UpdateDialogImpl] when a new version is found.
 */
@Composable
fun AppUpdater(isAutoUpdateEnabled: Boolean) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val prefs = remember { context.getSharedPreferences("links_prefs", Context.MODE_PRIVATE) }
    val updateChannel = prefs.getInt("update_channel", 1) // 1: Preview, 0: Stable
    val includePrerelease = updateChannel == 1

    var showUpdateDialog by rememberSaveable { mutableStateOf(false) }
    var currentDownloadStatus by remember {
        mutableStateOf<UpdateUtil.DownloadStatus>(UpdateUtil.DownloadStatus.NotYet)
    }
    var updateJob by remember { mutableStateOf<Job?>(null) }
    var release by remember { mutableStateOf(UpdateUtil.Release()) }

    LaunchedEffect(isAutoUpdateEnabled, updateChannel) {
        if (!isAutoUpdateEnabled) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            runCatching {
                UpdateUtil.checkForUpdate(context, includePrerelease = includePrerelease)?.let { foundRelease ->
                    release = foundRelease
                    showUpdateDialog = true
                }
            }.onFailure { it.printStackTrace() }
        }
    }

    if (showUpdateDialog) {
        UpdateDialogImpl(
            onDismissRequest = {
                showUpdateDialog = false
                updateJob?.cancel()
            },
            title = release.name ?: release.tagName ?: "New Update",
            releaseNote = release.body ?: "",
            downloadStatus = currentDownloadStatus,
            onConfirmUpdate = {
                updateJob = scope.launch(Dispatchers.IO) {
                    runCatching {
                        UpdateUtil.downloadApk(context, release).collect { status ->
                            currentDownloadStatus = status
                            if (status is UpdateUtil.DownloadStatus.Finished) {
                                withContext(Dispatchers.Main) {
                                    UpdateUtil.installLatestApk(context)
                                }
                            }
                        }
                    }.onFailure {
                        it.printStackTrace()
                        currentDownloadStatus = UpdateUtil.DownloadStatus.NotYet
                    }
                }
            },
        )
    }
}
