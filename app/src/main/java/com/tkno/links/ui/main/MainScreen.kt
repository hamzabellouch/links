package com.tkno.links.ui.main

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.tkno.links.R
import com.tkno.links.*
import com.tkno.links.ui.menu.MenuScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.tkno.links.ui.page.AppUpdater

enum class Tab {
    ShortUrl, Security, QrCode, Menu
}

data class TabInfo(
    val tab: Tab,
    val label: String,
    val icon: ImageVector
)

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(Tab.ShortUrl) }
    val saveableStateHolder = rememberSaveableStateHolder()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val prefs = remember { context.getSharedPreferences("links_prefs", Context.MODE_PRIVATE) }
    val isAutoUpdateEnabled by remember { mutableStateOf(prefs.getBoolean("auto_update_enabled", true)) }

    // Launch background update check on app startup
    AppUpdater(isAutoUpdateEnabled = isAutoUpdateEnabled)

    @Composable
    fun getTabInfo(tab: Tab): TabInfo {
        return when (tab) {
            Tab.ShortUrl -> TabInfo(Tab.ShortUrl, stringResource(R.string.short_url), Icons.Default.Link)
            Tab.Security -> TabInfo(Tab.Security, stringResource(R.string.security), Icons.Outlined.Security)
            Tab.QrCode -> TabInfo(Tab.QrCode, stringResource(R.string.qr_code), Icons.Outlined.QrCode)
            Tab.Menu -> TabInfo(Tab.Menu, stringResource(R.string.menu), Icons.Default.Menu)
        }
    }

    fun loadNavOrder(): List<Tab> {
        val saved = prefs.getString("nav_tab_order", null) ?: return listOf(Tab.ShortUrl, Tab.Security, Tab.QrCode)
        val names = saved.split(",")
        val list = names.mapNotNull { name ->
            try { Tab.valueOf(name) } catch (e: Exception) { null }
        }.filter { it != Tab.Menu }
        return if (list.size == 3) list else listOf(Tab.ShortUrl, Tab.Security, Tab.QrCode)
    }

    fun saveNavOrder(list: List<Tab>) {
        val saved = list.joinToString(",") { it.name }
        prefs.edit().putString("nav_tab_order", saved).apply()
    }

    val reorderableTabs = remember { mutableStateListOf(*loadNavOrder().toTypedArray()) }

    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var currentDragOffset by remember { mutableStateOf(0f) }
    var itemWidthPx by remember { mutableStateOf(0f) }

    BackHandler(enabled = currentTab != Tab.ShortUrl) {
        currentTab = Tab.ShortUrl
    }

    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant
    val indicatorCapsuleColor = MaterialTheme.colorScheme.secondaryContainer
    val navContainerColor = MaterialTheme.colorScheme.surfaceContainer
    val appBackground = MaterialTheme.colorScheme.background

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = navContainerColor,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .height(72.dp)
                    .onGloballyPositioned { coordinates ->
                        val totalWidth = coordinates.size.width.toFloat()
                        if (totalWidth > 0) {
                            itemWidthPx = totalWidth / 4f
                        }
                    }
            ) {
                // Render the 3 reorderable tabs
                reorderableTabs.forEachIndexed { index, tab ->
                    val info = getTabInfo(tab)
                    val isSelected = currentTab == tab
                    val isDragging = draggingIndex == index

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = info.icon,
                                contentDescription = info.label,
                                tint = if (isSelected) activeColor else inactiveColor
                            )
                        },
                        label = {
                            Text(
                                text = info.label,
                                color = if (isSelected) activeColor else inactiveColor,
                                fontSize = 12.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = indicatorCapsuleColor
                        ),
                        modifier = Modifier
                            .graphicsLayer {
                                if (isDragging) {
                                    translationX = currentDragOffset
                                    scaleX = 1.12f
                                    scaleY = 1.12f
                                }
                            }
                            .pointerInput(index) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    var isLongPressActive = false

                                    val longPressTimer = scope.launch {
                                        delay(viewConfiguration.longPressTimeoutMillis)
                                        isLongPressActive = true
                                        draggingIndex = index
                                        currentDragOffset = 0f
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }

                                    val pointer = down.id
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull { it.id == pointer }

                                        if (change == null || !change.pressed) {
                                            longPressTimer.cancel()
                                            if (isLongPressActive) {
                                                draggingIndex = null
                                                currentDragOffset = 0f
                                            }
                                            break
                                        }

                                        if (!isLongPressActive) {
                                            val diff = change.position - down.position
                                            if (diff.getDistance() > viewConfiguration.touchSlop) {
                                                longPressTimer.cancel()
                                            }
                                        } else {
                                            change.consume()
                                            val deltaX = change.position.x - change.previousPosition.x
                                            val activeIndex = draggingIndex ?: break
                                            currentDragOffset += deltaX

                                            val threshold = if (itemWidthPx > 0f) itemWidthPx * 0.5f else 100f

                                            if (currentDragOffset > threshold && activeIndex < reorderableTabs.size - 1) {
                                                val temp = reorderableTabs[activeIndex]
                                                reorderableTabs[activeIndex] = reorderableTabs[activeIndex + 1]
                                                reorderableTabs[activeIndex + 1] = temp
                                                draggingIndex = activeIndex + 1
                                                currentDragOffset -= itemWidthPx
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                saveNavOrder(reorderableTabs)
                                            } else if (currentDragOffset < -threshold && activeIndex > 0) {
                                                val temp = reorderableTabs[activeIndex]
                                                reorderableTabs[activeIndex] = reorderableTabs[activeIndex - 1]
                                                reorderableTabs[activeIndex - 1] = temp
                                                draggingIndex = activeIndex - 1
                                                currentDragOffset += itemWidthPx
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                saveNavOrder(reorderableTabs)
                                            }
                                        }
                                    }
                                }
                            }
                    )
                }

                // Render the fixed Menu tab at the end
                val menuInfo = getTabInfo(Tab.Menu)
                val isMenuSelected = currentTab == Tab.Menu
                NavigationBarItem(
                    selected = isMenuSelected,
                    onClick = { currentTab = Tab.Menu },
                    icon = {
                        Icon(
                            imageVector = menuInfo.icon,
                            contentDescription = menuInfo.label,
                            tint = if (isMenuSelected) activeColor else inactiveColor
                        )
                    },
                    label = {
                        Text(
                            text = menuInfo.label,
                            color = if (isMenuSelected) activeColor else inactiveColor,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = indicatorCapsuleColor
                    )
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
