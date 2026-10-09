package com.tkno.links.ui.menu

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SettingsApplications
import androidx.compose.material.icons.rounded.ViewComfy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.tkno.links.ui.icon.LocalFireDepartment
import com.tkno.links.ui.icon.Policy
import com.tkno.links.ui.icon.Report
import com.tkno.links.ui.icon.ArrowDownward
import com.tkno.links.util.LanguageManager
import com.tkno.links.util.RemoteLanguageItem
import com.tkno.links.util.CustomLanguage
import com.tkno.links.ui.icon.StarIcon
import com.tkno.links.ui.svg.drawablevectors.DynamicColorImageVectors
import com.tkno.links.ui.svg.drawablevectors.coder
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tkno.links.R
import com.tkno.links.ui.component.*
import com.tkno.links.ui.page.AppUpdater
import com.tkno.links.ui.page.WhatsNewDialog
import com.tkno.links.ui.page.settings.BasePreferencePage
import com.tkno.links.ui.page.settings.about.UpdatePage
import com.tkno.links.ui.page.settings.appearance.DarkThemePreferences
import java.util.Locale

enum class MenuSubScreen {
    Main, Settings, GeneralSettings, LookAndFeel, InterfaceAndInteraction, Languages, DarkTheme, Sponsor, Troubleshooting, About, Credits, Update
}

@Composable
fun MenuScreen(
    navigateToUpdate: Boolean = false,
    triggerUpdate: Boolean = false,
    onNavigateToUpdateConsumed: () -> Unit = {}
) {
    var currentSubScreen by remember { mutableStateOf(MenuSubScreen.Main) }
    var autoTriggerUpdate by remember { mutableStateOf(false) }

    LaunchedEffect(navigateToUpdate, triggerUpdate) {
        if (navigateToUpdate) {
            currentSubScreen = MenuSubScreen.Update
            autoTriggerUpdate = triggerUpdate
            onNavigateToUpdateConsumed()
        }
    }

    BackHandler(enabled = currentSubScreen != MenuSubScreen.Main) {
        when (currentSubScreen) {
            MenuSubScreen.GeneralSettings -> currentSubScreen = MenuSubScreen.Settings
            MenuSubScreen.LookAndFeel -> currentSubScreen = MenuSubScreen.Settings
            MenuSubScreen.InterfaceAndInteraction -> currentSubScreen = MenuSubScreen.Settings
            MenuSubScreen.Languages -> currentSubScreen = MenuSubScreen.LookAndFeel
            MenuSubScreen.DarkTheme -> currentSubScreen = MenuSubScreen.LookAndFeel
            MenuSubScreen.Credits -> currentSubScreen = MenuSubScreen.About
            MenuSubScreen.Update -> {
                autoTriggerUpdate = false
                currentSubScreen = MenuSubScreen.About
            }
            else -> currentSubScreen = MenuSubScreen.Main
        }
    }

    when (currentSubScreen) {
        MenuSubScreen.Main -> MainMenuList(
            onNavigateTo = { currentSubScreen = it }
        )
        MenuSubScreen.Settings -> SettingsPage(
            onNavigateBack = { currentSubScreen = MenuSubScreen.Main },
            onNavigateTo = { route ->
                when (route) {
                    "general" -> currentSubScreen = MenuSubScreen.GeneralSettings
                    "appearance" -> currentSubScreen = MenuSubScreen.LookAndFeel
                    "interface_interaction" -> currentSubScreen = MenuSubScreen.InterfaceAndInteraction
                }
            }
        )
        MenuSubScreen.GeneralSettings -> GeneralSettingsPage(
            onNavigateBack = { currentSubScreen = MenuSubScreen.Settings }
        )
        MenuSubScreen.InterfaceAndInteraction -> InterfaceAndInteractionPage(
            onNavigateBack = { currentSubScreen = MenuSubScreen.Settings }
        )
        MenuSubScreen.LookAndFeel -> AppearancePreferences(
            onNavigateBack = { currentSubScreen = MenuSubScreen.Settings },
            onNavigateTo = { route ->
                if (route == "languages") currentSubScreen = MenuSubScreen.Languages
                else if (route == "dark_theme") currentSubScreen = MenuSubScreen.DarkTheme
            }
        )
        MenuSubScreen.DarkTheme -> DarkThemePreferences(
            onNavigateBack = { currentSubScreen = MenuSubScreen.LookAndFeel }
        )
        MenuSubScreen.Languages -> LanguagesPage(
            onNavigateBack = { currentSubScreen = MenuSubScreen.LookAndFeel }
        )
        MenuSubScreen.Sponsor -> SponsorsPage(
            onNavigateBack = { currentSubScreen = MenuSubScreen.Main }
        )
        MenuSubScreen.Troubleshooting -> TroubleShootingPage(
            onNavigateBack = { currentSubScreen = MenuSubScreen.Main }
        )
        MenuSubScreen.About -> AboutPage(
            onNavigateBack = { currentSubScreen = MenuSubScreen.Main },
            onNavigateToCreditsPage = { currentSubScreen = MenuSubScreen.Credits },
            onNavigateToUpdatePage = { currentSubScreen = MenuSubScreen.Update }
        )
        MenuSubScreen.Credits -> CreditsPage(
            onNavigateBack = { currentSubScreen = MenuSubScreen.About }
        )
        MenuSubScreen.Update -> UpdatePage(
            onNavigateBack = {
                autoTriggerUpdate = false
                currentSubScreen = MenuSubScreen.About
            },
            triggerUpdate = autoTriggerUpdate
        )
    }
}

