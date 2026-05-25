package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    secondary = GreenGrey80,
    tertiary = LightGreen80,
    background = Color(0xFF101410),       // Premium dark forest background
    surface = Color(0xFF1A1F1A),          // Elevated card/container surfaces
    onBackground = Color(0xFFE2E3E2),     // Light text on backgrounds
    onSurface = Color(0xFFE2E3E2),        // Light text on surfaces
    surfaceVariant = Color(0xFF242B24),    // Context/inner surfaces
    onSurfaceVariant = Color(0xFFC2C8C2),   // Secondary labels
    outline = Color(0xFF3F493F)           // Input borders
)

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    secondary = GreenGrey40,
    tertiary = LightGreen40
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark mode as requested by user
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
