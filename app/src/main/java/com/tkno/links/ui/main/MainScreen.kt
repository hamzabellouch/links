package com.tkno.links.ui.main

import android.content.Context
import android.content.SharedPreferences
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tkno.links.*
import com.tkno.links.ui.component.ClassicBottomBar
import com.tkno.links.ui.component.FloatingBottomBar
import com.tkno.links.ui.component.loadNavOrder
import com.tkno.links.ui.menu.MenuScreen
import com.tkno.links.ui.page.AppUpdater

enum class Tab {
    ShortUrl, Security, QrCode, Menu
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("links_prefs", Context.MODE_PRIVATE) }
    var isAutoUpdateEnabled by remember { mutableStateOf(prefs.getBoolean("auto_update_enabled", true)) }
    var hideLabels by remember { mutableStateOf(prefs.getBoolean("hide_navigation_labels", false)) }
    var useClassicTaskbar by remember { mutableStateOf(prefs.getBoolean("use_classic_taskbar", false)) }

    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            when (key) {
                "auto_update_enabled" -> isAutoUpdateEnabled = p.getBoolean("auto_update_enabled", true)
                "hide_navigation_labels" -> hideLabels = p.getBoolean("hide_navigation_labels", false)
                "use_classic_taskbar" -> useClassicTaskbar = p.getBoolean("use_classic_taskbar", false)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    // Launch background update check on app startup
    AppUpdater(isAutoUpdateEnabled = isAutoUpdateEnabled)

    val reorderableTabs = remember { mutableStateListOf(*loadNavOrder(prefs).toTypedArray()) }
    val defaultTab by remember { derivedStateOf { reorderableTabs.firstOrNull() ?: Tab.ShortUrl } }
    var currentTab by rememberSaveable { mutableStateOf(loadNavOrder(prefs).firstOrNull() ?: Tab.ShortUrl) }
    val saveableStateHolder = rememberSaveableStateHolder()

    BackHandler(enabled = currentTab != defaultTab) {
        currentTab = defaultTab
    }

    val appBackground = MaterialTheme.colorScheme.background

    Scaffold(
        bottomBar = {
            if (useClassicTaskbar) {
                ClassicBottomBar(
                    selectedTab = currentTab,
                    onTabSelect = { currentTab = it },
                    reorderableTabs = reorderableTabs,
                    hideLabels = hideLabels
                )
            } else {
                FloatingBottomBar(
                    selectedTab = currentTab,
                    onTabSelect = { currentTab = it },
                    reorderableTabs = reorderableTabs,
                    hideLabels = hideLabels
                )
            }
        },
        containerColor = appBackground,
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appBackground)
                .padding(paddingValues)
        ) {
            saveableStateHolder.SaveableStateProvider(currentTab) {
                when (currentTab) {
                    Tab.ShortUrl -> ShortUrlScreen()
                    Tab.Security -> SecurityScreen()
                    Tab.QrCode -> QrCodeScreen()
                    Tab.Menu -> MenuScreen()
                }
            }
        }
    }
}
