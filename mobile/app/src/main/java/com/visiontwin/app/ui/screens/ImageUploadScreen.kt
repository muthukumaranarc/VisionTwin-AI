package com.visiontwin.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.visiontwin.app.data.repository.VisionTwinRepository
import com.visiontwin.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageUploadScreen(
    machineId: String,
    machineName: String,
    repository: VisionTwinRepository,
    onBack: () -> Unit,
    onResult: (String) -> Unit
) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var problemDescription by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Camera URI
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) imageUri = cameraUri
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) imageUri = uri
    }

    // Loading messages rotation
    val loadingMessages = listOf(
        "Analyzing machine...",
        "Understanding uploaded image...",
        "Searching optimized knowledge...",
        "Searching manuals...",
        "Preparing solution..."
    )
    var currentMsgIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(isAnalyzing) {
        if (isAnalyzing) {
            currentMsgIndex = 0
            while (isAnalyzing) {
                delay(2000)
                currentMsgIndex = (currentMsgIndex + 1) % loadingMessages.size
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(machineName, fontWeight = FontWeight.Bold) },
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Image Preview
                Card(
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = NeutralGray)
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Selected image",
                            modifier = Modifier.fillMaxSize().clip(CardShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MediumGray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Capture or select machine image", color = MediumGray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Camera & Gallery buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            val file = File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
                            cameraUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            cameraLauncher.launch(cameraUri!!)
                        },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = ButtonShape
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Camera")
                    }
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = ButtonShape
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gallery")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Problem description
                OutlinedTextField(
                    value = problemDescription,
                    onValueChange = { problemDescription = it },
                    label = { Text("Describe the problem") },
                    placeholder = { Text("e.g. The shaft seems stuck and is making a grinding noise") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Error message
                if (errorMsg != null) {
                    Text(errorMsg!!, color = ErrorRed, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Analyze button
                Button(
                    onClick = {
                        if (imageUri == null) { errorMsg = "Please select an image first"; return@Button }
                        if (problemDescription.isBlank()) { errorMsg = "Please describe the problem"; return@Button }
                        errorMsg = null
                        isAnalyzing = true
                        scope.launch {
                            repository.diagnose(context, machineId, problemDescription, imageUri!!)
                                .onSuccess { report ->
                                    isAnalyzing = false
                                    onResult(report.id)
                                }
                                .onFailure {
                                    isAnalyzing = false
                                    errorMsg = it.message ?: "Analysis failed. Please try again."
                                }
                        }
                    },
                    enabled = !isAnalyzing,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text("Analyze", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = White)
                }
            }

            // Full-screen loading overlay
            AnimatedVisibility(visible = isAnalyzing, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.92f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = PrimaryBlue,
                            modifier = Modifier.size(56.dp),
                            strokeWidth = 5.dp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = loadingMessages[currentMsgIndex],
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = DarkText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
