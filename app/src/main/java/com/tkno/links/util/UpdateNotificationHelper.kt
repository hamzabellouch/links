package com.tkno.links.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.tkno.links.MainActivity
import com.tkno.links.R

object UpdateNotificationHelper {

    const val CHANNEL_ID = "links_app_updates"
    const val NOTIFICATION_ID = 1001

    const val EXTRA_NAVIGATE_TO = "navigate_to"
    const val NAV_TARGET_AUTO_UPDATE = "auto_update"
    const val EXTRA_TRIGGER_UPDATE = "trigger_update"

    private const val PREF_LAST_NOTIFIED_TAG = "last_notified_update_tag"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.update_notification_channel_name)
            val descriptionText = context.getString(R.string.update_notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showUpdateNotification(context: Context, release: UpdateUtil.Release) {
        val prefs = context.getSharedPreferences("links_prefs", Context.MODE_PRIVATE)

        val autoUpdate = prefs.getBoolean("auto_update_enabled", true)
        val bellEnabled = prefs.getBoolean("update_bell_enabled", true)

        if (!autoUpdate || !bellEnabled) {
            return
        }

        // Check notification permissions for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) {
                return
            }
        }

        val releaseTag = release.tagName ?: release.name ?: ""
        val lastNotifiedTag = prefs.getString(PREF_LAST_NOTIFIED_TAG, null)

        // Don't show duplicate notification for the exact same release tag
        if (releaseTag.isNotEmpty() && releaseTag == lastNotifiedTag) {
            return
        }

        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAVIGATE_TO, NAV_TARGET_AUTO_UPDATE)
            putExtra(EXTRA_TRIGGER_UPDATE, true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.update_available_title, releaseTag)
        val bodyContent = release.body?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.update_available_desc)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(bodyContent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyContent))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            if (releaseTag.isNotEmpty()) {
                prefs.edit().putString(PREF_LAST_NOTIFIED_TAG, releaseTag).apply()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun showDownloadProgressNotification(context: Context, release: UpdateUtil.Release, percent: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) return
        }

        createNotificationChannel(context)

        val releaseTag = release.tagName ?: release.name ?: ""
        val title = context.getString(R.string.downloading_update_notification, releaseTag)
        val contentText = "$percent%"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(contentText)
            .setProgress(100, percent, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun showDownloadCompletedNotification(context: Context, release: UpdateUtil.Release) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) return
        }

        createNotificationChannel(context)

        val releaseTag = release.tagName ?: release.name ?: ""
        val prefs = context.getSharedPreferences("links_prefs", Context.MODE_PRIVATE)
        val customUriString = prefs.getString("app_update_directory_uri", null)

        val openFilesIntent = if (!customUriString.isNullOrBlank()) {
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(android.net.Uri.parse(customUriString), "vnd.android.document/directory")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
        } else {
            Intent(android.app.DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            openFilesIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.update_download_completed_title, releaseTag)
        val message = context.getString(R.string.update_download_completed_desc)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
