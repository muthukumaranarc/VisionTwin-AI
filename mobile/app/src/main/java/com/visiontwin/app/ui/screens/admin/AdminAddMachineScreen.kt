package com.visiontwin.app.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visiontwin.app.data.repository.VisionTwinRepository
import com.visiontwin.app.ui.components.*
import com.visiontwin.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminAddMachineScreen(
    repository: VisionTwinRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var thumbnailUri by remember { mutableStateOf<Uri?>(null) }
    var manualUri by remember { mutableStateOf<Uri?>(null) }
    var userGuideUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var savedMachineId by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var messageIsError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val thumbnailPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) thumbnailUri = uri
    }
    val manualPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) manualUri = uri
    }
    val guidePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) userGuideUri = uri
    }

    Scaffold(
        topBar = { VTTopBar(title = "Add Machine", onBack = onBack) },
        containerColor = VTBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Machine Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = InputShape,
                enabled = savedMachineId == null
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = manufacturer, onValueChange = { manufacturer = it },
                label = { Text("Manufacturer") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = InputShape,
                enabled = savedMachineId == null
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = model, onValueChange = { model = it },
                label = { Text("Model") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = InputShape,
                enabled = savedMachineId == null
            )
            Spacer(modifier = Modifier.height(16.dp))

            // File pickers
            FilePickerButton(
                text = if (thumbnailUri != null) "Thumbnail Selected ✓" else "Select Thumbnail Image",
                icon = Icons.Filled.Image,
                onClick = { thumbnailPicker.launch("image/*") },
                enabled = savedMachineId == null
            )
            Spacer(modifier = Modifier.height(8.dp))

            FilePickerButton(
                text = if (manualUri != null) "Manual Selected ✓" else "Select Manual (.md)",
                icon = Icons.Filled.PictureAsPdf,
                onClick = { manualPicker.launch("text/markdown") },
                enabled = savedMachineId == null
            )
            Spacer(modifier = Modifier.height(8.dp))

            FilePickerButton(
                text = if (userGuideUri != null) "User Guide Selected ✓" else "Select User Guide (.md)",
                icon = Icons.Filled.PictureAsPdf,
                onClick = { guidePicker.launch("text/markdown") },
                enabled = savedMachineId == null
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (message != null) {
                Text(
                    message!!,
                    color = if (messageIsError) VTError else VTSuccess,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (savedMachineId == null) {
                VTButton(
                    text = "Save Machine",
                    onClick = {
                        if (name.isBlank()) { message = "Machine name required"; messageIsError = true; return@VTButton }
                        message = null
                        isSaving = true
                        scope.launch {
                            repository.createMachine(context, name, manufacturer, model, thumbnailUri, manualUri, userGuideUri)
                                .onSuccess {
                                    savedMachineId = it.id
                                    message = "Machine saved successfully!"
                                    messageIsError = false
                                }
                                .onFailure {
                                    message = it.message ?: "Save failed"
                                    messageIsError = true
                                }
                            isSaving = false
                        }
                    },
                    enabled = !isSaving,
                    loading = isSaving,
                    icon = Icons.Filled.Build,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Generate Knowledge
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                VTCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Knowledge Base", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = VTOnSurface)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Generate AI knowledge base from uploaded Markdown files so the diagnostic engine can search machine manuals.",
                            fontSize = 14.sp,
                            color = VTOnSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        VTButton(
                            text = "Generate Knowledge Base",
                            onClick = {
                                isGenerating = true
                                message = null
                                scope.launch {
                                    repository.generateKnowledge(savedMachineId!!)
                                        .onSuccess {
                                            message = it.message ?: "Knowledge base generated!"
                                            messageIsError = false
                                        }
                                        .onFailure {
                                            message = it.message ?: "Generation failed"
                                            messageIsError = true
                                        }
                                    isGenerating = false
                                }
                            },
                            enabled = !isGenerating,
                            loading = isGenerating,
                            containerColor = VTPrimaryContainer,
                            contentColor = VTOnPrimaryContainer,
                            icon = Icons.Filled.Psychology,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilePickerButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = InputShape,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = VTOnSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, VTOutlineVariant),
        enabled = enabled
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 14.sp)
    }
}
