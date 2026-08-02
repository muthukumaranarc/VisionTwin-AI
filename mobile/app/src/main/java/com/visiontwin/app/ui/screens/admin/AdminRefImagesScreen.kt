package com.visiontwin.app.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.visiontwin.app.data.api.RetrofitClient
import com.visiontwin.app.data.model.ReferenceImageDto
import com.visiontwin.app.data.repository.VisionTwinRepository
import com.visiontwin.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRefImagesScreen(
    machineId: String,
    machineName: String,
    repository: VisionTwinRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var refImages by remember { mutableStateOf<List<ReferenceImageDto>>(emptyList()) }
    var isLoadingList by remember { mutableStateOf(true) }

    // Form states
    var partName by remember { mutableStateOf("") }
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    var circleX by remember { mutableFloatStateOf(0.5f) }
    var circleY by remember { mutableFloatStateOf(0.5f) }
    var circleRadius by remember { mutableFloatStateOf(0.15f) }
    var isUploading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var messageIsError by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) pickedImageUri = uri
    }

    fun loadRefImages() {
        scope.launch {
            isLoadingList = true
            repository.getReferenceImages(machineId)
                .onSuccess { refImages = it }
            isLoadingList = false
        }
    }

    LaunchedEffect(machineId) {
        loadRefImages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Target Calibration", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Machine Info
            Text(
                text = machineName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            Text(
                text = "Reference targets map physical parts to image coordinates for spatial detection.",
                fontSize = 13.sp,
                color = SecondaryText,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // Form: Add New Target
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add New Target Zone", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkText)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = partName,
                        onValueChange = { partName = it },
                        label = { Text("Part Name (e.g., Tension Pulley)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isUploading
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = ButtonShape,
                        enabled = !isUploading
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (pickedImageUri != null) "Image Selected ✓" else "Select Target Image")
                    }

                    // Interactive Painter Canvas
                    if (pickedImageUri != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Tap on the image to set target center. Adjust target radius below.",
                            fontSize = 12.sp,
                            color = SecondaryText
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(CardShape)
                                .background(Color.Black)
                                .border(1.dp, LightGray, CardShape),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = pickedImageUri,
                                contentDescription = "Calibration preview",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTapGestures { offset ->
                                            circleX = offset.x / size.width
                                            circleY = offset.y / size.height
                                        }
                                    },
                                contentScale = ContentScale.Fit
                            )

                            // Interactive circle overlay
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val cx = circleX * size.width
                                val cy = circleY * size.height
                                val rad = circleRadius * size.width

                                drawCircle(
                                    color = PrimaryBlue.copy(alpha = 0.25f),
                                    radius = rad,
                                    center = Offset(cx, cy)
                                )
                                drawCircle(
                                    color = PrimaryBlue,
                                    radius = rad,
                                    center = Offset(cx, cy),
                                    style = Stroke(width = 3f)
                                )
                                // Crosshair center
                                drawLine(
                                    color = PrimaryBlue,
                                    start = Offset(cx - 15f, cy),
                                    end = Offset(cx + 15f, cy),
                                    strokeWidth = 3f
                                )
                                drawLine(
                                    color = PrimaryBlue,
                                    start = Offset(cx, cy - 15f),
                                    end = Offset(cx, cy + 15f),
                                    strokeWidth = 3f
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Radius Slider
                        Text("Target Zone Radius: ${String.format("%.2f", circleRadius)}", fontSize = 13.sp)
                        Slider(
                            value = circleRadius,
                            onValueChange = { circleRadius = it },
                            valueRange = 0.02f..0.35f,
                            colors = SliderDefaults.colors(
                                thumbColor = PrimaryBlue,
                                activeTrackColor = PrimaryBlue
                            ),
                            enabled = !isUploading
                        )
                    }

                    if (message != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(message!!, color = if (messageIsError) ErrorRed else SuccessGreen, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (partName.isBlank()) {
                                message = "Part name required"
                                messageIsError = true
                                return@Button
                            }
                            if (pickedImageUri == null) {
                                message = "Target image required"
                                messageIsError = true
                                return@Button
                            }
                            message = null
                            isUploading = true
                            scope.launch {
                                repository.addReferenceImage(
                                    context = context,
                                    machineId = machineId,
                                    partName = partName,
                                    circleX = circleX,
                                    circleY = circleY,
                                    circleRadius = circleRadius,
                                    imageUri = pickedImageUri!!
                                ).onSuccess {
                                    message = "Reference target uploaded!"
                                    messageIsError = false
                                    partName = ""
                                    pickedImageUri = null
                                    loadRefImages()
                                }.onFailure {
                                    message = it.message ?: "Upload failed"
                                    messageIsError = true
                                }
                                isUploading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = ButtonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        enabled = !isUploading
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(color = White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Upload Target Reference")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Library List
            Text("Reference Library", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkText)
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoadingList) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (refImages.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = NeutralGray)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No reference targets configured yet.", color = SecondaryText, fontSize = 13.sp)
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        refImages.forEach { target ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = RetrofitClient.fileUrl(target.filePath),
                                    contentDescription = target.partName,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(target.partName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(
                                        "Center: (${String.format("%.2f", target.circleX ?: 0f)}, ${String.format("%.2f", target.circleY ?: 0f)}) r=${String.format("%.2f", target.circleRadius ?: 0f)}",
                                        fontSize = 11.sp,
                                        color = SecondaryText
                                    )
                                }
                            }
                            if (target != refImages.last()) {
                                Divider(color = LightGray.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}
