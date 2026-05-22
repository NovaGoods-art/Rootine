package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = EarthPrimaryDark,
    onPrimary = EarthBackgroundDark,
    primaryContainer = EarthCardDark,
    onPrimaryContainer = EarthLightText,
    secondary = EarthAmberDark,
    background = EarthBackgroundDark,
    onBackground = EarthLightText,
    surface = EarthCardDark,
    onSurface = EarthLightText,
    error = EarthAmberDark, // Warm amber instead of aggressive red
    outline = EarthGrayQuietDark
)

private val LightColorScheme = lightColorScheme(
    primary = EarthPrimary,
    onPrimary = EarthBackgroundLight,
    primaryContainer = EarthCardLight,
    onPrimaryContainer = EarthDarkText,
    secondary = EarthAmber,
    background = EarthBackgroundLight,
    onBackground = EarthDarkText,
    surface = EarthCardLight,
    onSurface = EarthDarkText,
    error = EarthAmber, // Warm amber instead of aggressive red
    outline = EarthGrayQuiet
)

@Composable
fun RootineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
