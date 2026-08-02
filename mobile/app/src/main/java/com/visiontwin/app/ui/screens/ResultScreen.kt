package com.visiontwin.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.visiontwin.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    reportId: String,
    repository: VisionTwinRepository,
    onBack: () -> Unit,
    onChat: (String) -> Unit
) {
    var report by remember { mutableStateOf<DiagnosisReportDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(reportId) {
        repository.getReportDetail(reportId)
            .onSuccess { report = it }
            .onFailure { /* use cached */ report = repository.run { null } }
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
        topBar = {
            TopAppBar(
                title = { Text("Diagnosis Result", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue, titleContentColor = White,
                    navigationIconContentColor = White
                )
            )
        },
        containerColor = ScreenBackground
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (report == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Report not found", color = ErrorRed)
            }
        } else {
            val r = report!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Image with highlight overlay
                var imageSize by remember { mutableStateOf(IntSize.Zero) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(CardShape)
                ) {
                    AsyncImage(
                        model = RetrofitClient.fileUrl(r.uploadedImagePath),
                        contentDescription = "Machine image",
                        modifier = Modifier
                            .fillMaxSize()
                            .onSizeChanged { imageSize = it },
                        contentScale = ContentScale.Crop
                    )

                    // Draw animated red highlight circle
                    if (r.highlightX != null && r.highlightY != null && r.highlightRadius != null) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = r.highlightX * size.width
                            val cy = r.highlightY * size.height
                            val rad = r.highlightRadius * size.width * pulseScale

                            // Semi-transparent fill
                            drawCircle(
                                color = Color.Red.copy(alpha = 0.2f),
                                radius = rad,
                                center = Offset(cx, cy)
                            )
                            // Solid stroke
                            drawCircle(
                                color = Color.Red.copy(alpha = 0.75f),
                                radius = rad,
                                center = Offset(cx, cy),
                                style = Stroke(width = 4f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Problem Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Problem", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ErrorRed)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(r.diagnosisProblem, fontSize = 16.sp, color = DarkText, lineHeight = 22.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Solution Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("How to Solve", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SuccessGreen)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(r.diagnosisSolution, fontSize = 15.sp, color = DarkText, lineHeight = 22.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Chat button
                Button(
                    onClick = { onChat(reportId) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ask AI Assistant", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
