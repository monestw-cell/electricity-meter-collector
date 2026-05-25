package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    secondary = SecondaryGreen,
    tertiary = AccentYellow,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = Color(0xFF162B26),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = BackgroundDark,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceLight.copy(alpha = 0.8f),
    primaryContainer = Color(0xFF064E3B),
    secondaryContainer = Color(0xFF065F46),
    onPrimaryContainer = Color(0xFFD1FAE5),
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    secondary = SecondaryGreen,
    tertiary = AccentYellow,
    background = Color(0xFFF4FAF7),
    surface = Color.White,
    surfaceVariant = Color(0xFFEBF5F0),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = BackgroundDark,
    onBackground = Color(0xFF0C1D18),
    onSurface = Color(0xFF0C1D18),
    onSurfaceVariant = Color(0xFF283D36),
    primaryContainer = Color(0xFFDCFCE7),
    secondaryContainer = Color(0xFFE6F4EA),
    onPrimaryContainer = Color(0xFF047857),
    error = ErrorRed
)

@Composable
fun AadadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