@Composable
fun MainMenuList(
    onNavigateTo: (MenuSubScreen) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(bottom = 104.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 12.dp)
                    .height(48.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.menu),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.settings)) },
                    icon = { Icon(Icons.Outlined.Settings, null) },
                    onClick = { onNavigateTo(MenuSubScreen.Settings) },
                    selected = false,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.sponsor)) },
                    icon = { Icon(Icons.Outlined.VolunteerActivism, null) },
                    onClick = { onNavigateTo(MenuSubScreen.Sponsor) },
                    selected = false,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.trouble_shooting)) },
                    icon = { Icon(Icons.Outlined.BugReport, null) },
                    onClick = { onNavigateTo(MenuSubScreen.Troubleshooting) },
                    selected = false,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.about)) },
                    icon = { Icon(Icons.Outlined.Info, null) },
                    onClick = { onNavigateTo(MenuSubScreen.About) },
                    selected = false,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                )
            }
        }
    }
}

/* ---------------- SettingsPage ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(onNavigateBack: () -> Unit, onNavigateTo: (String) -> Unit) {
    BasePreferencePage(
        title = stringResource(id = R.string.settings),
        onBack = onNavigateBack,
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = padding) {
            item {
                SettingItem(
                    title = stringResource(id = R.string.general_settings),
                    description = stringResource(id = R.string.general_settings_desc),
                    icon = Icons.Rounded.SettingsApplications,
                ) {
                    onNavigateTo("general")
                }
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.look_and_feel),
                    description = stringResource(id = R.string.display_settings),
                    icon = Icons.Rounded.Palette,
                ) {
                    onNavigateTo("appearance")
                }
            }
            item {
                SettingItem(
                    title = stringResource(id = R.string.interface_interaction),
                    description = stringResource(id = R.string.interface_interaction_desc),
                    icon = Icons.Rounded.ViewComfy,
                ) {
                    onNavigateTo("interface_interaction")
                }
            }
        }
    }
}

/* ---------------- GeneralSettingsPage ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsPage(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("links_prefs", android.content.Context.MODE_PRIVATE) }

    var showCostumeTab by remember { mutableStateOf(prefs.getBoolean("show_costume_tab", true)) }
    var showZoomSlider by remember {
        mutableStateOf(
            if (prefs.contains("show_zoom_slider")) {
                prefs.getBoolean("show_zoom_slider", false)
            } else if (prefs.contains("qr_pinch_zoom")) {
                !prefs.getBoolean("qr_pinch_zoom", true)
            } else {
                false
            }
        )
    }

    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "show_costume_tab") {
                showCostumeTab = p.getBoolean("show_costume_tab", true)
            } else if (key == "show_zoom_slider") {
                showZoomSlider = p.getBoolean("show_zoom_slider", false)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    BasePreferencePage(
        title = stringResource(id = R.string.general_settings),
        onBack = onNavigateBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            PreferenceSubtitle(text = stringResource(R.string.qr_code))

            PreferenceSwitch(
                title = stringResource(R.string.show_costume_tab),
                description = stringResource(R.string.show_costume_tab_desc),
                icon = Icons.Outlined.QrCode,
                isChecked = showCostumeTab,
                onClick = {
                    val newValue = !showCostumeTab
                    showCostumeTab = newValue
                    prefs.edit().putBoolean("show_costume_tab", newValue).apply()
                }
            )

            PreferenceSwitch(
                title = stringResource(R.string.show_zoom_slider),
                description = stringResource(R.string.show_zoom_slider_desc),
                icon = Icons.Outlined.ZoomIn,
                isChecked = showZoomSlider,
                onClick = {
                    val newValue = !showZoomSlider
                    showZoomSlider = newValue
                    prefs.edit().putBoolean("show_zoom_slider", newValue).apply()
                }
            )
        }
    }
}

/* ---------------- InterfaceAndInteractionPage ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterfaceAndInteractionPage(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("links_prefs", android.content.Context.MODE_PRIVATE) }

    var hideLabels by remember { mutableStateOf(prefs.getBoolean("hide_navigation_labels", false)) }
    var useClassicTaskbar by remember { mutableStateOf(prefs.getBoolean("use_classic_taskbar", false)) }
    var animateIndicator by remember { mutableStateOf(prefs.getBoolean("animate_taskbar_indicator", true)) }

    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "hide_navigation_labels") {
                hideLabels = p.getBoolean("hide_navigation_labels", false)
            } else if (key == "use_classic_taskbar") {
                useClassicTaskbar = p.getBoolean("use_classic_taskbar", false)
            } else if (key == "animate_taskbar_indicator") {
                animateIndicator = p.getBoolean("animate_taskbar_indicator", true)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    BasePreferencePage(
        title = stringResource(id = R.string.interface_interaction),
        onBack = onNavigateBack,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            PreferenceSubtitle(text = stringResource(R.string.navigation))

            PreferenceSwitch(
                title = stringResource(R.string.hide_navigation_labels),
                description = stringResource(R.string.hide_navigation_labels_desc),
                icon = Icons.Outlined.VisibilityOff,
                isChecked = hideLabels,
                onClick = {
                    val newValue = !hideLabels
                    hideLabels = newValue
                    prefs.edit().putBoolean("hide_navigation_labels", newValue).apply()
                }
            )

            PreferenceSwitch(
                title = stringResource(R.string.use_classic_taskbar),
                description = stringResource(R.string.use_classic_taskbar_desc),
                icon = Icons.Outlined.Dashboard,
                isChecked = useClassicTaskbar,
                onClick = {
                    val newValue = !useClassicTaskbar
                    useClassicTaskbar = newValue
                    prefs.edit().putBoolean("use_classic_taskbar", newValue).apply()
                }
            )

            PreferenceSwitch(
                title = stringResource(R.string.animate_taskbar_indicator),
                description = stringResource(R.string.animate_taskbar_indicator_desc),
                icon = Icons.Outlined.Animation,
                isChecked = animateIndicator,
                onClick = {
                    val newValue = !animateIndicator
                    animateIndicator = newValue
                    prefs.edit().putBoolean("animate_taskbar_indicator", newValue).apply()
                }
            )
        }
    }
}

/* ---------------- AppearancePreferences ---------------- */

