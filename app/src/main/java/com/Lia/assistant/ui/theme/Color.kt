package com.Lia.assistant.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class NovaColorScheme(
    val background: Color,
    val backgroundGradientTop: Color,
    val surface: Color,
    val surfaceGlass: Color,
    val surfaceBorder: Color,
    val surfaceRaised: Color,
    val accent: Color,
    val accentSecondary: Color,
    val accentGlow: Color,
    val onAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val userBubble: Color,
    val assistantBubble: Color,
    val depthShadow: Color,
    val rimLight: Color,
    val lagoon: Color,
    val lotus: Color
)

val DarkNovaColors = NovaColorScheme(
    background = Color(0xFF06050E),
    backgroundGradientTop = Color(0xFF15113A),
    surface = Color(0xFF17143A),
    surfaceGlass = Color(0x3317143A),
    surfaceBorder = Color(0x33EEE9FF),
    surfaceRaised = Color(0xFF221E4D),
    accent = Color(0xFFFFB547),
    accentSecondary = Color(0xFFFF8A3D),
    accentGlow = Color(0x66FFB547),
    onAccent = Color(0xFF15112E),
    textPrimary = Color(0xFFEEE9FF),
    textSecondary = Color(0xFFA7A0CC),
    textTertiary = Color(0xFF7A73A6),
    success = Color(0xFF34D399),
    warning = Color(0xFFFBBF24),
    error = Color(0xFFFF7A85),
    userBubble = Color(0xFF2E2660),
    assistantBubble = Color(0x401E1A47),
    depthShadow = Color(0x99000000),
    rimLight = Color(0x40FFFFFF),
    lagoon = Color(0xFF3FE0D0),
    lotus = Color(0xFFFF6FAE)
)

val LightNovaColors = NovaColorScheme(
    background = Color(0xFFF3F0FB),
    backgroundGradientTop = Color(0xFFE5DEFB),
    surface = Color(0xFFFFFFFF),
    surfaceGlass = Color(0x80FFFFFF),
    surfaceBorder = Color(0x2215112E),
    surfaceRaised = Color(0xFFF9F7FE),
    accent = Color(0xFFB86A00),
    accentSecondary = Color(0xFFD97706),
    accentGlow = Color(0x33B86A00),
    onAccent = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF15112E),
    textSecondary = Color(0xFF5B5480),
    textTertiary = Color(0xFF8A82B0),
    success = Color(0xFF059669),
    warning = Color(0xFFD97706),
    error = Color(0xFFDC2626),
    userBubble = Color(0xFFE9E4F7),
    assistantBubble = Color(0xFFFFFFFF),
    depthShadow = Color(0x1A000000),
    rimLight = Color(0x80FFFFFF),
    lagoon = Color(0xFF00A896),
    lotus = Color(0xFFD94680)
)

val LocalNovaColors = staticCompositionLocalOf { DarkNovaColors }
val LocalNovaReducedMotion = staticCompositionLocalOf { false }
