package com.tkno.links.ui.component

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.tkno.links.R
import com.tkno.links.ui.main.Tab

data class NavigationTabItem(
    val tab: Tab,
    val outlineIcon: ImageVector,
    val filledIcon: ImageVector,
    val labelRes: Int
)

fun loadNavOrder(prefs: SharedPreferences): List<Tab> {
    val saved = prefs.getString("nav_tab_order", null) ?: return listOf(Tab.ShortUrl, Tab.Security, Tab.QrCode)
    val names = saved.split(",")
    val list = names.mapNotNull { name ->
        try { Tab.valueOf(name) } catch (e: Exception) { null }
    }.filter { it != Tab.Menu }
    return if (list.size == 3) list else listOf(Tab.ShortUrl, Tab.Security, Tab.QrCode)
}

fun saveNavOrder(prefs: SharedPreferences, list: List<Tab>) {
    val saved = list.joinToString(",") { it.name }
    prefs.edit().putString("nav_tab_order", saved).apply()
}

private fun getTabItem(tab: Tab): NavigationTabItem {
    return when (tab) {
        Tab.ShortUrl -> NavigationTabItem(Tab.ShortUrl, Icons.Outlined.Link, Icons.Filled.Link, R.string.short_url)
        Tab.Security -> NavigationTabItem(Tab.Security, Icons.Outlined.Security, Icons.Filled.Security, R.string.security)
        Tab.QrCode -> NavigationTabItem(Tab.QrCode, Icons.Outlined.QrCode, Icons.Filled.QrCode, R.string.qr_code)
        Tab.Menu -> NavigationTabItem(Tab.Menu, Icons.Outlined.Menu, Icons.Filled.Menu, R.string.menu)
    }
}

