package com.tkno.links.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tkno.links.R

data class DarkThemePreference(
    val darkThemeValue: Int = FOLLOW_SYSTEM,
    val isHighContrastModeEnabled: Boolean = false,
) {
    companion object {
        const val FOLLOW_SYSTEM = 1
        const val ON = 2
        const val OFF = 3
    }

    @Composable
    fun isDarkTheme(): Boolean {
        return if (darkThemeValue == FOLLOW_SYSTEM || darkThemeValue == 0) isSystemInDarkTheme() else darkThemeValue == ON
    }

    @Composable
    fun getDarkThemeDesc(): String {
        return when (darkThemeValue) {
            FOLLOW_SYSTEM, 0 -> stringResource(R.string.follow_system)
            ON -> stringResource(R.string.on)
            else -> stringResource(R.string.off)
        }
    }
}

object PreferenceUtil {
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences("links_prefs", Context.MODE_PRIVATE)
    }

    fun modifyDarkThemePreference(
        darkThemeValue: Int? = null,
        isHighContrastModeEnabled: Boolean? = null
    ) {
        prefs?.edit()?.apply {
            darkThemeValue?.let { putInt("dark_theme", it) }
            isHighContrastModeEnabled?.let { putBoolean("high_contrast_dark_theme", it) }
            apply()
        }
    }
}