fun getSavedLocaleDisplayName(context: android.content.Context): String {
    val prefs = context.getSharedPreferences("links_prefs", android.content.Context.MODE_PRIVATE)
    val langTag = prefs.getString("app_language", "system") ?: "system"
    return LanguageManager.getDisplayName(context, langTag)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearancePreferences(onNavigateBack: () -> Unit, onNavigateTo: (String) -> Unit = {}) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("links_prefs", android.content.Context.MODE_PRIVATE) }

    var darkThemePref by remember { mutableIntStateOf(prefs.getInt("dark_theme", 0)) }
    var isDynamicColor by remember { mutableStateOf(prefs.getBoolean("dynamic_color", true)) }

    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "dark_theme") {
                darkThemePref = p.getInt("dark_theme", 0)
            } else if (key == "dynamic_color") {
                isDynamicColor = p.getBoolean("dynamic_color", true)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val isDark = when (darkThemePref) {
        1 -> true
        2 -> false
        else -> isSystemInDarkTheme()
    }

    val darkThemeDesc = when (darkThemePref) {
        1 -> stringResource(id = R.string.on)
        2 -> stringResource(id = R.string.off)
        else -> stringResource(id = R.string.follow_system)
    }

    BasePreferencePage(
        title = stringResource(id = R.string.look_and_feel),
        onBack = onNavigateBack,
    ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                PreferenceSwitch(
                    title = stringResource(id = R.string.dynamic_color),
                    description = stringResource(id = R.string.dynamic_color_desc),
                    icon = Icons.Outlined.Colorize,
                    isChecked = isDynamicColor,
                    onClick = {
                        val newValue = !isDynamicColor
                        isDynamicColor = newValue
                        prefs.edit().putBoolean("dynamic_color", newValue).apply()
                    },
                )
                PreferenceSwitchWithDivider(
                    title = stringResource(id = R.string.dark_theme),
                    icon = if (isDark) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                    isChecked = isDark,
                    description = darkThemeDesc,
                    onChecked = {
                        val newPref = if (isDark) 2 else 1
                        darkThemePref = newPref
                        prefs.edit().putInt("dark_theme", newPref).apply()
                    },
                    onClick = { onNavigateTo("dark_theme") },
                )
                PreferenceItem(
                    title = stringResource(R.string.language),
                    icon = Icons.Outlined.Language,
                    description = getSavedLocaleDisplayName(context),
                ) {
                    onNavigateTo("languages")
                }
            }
    }
}