@Composable
fun FloatingBottomBar(
    selectedTab: Tab,
    onTabSelect: (Tab) -> Unit,
    reorderableTabs: SnapshotStateList<Tab>,
    hideLabels: Boolean = false,
    animateIndicator: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val prefs = remember { context.getSharedPreferences("links_prefs", Context.MODE_PRIVATE) }

    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var currentDragOffset by remember { mutableStateOf(0f) }
    var itemWidthPx by remember { mutableStateOf(0f) }

    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant
    val indicatorCapsuleColor = MaterialTheme.colorScheme.secondaryContainer

    Box(
        modifier =
            modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .padding(bottom = 16.dp, start = 22.dp, end = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Pill Navigation Bar Dock - تصميم عائم بشكل كبسولة
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f),
            shape = RoundedCornerShape(percent = 50),
            tonalElevation = 6.dp,
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth(),
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val itemCount = (reorderableTabs.size + 1).coerceAtLeast(1)
                val horizontalPadding = 6.dp
                val verticalPadding = 6.dp
                val itemSpacing = 4.dp

                val totalSpacing = itemSpacing * (itemCount - 1)
                val totalSidePadding = horizontalPadding * 2
                val singleIndicatorWidth = (maxWidth - totalSidePadding - totalSpacing) / itemCount
                val step = singleIndicatorWidth + itemSpacing

                itemWidthPx = with(density) { step.toPx() }

                val selectedIndex = if (selectedTab == Tab.Menu) {
                    reorderableTabs.size
                } else {
                    val idx = reorderableTabs.indexOf(selectedTab)
                    if (idx != -1) idx else 0
                }

                val clampedIndex = selectedIndex.coerceIn(0, itemCount - 1)
                val targetOffsetX = horizontalPadding + (step * clampedIndex)

                // أنيميشن حركة الهالة الانزلاقية (Spring Physics) - مفعلة عند تفعيل الخيار
                if (animateIndicator) {
                    val animatedOffsetX by animateDpAsState(
                        targetValue = targetOffsetX,
                        animationSpec = spring(
                            dampingRatio = 0.8f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "indicatorOffsetX"
                    )

                    // عنصر الهالة / المؤشر المنزلق في الخلفية
                    Box(
                        modifier = Modifier
                            .offset(x = animatedOffsetX)
                            .width(singleIndicatorWidth)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = if (hideLabels) {
                                Modifier
                                    .height(44.dp)
                                    .width(56.dp)
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(indicatorCapsuleColor)
                            } else {
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                                    .padding(vertical = verticalPadding)
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(indicatorCapsuleColor)
                            }
                        )
                    }
                }

                // صف الأيقونات والتبويبات
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = horizontalPadding),
                ) {
                    // 1. Render Reorderable Tabs (ShortUrl, Security, QrCode)
                    reorderableTabs.forEachIndexed { index, tab ->
                        val item = getTabItem(tab)
                        val isSelected = selectedTab == item.tab
                        val isDragging = draggingIndex == index
                        val icon = if (isSelected) item.filledIcon else item.outlineIcon
                        val label = stringResource(item.labelRes)

                        val staticIndicatorColor by
                            animateColorAsState(
                                targetValue =
                                    if (isSelected && !animateIndicator) indicatorCapsuleColor
                                    else Color.Transparent,
                                animationSpec = tween(250),
                                label = "staticIndicatorColor",
                            )

                        val iconTint by
                            animateColorAsState(
                                targetValue =
                                    if (isSelected) activeColor
                                    else inactiveColor,
                                animationSpec = tween(250),
                                label = "iconTint",
                            )

                        val textColor by
                            animateColorAsState(
                                targetValue =
                                    if (isSelected) activeColor
                                    else inactiveColor.copy(alpha = 0.8f),
                                animationSpec = tween(250),
                                label = "textColor",
                            )

                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .zIndex(if (isDragging) 10f else 1f)
                                    .graphicsLayer {
                                        if (isDragging) {
                                            translationX = currentDragOffset
                                            scaleX = 1.12f
                                            scaleY = 1.12f
                                        }
                                    }
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) {
                                        onTabSelect(tab)
                                    }
                                    .pointerInput(tab, index) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                draggingIndex = index
                                                currentDragOffset = 0f
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                            onDragEnd = {
                                                draggingIndex = null
                                                currentDragOffset = 0f
                                            },
                                            onDragCancel = {
                                                draggingIndex = null
                                                currentDragOffset = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                val activeIndex = draggingIndex ?: return@detectDragGesturesAfterLongPress
                                                currentDragOffset += dragAmount.x

                                                val threshold = if (itemWidthPx > 0f) itemWidthPx * 0.5f else 80f

                                                if (currentDragOffset > threshold && activeIndex < reorderableTabs.size - 1) {
                                                    val temp = reorderableTabs[activeIndex]
                                                    reorderableTabs[activeIndex] = reorderableTabs[activeIndex + 1]
                                                    reorderableTabs[activeIndex + 1] = temp
                                                    draggingIndex = activeIndex + 1
                                                    currentDragOffset -= itemWidthPx
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    saveNavOrder(prefs, reorderableTabs)
                                                } else if (currentDragOffset < -threshold && activeIndex > 0) {
                                                    val temp = reorderableTabs[activeIndex]
                                                    reorderableTabs[activeIndex] = reorderableTabs[activeIndex - 1]
                                                    reorderableTabs[activeIndex - 1] = temp
                                                    draggingIndex = activeIndex - 1
                                                    currentDragOffset += itemWidthPx
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    saveNavOrder(prefs, reorderableTabs)
                                                }
                                            }
                                        )
                                    },
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier =
                                    if (!animateIndicator) {
                                        if (hideLabels) {
                                            Modifier.height(44.dp)
                                                .width(56.dp)
                                                .clip(RoundedCornerShape(percent = 50))
                                                .background(staticIndicatorColor)
                                        } else {
                                            Modifier.fillMaxHeight()
                                                .padding(vertical = verticalPadding)
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(percent = 50))
                                                .background(staticIndicatorColor)
                                        }
                                    } else {
                                        Modifier.fillMaxSize()
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 2.dp),
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = iconTint,
                                        modifier = Modifier.size(if (hideLabels) 24.dp else 22.dp),
                                    )
                                    if (!hideLabels) {
                                        Text(
                                            text = label,
                                            style =
                                                MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                    fontSize = 11.sp,
                                                ),
                                            color = textColor,
                                            maxLines = 1,
                                            modifier = Modifier.padding(top = 2.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Render Fixed Menu Tab
                    val menuTab = Tab.Menu
                    val menuItem = getTabItem(menuTab)
                    val isMenuSelected = selectedTab == menuTab
                    val menuIcon = if (isMenuSelected) menuItem.filledIcon else menuItem.outlineIcon
                    val menuLabel = stringResource(menuItem.labelRes)

                    val staticMenuIndicatorColor by
                        animateColorAsState(
                            targetValue =
                                if (isMenuSelected && !animateIndicator) indicatorCapsuleColor
                                else Color.Transparent,
                            animationSpec = tween(250),
                            label = "staticMenuIndicatorColor",
                        )

                    val menuIconTint by
                        animateColorAsState(
                            targetValue =
                                if (isMenuSelected) activeColor
                                else inactiveColor,
                            animationSpec = tween(250),
                            label = "menuIconTint",
                        )

                    val menuTextColor by
                        animateColorAsState(
                            targetValue =
                                if (isMenuSelected) activeColor
                                else inactiveColor.copy(alpha = 0.8f),
                            animationSpec = tween(250),
                            label = "menuTextColor",
                        )

                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    onTabSelect(menuTab)
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier =
                                if (!animateIndicator) {
                                    if (hideLabels) {
                                        Modifier.height(44.dp)
                                            .width(56.dp)
                                            .clip(RoundedCornerShape(percent = 50))
                                            .background(staticMenuIndicatorColor)
                                    } else {
                                        Modifier.fillMaxHeight()
                                            .padding(vertical = verticalPadding)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(percent = 50))
                                            .background(staticMenuIndicatorColor)
                                    }
                                } else {
                                    Modifier.fillMaxSize()
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 2.dp),
                            ) {
                                Icon(
                                    imageVector = menuIcon,
                                    contentDescription = menuLabel,
                                    tint = menuIconTint,
                                    modifier = Modifier.size(if (hideLabels) 24.dp else 22.dp),
                                )
                                if (!hideLabels) {
                                    Text(
                                        text = menuLabel,
                                        style =
                                            MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isMenuSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                fontSize = 11.sp,
                                            ),
                                        color = menuTextColor,
                                        maxLines = 1,
                                        modifier = Modifier.padding(top = 2.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassicBottomBar(
    selectedTab: Tab,
    onTabSelect: (Tab) -> Unit,
    reorderableTabs: SnapshotStateList<Tab>,
    hideLabels: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val prefs = remember { context.getSharedPreferences("links_prefs", Context.MODE_PRIVATE) }

    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var currentDragOffset by remember { mutableStateOf(0f) }
    var itemWidthPx by remember { mutableStateOf(0f) }

    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant
    val indicatorCapsuleColor = MaterialTheme.colorScheme.secondaryContainer
    val navContainerColor = MaterialTheme.colorScheme.surfaceContainer

    NavigationBar(
        containerColor = navContainerColor,
        tonalElevation = 0.dp,
        modifier = modifier
            .height(72.dp)
            .onGloballyPositioned { coordinates ->
                val totalWidth = coordinates.size.width.toFloat()
                if (totalWidth > 0) {
                    itemWidthPx = totalWidth / 4f
                }
            }
    ) {
        // 1. Render Reorderable Tabs
        reorderableTabs.forEachIndexed { index, tab ->
            val item = getTabItem(tab)
            val isSelected = selectedTab == item.tab
            val isDragging = draggingIndex == index
            val icon = if (isSelected) item.filledIcon else item.outlineIcon
            val label = stringResource(item.labelRes)

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelect(tab) },
                alwaysShowLabel = !hideLabels,
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) activeColor else inactiveColor
                    )
                },
                label = if (!hideLabels) {
                    {
                        Text(
                            text = label,
                            color = if (isSelected) activeColor else inactiveColor,
                            fontSize = 12.sp
                        )
                    }
                } else null,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = indicatorCapsuleColor
                ),
                modifier = Modifier
                    .zIndex(if (isDragging) 10f else 1f)
                    .graphicsLayer {
                        if (isDragging) {
                            translationX = currentDragOffset
                            scaleX = 1.12f
                            scaleY = 1.12f
                        }
                    }
                    .pointerInput(tab, index) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggingIndex = index
                                currentDragOffset = 0f
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDragEnd = {
                                draggingIndex = null
                                currentDragOffset = 0f
                            },
                            onDragCancel = {
                                draggingIndex = null
                                currentDragOffset = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val activeIndex = draggingIndex ?: return@detectDragGesturesAfterLongPress
                                currentDragOffset += dragAmount.x

                                val threshold = if (itemWidthPx > 0f) itemWidthPx * 0.5f else 80f

                                if (currentDragOffset > threshold && activeIndex < reorderableTabs.size - 1) {
                                    val temp = reorderableTabs[activeIndex]
                                    reorderableTabs[activeIndex] = reorderableTabs[activeIndex + 1]
                                    reorderableTabs[activeIndex + 1] = temp
                                    draggingIndex = activeIndex + 1
                                    currentDragOffset -= itemWidthPx
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    saveNavOrder(prefs, reorderableTabs)
                                } else if (currentDragOffset < -threshold && activeIndex > 0) {
                                    val temp = reorderableTabs[activeIndex]
                                    reorderableTabs[activeIndex] = reorderableTabs[activeIndex - 1]
                                    reorderableTabs[activeIndex - 1] = temp
                                    draggingIndex = activeIndex - 1
                                    currentDragOffset += itemWidthPx
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    saveNavOrder(prefs, reorderableTabs)
                                }
                            }
                        )
                    }
            )
        }

        // 2. Render Fixed Menu Tab
        val menuItem = getTabItem(Tab.Menu)
        val isMenuSelected = selectedTab == Tab.Menu
        val menuIcon = if (isMenuSelected) menuItem.filledIcon else menuItem.outlineIcon
        val menuLabel = stringResource(menuItem.labelRes)

        NavigationBarItem(
            selected = isMenuSelected,
            onClick = { onTabSelect(Tab.Menu) },
            alwaysShowLabel = !hideLabels,
            icon = {
                Icon(
                    imageVector = menuIcon,
                    contentDescription = menuLabel,
                    tint = if (isMenuSelected) activeColor else inactiveColor
                )
            },
            label = if (!hideLabels) {
                {
                    Text(
                        text = menuLabel,
                        color = if (isMenuSelected) activeColor else inactiveColor,
                        fontSize = 12.sp
                    )
                }
            } else null,
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = indicatorCapsuleColor
            )
        )
    }
}
