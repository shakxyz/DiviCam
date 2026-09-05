package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DiviCyanAccent,
    onPrimary = Color.Black,
    primaryContainer = DiviBluePrimary,
    onPrimaryContainer = Color.White,
    secondary = DiviBluePrimary,
    onSecondary = Color.White,
    background = DiviDarkBackground,
    onBackground = DiviTextPrimary,
    surface = DiviDarkSurface,
    onSurface = DiviTextPrimary,
    surfaceVariant = DiviCardSurface,
    onSurfaceVariant = DiviTextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
