package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GarminOrange,
    onPrimary = TextPrimaryDark,
    primaryContainer = GarminOrangeDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = GarminCyan,
    onSecondary = DarkTacticalBackground,
    secondaryContainer = DarkTacticalSurfaceVariant,
    onSecondaryContainer = GarminCyan,
    tertiary = GarminEmerald,
    background = DarkTacticalBackground,
    surface = DarkTacticalSurface,
    surfaceVariant = DarkTacticalSurfaceVariant,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkTacticalBorder,
    error = GarminRed
)

private val LightColorScheme = lightColorScheme(
    primary = GarminOrange,
    onPrimary = TextPrimaryDark,
    primaryContainer = GarminOrangeDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = GarminBlue,
    onSecondary = TextPrimaryDark,
    secondaryContainer = LightOutdoorSurfaceVariant,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = GarminEmerald,
    background = LightOutdoorBackground,
    surface = LightOutdoorSurface,
    surfaceVariant = LightOutdoorSurfaceVariant,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightOutdoorBorder,
    error = GarminRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to rugged outdoor tactical dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
