package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val AstraNovaDarkColorScheme = darkColorScheme(
    primary = ChampagneGold,
    onPrimary = MidnightCosmos,
    primaryContainer = CosmicSlate,
    onPrimaryContainer = StarlightIvory,
    secondary = MoonlitLavender,
    onSecondary = MidnightCosmos,
    secondaryContainer = ElevatedTwilight,
    onSecondaryContainer = StarlightIvory,
    tertiary = RoseQuartz,
    onTertiary = MidnightCosmos,
    tertiaryContainer = ElevatedTwilight,
    onTertiaryContainer = RoseQuartz,
    background = MidnightCosmos,
    onBackground = StarlightIvory,
    surface = DeepIndigoSurface,
    onSurface = StarlightIvory,
    surfaceVariant = ElevatedTwilight,
    onSurfaceVariant = MutedNebula,
    outline = HairlineBorder,
    error = SoftErrorCoral,
    onError = MidnightCosmos
)

private val AstraNovaShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun AstraNovaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AstraNovaDarkColorScheme,
        typography = AstraNovaTypography,
        shapes = AstraNovaShapes,
        content = content
    )
}
