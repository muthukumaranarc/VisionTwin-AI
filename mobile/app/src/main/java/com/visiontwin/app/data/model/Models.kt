package com.visiontwin.app.data.model

data class MachineDto(
    val id: String = "",
    val name: String = "",
    val manufacturer: String = "",
    val model: String = "",
    val thumbnailPath: String? = null,
    val manualPdfPath: String? = null,
    val userGuidePdfPath: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class ReferenceImageDto(
    val id: String = "",
    val filename: String = "",
    val partName: String = "",
    val circleX: Float? = null,
    val circleY: Float? = null,
    val circleRadius: Float? = null,
    val filePath: String = ""
)

data class DiagnosisReportDto(
    val id: String = "",
    val machineId: String = "",
    val machineName: String = "",
    val problemDescription: String = "",
    val uploadedImagePath: String = "",
    val diagnosisProblem: String = "",
    val diagnosisSolution: String = "",
    val highlightX: Float? = null,
    val highlightY: Float? = null,
    val highlightRadius: Float? = null,
    val timestamp: String? = null
)

data class ChatMessageDto(
    val id: String = "",
    val sender: String = "",
    val messageText: String = "",
    val timestamp: String? = null
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean = false,
    val token: String? = null,
    val message: String? = null
)

data class ChatRequest(
    val message: String
)

data class DashboardStats(
    val totalMachines: Int = 0,
    val totalReports: Int = 0,
    val totalLayer1Datastores: Int = 0,
    val totalLayer2Vectors: Int = 0
)

data class KnowledgeResponse(
    val success: Boolean = false,
    val message: String? = null
)

data class HealthResponse(
    val status: String = "",
    val service: String = "",
    val timestamp: Long = 0L
)
