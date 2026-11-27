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

private val NetCityLightColors = lightColorScheme(
    primary = NetCyanPrimary,
    onPrimary = Color.White,
    primaryContainer = NetCyanContainer,
    onPrimaryContainer = NetOnCyanContainer,
    secondary = NetEmerald,
    onSecondary = Color.White,
    secondaryContainer = NetEmeraldContainer,
    onSecondaryContainer = NetOnEmeraldContainer,
    tertiary = NetAmber,
    onTertiary = Color.White,
    tertiaryContainer = NetAmberContainer,
    onTertiaryContainer = NetOnAmberContainer,
    error = NetCoralError,
    onError = Color.White,
    errorContainer = NetCoralContainer,
    onErrorContainer = Color(0xFF7F1D1D),
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

private val NetCityDarkColors = darkColorScheme(
    primary = NetCyanBright,
    onPrimary = NetNavyDeep,
    primaryContainer = NetNavyElevated,
    onPrimaryContainer = NetCyanBright,
    secondary = NetEmeraldBright,
    onSecondary = NetNavyDeep,
    secondaryContainer = Color(0xFF0A3D2E),
    onSecondaryContainer = NetEmeraldBright,
    tertiary = NetAmberBright,
    onTertiary = NetNavyDeep,
    tertiaryContainer = Color(0xFF4A2E05),
    onTertiaryContainer = NetAmberBright,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

val NetCityShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun NetCityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NetCityDarkColors else NetCityLightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NetCityTypography,
        shapes = NetCityShapes,
        content = content
    )
}
