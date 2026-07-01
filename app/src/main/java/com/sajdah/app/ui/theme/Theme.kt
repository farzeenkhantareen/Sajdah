package com.sajdah.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Premium Islamic Dark Palette
val DeepNavy       = Color(0xFF0A0E1A)   // Background
val CardNavy       = Color(0xFF111827)   // Surface
val ElevatedNavy   = Color(0xFF1A2235)   // Surface variant
val EmeraldPrimary = Color(0xFF10B981)   // Primary accent (emerald green)
val EmeraldLight   = Color(0xFF34D399)   // Primary light
val GoldAccent     = Color(0xFFD4A843)   // Secondary (gold)
val GoldLight      = Color(0xFFFFD76E)   // Secondary container
val RoseAccent     = Color(0xFFF87171)   // Error / tertiary
val WhiteText      = Color(0xFFF0F4FF)   // On-background
val MutedText      = Color(0xFF8899BB)   // Subdued text

private val DarkColorScheme = darkColorScheme(
    primary            = EmeraldPrimary,
    onPrimary          = Color(0xFF002D1E),
    primaryContainer   = Color(0xFF00412D),
    onPrimaryContainer = EmeraldLight,

    secondary            = GoldAccent,
    onSecondary          = Color(0xFF1A0F00),
    secondaryContainer   = Color(0xFF3D2800),
    onSecondaryContainer = GoldLight,

    tertiary            = RoseAccent,
    onTertiary          = Color(0xFF2D0000),
    tertiaryContainer   = Color(0xFF3D0000),
    onTertiaryContainer = Color(0xFFFFB4AB),

    background         = DeepNavy,
    onBackground       = WhiteText,
    surface            = CardNavy,
    onSurface          = WhiteText,
    surfaceVariant     = ElevatedNavy,
    onSurfaceVariant   = MutedText,

    outline            = Color(0xFF2A3A55),
    outlineVariant     = Color(0xFF1A2540),
    error              = RoseAccent,
)

@Composable
fun SajdahTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
