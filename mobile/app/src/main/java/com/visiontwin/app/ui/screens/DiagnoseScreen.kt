package com.visiontwin.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.visiontwin.app.data.api.RetrofitClient
import com.visiontwin.app.data.model.MachineDto
import com.visiontwin.app.data.repository.VisionTwinRepository
import com.visiontwin.app.ui.components.*
import com.visiontwin.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

// ─── AI Diagnosis tab (main) ──────────────────────────────────────────────────

@Composable
fun DiagnoseScreen(
    repository: VisionTwinRepository,
    onTabSelected: (VTTab) -> Unit,
    onResult: (String) -> Unit
) {
    Scaffold(
        topBar = { VTTopBar(title = "VisionTwin AI") },
        containerColor = VTBackground,
        bottomBar = { VTBottomNav(selected = VTTab.Diagnose, onSelect = onTabSelected) }
    ) { padding ->
        DiagnoseForm(
            repository = repository,
            modifier = Modifier.padding(padding),
            preselectedMachineId = null,
            onResult = onResult
        )
    }
}

// ─── Diagnosis form shared with the machine-specific upload flow ──────────────

@Composable
fun DiagnoseForm(
    repository: VisionTwinRepository,
    modifier: Modifier = Modifier,
    preselectedMachineId: String?,
    onResult: (String) -> Unit
) {
    val context = LocalContext.current
    var machines by remember { mutableStateOf<List<MachineDto>>(emptyList()) }
    var machineId by remember { mutableStateOf(preselectedMachineId ?: "") }
    var models by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedModel by remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var problemDescription by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Camera / gallery
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) imageUri = cameraUri
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
            cameraUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraLauncher.launch(cameraUri!!)
        } else {
            android.widget.Toast.makeText(
                context,
                "Camera permission is required to take photos",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) imageUri = uri
    }

    // Machine + model dropdowns
    var machineMenuExpanded by remember { mutableStateOf(false) }
    var modelMenuExpanded by remember { mutableStateOf(false) }

    val loadingMessages = listOf(
        "Analyzing machine...",
        "Understanding uploaded image...",
        "Searching optimized knowledge...",
        "Searching manuals...",
        "Preparing solution..."
    )
    var currentMsgIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(RetrofitClient.urlVersion.value) {
        repository.getMachines().onSuccess {
            machines = it
            if (machineId.isBlank() && it.isNotEmpty()) machineId = it.first().id
        }
        repository.getDiagnosisModels().onSuccess { res ->
            if (res.models.isNotEmpty()) {
                models = res.models
                selectedModel = res.default.ifBlank { res.models.first() }
            } else {
                models = listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash")
                selectedModel = "gemini-1.5-flash"
            }
        }.onFailure {
            models = listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash")
            selectedModel = "gemini-1.5-flash"
        }
    }

    LaunchedEffect(isAnalyzing) {
        if (isAnalyzing) {
            currentMsgIndex = 0
            while (isAnalyzing) {
                delay(2000)
                currentMsgIndex = (currentMsgIndex + 1) % loadingMessages.size
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Capture area (design: 4:3 dashed drop zone)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, VTOutlineVariant, RoundedCornerShape(8.dp))
                .background(VTSurfaceLow)
                .let { base ->
                    if (imageUri == null) {
                        base.clickableNoRipple { galleryLauncher.launch("image/*") }
                    } else base
                },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Selected image",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TextButton(onClick = { galleryLauncher.launch("image/*") }, contentPadding = PaddingValues(0.dp)) {
                            Text("Replace", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        TextButton(onClick = { imageUri = null }, contentPadding = PaddingValues(0.dp)) {
                            Text("Remove", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = VTPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Capture or Drop Media",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VTOnSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Point camera at machine component for real-time AI diagnostic analysis.",
                        fontSize = 13.sp,
                        color = VTOnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Camera / Gallery actions
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = {
                    val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.CAMERA
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                    if (hasCameraPermission) {
                        val file = File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
                        cameraUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        cameraLauncher.launch(cameraUri!!)
                    } else {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VTOnSurfaceVariant),
                border = BorderStroke(1.dp, VTOutlineVariant)
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Camera", fontSize = 13.sp, color = VTOnSurface)
            }
            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VTOnSurfaceVariant),
                border = BorderStroke(1.dp, VTOutlineVariant)
            ) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Gallery", fontSize = 13.sp, color = VTOnSurface)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Machine selector
        DropdownField(
            label = "Machine",
            value = machines.find { it.id == machineId }?.name ?: "Select a machine...",
            expanded = machineMenuExpanded,
            onExpand = { machineMenuExpanded = true; modelMenuExpanded = false },
            onDismiss = { machineMenuExpanded = false }
        ) {
            machines.forEach { m ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = "${m.name} (${m.manufacturer} ${m.model})", 
                            color = VTOnSurface,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    onClick = {
                        machineId = m.id
                        machineMenuExpanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = VTOnSurface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // AI model selector (Gemini models from backend)
        DropdownField(
            label = "AI Model",
            value = selectedModel.ifBlank { "Default model" },
            expanded = modelMenuExpanded,
            onExpand = { modelMenuExpanded = true; machineMenuExpanded = false },
            onDismiss = { modelMenuExpanded = false }
        ) {
            models.forEach { m ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = m, 
                            color = VTOnSurface,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    onClick = {
                        selectedModel = m
                        modelMenuExpanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = VTOnSurface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Problem description
        OutlinedTextField(
            value = problemDescription,
            onValueChange = { problemDescription = it },
            label = { Text("Describe the problem", color = VTOnSurfaceVariant) },
            placeholder = { Text("e.g. The shaft seems stuck and is making a grinding noise", color = VTOutline) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(color = VTOnSurface),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = VTOnSurface,
                unfocusedTextColor = VTOnSurface,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = VTPrimary,
                unfocusedBorderColor = VTOutlineVariant
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (errorMsg != null) {
            Text(errorMsg!!, color = VTError, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Analyze button
        VTButton(
            text = "Start AI Diagnosis",
            onClick = {
                if (machineId.isBlank()) { errorMsg = "Please select a machine"; return@VTButton }
                if (imageUri == null) { errorMsg = "Please select an image first"; return@VTButton }
                if (problemDescription.isBlank()) { errorMsg = "Please describe the problem"; return@VTButton }
                errorMsg = null
                isAnalyzing = true
                scope.launch {
                    repository.diagnose(context, machineId, problemDescription, imageUri!!, selectedModel)
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
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Filled.Psychology
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Full-screen loading overlay
    AnimatedVisibility(visible = isAnalyzing, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VTBackground.copy(alpha = 0.94f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    color = VTPrimary,
                    modifier = Modifier.size(56.dp),
                    strokeWidth = 5.dp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = loadingMessages[currentMsgIndex],
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = VTOnSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
}

// ─── Machine-specific upload flow (from machine detail) ───────────────────────

@Composable
fun UploadDiagnoseScreen(
    machineId: String,
    machineName: String,
    repository: VisionTwinRepository,
    onBack: () -> Unit,
    onResult: (String) -> Unit
) {
    Scaffold(
        topBar = {
            VTTopBar(
                title = machineName,
                onBack = onBack
            )
        },
        containerColor = VTBackground
    ) { padding ->
        DiagnoseForm(
            repository = repository,
            modifier = Modifier.padding(padding),
            preselectedMachineId = machineId,
            onResult = onResult
        )
    }
}

// ─── Dropdown field helper ────────────────────────────────────────────────────

@Composable
private fun DropdownField(
    label: String,
    value: String,
    expanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = VTOnSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val width = maxWidth
            OutlinedButton(
                onClick = onExpand,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VTOnSurface),
                border = BorderStroke(1.dp, VTOutlineVariant)
            ) {
                Text(
                    value,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start,
                    color = VTOnSurface
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = VTOnSurfaceVariant)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismiss,
                modifier = Modifier.width(width),
                containerColor = Color.White
            ) {
                content()
            }
        }
    }
}
