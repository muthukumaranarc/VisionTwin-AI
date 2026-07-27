package com.visiontwin.app.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.visiontwin.app.data.repository.VisionTwinRepository
import com.visiontwin.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
        topBar = {
            TopAppBar(
                title = { Text("Add Machine", fontWeight = FontWeight.Bold) },
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
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Machine Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp),
                enabled = savedMachineId == null
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = manufacturer, onValueChange = { manufacturer = it },
                label = { Text("Manufacturer") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp),
                enabled = savedMachineId == null
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = model, onValueChange = { model = it },
                label = { Text("Model") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp),
                enabled = savedMachineId == null
            )
            Spacer(modifier = Modifier.height(16.dp))

            // File pickers
            OutlinedButton(
                onClick = { thumbnailPicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = ButtonShape,
                enabled = savedMachineId == null
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (thumbnailUri != null) "Thumbnail Selected ✓" else "Select Thumbnail Image")
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { manualPicker.launch("application/pdf") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = ButtonShape,
                enabled = savedMachineId == null
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (manualUri != null) "Manual PDF Selected ✓" else "Select Manual PDF")
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { guidePicker.launch("application/pdf") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = ButtonShape,
                enabled = savedMachineId == null
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (userGuideUri != null) "User Guide Selected ✓" else "Select User Guide PDF")
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (message != null) {
                Text(
                    message!!,
                    color = if (messageIsError) ErrorRed else SuccessGreen,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (savedMachineId == null) {
                // Save button
                Button(
                    onClick = {
                        if (name.isBlank()) { message = "Machine name required"; messageIsError = true; return@Button }
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
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save Machine", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Generate Knowledge button
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                Text("Knowledge Base", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkText)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Generate AI knowledge base from uploaded PDFs.", fontSize = 14.sp, color = SecondaryText)
                Spacer(modifier = Modifier.height(12.dp))

                Button(
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
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    enabled = !isGenerating
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generating...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Generate Knowledge Base", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
