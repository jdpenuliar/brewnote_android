package com.example.brewnote.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Neutral900,
    onPrimary = NeutralWhite,
    primaryContainer = Neutral100,
    onPrimaryContainer = Neutral900,
    secondary = Neutral500,
    onSecondary = NeutralWhite,
    background = NeutralWhite,
    onBackground = Neutral900,
    surface = Neutral50,
    onSurface = Neutral900,
    surfaceVariant = Neutral50,
    onSurfaceVariant = Neutral500,
    outline = Neutral200,
    outlineVariant = Neutral200,
)

private val DarkColorScheme = darkColorScheme(
    primary = NeutralWhite,
    onPrimary = Neutral950,
    primaryContainer = Neutral800,
    onPrimaryContainer = NeutralWhite,
    secondary = Neutral500,
    onSecondary = Neutral950,
    background = Neutral950,
    onBackground = NeutralWhite,
    surface = Neutral900,
    onSurface = NeutralWhite,
    surfaceVariant = Neutral900,
    onSurfaceVariant = Neutral500,
    outline = Neutral800,
    outlineVariant = Neutral700,
)

@Composable
fun BrewnoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
