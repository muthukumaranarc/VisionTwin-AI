package com.visiontwin.app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.visiontwin.app.data.api.RetrofitClient
import com.visiontwin.app.data.model.MachineDto
import com.visiontwin.app.data.model.ReferenceImageDto
import com.visiontwin.app.data.repository.VisionTwinRepository
import com.visiontwin.app.ui.components.*
import com.visiontwin.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MachineDetailScreen(
    machineId: String,
    repository: VisionTwinRepository,
    onBack: () -> Unit,
    onDiagnose: (String, String) -> Unit,
    onManageRefImages: (String, String) -> Unit
) {
    val context = LocalContext.current
    var machine by remember { mutableStateOf<MachineDto?>(null) }
    var refImages by remember { mutableStateOf<List<ReferenceImageDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(machineId) {
        repository.getMachines()
            .onSuccess { list ->
                machine = list.find { it.id == machineId }
                if (machine != null) {
                    repository.getReferenceImages(machineId)
                        .onSuccess { refImages = it }
                        .onFailure { error = it.message }
                } else {
                    error = "Machine not found"
                }
            }
            .onFailure { error = it.message }
        isLoading = false
    }

    fun openPdf(path: String?) {
        val url = RetrofitClient.fileUrl(path)
        if (url.isBlank()) {
            error = "No document uploaded for this machine."
            return
        }
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            error = "No app available to open the document."
        }
    }

    Scaffold(
        topBar = {
            VTTopBar(
                title = machine?.name ?: "Machine",
                onBack = onBack
            )
        },
        containerColor = VTBackground
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VTPrimary)
            }
        } else if (machine == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(error ?: "Machine not found", color = VTError)
            }
        } else {
            val m = machine!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Hero image
                AsyncImage(
                    model = RetrofitClient.fileUrl(m.thumbnailPath),
                    contentDescription = m.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(m.name, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = VTOnSurface, modifier = Modifier.weight(1f))
                    VTLabelChip(text = "ONLINE")
                }
                Text(
                    "${m.manufacturer} · Model ${m.model}",
                    fontSize = 14.sp,
                    color = VTOnSurfaceVariant
                )
                if (m.updatedAt != null) {
                    Text(
                        "LAST SYNC ${m.updatedAt.take(10)}",
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        letterSpacing = 0.5.sp,
                        color = VTOutline,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Diagnose CTA
                VTButton(
                    text = "Diagnose this machine",
                    onClick = { onDiagnose(m.id, m.name) },
                    icon = Icons.Filled.Psychology,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Documents
                VTSectionTitle("Documents")
                VTCard {
                    Column(modifier = Modifier.padding(8.dp)) {
                        DocumentRow(
                            title = "Service Manual",
                            icon = Icons.Filled.MenuBook,
                            state = if (m.manualPdfPath != null) "MD" else "NOT UPLOADED",
                            stateColor = if (m.manualPdfPath != null) VTSuccess else VTOutline,
                            onClick = { openPdf(m.manualPdfPath) }
                        )
                        HorizontalDivider(color = VTCardBorder, modifier = Modifier.padding(horizontal = 8.dp))
                        DocumentRow(
                            title = "User Guide",
                            icon = Icons.Filled.Description,
                            state = if (m.userGuidePdfPath != null) "MD" else "NOT UPLOADED",
                            stateColor = if (m.userGuidePdfPath != null) VTSuccess else VTOutline,
                            onClick = { openPdf(m.userGuidePdfPath) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Reference images
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Reference Targets", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = VTOnSurface)
                    TextButton(onClick = { onManageRefImages(m.id, m.name) }) {
                        Text("Manage", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = VTPrimary)
                    }
                }

                if (refImages.isEmpty()) {
                    VTCard {
                        Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                            Text("No reference targets configured.", fontSize = 13.sp, color = VTOnSurfaceVariant)
                        }
                    }
                } else {
                    refImages.forEach { img ->
                        Spacer(modifier = Modifier.height(8.dp))
                        ReferenceImageCard(img)
                    }
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(error!!, fontSize = 13.sp, color = VTError)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DocumentRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    state: String,
    stateColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = VTPrimary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = VTOnSurface, modifier = Modifier.weight(1f))
        VTLabelChip(text = state, container = stateColor.copy(alpha = 0.12f), contentColor = stateColor)
    }
}

@Composable
private fun ReferenceImageCard(img: ReferenceImageDto) {
    VTCard {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = RetrofitClient.fileUrl(img.filePath),
                    contentDescription = img.partName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = (img.circleX ?: 0.5f) * size.width
                    val cy = (img.circleY ?: 0.5f) * size.height
                    val rad = (img.circleRadius ?: 0.15f) * size.width
                    drawCircle(color = VTPrimary.copy(alpha = 0.25f), radius = rad, center = Offset(cx, cy))
                    drawCircle(
                        color = VTPrimary,
                        radius = rad,
                        center = Offset(cx, cy),
                        style = Stroke(width = 3f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Build, contentDescription = null, tint = VTPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(img.partName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = VTOnSurface)
            }
        }
    }
}