/* ---------------- LanguagesPage ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadLanguagesBottomSheet(
    onDismissRequest: () -> Unit,
    onLanguageInstalled: (CustomLanguage) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var remoteLanguages by remember { mutableStateOf<List<RemoteLanguageItem>>(emptyList()) }
    var downloadingCode by remember { mutableStateOf<String?>(null) }

    fun loadLanguages() {
        isLoading = true
        errorMessage = null
        if (!com.tkno.links.LinkResolver.isNetworkAvailable(context)) {
            isLoading = false
            errorMessage = context.getString(R.string.no_internet_error)
            return
        }
        coroutineScope.launch {
            val result = LanguageManager.fetchRemoteLanguages()
            result.onSuccess { list ->
                remoteLanguages = list
                isLoading = false
            }.onFailure { err ->
                if (!com.tkno.links.LinkResolver.isNetworkAvailable(context) ||
                    err is java.net.UnknownHostException ||
                    err is java.net.ConnectException ||
                    err is java.net.SocketTimeoutException ||
                    err is java.io.IOException
                ) {
                    errorMessage = context.getString(R.string.no_internet_error)
                } else {
                    errorMessage = err.localizedMessage ?: context.getString(R.string.download_failed)
                }
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadLanguages()
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(id = R.string.download_languages),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )
            Text(
                text = stringResource(id = R.string.download_languages_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.WifiOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { loadLanguages() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(id = R.string.retry))
                        }
                    }
                }
                remoteLanguages.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.no_downloaded_languages),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    val installed = remember(remoteLanguages) {
                        LanguageManager.getInstalledCustomLanguages(context).map { it.code }.toSet()
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                    ) {
                        items(remoteLanguages) { item ->
                            val isInstalled = installed.contains(item.code) || LanguageManager.isBuiltInLanguage(item.code)
                            val isDownloading = downloadingCode == item.code

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = item.englishName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isInstalled) {
                                        FilledTonalButton(
                                            onClick = {},
                                            enabled = false
                                        ) {
                                            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(stringResource(R.string.installed))
                                        }
                                    } else if (isDownloading) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                                    } else {
                                        Button(
                                            onClick = {
                                                if (!com.tkno.links.LinkResolver.isNetworkAvailable(context)) {
                                                    Toast.makeText(context, context.getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show()
                                                    return@Button
                                                }
                                                downloadingCode = item.code
                                                coroutineScope.launch {
                                                    val res = LanguageManager.downloadRemoteLanguage(context, item)
                                                    downloadingCode = null
                                                    res.onSuccess { customLang ->
                                                        Toast.makeText(context, context.getString(R.string.language_downloaded), Toast.LENGTH_SHORT).show()
                                                        onLanguageInstalled(customLang)
                                                        onDismissRequest()
                                                    }.onFailure { e ->
                                                        val msg = if (!com.tkno.links.LinkResolver.isNetworkAvailable(context) ||
                                                            e is java.net.UnknownHostException ||
                                                            e is java.net.ConnectException ||
                                                            e is java.net.SocketTimeoutException
                                                        ) {
                                                            context.getString(R.string.no_internet_error)
                                                        } else {
                                                            e.localizedMessage ?: context.getString(R.string.download_failed)
                                                        }
                                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(stringResource(R.string.install))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagesPage(onNavigateBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("links_prefs", android.content.Context.MODE_PRIVATE) }
    var selectedLangTag by remember { mutableStateOf(prefs.getString("app_language", "system") ?: "system") }
    var refreshCustomTrigger by remember { mutableIntStateOf(0) }
    var showDownloadSheet by remember { mutableStateOf(false) }

    val installedCustomLanguages = remember(selectedLangTag, refreshCustomTrigger) {
        LanguageManager.getInstalledCustomLanguages(context)
    }

    val defaultLanguagesList = remember {
        listOf(
            Triple("العربية", "ar", Locale.forLanguageTag("ar")),
            Triple("English", "en", Locale.ENGLISH),
            Triple("Français", "fr", Locale.FRENCH),
        )
    }

    fun setAppLanguage(langTag: String, locale: Locale?) {
        selectedLangTag = langTag
        prefs.edit().putString("app_language", langTag).apply()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(android.content.Context.LOCALE_SERVICE) as? android.app.LocaleManager
            if (localeManager != null) {
                val localeList = if (locale != null) android.os.LocaleList(locale) else android.os.LocaleList.getEmptyLocaleList()
                localeManager.applicationLocales = localeList
            }
        } else {
            val config = context.resources.configuration
            val targetLocale = locale ?: Locale.getDefault()
            config.setLocale(targetLocale)
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }

    if (showDownloadSheet) {
        DownloadLanguagesBottomSheet(
            onDismissRequest = { showDownloadSheet = false },
            onLanguageInstalled = { customLang ->
                refreshCustomTrigger++
                setAppLanguage(customLang.code, Locale.forLanguageTag(customLang.code))
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val typography = MaterialTheme.typography
            val overrideTypography = remember(typography) {
                typography.copy(headlineMedium = typography.displaySmall)
            }
            MaterialTheme(typography = overrideTypography) {
                LargeTopAppBar(
                    title = {
                        Text(text = stringResource(id = R.string.language))
                    },
                    navigationIcon = { BackButton { onNavigateBack() } },
                    actions = {
                        IconButton(onClick = { showDownloadSheet = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = stringResource(id = R.string.download_languages)
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    windowInsets = WindowInsets(0.dp),
                )
            }
        },
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 104.dp
                )
            ) {
                item {
                    PreferencesHintCard(
                        title = stringResource(id = R.string.translate),
                        description = stringResource(id = R.string.translate_desc),
                        icon = Icons.Outlined.Translate,
                    ) {
                        uriHandler.openUri("https://github.com/hamzabellouch/language/tree/main/Links")
                    }
                }

                // 1. Suggested (System only)
                item {
                    PreferenceSubtitle(text = stringResource(id = R.string.suggested))
                }

                item {
                    PreferenceSingleChoiceItem(
                        text = stringResource(id = R.string.follow_system),
                        selected = selectedLangTag == "system",
                        onClick = { setAppLanguage("system", null) },
                    )
                }

                // 2. Languages (Arabic, English, French)
                item {
                    PreferenceSubtitle(text = stringResource(id = R.string.languages))
                }

                items(defaultLanguagesList) { (displayName, langTag, locale) ->
                    PreferenceSingleChoiceItem(
                        text = displayName,
                        selected = selectedLangTag == langTag,
                        onClick = { setAppLanguage(langTag, locale) },
                    )
                }

                // 3. Downloaded Languages
                if (installedCustomLanguages.isNotEmpty()) {
                    item {
                        PreferenceSubtitle(text = stringResource(id = R.string.downloaded_languages))
                    }

                    items(installedCustomLanguages) { customLang ->
                        Surface(
                            onClick = {
                                setAppLanguage(customLang.code, Locale.forLanguageTag(customLang.code))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = customLang.name,
                                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = customLang.englishName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        LanguageManager.deleteCustomLanguage(context, customLang.code)
                                        refreshCustomTrigger++
                                        Toast.makeText(context, context.getString(R.string.language_deleted), Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = stringResource(R.string.delete_item),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                                RadioButton(
                                    selected = selectedLangTag == customLang.code,
                                    onClick = {
                                        setAppLanguage(customLang.code, Locale.forLanguageTag(customLang.code))
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        },
    )
}

@Composable
fun Conversation(modifier: Modifier = Modifier, text: String) {
    Row(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

/* ---------------- SponsorsPage ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SponsorsPage(onNavigateBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true },
    )
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val typography = MaterialTheme.typography
            val overrideTypography = remember(typography) {
                typography.copy(headlineMedium = typography.displaySmall)
            }

            MaterialTheme(typography = overrideTypography) {
                LargeTopAppBar(
                    title = {
                        Text(text = stringResource(id = R.string.sponsors))
                    },
                    navigationIcon = { BackButton { onNavigateBack() } },
                    scrollBehavior = scrollBehavior,
                    windowInsets = WindowInsets(0.dp),
                )
            }
        },
        content = { values ->
            LazyVerticalGrid(
                modifier = Modifier.padding(horizontal = 12.dp),
                columns = GridCells.Fixed(12),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = values,
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Surface(
                        shape = CardDefaults.shape,
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Text(
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .align(Alignment.CenterHorizontally),
                                text = stringResource(id = R.string.msg_from_developer),
                                style = MaterialTheme.typography.labelLarge,
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.developer_avatar),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .aspectRatio(1f, true)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Conversation(
                                        modifier = Modifier.padding(bottom = 12.dp),
                                        text = stringResource(id = R.string.sponsor_msg),
                                    )
                                    Conversation(
                                        modifier = Modifier,
                                        text = stringResource(id = R.string.sponsor_msg2),
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.sponsor_unavailable),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                },
                                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                modifier = Modifier.align(Alignment.End),
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(ButtonDefaults.IconSize),
                                    imageVector = Icons.Outlined.VolunteerActivism,
                                    contentDescription = null,
                                )

                                Text(text = stringResource(id = R.string.sponsor))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    uriHandler.openUri("https://github.com/hamzabellouch/links")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFC107),
                                    contentColor = Color(0xFF212121),
                                ),
                                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                modifier = Modifier.align(Alignment.End),
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(ButtonDefaults.IconSize),
                                    imageVector = StarIcon,
                                    contentDescription = null,
                                )

                                Text(text = stringResource(id = R.string.star))
                            }
                        }
                    }
                }
            }
        },
    )
}

/* ---------------- TroubleShootingPage ---------------- */

