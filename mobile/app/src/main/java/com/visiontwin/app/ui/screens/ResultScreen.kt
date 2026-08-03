package com.visiontwin.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.visiontwin.app.data.api.RetrofitClient
import com.visiontwin.app.data.model.DiagnosisReportDto
import com.visiontwin.app.data.repository.VisionTwinRepository
import com.visiontwin.app.ui.components.*
import com.visiontwin.app.ui.theme.*

@Composable
fun ResultScreen(
    reportId: String,
    repository: VisionTwinRepository,
    onBack: () -> Unit,
    onChat: (String) -> Unit
) {
    var report by remember { mutableStateOf<DiagnosisReportDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(reportId) {
        repository.getReportDetail(reportId).onSuccess { report = it }
        isLoading = false
    }

    // Pulse animation for the highlight circle
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Scaffold(
        topBar = { VTTopBar(title = "Diagnosis Result", onBack = onBack) },
        containerColor = VTBackground
    ) { padding ->
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VTPrimary)
                }
            }
            report == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    VTEmptyState(text = "Report not found", subtitle = "This report may have been removed.")
                }
            }
            else -> {
                val r = report!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Hero image with pulsing highlight
                    var imageSize by remember { mutableStateOf(IntSize.Zero) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = RetrofitClient.fileUrl(r.uploadedImagePath),
                            contentDescription = "Machine image",
                            modifier = Modifier
                                .fillMaxSize()
                                .onSizeChanged { imageSize = it },
                            contentScale = ContentScale.Crop
                        )

                        if (r.highlightX != null && r.highlightY != null && r.highlightRadius != null) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val cx = r.highlightX * size.width
                                val cy = r.highlightY * size.height
                                val rad = r.highlightRadius * size.width * pulseScale
                                drawCircle(
                                    color = VTError.copy(alpha = 0.2f),
                                    radius = rad,
                                    center = Offset(cx, cy)
                                )
                                drawCircle(
                                    color = VTError.copy(alpha = 0.75f),
                                    radius = rad,
                                    center = Offset(cx, cy),
                                    style = Stroke(width = 4f)
                                )
                            }
                        }

                        // Machine tag
                        Surface(
                            color = VTOnSurface.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(topStart = 8.dp),
                            modifier = Modifier.align(Alignment.BottomStart)
                        ) {
                            Text(
                                r.machineName.uppercase(),
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                letterSpacing = 0.6.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Detected Issue card (design: ring + diagnosis)
                    VTCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                VTProgressRing(progress = 0.85f, size = 56.dp, label = "85%")
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.WarningAmber,
                                            contentDescription = null,
                                            tint = VTError,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "DETECTED ISSUE",
                                            fontSize = 11.sp,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            letterSpacing = 0.6.sp,
                                            color = VTError
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        r.diagnosisProblem,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = VTOnSurface,
                                        lineHeight = 21.sp
                                    )
                                }
                            }

                            if (r.problemDescription.isNotBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = VTCardBorder)
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    "USER DESCRIPTION",
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    letterSpacing = 0.6.sp,
                                    color = VTOnSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(r.problemDescription, fontSize = 14.sp, color = VTOnSurfaceVariant, lineHeight = 20.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action plan checklist (design: solution steps as checklist)
                    VTCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "ACTION PLAN",
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                letterSpacing = 0.6.sp,
                                color = VTOnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val steps = r.diagnosisSolution
                                .split("\n")
                                .map { it.trim().removePrefix("-").removePrefix("*").trim() }
                                .filter { it.isNotBlank() }

                            steps.forEachIndexed { index, step ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .background(VTSuccess.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = VTSuccess,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        step,
                                        fontSize = 14.sp,
                                        color = VTOnSurface,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Ask AI Assistant
                    VTButton(
                        text = "Ask AI Assistant",
                        onClick = { onChat(reportId) },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Filled.Chat
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
