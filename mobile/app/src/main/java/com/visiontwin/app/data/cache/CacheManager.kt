package com.visiontwin.app.data.cache

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.visiontwin.app.data.model.DiagnosisReportDto
import com.visiontwin.app.data.model.MachineDto

class CacheManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("visiontwin_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveMachines(machines: List<MachineDto>) {
        prefs.edit().putString("cached_machines", gson.toJson(machines)).apply()
    }

    fun loadMachines(): List<MachineDto> {
        val json = prefs.getString("cached_machines", null) ?: return emptyList()
        val type = object : TypeToken<List<MachineDto>>() {}.type
        return try { gson.fromJson(json, type) } catch (e: Exception) { emptyList() }
    }

    fun saveLastReport(report: DiagnosisReportDto) {
        prefs.edit().putString("last_report", gson.toJson(report)).apply()
    }

    fun loadLastReport(): DiagnosisReportDto? {
        val json = prefs.getString("last_report", null) ?: return null
        return try { gson.fromJson(json, DiagnosisReportDto::class.java) } catch (e: Exception) { null }
    }

    fun saveAdminToken(token: String?) {
        prefs.edit().putString("admin_token", token).apply()
    }

    fun loadAdminToken(): String? = prefs.getString("admin_token", null)

    fun clearAdminToken() {
        prefs.edit().remove("admin_token").apply()
    }
}
