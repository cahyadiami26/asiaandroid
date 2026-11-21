package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HrisColorScheme = lightColorScheme(
    primary = Navy800,
    onPrimary = Color.White,
    primaryContainer = Navy900,
    onPrimaryContainer = Color.White,
    secondary = Teal600,
    onSecondary = Color.White,
    secondaryContainer = Teal50,
    onSecondaryContainer = Teal700,
    tertiary = Indigo600,
    onTertiary = Color.White,
    background = Bg,
    onBackground = Ink,
    surface = CardBg,
    onSurface = Ink,
    surfaceVariant = Bg2,
    onSurfaceVariant = Ink2,
    outline = Line,
    outlineVariant = LineStrong,
    error = Rose600,
    onError = Color.White,
    errorContainer = Rose50,
    onErrorContainer = Rose600
)

@Composable
fun HrisTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HrisColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