private const val reportProblemFormUrl = "https://docs.google.com/forms/d/e/1FAIpQLSf87zkBsPRiUX19qF42vekAwgV_bW2EWZZPEThTo8PFIOFc0w/viewform?usp=dialog"

@Composable
fun TroubleShootingPage(onNavigateBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    var showContactDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    val prefs = remember { context.getSharedPreferences("links_prefs", android.content.Context.MODE_PRIVATE) }
    var darkThemePref by remember { mutableIntStateOf(prefs.getInt("dark_theme", 0)) }

    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "dark_theme") {
                darkThemePref = p.getInt("dark_theme", 0)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val isDark = when (darkThemePref) {
        1 -> true
        2 -> false
        else -> isSystemInDarkTheme()
    }

    val emailContainer = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val emailContent = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    val whatsappContainer = if (isDark) Color(0xFF0A2B1D) else Color(0xFFE8F8F0)
    val whatsappContent = if (isDark) Color(0xFF25D366) else Color(0xFF128C7E)

    val facebookContainer = if (isDark) Color(0xFF0D2646) else Color(0xFFE7F3FF)
    val facebookContent = if (isDark) Color(0xFF4599FF) else Color(0xFF1877F2)

    val instagramContainer = if (isDark) Color(0xFF3D1625) else Color(0xFFFDF0F3)
    val instagramContent = if (isDark) Color(0xFFFF527B) else Color(0xFFD82E62)

    val linkedinContainer = if (isDark) Color(0xFF0E2E4E) else Color(0xFFE8F2FF)
    val linkedinContent = if (isDark) Color(0xFF55A4FC) else Color(0xFF0A66C2)

    val xContainer = if (isDark) Color(0xFF16181C) else Color(0xFFF5F8FA)
    val xContent = if (isDark) Color(0xFFE7E9EA) else Color(0xFF0F1419)

    val youtubeContainer = if (isDark) Color(0xFF3A1115) else Color(0xFFFFEBEE)
    val youtubeContent = if (isDark) Color(0xFFE53935) else Color(0xFFCC0000)

    val tiktokContainer = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF1F1F1)
    val tiktokContent = if (isDark) Color(0xFFFFFFFF) else Color(0xFF010101)

    val redditContainer = if (isDark) Color(0xFF3D1E16) else Color(0xFFFFEBE5)
    val redditContent = if (isDark) Color(0xFFFF5A1F) else Color(0xFFFF4500)

    val blueskyContainer = if (isDark) Color(0xFF0A2E4C) else Color(0xFFE8F8FF)
    val blueskyContent = if (isDark) Color(0xFF3BA1FF) else Color(0xFF0085FF)

    val telegramContainer = if (isDark) Color(0xFF0F2C3D) else Color(0xFFE8F5FA)
    val telegramContent = if (isDark) Color(0xFF52B6E9) else Color(0xFF24A1DE)

    BasePreferencePage(
        title = stringResource(R.string.trouble_shooting),
        onBack = onNavigateBack,
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            item {
                val pagerState = rememberPagerState(initialPage = 0) { 11 }
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth(),
                    ) { page ->
                        when (page) {
                            0 -> PreferencesHintCard(
                                title = stringResource(R.string.contact),
                                description = stringResource(R.string.contact_desc),
                                icon = Icons.Outlined.Email,
                                containerColor = emailContainer,
                                contentColor = emailContent,
                                textColor = Color.White,
                            ) { showContactDialog = true }

                            1 -> PreferencesHintCard(
                                title = stringResource(id = R.string.whatsapp),
                                icon = painterResource(id = R.drawable.ic_whatsapp),
                                description = stringResource(id = R.string.whatsapp_desc),
                                containerColor = whatsappContainer,
                                contentColor = whatsappContent,
                                textColor = Color.White,
                            ) { uriHandler.openUri("https://whatsapp.com/channel/0029Vb7MArw0LKZMpjjqOk2P") }

                            2 -> PreferencesHintCard(
                                title = stringResource(id = R.string.facebook),
                                icon = painterResource(id = R.drawable.ic_facebook),
                                description = stringResource(id = R.string.facebook_desc),
                                containerColor = facebookContainer,
                                contentColor = facebookContent,
                                textColor = Color.White,
                            ) { uriHandler.openUri("https://www.facebook.com/hamzabellouch0") }

                            3 -> PreferencesHintCard(
                                title = stringResource(id = R.string.instagram),
                                icon = painterResource(id = R.drawable.ic_instagram),
                                description = stringResource(id = R.string.instagram_desc),
                                containerColor = instagramContainer,
                                contentColor = instagramContent,
                                textColor = Color.White,
                            ) { uriHandler.openUri("https://www.instagram.com/hamzabellouch0") }

                            4 -> PreferencesHintCard(
                                title = stringResource(id = R.string.linkedin),
                                icon = painterResource(id = R.drawable.ic_linkedin),
                                description = stringResource(id = R.string.linkedin_desc),
                                containerColor = linkedinContainer,
                                contentColor = linkedinContent,
                                textColor = Color.White,
                            ) { uriHandler.openUri("https://www.linkedin.com/in/hamzabellouch") }

                            5 -> PreferencesHintCard(
                                title = stringResource(id = R.string.x_platform),
                                icon = painterResource(id = R.drawable.ic_x),
                                description = stringResource(id = R.string.x_desc),
                                containerColor = xContainer,
                                contentColor = xContent,
                                textColor = Color.White,
                            ) { uriHandler.openUri("https://x.com/hamzabellouch0") }

                            6 -> PreferencesHintCard(
                                title = stringResource(id = R.string.youtube),
                                icon = painterResource(id = R.drawable.ic_youtube),
                                description = stringResource(id = R.string.youtube_desc),
                                containerColor = youtubeContainer,
                                contentColor = youtubeContent,
                                textColor = Color.White,
                            ) { uriHandler.openUri("https://www.youtube.com/@hamzabellouch") }

                            7 -> PreferencesHintCard(
                                title = stringResource(id = R.string.tiktok),
                                icon = painterResource(id = R.drawable.ic_tiktok),
                                description = stringResource(id = R.string.tiktok_desc),
                                containerColor = tiktokContainer,
                                contentColor = tiktokContent,
                                textColor = Color.White,
                            ) { uriHandler.openUri("https://www.tiktok.com/@hamzabellouch0") }

                            8 -> PreferencesHintCard(
                                title = stringResource(id = R.string.reddit),
                                icon = painterResource(id = R.drawable.ic_reddit),
                                description = stringResource(id = R.string.reddit_desc),
                                containerColor = redditContainer,
                                contentColor = redditContent,
                                textColor = Color.White,
                            ) { uriHandler.openUri("https://www.reddit.com") }

                            9 -> PreferencesHintCard(
                                title = stringResource(id = R.string.bluesky),
                                icon = painterResource(id = R.drawable.ic_bluesky),
                                description = stringResource(id = R.string.bluesky_desc),
                                containerColor = blueskyContainer,
                                contentColor = blueskyContent,
                                textColor = Color.White,
                            ) { uriHandler.openUri("https://bsky.app/profile/hamzabellouch.bsky.social") }

                            10 -> PreferencesHintCard(
                                title = stringResource(id = R.string.telegram_channel),
                                icon = painterResource(id = R.drawable.icons8_telegram_app),
                                description = stringResource(id = R.string.telegram_channel_desc),
                                containerColor = telegramContainer,
                                contentColor = telegramContent,
                                textColor = Color.White,
                            ) { uriHandler.openUri("https://t.me/hamzabellouch") }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(11) { pageIndex ->
                            val isSelected = pagerState.currentPage == pageIndex
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant
                                    )
                            )
                        }
                    }
                }
            }
            item {
                OutlinedCard(modifier = Modifier.padding(16.dp)) {
                    PreferenceInfo(
                        modifier = Modifier,
                        text = stringResource(R.string.issue_tracker_hint),
                    )
                    PreferenceItem(
                        title = stringResource(R.string.links_issue_tracker),
                        description = null,
                        icon = Icons.AutoMirrored.Outlined.OpenInNew,
                        onClick = { uriHandler.openUri("https://github.com/hamzabellouch/links/issues") },
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp, bottom = 14.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(
                                    role = Role.Button,
                                    onClick = { showReportDialog = true }
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .height(36.dp)
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Report,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(R.string.problem_report),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            icon = {
                Icon(
                    imageVector = Report,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.problem_report),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.problem_report_dialog_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(
                                role = Role.Button,
                                onClick = {
                                    showReportDialog = false
                                    uriHandler.openUri(reportProblemFormUrl)
                                }
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Report,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.open_report_form),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButtonWithIcon(
                    icon = Icons.Outlined.Cancel,
                    text = stringResource(id = R.string.cancel),
                    onClick = { showReportDialog = false },
                )
            }
        )
    }

    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            confirmButton = {
                FilledButtonWithIcon(
                    icon = Icons.AutoMirrored.Outlined.ArrowForward,
                    text = stringResource(id = R.string.proceed),
                    onClick = {
                        showContactDialog = false
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:hamzabellouchcontact@gmail.com")
                            setPackage("com.google.android.gm")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val fallbackIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:hamzabellouchcontact@gmail.com")
                            }
                            try {
                                context.startActivity(Intent.createChooser(fallbackIntent, "Send Email"))
                            } catch (ex: Exception) {
                                android.widget.Toast.makeText(context, "No email app found", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                )
            },
            dismissButton = {
                OutlinedButtonWithIcon(
                    icon = Icons.Outlined.Cancel,
                    text = stringResource(id = R.string.cancel),
                    onClick = { showContactDialog = false },
                )
            },
            title = { Text(text = stringResource(R.string.contact_developer)) },
            text = { Text(text = stringResource(R.string.contact_developer_confirm)) },
        )
    }
}

/* ---------------- AboutPage ---------------- */

private const val releaseURL = "https://github.com/hamzabellouch/links/releases"
private const val repoUrl = "https://github.com/hamzabellouch/links/blob/main/README.md"
private const val githubIssueUrl = "https://github.com/hamzabellouch/links/issues"
private const val matrixSpaceUrl = "https://sites.google.com/view/hamzabellouch"
private const val githubSponsor = "https://github.com/sponsors/hamzabellouch"
private const val privacyPolicyUrl = "https://github.com/hamzabellouch/links/blob/main/PRIVACY_POLICY.md"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPage(
    onNavigateBack: () -> Unit,
    onNavigateToCreditsPage: () -> Unit,
    onNavigateToUpdatePage: () -> Unit = {},
    onNavigateToDonatePage: () -> Unit = {},
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            rememberTopAppBarState(),
            canScroll = { true },
        )
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val prefs = remember { context.getSharedPreferences("links_prefs", android.content.Context.MODE_PRIVATE) }
    var isAutoUpdateEnabled by remember {
        mutableStateOf(prefs.getBoolean("auto_update_enabled", false))
    }

    var showWhatsNewDialog by remember { mutableStateOf(false) }

    val whatsNewDismissedUntil = remember {
        prefs.getLong("whats_new_dismissed_until", 0L)
    }
    var isWhatsNewDismissed by remember {
        mutableStateOf(System.currentTimeMillis() < whatsNewDismissedUntil)
    }

    // Launch background update check
    AppUpdater(isAutoUpdateEnabled = isAutoUpdateEnabled)

    val versionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.5"
    } catch (e: Exception) {
        "0.0.5"
    }
    val info = "App version: $versionName\nPackage name: ${context.packageName}\nDevice: Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})"
    val uriHandler = LocalUriHandler.current

    fun openUrl(url: String) {
        uriHandler.openUri(url)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val typography = MaterialTheme.typography
            val overrideTypography =
                remember(typography) { typography.copy(headlineMedium = typography.displaySmall) }

            MaterialTheme(typography = overrideTypography) {
                LargeTopAppBar(
                    title = {
                        Text(modifier = Modifier, text = stringResource(id = R.string.about))
                    },
                    navigationIcon = { BackButton { onNavigateBack() } },
                    scrollBehavior = scrollBehavior,
                    windowInsets = WindowInsets(0.dp),
                    actions = {
                        if (!isWhatsNewDismissed) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .clip(CircleShape)
                            ) {
                                Row(
                                    modifier = Modifier.height(36.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // الجزء الأيسر: الأيقونة + النص (عند الضغط يفتح النافذة)
                                    Row(
                                        modifier = Modifier
                                            .clickable(
                                                role = Role.Button,
                                                onClick = { showWhatsNewDialog = true }
                                            )
                                            .padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = LocalFireDepartment,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = stringResource(R.string.whats_new),
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }

                                    // الجزء الأيمن: زر 'X' لإخفاء الكبسولة لمدة 24 ساعة
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 6.dp)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .clickable(
                                                role = Role.Button,
                                                onClick = {
                                                    val dismissUntil = System.currentTimeMillis() + 24 * 60 * 60 * 1000L
                                                    prefs.edit().putLong("whats_new_dismissed_until", dismissUntil).apply()
                                                    isWhatsNewDismissed = true
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.close),
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }
        },
        content = { scaffoldPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    top = scaffoldPadding.calculateTopPadding(),
                    bottom = scaffoldPadding.calculateBottomPadding() + 104.dp
                )
            ) {
                item {
                    PreferenceItem(
                        title = stringResource(R.string.readme),
                        description = stringResource(R.string.readme_desc),
                        icon = Icons.Outlined.Description,
                    ) {
                        openUrl(repoUrl)
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.release),
                        description = stringResource(R.string.release_desc),
                        icon = Icons.Outlined.NewReleases,
                    ) {
                        openUrl(releaseURL)
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.github_issue),
                        description = stringResource(R.string.github_issue_desc),
                        icon = Icons.Outlined.ContactSupport,
                    ) {
                        openUrl(githubIssueUrl)
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.github_stars),
                        description = stringResource(R.string.github_stars_desc),
                        icon = Icons.Outlined.StarBorder,
                    ) {
                        openUrl("https://github.com/hamzabellouch/links")
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.website),
                        description = matrixSpaceUrl,
                        icon = Icons.Outlined.Language,
                    ) {
                        openUrl(matrixSpaceUrl)
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.credits),
                        description = stringResource(id = R.string.credits_desc),
                        icon = Icons.Outlined.AutoAwesome,
                    ) {
                        onNavigateToCreditsPage()
                    }
                }
                item {
                    PreferenceSwitchWithDivider(
                        title = stringResource(R.string.auto_update),
                        description = stringResource(R.string.check_for_updates_desc),
                        icon =
                            if (isAutoUpdateEnabled) Icons.Outlined.Update
                            else Icons.Outlined.UpdateDisabled,
                        isChecked = isAutoUpdateEnabled,
                        isSwitchEnabled = true,
                        onClick = onNavigateToUpdatePage,
                        onChecked = {
                            isAutoUpdateEnabled = !isAutoUpdateEnabled
                            prefs.edit().putBoolean("auto_update_enabled", isAutoUpdateEnabled).apply()
                        },
                    )
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.privacy_policy),
                        description = stringResource(R.string.privacy_policy_desc),
                        icon = Policy,
                    ) {
                        openUrl(privacyPolicyUrl)
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.version),
                        description = versionName,
                        icon = Icons.Outlined.Info,
                    ) {
                        clipboardManager.setText(AnnotatedString(info))
                        android.widget.Toast.makeText(context, context.getString(R.string.info_copied), android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(R.string.package_name),
                        description = context.packageName,
                        icon = Icons.Outlined.Code,
                    ) {
                        clipboardManager.setText(AnnotatedString(context.packageName))
                        android.widget.Toast.makeText(context, context.getString(R.string.info_copied), android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        },
    )

    if (showWhatsNewDialog) {
        WhatsNewDialog(onDismissRequest = { showWhatsNewDialog = false })
    }
}

