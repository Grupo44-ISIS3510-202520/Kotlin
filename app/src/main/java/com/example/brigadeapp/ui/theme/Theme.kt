package com.example.brigadeapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = White,
    secondary = BrandBlue,
    tertiary = PastelBlue,
    background = White,
    surface = White,
    onSurface = Ink,
    error = BrandRed,
    onError = White
)

private val DarkColors = darkColorScheme(
    primary = BrandBlue,
    onPrimary = White,
    background = Ink,
    surface = Ink,
    onSurface = White,
    error = BrandRed,
    onError = White
)

@Composable
fun BrigadeAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
