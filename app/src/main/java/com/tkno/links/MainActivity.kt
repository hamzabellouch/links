package com.tkno.links

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.tkno.links.theme.LinksTheme
import com.tkno.links.ui.main.MainScreen
import com.tkno.links.ui.onboarding.OnboardingScreen
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

import android.content.Intent
import com.tkno.links.ui.common.LocalDarkTheme
import com.tkno.links.util.DarkThemePreference
import com.tkno.links.util.LanguageManager
import com.tkno.links.util.PreferenceUtil
import com.tkno.links.util.UpdateNotificationHelper

class MainActivity : ComponentActivity() {
  private var navigateToUpdateState = mutableStateOf(false)
  private var triggerUpdateState = mutableStateOf(false)

  override fun attachBaseContext(newBase: Context) {
    val prefs = newBase.getSharedPreferences("links_prefs", Context.MODE_PRIVATE)
    val appLanguage = prefs.getString("app_language", "system") ?: "system"
    if (appLanguage != "system") {
      val localizedContext = LanguageManager.wrapContext(newBase, appLanguage)
      super.attachBaseContext(localizedContext)
    } else {
      super.attachBaseContext(newBase)
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIntent(intent)
  }

  private fun handleIntent(intent: Intent?) {
    if (intent == null) return
    val navigateTo = intent.getStringExtra(UpdateNotificationHelper.EXTRA_NAVIGATE_TO)
    val trigger = intent.getBooleanExtra(UpdateNotificationHelper.EXTRA_TRIGGER_UPDATE, false)
    if (navigateTo == UpdateNotificationHelper.NAV_TARGET_AUTO_UPDATE) {
      navigateToUpdateState.value = true
      triggerUpdateState.value = trigger
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    PreferenceUtil.init(this)
    UpdateNotificationHelper.createNotificationChannel(this)
    handleIntent(intent)

    enableEdgeToEdge()
    setContent {
      val context = LocalContext.current
      val prefs = remember { context.getSharedPreferences("links_prefs", Context.MODE_PRIVATE) }

      val hasCompletedOnboarding = remember { prefs.getBoolean("onboarding_completed", false) }
      var showOnboarding by rememberSaveable { mutableStateOf(!hasCompletedOnboarding) }

      var darkThemePref by remember { mutableIntStateOf(prefs.getInt("dark_theme", DarkThemePreference.FOLLOW_SYSTEM)) }
      var isHighContrastPref by remember { mutableStateOf(prefs.getBoolean("high_contrast_dark_theme", false)) }
      var dynamicColorPref by remember { mutableStateOf(prefs.getBoolean("dynamic_color", true)) }
      var appLanguagePref by remember { mutableStateOf(prefs.getString("app_language", "system") ?: "system") }

      DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
          if (key == "dark_theme") {
            darkThemePref = p.getInt("dark_theme", DarkThemePreference.FOLLOW_SYSTEM)
          } else if (key == "high_contrast_dark_theme") {
            isHighContrastPref = p.getBoolean("high_contrast_dark_theme", false)
          } else if (key == "dynamic_color") {
            dynamicColorPref = p.getBoolean("dynamic_color", true)
          } else if (key == "app_language") {
            appLanguagePref = p.getString("app_language", "system") ?: "system"
          }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
          prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
      }

      val localeContext = remember(appLanguagePref, context) {
        LanguageManager.wrapContext(context, appLanguagePref)
      }

      val darkThemePreference = remember(darkThemePref, isHighContrastPref) {
        DarkThemePreference(darkThemePref, isHighContrastPref)
      }

      val isDark = darkThemePreference.isDarkTheme()

      val currentLocale = remember(appLanguagePref) {
        if (appLanguagePref == "system") {
          androidx.core.os.ConfigurationCompat.getLocales(resources.configuration)[0] ?: Locale.getDefault()
        } else {
          val custom = LanguageManager.loadCustomLanguage(context, appLanguagePref)
          if (custom != null) {
            Locale.forLanguageTag(custom.code)
          } else {
            Locale.forLanguageTag(appLanguagePref)
          }
        }
      }

      val isRtl = remember(currentLocale, appLanguagePref) {
        val custom = LanguageManager.loadCustomLanguage(context, appLanguagePref)
        if (custom != null) {
          custom.direction.equals("rtl", ignoreCase = true)
        } else {
          androidx.core.text.TextUtilsCompat.getLayoutDirectionFromLocale(currentLocale) == android.view.View.LAYOUT_DIRECTION_RTL
        }
      }

      val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

      androidx.compose.runtime.LaunchedEffect(isDark) {
        enableEdgeToEdge(
          statusBarStyle = if (isDark) {
            androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
          } else {
            androidx.activity.SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
          },
          navigationBarStyle = if (isDark) {
            androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
          } else {
            androidx.activity.SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
          }
        )
      }

      CompositionLocalProvider(
        LocalContext provides localeContext,
        LocalLayoutDirection provides layoutDirection,
        LocalDarkTheme provides darkThemePreference
      ) {
        LinksTheme(
          darkTheme = isDark,
          isHighContrast = isHighContrastPref,
          dynamicColor = dynamicColorPref
        ) {
          Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (showOnboarding) {
              OnboardingScreen(
                onFinished = {
                  prefs.edit().putBoolean("onboarding_completed", true).apply()
                  showOnboarding = false
                }
              )
            } else {
              MainScreen(
                navigateToUpdate = navigateToUpdateState.value,
                triggerUpdate = triggerUpdateState.value,
                onNavigateToUpdateConsumed = {
                  navigateToUpdateState.value = false
                  triggerUpdateState.value = false
                }
              )
            }
          }
        }
      }
    }
  }
}
