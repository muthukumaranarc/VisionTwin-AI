package com.visiontwin.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visiontwin.app.ui.theme.VTPrimary
import com.visiontwin.app.ui.theme.VTPrimaryContainer
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToDashboard: () -> Unit) {
    val fadeAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.6f) }

    LaunchedEffect(Unit) {
        fadeAnim.animateTo(1f, animationSpec = tween(800, easing = EaseOutCubic))
    }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(1f, animationSpec = tween(900, easing = EaseOutBack))
    }
    LaunchedEffect(Unit) {
        delay(2000)
        onNavigateToDashboard()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0xFFF2F3FF))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(fadeAnim.value)
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .scale(scaleAnim.value)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFC2C6D8), RoundedCornerShape(24.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = com.visiontwin.app.R.drawable.visiontwin_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "VisionTwin AI",
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.5).sp,
                color = VTPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "INTELLIGENCE IN YOUR POCKET",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                color = Color(0xFF424656),
                textAlign = TextAlign.Center
            )
        }
    }
}
