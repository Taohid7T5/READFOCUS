package com.example.ui.theme

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

private val ElegantDarkColorScheme =
  darkColorScheme(
    primary = ElegantPrimary,
    onPrimary = ElegantOnPrimary,
    primaryContainer = ElegantPrimaryContainer,
    onPrimaryContainer = ElegantOnPrimaryContainer,
    secondary = ElegantTextSecondary,
    onSecondary = ElegantDarkBg,
    secondaryContainer = ElegantNavPill,
    onSecondaryContainer = ElegantOnPrimaryContainer,
    tertiary = SpotifyGreen,
    onTertiary = Color.Black,
    background = ElegantDarkBg,
    onBackground = ElegantTextPrimary,
    surface = ElegantSurface,
    onSurface = ElegantTextPrimary,
    surfaceVariant = ElegantSurfaceVariant,
    onSurfaceVariant = ElegantTextSecondary,
    outline = Color(0x33CAC4D0),
    outlineVariant = Color(0x1FFFFFFF),
    error = CoralWarning,
    onError = CoralWarningDark
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to Elegant Dark
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = ElegantDarkColorScheme,
    typography = Typography,
    content = content
  )
}
