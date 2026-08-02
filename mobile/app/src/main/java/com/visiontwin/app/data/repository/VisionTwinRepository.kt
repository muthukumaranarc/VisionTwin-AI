package com.visiontwin.app.data.repository

import android.content.Context
import android.net.Uri
import com.visiontwin.app.data.api.ApiService
import com.visiontwin.app.data.cache.CacheManager
import com.visiontwin.app.data.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class VisionTwinRepository(
    private val api: ApiService,
    private val cache: CacheManager
) {

    suspend fun getMachines(): Result<List<MachineDto>> = runCatching {
        val response = api.getMachines()
        if (response.isSuccessful) {
            val machines = response.body() ?: emptyList()
            cache.saveMachines(machines)
            machines
        } else {
            cache.loadMachines().ifEmpty { throw Exception("Failed to load machines") }
        }
    }.recoverCatching { cache.loadMachines().ifEmpty { throw it } }

    suspend fun diagnose(
        context: Context, machineId: String, problemDescription: String, imageUri: Uri
    ): Result<DiagnosisReportDto> = runCatching {
        val imageFile = uriToFile(context, imageUri, "upload_image.jpg")
        val imagePart = MultipartBody.Part.createFormData(
            "image", imageFile.name,
            imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        )
        val machineIdBody = machineId.toRequestBody("text/plain".toMediaTypeOrNull())
        val descBody = problemDescription.toRequestBody("text/plain".toMediaTypeOrNull())

        val response = api.diagnose(machineIdBody, descBody, imagePart)
        if (response.isSuccessful) {
            val report = response.body() ?: throw Exception("Empty response from server")
            cache.saveLastReport(report)
            report
        } else {
            throw Exception("Diagnosis failed: ${response.code()}")
        }
    }

    suspend fun sendChat(reportId: String, message: String): Result<ChatMessageDto> = runCatching {
        val response = api.sendChatMessage(reportId, ChatRequest(message))
        if (response.isSuccessful) {
            response.body() ?: throw Exception("Empty chat response")
        } else {
            throw Exception("Chat failed: ${response.code()}")
        }
    }

    suspend fun getChatHistory(reportId: String): Result<List<ChatMessageDto>> = runCatching {
        val response = api.getChatHistory(reportId)
        if (response.isSuccessful) response.body() ?: emptyList()
        else throw Exception("Failed to load history: ${response.code()}")
    }

    suspend fun adminLogin(username: String, password: String): Result<LoginResponse> = runCatching {
        val response = api.adminLogin(LoginRequest(username, password))
        if (response.isSuccessful) {
            val loginResp = response.body() ?: throw Exception("Empty login response")
            if (loginResp.success) cache.saveAdminToken(loginResp.token)
            loginResp
        } else {
            LoginResponse(false, message = "Invalid credentials")
        }
    }

    suspend fun getDashboardStats(): Result<DashboardStats> = runCatching {
        val response = api.getDashboardStats()
        if (response.isSuccessful) response.body() ?: DashboardStats()
        else throw Exception("Dashboard failed: ${response.code()}")
    }

    suspend fun getAllReports(): Result<List<DiagnosisReportDto>> = runCatching {
        val response = api.getAllReports()
        if (response.isSuccessful) response.body() ?: emptyList()
        else throw Exception("Reports failed: ${response.code()}")
    }

    suspend fun getReportDetail(reportId: String): Result<DiagnosisReportDto> = runCatching {
        val response = api.getReportDetail(reportId)
        if (response.isSuccessful) response.body() ?: throw Exception("Report not found")
        else throw Exception("Report detail failed: ${response.code()}")
    }

    suspend fun createMachine(
        context: Context, name: String, manufacturer: String, model: String,
        thumbnailUri: Uri?, manualUri: Uri?, userGuideUri: Uri?
    ): Result<MachineDto> = runCatching {
        val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val mfgPart = manufacturer.toRequestBody("text/plain".toMediaTypeOrNull())
        val modelPart = model.toRequestBody("text/plain".toMediaTypeOrNull())

        val thumbPart = thumbnailUri?.let {
            val file = uriToFile(context, it, "thumbnail.jpg")
            MultipartBody.Part.createFormData("thumbnail", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
        }
        val manualPart = manualUri?.let {
            val file = uriToFile(context, it, "manual.pdf")
            MultipartBody.Part.createFormData("manual", file.name, file.asRequestBody("application/pdf".toMediaTypeOrNull()))
        }
        val guidePart = userGuideUri?.let {
            val file = uriToFile(context, it, "userguide.pdf")
            MultipartBody.Part.createFormData("userGuide", file.name, file.asRequestBody("application/pdf".toMediaTypeOrNull()))
        }

        val response = api.createMachine(namePart, mfgPart, modelPart, thumbPart, manualPart, guidePart)
        if (response.isSuccessful) response.body() ?: throw Exception("Empty response")
        else throw Exception("Create machine failed: ${response.code()}")
    }

    suspend fun generateKnowledge(machineId: String): Result<KnowledgeResponse> = runCatching {
        val response = api.generateKnowledge(machineId)
        if (response.isSuccessful) response.body() ?: KnowledgeResponse(false, "Empty response")
        else throw Exception("Knowledge generation failed: ${response.code()}")
    }

    suspend fun addReferenceImage(
        context: Context, machineId: String, partName: String,
        circleX: Float, circleY: Float, circleRadius: Float, imageUri: Uri
    ): Result<ReferenceImageDto> = runCatching {
        val imageFile = uriToFile(context, imageUri, "${partName.replace(" ", "_")}.jpg")
        val imagePart = MultipartBody.Part.createFormData(
            "image", imageFile.name, imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        )
        val response = api.addReferenceImage(
            machineId,
            partName.toRequestBody("text/plain".toMediaTypeOrNull()),
            circleX.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            circleY.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            circleRadius.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            imagePart
        )
        if (response.isSuccessful) response.body() ?: throw Exception("Empty response")
        else throw Exception("Add reference image failed: ${response.code()}")
    }

    suspend fun getReferenceImages(machineId: String): Result<List<ReferenceImageDto>> = runCatching {
        val response = api.getReferenceImages(machineId)
        if (response.isSuccessful) response.body() ?: emptyList()
        else throw Exception("Failed to load reference images: ${response.code()}")
    }

    private fun uriToFile(context: Context, uri: Uri, fileName: String): File {
        val file = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        }
        return file
    }
}