/* ---------------- CreditsPage ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsPage(onNavigateBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val creditsList = remember {
        listOf(
            Triple("Android Jetpack", "Apache License, Version 2.0", "https://github.com/androidx/androidx"),
            Triple("Kotlin", "Apache License, Version 2.0", "https://kotlinlang.org/"),
            Triple("Material Design 3", "Apache License, Version 2.0", "https://m3.material.io/"),
            Triple("Material Icons", "Apache License, Version 2.0", "https://fonts.google.com/icons"),
            Triple("Accompanist", "Apache License, Version 2.0", "https://github.com/google/accompanist"),
            Triple("ZXing", "Apache License, Version 2.0", "https://github.com/zxing/zxing"),
            Triple("App icon by Icons8", "Universal Multimedia Licensing Agreement for Icons8", "https://icons8.com/"),
        )
    }

    val uriHandler = LocalUriHandler.current

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.credits),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = { BackButton { onNavigateBack() } },
            )
        },
        content = { padding ->
            LazyColumn(modifier = Modifier.padding(padding)) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                            .clip(MaterialTheme.shapes.large)
                            .clickable {}
                            .clearAndSetSemantics {},
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        val painter = rememberVectorPainter(image = DynamicColorImageVectors.coder())
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier.padding(horizontal = 72.dp, vertical = 48.dp),
                        )
                    }
                }
                items(creditsList) { item ->
                    CreditItem(title = item.first, license = item.second) {
                        uriHandler.openUri(item.third)
                    }
                }
            }
        },
    )
}
