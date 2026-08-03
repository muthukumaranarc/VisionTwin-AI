package com.visiontwin.app.data.api

import com.visiontwin.app.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Machine APIs
    @GET("api/machines")
    suspend fun getMachines(): Response<List<MachineDto>>

    @GET("api/machines/{id}")
    suspend fun getMachineById(@Path("id") id: String): Response<MachineDto>

    @Multipart
    @POST("api/machines")
    suspend fun createMachine(
        @Part("name") name: RequestBody,
        @Part("manufacturer") manufacturer: RequestBody,
        @Part("model") model: RequestBody,
        @Part thumbnail: MultipartBody.Part?,
        @Part manual: MultipartBody.Part?,
        @Part userGuide: MultipartBody.Part?
    ): Response<MachineDto>

    @Multipart
    @POST("api/machines/{id}/ref-image")
    suspend fun addReferenceImage(
        @Path("id") machineId: String,
        @Part("partName") partName: RequestBody,
        @Part("circleX") circleX: RequestBody,
        @Part("circleY") circleY: RequestBody,
        @Part("circleRadius") circleRadius: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<ReferenceImageDto>

    @GET("api/machines/{id}/ref-images")
    suspend fun getReferenceImages(@Path("id") machineId: String): Response<List<ReferenceImageDto>>

    // Analysis APIs
    @Multipart
    @POST("api/analysis/diagnose")
    suspend fun diagnose(
        @Part("machineId") machineId: RequestBody,
        @Part("problemDescription") problemDescription: RequestBody,
        @Part image: MultipartBody.Part,
        @Part("model") model: RequestBody?
    ): Response<DiagnosisReportDto>

    @GET("api/analysis/models")
    suspend fun getDiagnosisModels(): Response<DiagnosisModelsResponse>

    // Reference image edit/delete
    @Multipart
    @PUT("api/machines/ref-images/{id}")
    suspend fun updateReferenceImage(
        @Path("id") id: String,
        @Part("partName") partName: RequestBody,
        @Part("circleX") circleX: RequestBody,
        @Part("circleY") circleY: RequestBody,
        @Part("circleRadius") circleRadius: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<ReferenceImageDto>

    @DELETE("api/machines/ref-images/{id}")
    suspend fun deleteReferenceImage(@Path("id") id: String): Response<Unit>

    // Chat APIs
    @POST("api/chat/{reportId}")
    suspend fun sendChatMessage(
        @Path("reportId") reportId: String,
        @Body request: ChatRequest
    ): Response<ChatMessageDto>

    @GET("api/chat/{reportId}/history")
    suspend fun getChatHistory(@Path("reportId") reportId: String): Response<List<ChatMessageDto>>

    // Admin APIs
    @POST("api/admin/login")
    suspend fun adminLogin(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/admin/dashboard")
    suspend fun getDashboardStats(): Response<DashboardStats>

    @GET("api/admin/reports")
    suspend fun getAllReports(): Response<List<DiagnosisReportDto>>

    @GET("api/admin/reports/{id}")
    suspend fun getReportDetail(@Path("id") id: String): Response<DiagnosisReportDto>

    // Knowledge APIs
    @POST("api/knowledge/generate/{machineId}")
    suspend fun generateKnowledge(@Path("machineId") machineId: String): Response<KnowledgeResponse>

    // Health
    @GET("api/health")
    suspend fun healthCheck(): Response<HealthResponse>
}
