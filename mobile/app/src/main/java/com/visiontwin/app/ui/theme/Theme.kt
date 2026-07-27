package com.visiontwin.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val PrimaryBlue = Color(0xFF1976D2)
val PrimaryBlueDark = Color(0xFF0D47A1)
val PrimaryBlueLight = Color(0xFFBBDEFB)
val AccentOrange = Color(0xFFFF9800)
val NeutralGray = Color(0xFFF5F5F5)
val LightGray = Color(0xFFE0E0E0)
val MediumGray = Color(0xFF9E9E9E)
val DarkText = Color(0xFF212121)
val SecondaryText = Color(0xFF757575)
val White = Color.White
val ErrorRed = Color(0xFFD32F2F)
val SuccessGreen = Color(0xFF388E3C)
val HighlightRed = Color(0xFFE53935)
val CardBackground = Color.White
val ScreenBackground = Color(0xFFFAFAFA)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = White,
    primaryContainer = PrimaryBlueLight,
    secondary = AccentOrange,
    onSecondary = White,
    surface = White,
    onSurface = DarkText,
    background = ScreenBackground,
    onBackground = DarkText,
    error = ErrorRed,
    onError = White,
    outline = LightGray,
    surfaceVariant = NeutralGray,
)

val CardShape = RoundedCornerShape(16.dp)
val ButtonShape = RoundedCornerShape(12.dp)

@Composable
fun VisionTwinTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(
            headlineLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DarkText),
            headlineMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkText),
            titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = DarkText),
            titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkText),
            bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, color = DarkText),
            bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = SecondaryText),
            labelLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = White),
        ),
        shapes = Shapes(
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(16.dp),
        ),
        content = content
    )
}
