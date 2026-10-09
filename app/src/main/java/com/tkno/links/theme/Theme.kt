package com.tkno.links.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF0C100D),
    primaryContainer = Color(0xFF1B263B),
    onPrimaryContainer = Color(0xFFD2E3FC),
    secondary = Color(0xFF8AB4F8),
    onSecondary = Color(0xFF0C100D),
    secondaryContainer = Color(0xFF203248),
    onSecondaryContainer = Color(0xFFD2E3FC),
    background = Color(0xFF0C100D),
    onBackground = Color(0xFFE2E3E0),
    surface = Color(0xFF0C100D),
    onSurface = Color(0xFFE2E3E0),
    surfaceVariant = Color(0xFF1E2621),
    onSurfaceVariant = Color(0xFFC2C9C2),
    surfaceContainer = Color(0xFF131815),
    surfaceContainerLow = Color(0xFF181F1B),
    surfaceContainerHigh = Color(0xFF1D2520),
    surfaceContainerHighest = Color(0xFF232B25),
    outline = Color(0xFF8C938C),
    outlineVariant = Color(0xFF202A36),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A73E8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD2E3FC),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF1A73E8),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F0FE),
    onSecondaryContainer = Color(0xFF001D36),
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE1E3E1),
    onSurfaceVariant = Color(0xFF444746),
    surfaceContainer = Color(0xFFF1F4F1),
    surfaceContainerLow = Color(0xFFF6F9F6),
    surfaceContainerHigh = Color(0xFFEBEFEB),
    surfaceContainerHighest = Color(0xFFE3E8E3),
    outline = Color(0xFF747775),
    outlineVariant = Color(0xFFC4C7C5),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun LinksTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isHighContrast: Boolean = false,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val dynamicScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            val brandPrimary = if (darkTheme) Color(0xFF8AB4F8) else Color(0xFF1A73E8)
            val brandOnPrimary = if (darkTheme) Color(0xFF0C100D) else Color.White
            val brandPrimaryContainer = if (darkTheme) Color(0xFF1B263B) else Color(0xFFD2E3FC)
            val brandSecondaryContainer = if (darkTheme) Color(0xFF203248) else Color(0xFFE8F0FE)
            val brandOnSecondaryContainer = if (darkTheme) Color(0xFFD2E3FC) else Color(0xFF001D36)

            val baseScheme = dynamicScheme.copy(
                primary = brandPrimary,
                onPrimary = brandOnPrimary,
                primaryContainer = brandPrimaryContainer,
                onPrimaryContainer = brandOnSecondaryContainer,
                secondary = brandPrimary,
                onSecondary = brandOnPrimary,
                secondaryContainer = brandSecondaryContainer,
                onSecondaryContainer = brandOnSecondaryContainer
            )

            if (darkTheme && isHighContrast) {
                baseScheme.copy(
                    background = Color.Black,
                    surface = Color.Black,
                    surfaceContainer = Color(0xFF121212),
                    surfaceContainerLow = Color(0xFF0A0A0A),
                    surfaceContainerHigh = Color(0xFF1A1A1A),
                    surfaceContainerHighest = Color(0xFF222222),
                )
            } else {
                baseScheme
            }
        }
        darkTheme -> {
            if (isHighContrast) {
                DarkColorScheme.copy(
                    background = Color.Black,
                    surface = Color.Black,
                    surfaceContainer = Color(0xFF121212),
                    surfaceContainerLow = Color(0xFF0A0A0A),
                    surfaceContainerHigh = Color(0xFF1A1A1A),
                    surfaceContainerHighest = Color(0xFF222222),
                )
            } else {
                DarkColorScheme
            }
        }
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
