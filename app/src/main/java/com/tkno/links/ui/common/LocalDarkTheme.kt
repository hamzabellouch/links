package com.tkno.links.ui.common

import androidx.compose.runtime.compositionLocalOf
import com.tkno.links.util.DarkThemePreference

val LocalDarkTheme = compositionLocalOf { DarkThemePreference() }
