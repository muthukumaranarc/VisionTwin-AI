package com.visiontwin.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visiontwin.app.ui.theme.PrimaryBlue
import com.visiontwin.app.ui.theme.PrimaryBlueDark
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToMachines: () -> Unit) {
    val fadeAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.5f) }

    LaunchedEffect(Unit) {
        fadeAnim.animateTo(1f, animationSpec = tween(1000, easing = EaseOutCubic))
    }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(1f, animationSpec = tween(1200, easing = EaseOutBack))
    }
    LaunchedEffect(Unit) {
        delay(2500)
        onNavigateToMachines()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(PrimaryBlue, PrimaryBlueDark),
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(fadeAnim.value)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Logo",
                tint = Color.White,
                modifier = Modifier
                    .size(80.dp)
                    .scale(scaleAnim.value)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "VisionTwin AI",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Smart Manufacturing Assistant",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}
