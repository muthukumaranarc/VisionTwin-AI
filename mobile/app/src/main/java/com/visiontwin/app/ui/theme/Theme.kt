package com.visiontwin.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── VisionTwin AI Design Tokens (mobile/Design/DESIGN.md) ────────────────────

val VTPrimary = Color(0xFF0050CB)
val VTOnPrimary = Color(0xFFFFFFFF)
val VTPrimaryContainer = Color(0xFF0066FF)
val VTOnPrimaryContainer = Color(0xFFF8F7FF)
val VTPrimaryFixed = Color(0xFFDAE1FF)
val VTPrimaryFixedDim = Color(0xFFB3C5FF)
val VTOnPrimaryFixedVariant = Color(0xFF003FA4)

val VTBackground = Color(0xFFFAF8FF)
val VTOnBackground = Color(0xFF131B2E)
val VTSurface = Color(0xFFFAF8FF)
val VTSurfaceBright = Color(0xFFFAF8FF)
val VTSurfaceLow = Color(0xFFF2F3FF)
val VTSurfaceContainer = Color(0xFFEAEDFF)
val VTSurfaceContainerHigh = Color(0xFFE2E7FF)
val VTSurfaceContainerHighest = Color(0xFFDAE2FD)
val VTOnSurface = Color(0xFF131B2E)
val VTOnSurfaceVariant = Color(0xFF424656)
val VTOutline = Color(0xFF727687)
val VTOutlineVariant = Color(0xFFC2C6D8)
val VTSurfaceVariant = Color(0xFFDAE2FD)

val VTSecondary = Color(0xFF585F6C)
val VTOnSecondary = Color(0xFFFFFFFF)
val VTSecondaryContainer = Color(0xFFDCE2F3)
val VTOnSecondaryContainer = Color(0xFF5E6572)

val VTError = Color(0xFFBA1A1A)
val VTOnError = Color(0xFFFFFFFF)
val VTErrorContainer = Color(0xFFFFDAD6)
val VTOnErrorContainer = Color(0xFF93000A)

val VTSuccess = Color(0xFF2E7D32)

// Card chrome per design: 1px border #E5E7EB + soft shadow 0 4px 12px rgba(0,0,0,0.05)
val VTCardBorder = Color(0xFFE5E7EB)
val VTCardShadow = Color(0x0D000000)

// Legacy aliases so existing screens keep compiling while migrating to the new tokens
val PrimaryBlue = VTPrimary
val PrimaryBlueDark = Color(0xFF003A94)
val PrimaryBlueLight = VTPrimaryFixed
val AccentOrange = Color(0xFFFF9800)
val NeutralGray = VTSurfaceLow
val LightGray = VTOutlineVariant
val MediumGray = VTOutline
val DarkText = VTOnSurface
val SecondaryText = VTOnSurfaceVariant
val White = Color.White
val ErrorRed = VTError
val SuccessGreen = VTSuccess
val CardBackground = Color.White
val ScreenBackground = VTBackground

// ─── Typography (Hanken Grotesk / Inter / JetBrains Mono — system fallbacks) ──

private val LabelCaps = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 12.sp,
    fontWeight = FontWeight.Medium,
    lineHeight = 16.sp,
    letterSpacing = 0.6.sp
)

val LabelCapsStyle: TextStyle
    @Composable get() = LabelCaps

private val LightColorScheme = lightColorScheme(
    primary = VTPrimary,
    onPrimary = VTOnPrimary,
    primaryContainer = VTPrimaryContainer,
    onPrimaryContainer = VTOnPrimaryContainer,
    secondary = VTSecondary,
    onSecondary = VTOnSecondary,
    secondaryContainer = VTSecondaryContainer,
    onSecondaryContainer = VTOnSecondaryContainer,
    background = VTBackground,
    onBackground = VTOnBackground,
    surface = VTSurface,
    onSurface = VTOnSurface,
    surfaceVariant = VTSurfaceVariant,
    onSurfaceVariant = VTOnSurfaceVariant,
    surfaceContainerLow = VTSurfaceLow,
    surfaceContainer = VTSurfaceContainer,
    surfaceContainerHigh = VTSurfaceContainerHigh,
    surfaceContainerHighest = VTSurfaceContainerHighest,
    outline = VTOutline,
    outlineVariant = VTOutlineVariant,
    error = VTError,
    onError = VTOnError,
    errorContainer = VTErrorContainer,
    onErrorContainer = VTOnErrorContainer,
)

val CardShape = RoundedCornerShape(8.dp)
val ButtonShape = RoundedCornerShape(8.dp)
val InputShape = RoundedCornerShape(8.dp)
val SheetShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)

@Composable
fun VisionTwinTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(
            headlineLarge = TextStyle(
                fontSize = 28.sp, fontWeight = FontWeight.SemiBold,
                lineHeight = 34.sp, letterSpacing = (-0.5).sp, color = VTOnSurface
            ),
            headlineMedium = TextStyle(
                fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                lineHeight = 28.sp, letterSpacing = (-0.2).sp, color = VTOnSurface
            ),
            titleLarge = TextStyle(
                fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                lineHeight = 26.sp, color = VTOnSurface
            ),
            titleMedium = TextStyle(
                fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                lineHeight = 22.sp, color = VTOnSurface
            ),
            bodyLarge = TextStyle(
                fontSize = 16.sp, fontWeight = FontWeight.Normal,
                lineHeight = 24.sp, color = VTOnSurface
            ),
            bodyMedium = TextStyle(
                fontSize = 14.sp, fontWeight = FontWeight.Normal,
                lineHeight = 20.sp, color = VTOnSurfaceVariant
            ),
            labelLarge = TextStyle(
                fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp, color = VTOnPrimary
            ),
            labelSmall = LabelCaps,
        ),
        shapes = Shapes(
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(8.dp),
            large = RoundedCornerShape(12.dp),
        ),
        content = content
    )
}
