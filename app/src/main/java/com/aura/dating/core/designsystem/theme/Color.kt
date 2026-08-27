package com.aura.dating.core.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Gradient & Brand Colors
val AuraRose = Color(0xFFFF3366)
val AuraViolet = Color(0xFF7928CA)
val AuraPurple = Color(0xFF6C5CE7)
val AuraCoral = Color(0xFFFF6584)
val AuraCyan = Color(0xFF00C9FF)
val AuraBlue = Color(0xFF0072FF)
val AuraAmber = Color(0xFFFFB300)
val AuraEmerald = Color(0xFF00E676)

// Dark Theme Surfaces (2026 Obsidian / Glassmorphism)
val DarkBackground = Color(0xFF0A0B10)
val DarkSurface = Color(0xFF13151F)
val DarkSurfaceVariant = Color(0xFF1C1F2E)
val DarkCardBackground = Color(0xFF181B27)
val DarkBorder = Color(0xFF2A2E42)
val DarkTextPrimary = Color(0xFFF5F6FA)
val DarkTextSecondary = Color(0xFF9EA3B8)
val DarkTextTertiary = Color(0xFF656B82)

// Light Theme Surfaces
val LightBackground = Color(0xFFF8F9FD)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEEF1F8)
val LightCardBackground = Color(0xFFFFFFFF)
val LightBorder = Color(0xFFE2E6F0)
val LightTextPrimary = Color(0xFF11142D)
val LightTextSecondary = Color(0xFF6B7280)
val LightTextTertiary = Color(0xFF9CA3AF)

// Status & Action Colors
val LikeColor = Color(0xFF00E676)
val PassColor = Color(0xFFFF4757)
val SuperLikeColor = Color(0xFF00C9FF)
val RewindColor = Color(0xFFFFB300)
val OnlineColor = Color(0xFF00E676)
val OfflineColor = Color(0xFF9E9E9E)

// Gradients
val AuraPrimaryGradient = Brush.horizontalGradient(
    colors = listOf(AuraRose, AuraViolet)
)

val AuraSuperLikeGradient = Brush.horizontalGradient(
    colors = listOf(AuraCyan, AuraBlue)
)

val AuraDarkCardGradient = Brush.verticalGradient(
    colors = listOf(Color.Transparent, Color(0xCC0A0B10), Color(0xF00A0B10))
)

val AuraGoldGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
)
