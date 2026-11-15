package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = Color.White,
    primaryContainer = SageContainer,
    onPrimaryContainer = ForestDeep,
    secondary = Terracotta,
    onSecondary = Color.White,
    secondaryContainer = OchreSand,
    onSecondaryContainer = TerracottaDark,
    tertiary = TrailEmerald,
    onTertiary = Color.White,
    background = ParchmentBg,
    onBackground = SlateInk,
    surface = CardWhite,
    onSurface = SlateInk,
    surfaceVariant = SoftSageSurface,
    onSurfaceVariant = MutedMossText,
    outline = StoneBorder,
    outlineVariant = Color(0xFFE7ECE6)
)

private val DarkColors = darkColorScheme(
    primary = NightMintPrimary,
    onPrimary = ForestDeep,
    primaryContainer = PineGreen,
    onPrimaryContainer = NightCreamText,
    secondary = Terracotta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A2516),
    onSecondaryContainer = Color(0xFFFFD8CA),
    tertiary = MeadowMint,
    onTertiary = ForestDeep,
    background = NightPineBg,
    onBackground = NightCreamText,
    surface = NightCardSurface,
    onSurface = NightCreamText,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = NightMutedText,
    outline = Color(0xFF2C4437),
    outlineVariant = Color(0xFF213329)
)

val TrailIndiaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun TrailIndiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TrailIndiaTypography,
        shapes = TrailIndiaShapes,
        content = content
    )
}
