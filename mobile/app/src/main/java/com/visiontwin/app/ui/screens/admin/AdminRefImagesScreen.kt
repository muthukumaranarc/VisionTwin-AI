package com.visiontwin.app.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.visiontwin.app.ui.components.*
import com.visiontwin.app.ui.theme.*
import kotlinx.coroutines.launch

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

    // Add form states
    var partName by remember { mutableStateOf("") }
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    var circleX by remember { mutableFloatStateOf(0.5f) }
    var circleY by remember { mutableFloatStateOf(0.5f) }
    var circleRadius by remember { mutableFloatStateOf(0.15f) }
    var isUploading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var messageIsError by remember { mutableStateOf(false) }

    // Edit / delete states
    var targetToEdit by remember { mutableStateOf<ReferenceImageDto?>(null) }
    var editPartName by remember { mutableStateOf("") }
    var editImageUri by remember { mutableStateOf<Uri?>(null) }
    var isSavingEdit by remember { mutableStateOf(false) }
    var targetToDelete by remember { mutableStateOf<ReferenceImageDto?>(null) }
    var isDeleting by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) pickedImageUri = uri
    }
    val editImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) editImageUri = uri
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
        topBar = { VTTopBar(title = "Target Calibration", onBack = onBack) },
        containerColor = VTBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = machineName,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = VTOnSurface
            )
            Text(
                text = "Reference targets map physical parts to image coordinates for spatial detection.",
                fontSize = 13.sp,
                color = VTOnSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // ─── Add New Target form ─────────────────────────────────────────
            VTCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add New Target Zone", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = VTOnSurface)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = partName,
                        onValueChange = { partName = it },
                        label = { Text("Part Name (e.g., Tension Pulley)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = InputShape,
                        enabled = !isUploading
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = InputShape,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VTOnSurfaceVariant),
                        border = BorderStroke(1.dp, VTOutlineVariant),
                        enabled = !isUploading
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (pickedImageUri != null) "Image Selected ✓" else "Select Target Image")
                    }

                    // Interactive calibration canvas
                    if (pickedImageUri != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Tap on the image to set target center. Adjust target radius below.",
                            fontSize = 12.sp,
                            color = VTOnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                                .border(1.dp, VTOutlineVariant, RoundedCornerShape(8.dp)),
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

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val cx = circleX * size.width
                                val cy = circleY * size.height
                                val rad = circleRadius * size.width

                                drawCircle(
                                    color = VTPrimary.copy(alpha = 0.25f),
                                    radius = rad,
                                    center = Offset(cx, cy)
                                )
                                drawCircle(
                                    color = VTPrimary,
                                    radius = rad,
                                    center = Offset(cx, cy),
                                    style = Stroke(width = 3f)
                                )
                                drawLine(
                                    color = VTPrimary,
                                    start = Offset(cx - 15f, cy),
                                    end = Offset(cx + 15f, cy),
                                    strokeWidth = 3f
                                )
                                drawLine(
                                    color = VTPrimary,
                                    start = Offset(cx, cy - 15f),
                                    end = Offset(cx, cy + 15f),
                                    strokeWidth = 3f
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Target Zone Radius: ${String.format("%.2f", circleRadius)}", fontSize = 13.sp, color = VTOnSurfaceVariant)
                        Slider(
                            value = circleRadius,
                            onValueChange = { circleRadius = it },
                            valueRange = 0.02f..0.35f,
                            colors = SliderDefaults.colors(
                                thumbColor = VTPrimary,
                                activeTrackColor = VTPrimary
                            ),
                            enabled = !isUploading
                        )
                    }

                    if (message != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(message!!, color = if (messageIsError) VTError else VTSuccess, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    VTButton(
                        text = "Upload Target Reference",
                        onClick = {
                            if (partName.isBlank()) {
                                message = "Part name required"
                                messageIsError = true
                                return@VTButton
                            }
                            if (pickedImageUri == null) {
                                message = "Target image required"
                                messageIsError = true
                                return@VTButton
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
                        enabled = !isUploading,
                        loading = isUploading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ─── Reference Library ───────────────────────────────────────────
            VTSectionTitle(text = "Reference Library")

            if (isLoadingList) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VTPrimary)
                }
            } else if (refImages.isEmpty()) {
                VTCard {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No reference targets configured yet.", color = VTOnSurfaceVariant, fontSize = 13.sp)
                    }
                }
            } else {
                VTCard {
                    Column(modifier = Modifier.padding(12.dp)) {
                        refImages.forEachIndexed { index, target ->
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
                                    Text(target.partName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = VTOnSurface)
                                    Text(
                                        "Center: (${String.format("%.2f", target.circleX ?: 0f)}, ${String.format("%.2f", target.circleY ?: 0f)}) r=${String.format("%.2f", target.circleRadius ?: 0f)}",
                                        fontSize = 11.sp,
                                        color = VTOnSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        targetToEdit = target
                                        editPartName = target.partName
                                        editImageUri = null
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = "Edit",
                                        tint = VTPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { targetToDelete = target },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete",
                                        tint = VTError,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            if (index != refImages.lastIndex) {
                                HorizontalDivider(color = VTCardBorder)
                            }
                        }
                    }
                }
            }
        }
    }

    // ─── Edit dialog ─────────────────────────────────────────────────────────
    targetToEdit?.let { target ->
        AlertDialog(
            onDismissRequest = { if (!isSavingEdit) targetToEdit = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            title = { Text("Edit Target", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = VTOnSurface) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editPartName,
                        onValueChange = { editPartName = it },
                        label = { Text("Part Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = InputShape,
                        enabled = !isSavingEdit
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { editImagePicker.launch("image/*") },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = InputShape,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VTOnSurfaceVariant),
                        border = BorderStroke(1.dp, VTOutlineVariant),
                        enabled = !isSavingEdit
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (editImageUri != null) "New Image Selected ✓" else "Replace Image (optional)")
                    }
                    if (editImageUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AsyncImage(
                            model = editImageUri,
                            contentDescription = "New image preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editPartName.isBlank()) return@TextButton
                        isSavingEdit = true
                        scope.launch {
                            repository.updateReferenceImage(
                                context = context,
                                refImageId = target.id,
                                partName = editPartName,
                                circleX = target.circleX ?: 0.5f,
                                circleY = target.circleY ?: 0.5f,
                                circleRadius = target.circleRadius ?: 0.15f,
                                imageUri = editImageUri
                            ).onSuccess {
                                targetToEdit = null
                                message = "Reference target updated!"
                                messageIsError = false
                                loadRefImages()
                            }.onFailure {
                                message = it.message ?: "Update failed"
                                messageIsError = true
                            }
                            isSavingEdit = false
                        }
                    },
                    enabled = !isSavingEdit
                ) {
                    Text(
                        if (isSavingEdit) "Saving..." else "Save",
                        color = VTPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isSavingEdit) targetToEdit = null }) {
                    Text("Cancel", color = VTOnSurfaceVariant)
                }
            }
        )
    }

    // ─── Delete confirm dialog ───────────────────────────────────────────────
    targetToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { if (!isDeleting) targetToDelete = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            title = { Text("Delete Target", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = VTOnSurface) },
            text = {
                Text(
                    "Remove \"${target.partName}\" from the reference library?",
                    color = VTOnSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleting = true
                        scope.launch {
                            repository.deleteReferenceImage(target.id)
                                .onSuccess {
                                    targetToDelete = null
                                    message = "Reference target deleted."
                                    messageIsError = false
                                    loadRefImages()
                                }
                                .onFailure {
                                    message = it.message ?: "Delete failed"
                                    messageIsError = true
                                }
                            isDeleting = false
                        }
                    },
                    enabled = !isDeleting
                ) {
                    Text(if (isDeleting) "Deleting..." else "Delete", color = VTError, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isDeleting) targetToDelete = null }) {
                    Text("Cancel", color = VTOnSurfaceVariant)
                }
            }
        )
    }
}
