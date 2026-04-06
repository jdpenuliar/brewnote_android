package com.example.brewnote.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = CoffeeBrown,
    onPrimary = NeutralWhite,
    primaryContainer = Latte,
    onPrimaryContainer = Espresso,
    secondary = Espresso,
    onSecondary = NeutralWhite,
    tertiary = Cappuccino,
    background = LightCream,
    onBackground = Espresso,
    surface = Cream,
    onSurface = Espresso,
    surfaceVariant = Latte,
    onSurfaceVariant = CoffeeBrown,
    outline = Cappuccino,
    outlineVariant = SecondaryFill,
    error = Destructive,
    onError = NeutralWhite,
)

// Since the provided Swift theme is light-mode focused, 
// we'll create a dark variant that maintains the coffee aesthetic.
private val DarkColorScheme = darkColorScheme(
    primary = Latte,
    onPrimary = Espresso,
    primaryContainer = CoffeeBrown,
    onPrimaryContainer = LightCream,
    secondary = Cappuccino,
    onSecondary = Espresso,
    background = Espresso,
    onBackground = LightCream,
    surface = Color(0xFF2D201B), // Darker Espresso
    onSurface = LightCream,
    surfaceVariant = CoffeeBrown,
    onSurfaceVariant = Latte,
    outline = CoffeeBrown,
    outlineVariant = Espresso,
    error = Destructive,
    onError = NeutralWhite,
)

object AppDimensions {
    val CardCornerRadius = 12.dp
    val ButtonCornerRadius = 10.dp
    val InputCornerRadius = 8.dp
    val DefaultPadding = 16.dp
    val CompactPadding = 8.dp
}

@Composable
fun BrewnoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
