package com.example.data.repo

import android.content.Context
import com.example.data.api.ApiClient
import com.example.data.api.model.AnnouncementDto
import com.example.data.api.model.ApiEnvelope
import com.example.data.api.model.AppConfigDto
import com.example.data.api.model.ApprovalItemDto
import com.example.data.api.model.AttendanceRecordDto
import com.example.data.api.model.AttendanceSummaryData
import com.example.data.api.model.AttendanceTodayDto
import com.example.data.api.model.AuthMeData
import com.example.data.api.model.ClaimCategoryDto
import com.example.data.api.model.ClaimDto
import com.example.data.api.model.CreateClaimPayload
import com.example.data.api.model.CreateLeavePayload
import com.example.data.api.model.CreateOvertimePayload
import com.example.data.api.model.CreateScheduleRequestPayload
import com.example.data.api.model.DecidePayload
import com.example.data.api.model.EmployeeDto
import com.example.data.api.model.EmployeeProfileData
import com.example.data.api.model.EmployeeProfileParser
import com.example.data.api.model.LeaveBalanceDto
import com.example.data.api.model.LeaveDto
import com.example.data.api.model.LeaveTypeDto
import com.example.data.api.model.LoginRequest
import com.example.data.api.model.NotificationDto
import com.example.data.api.model.OvertimeDto
import com.example.data.api.model.PayslipDto
import com.example.data.api.model.PunchRequest
import com.example.data.api.model.PunchResponseData
import com.example.data.api.model.ScheduleDayDto
import com.example.data.api.model.ScheduleRequestDto
import com.example.data.api.model.ScheduleTodayData
import com.example.data.api.model.ShiftDto
import com.example.data.api.model.TaxSlipDto
import com.example.data.local.AppDatabase
import com.example.data.local.PendingPunch
import com.example.data.local.SessionPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class Resource<out T> {
    data class Success<out T>(val data: T, val message: String? = null) : Resource<T>()
    data class Error(val message: String, val code: String? = null, val fieldErrors: Map<String, String>? = null) : Resource<Nothing>()
}

class HrisRepository(
    private val context: Context,
    private val apiClient: ApiClient,
    val sessionPreferences: SessionPreferences,
    private val database: AppDatabase
) {
    private val pendingPunchDao = database.pendingPunchDao()

    val pendingPunchesFlow: Flow<List<PendingPunch>> = pendingPunchDao.getAllPunches()
    val queuedPunchCountFlow: Flow<Int> = pendingPunchDao.getQueuedCount()
    val serverUrlFlow: Flow<String> = sessionPreferences.serverUrlFlow
    val employeeRoleFlow: Flow<String?> = sessionPreferences.employeeRoleFlow
    val employeeNameFlow: Flow<String?> = sessionPreferences.employeeNameFlow
    val employeeCodeFlow: Flow<String?> = sessionPreferences.employeeCodeFlow
    val employeeEmailFlow: Flow<String?> = sessionPreferences.employeeEmailFlow
    val canApproveFlow: Flow<Boolean> = sessionPreferences.canApproveFlow

    private fun <T> parseError(response: Response<T>): Resource.Error {
        val errorBodyStr = response.errorBody()?.string()
        if (!errorBodyStr.isNullOrBlank()) {
            try {
                val json = org.json.JSONObject(errorBodyStr)
                val msg = json.optString("message").takeIf { it.isNotBlank() }
                val code = json.optString("code").takeIf { it.isNotBlank() }
                val fieldErrors = mutableMapOf<String, String>()
                val errorsObj = json.optJSONObject("errors")
                if (errorsObj != null) {
                    val keys = errorsObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val v = errorsObj.optString(key)
                        if (v.isNotBlank()) {
                            fieldErrors[key] = v
                        }
                    }
                }
                if (!msg.isNullOrBlank()) {
                    return Resource.Error(msg, code = code, fieldErrors = fieldErrors.ifEmpty { null })
                }
            } catch (_: Exception) {}
        }
        val defaultMsg = when (response.code()) {
            401 -> "Email/NIK atau kata sandi salah, atau sesi Anda telah berakhir."
            403 -> "Anda tidak memiliki akses ke fitur atau data ini."
            404 -> "Data tidak ditemukan di server."
            409 -> "Status data tidak memungkinkan atau pengajuan serupa masih terbuka."
            422 -> "Permintaan ditolak oleh aturan bisnis server."
            429 -> "Terlalu banyak permintaan. Silakan tunggu 1 menit sebelum mencoba lagi."
            in 500..599 -> "Server sedang mengalami gangguan (Kode ${response.code()})."
            else -> "Terjadi kesalahan pada koneksi server (Kode HTTP ${response.code()})."
        }
        return Resource.Error(defaultMsg, code = response.code().toString())
    }

    // --- Authentication & Session ---
    suspend fun login(request: LoginRequest): Resource<EmployeeDto> = withContext(Dispatchers.IO) {
        try {
            val api = apiClient.getApiService()
            val response = api.login(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                sessionPreferences.saveTokens(
                    data.tokens.accessToken,
                    data.tokens.refreshToken,
                    data.tokens.expiresAt
                )
                sessionPreferences.saveEmployeeInfo(
                    data.employee.fullName,
                    data.employee.employeeCode,
                    data.employee.email,
                    data.employee.role,
                    data.employee.role == "HR" || data.employee.role == "FIN"
                )
                return@withContext Resource.Success(data.employee)
            } else {
                return@withContext parseError(response)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Server tidak dapat dihubungi (${e.localizedMessage ?: "koneksi gagal"}). Periksa alamat server & koneksi internet.")
        }
    }

    suspend fun logout(): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            apiClient.getApiService().logout()
        } catch (_: Exception) {}
        sessionPreferences.clearSession()
        Resource.Success(Unit)
    }

    suspend fun getMe(): Resource<AuthMeData> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getMe()
            if (res.isSuccessful && res.body()?.success == true && res.body()?.data != null) {
                val data = res.body()!!.data!!
                sessionPreferences.saveEmployeeInfo(
                    data.employee.fullName,
                    data.employee.employeeCode,
                    data.employee.email,
                    data.employee.role,
                    data.permissions?.canApprove ?: (data.employee.role == "HR" || data.employee.role == "FIN")
                )
                return@withContext Resource.Success(data)
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat profil: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun getMyProfile(year: Int? = null): Resource<EmployeeProfileData> = withContext(Dispatchers.IO) {
        val validYear = year?.coerceIn(2000, 2100)
        try {
            val res = apiClient.getApiService().getMyProfile(validYear)
            if (res.isSuccessful) {
                val rawBody = res.body()?.string().orEmpty()
                if (rawBody.isBlank()) {
                    return@withContext Resource.Error("Respons profil dari server kosong.")
                }
                val json = org.json.JSONObject(rawBody)
                if (!json.optBoolean("success", true)) {
                    val msg = json.optString("message").takeIf { it.isNotBlank() } ?: "Gagal memuat profil karyawan."
                    val code = json.optString("code").takeIf { it.isNotBlank() }
                    return@withContext Resource.Error(msg, code = code)
                }
                val parsed = EmployeeProfileParser.parse(rawBody, validYear)
                    ?: return@withContext Resource.Error("Format data profil karyawan tidak sesuai.")
                sessionPreferences.saveEmployeeInfo(
                    parsed.profile.fullName,
                    parsed.profile.employeeCode,
                    parsed.contact.companyEmail,
                    parsed.permissions.role,
                    parsed.permissions.canApprove
                )
                return@withContext Resource.Success(parsed)
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat profil karyawan: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun resolveMediaUrl(relativePath: String?): String = apiClient.resolveServerUrl(relativePath)

    suspend fun getAppConfig(): Resource<AppConfigDto> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getAppConfig()
            if (res.isSuccessful && res.body()?.success == true && res.body()?.data != null) {
                return@withContext Resource.Success(res.body()!!.data!!)
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat konfigurasi server: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    // --- Attendance & Geofence Punch ---
    suspend fun getAttendanceToday(): Resource<AttendanceTodayDto> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getAttendanceToday()
            if (res.isSuccessful && res.body()?.success == true && res.body()?.data != null) {
                return@withContext Resource.Success(res.body()!!.data!!)
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat status presensi hari ini: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun punchAttendance(
        type: String, // "in" or "out"
        lat: Double,
        lng: Double,
        accuracy: Double?,
        photoBase64: String?,
        note: String?
    ): Resource<PunchResponseData> = withContext(Dispatchers.IO) {
        val idempotencyKey = UUID.randomUUID().toString()
        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+07:00", Locale.getDefault()).format(Date())

        val req = PunchRequest(
            type = type,
            lat = lat,
            lng = lng,
            accuracy = accuracy,
            photo = photoBase64,
            note = note,
            capturedAt = nowIso,
            offline = false
        )

        try {
            val api = apiClient.getApiService()
            val response = api.punchAttendance(idempotencyKey, req)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val data = response.body()!!.data!!
                return@withContext Resource.Success(data, data.message)
            } else {
                return@withContext parseError(response)
            }
        } catch (e: Exception) {
            // Save to Room offline queue
            pendingPunchDao.insertPunch(
                PendingPunch(
                    type = type,
                    lat = lat,
                    lng = lng,
                    accuracy = accuracy,
                    photoBase64 = photoBase64?.take(1000),
                    note = note,
                    capturedAt = nowIso,
                    idempotencyKey = idempotencyKey,
                    status = "QUEUED",
                    attempts = 1,
                    lastError = e.localizedMessage
                )
            )
            return@withContext Resource.Success(
                PunchResponseData(
                    punch = null,
                    attendance = null,
                    today = null,
                    message = "Koneksi internet terputus. Presensi telah disimpan di antrean perangkat dan akan disinkronkan otomatis saat online."
                )
            )
        }
    }

    suspend fun syncPendingPunches(): Int = withContext(Dispatchers.IO) {
        val queued = pendingPunchDao.getQueuedPunches()
        var synced = 0
        for (item in queued) {
            try {
                val req = PunchRequest(
                    type = item.type,
                    lat = item.lat,
                    lng = item.lng,
                    accuracy = item.accuracy,
                    photo = item.photoBase64,
                    note = item.note,
                    capturedAt = item.capturedAt,
                    offline = true
                )
                val res = apiClient.getApiService().punchAttendance(item.idempotencyKey, req)
                if (res.isSuccessful && res.body()?.success == true) {
                    pendingPunchDao.deleteById(item.id)
                    synced++
                } else if (res.code() == 422) {
                    // Business rule rejection (e.g. out of geofence / duplicate)
                    pendingPunchDao.updatePunch(item.copy(status = "FAILED", lastError = res.body()?.message ?: "Ditolak server"))
                }
            } catch (e: Exception) {
                pendingPunchDao.updatePunch(item.copy(attempts = item.attempts + 1, lastError = e.localizedMessage))
            }
        }
        synced
    }

    suspend fun deletePendingPunch(id: Long) = withContext(Dispatchers.IO) {
        pendingPunchDao.deleteById(id)
    }

    suspend fun getAttendanceHistory(month: String): Resource<List<AttendanceRecordDto>> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getAttendanceHistory(month)
            if (res.isSuccessful && res.body()?.success == true) {
                return@withContext Resource.Success(res.body()!!.data ?: emptyList())
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat riwayat presensi: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun getAttendanceSummary(month: String): Resource<AttendanceSummaryData> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getAttendanceSummary(month)
            if (res.isSuccessful && res.body()?.success == true && res.body()?.data != null) {
                return@withContext Resource.Success(res.body()!!.data!!)
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat ringkasan presensi: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    // --- Schedule & Shifts ---
    suspend fun getScheduleToday(): Resource<ScheduleTodayData> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getScheduleToday()
            if (res.isSuccessful && res.body()?.success == true && res.body()?.data != null) {
                return@withContext Resource.Success(res.body()!!.data!!)
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat jadwal hari ini: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun getScheduleCalendar(from: String, to: String): Resource<List<ScheduleDayDto>> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getScheduleCalendar(from, to)
            if (res.isSuccessful && res.body()?.success == true) {
                return@withContext Resource.Success(res.body()!!.data ?: emptyList())
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat kalender jadwal: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun getShifts(): Resource<List<ShiftDto>> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getShifts()
            if (res.isSuccessful && res.body()?.success == true) {
                return@withContext Resource.Success(res.body()!!.data ?: emptyList())
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat daftar shift: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun getScheduleRequests(): Resource<List<ScheduleRequestDto>> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getScheduleRequests()
            if (res.isSuccessful && res.body()?.success == true) {
                return@withContext Resource.Success(res.body()!!.data ?: emptyList())
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat riwayat pengajuan perubahan shift: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun createScheduleRequest(payload: CreateScheduleRequestPayload): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().createScheduleRequest(payload)
            if (res.isSuccessful && res.body()?.success == true) {
                val msg = res.body()?.message ?: "Pengajuan perubahan jadwal berhasil dibuat."
                return@withContext Resource.Success(Unit, msg)
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal mengirim pengajuan jadwal: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun cancelScheduleRequest(id: Int): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().cancelScheduleRequest(id)
            if (res.isSuccessful && res.body()?.success == true) {
                return@withContext Resource.Success(Unit, "Pengajuan perubahan jadwal dibatalkan.")
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal membatalkan pengajuan: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    // --- Leave (Cuti) ---
    suspend fun getLeaveBalances(year: Int = 2026): Resource<List<LeaveBalanceDto>> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getLeaveBalances(year)
            if (res.isSuccessful && res.body()?.success == true) {
                return@withContext Resource.Success(res.body()!!.data ?: emptyList())
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat saldo cuti: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun getLeaveTypes(): Resource<List<LeaveTypeDto>> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getLeaveTypes()
            if (res.isSuccessful && res.body()?.success == true) {
                return@withContext Resource.Success(res.body()!!.data ?: emptyList())
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat jenis cuti: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun getLeaves(): Resource<List<LeaveDto>> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getLeaves()
            if (res.isSuccessful && res.body()?.success == true) {
                return@withContext Resource.Success(res.body()!!.data ?: emptyList())
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat riwayat cuti: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun getLeaveDetail(id: Int): Resource<LeaveDto> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getLeaveDetail(id)
            if (res.isSuccessful && res.body()?.success == true && res.body()?.data != null) {
                return@withContext Resource.Success(res.body()!!.data!!)
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat detail cuti: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun createLeave(payload: CreateLeavePayload): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().createLeave(payload)
            if (res.isSuccessful && res.body()?.success == true) {
                val msg = res.body()?.message ?: "Pengajuan cuti berhasil dikirim."
                return@withContext Resource.Success(Unit, msg)
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal mengirim pengajuan cuti: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun cancelLeave(id: Int): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().cancelLeave(id)
            if (res.isSuccessful && res.body()?.success == true) {
                return@withContext Resource.Success(Unit, "Pengajuan cuti berhasil dibatalkan.")
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal membatalkan pengajuan cuti: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    // --- Overtime (Lembur) ---
    suspend fun getOvertimes(month: String? = null, status: String? = null): Resource<List<OvertimeDto>> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getOvertimes(month, status)
            if (res.isSuccessful && res.body()?.success == true) {
                return@withContext Resource.Success(res.body()!!.data ?: emptyList())
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat daftar lembur: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun getOvertimePolicy(): Resource<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getOvertimePolicy()
            if (res.isSuccessful && res.body()?.success == true) {
                return@withContext Resource.Success(res.body()!!.data ?: emptyMap())
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat kebijakan lembur: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun createOvertime(payload: CreateOvertimePayload): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().createOvertime(payload)
            if (res.isSuccessful && res.body()?.success == true) {
                val msg = res.body()?.message ?: "Pengajuan lembur berhasil dibuat."
                return@withContext Resource.Success(Unit, msg)
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal mengirim pengajuan lembur: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun cancelOvertime(id: Int, note: String? = null): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val noteMap = if (note != null) mapOf("note" to note) else null
            val res = apiClient.getApiService().cancelOvertime(id, noteMap)
            if (res.isSuccessful && res.body()?.success == true) {
                return@withContext Resource.Success(Unit, "Pengajuan lembur dibatalkan.")
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal membatalkan pengajuan lembur: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    // --- Claims (Reimbursement) ---
    suspend fun getClaimCategories(): Resource<List<ClaimCategoryDto>> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getClaimCategories()
            if (res.isSuccessful && res.body()?.success == true) {
                return@withContext Resource.Success(res.body()!!.data ?: emptyList())
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat kategori klaim: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun getClaims(status: String? = null): Resource<List<ClaimDto>> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getClaims(status)
            if (res.isSuccessful && res.body()?.success == true) {
                return@withContext Resource.Success(res.body()!!.data ?: emptyList())
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat riwayat klaim: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun getClaimDetail(id: Int): Resource<ClaimDto> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getClaimDetail(id)
            if (res.isSuccessful && res.body()?.success == true && res.body()?.data != null) {
                return@withContext Resource.Success(res.body()!!.data!!)
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat detail klaim: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun createClaim(payload: CreateClaimPayload): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().createClaim(payload)
            if (res.isSuccessful && res.body()?.success == true) {
                val msg = res.body()?.message ?: "Klaim berhasil diajukan."
                return@withContext Resource.Success(Unit, msg)
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal mengajukan klaim: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun cancelClaim(id: Int): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().cancelClaim(id)
            if (res.isSuccessful && res.body()?.success == true) {
                return@withContext Resource.Success(Unit, "Pengajuan klaim berhasil dibatalkan.")
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal membatalkan klaim: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    // --- Payslips (Slip Gaji) ---
    suspend fun getPayslips(year: Int? = null): Resource<List<PayslipDto>> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getPayslips(year)
            if (res.isSuccessful && res.body()?.success == true) {
                return@withContext Resource.Success(res.body()!!.data ?: emptyList())
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat daftar slip gaji: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun getPayslipDetail(id: Int): Resource<PayslipDto> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getPayslipDetail(id)
            if (res.isSuccessful && res.body()?.success == true && res.body()?.data != null) {
                return@withContext Resource.Success(res.body()!!.data!!)
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat detail slip gaji: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    // --- Tax Slips (Bukti Potong 1721-A1) ---
    suspend fun getTaxSlips(year: Int? = null): Resource<List<TaxSlipDto>> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getTaxSlips(year)
            if (res.isSuccessful && res.body()?.success == true) {
                return@withContext Resource.Success(res.body()!!.data ?: emptyList())
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat bukti potong pajak: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun getTaxSlipDetail(year: Int): Resource<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getTaxSlipDetail(year)
            if (res.isSuccessful && res.body()?.success == true && res.body()?.data != null) {
                return@withContext Resource.Success(res.body()!!.data!!)
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat rincian pajak: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    // --- Announcements & Notifications ---
    suspend fun getAnnouncements(page: Int? = null, perPage: Int? = null, category: String? = null, query: String? = null, unread: Boolean? = null): Resource<List<AnnouncementDto>> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getAnnouncements(page, perPage, category, query, unread)
            if (res.isSuccessful && res.body()?.success == true) {
                return@withContext Resource.Success(res.body()!!.data ?: emptyList())
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat pengumuman: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun getAnnouncementDetail(id: Int): Resource<AnnouncementDto> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getAnnouncementDetail(id)
            if (res.isSuccessful && res.body()?.success == true && res.body()?.data != null) {
                return@withContext Resource.Success(res.body()!!.data!!)
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat isi pengumuman: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun markAnnouncementRead(id: Int) = withContext(Dispatchers.IO) {
        try {
            apiClient.getApiService().markAnnouncementRead(id)
        } catch (_: Exception) {}
    }

    suspend fun getNotifications(page: Int? = null, perPage: Int? = null, unread: Boolean? = null, type: String? = null): Resource<List<NotificationDto>> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getNotifications(page, perPage, unread, type)
            if (res.isSuccessful && res.body()?.success == true) {
                return@withContext Resource.Success(res.body()!!.data ?: emptyList())
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat notifikasi: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun markNotificationRead(id: Int) = withContext(Dispatchers.IO) {
        try {
            apiClient.getApiService().markNotificationRead(id)
        } catch (_: Exception) {}
    }

    suspend fun markAllNotificationsRead() = withContext(Dispatchers.IO) {
        try {
            apiClient.getApiService().markAllNotificationsRead()
        } catch (_: Exception) {}
    }

    // --- Approvals (Kotak Masuk Persetujuan) ---
    suspend fun getApprovals(type: String = "all", page: Int? = null, perPage: Int? = null): Resource<List<ApprovalItemDto>> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().getApprovals(type, page, perPage)
            if (res.isSuccessful && res.body()?.success == true) {
                return@withContext Resource.Success(res.body()!!.data ?: emptyList())
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memuat kotak persetujuan: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    suspend fun decideApproval(type: String, id: Int, decision: String, note: String?): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = apiClient.getApiService().decideApproval(type, id, DecidePayload(decision, note))
            if (res.isSuccessful && res.body()?.success == true) {
                val msg = res.body()?.message ?: "Keputusan telah berhasil disimpan di server."
                return@withContext Resource.Success(Unit, msg)
            } else {
                return@withContext parseError(res)
            }
        } catch (e: Exception) {
            return@withContext Resource.Error("Gagal memproses keputusan: ${e.localizedMessage ?: "Koneksi terputus"}")
        }
    }

    // --- Server & Settings ---
    suspend fun updateServerUrl(newUrl: String) = withContext(Dispatchers.IO) {
        sessionPreferences.setServerUrl(newUrl)
    }

    suspend fun getServerUrl(): String = sessionPreferences.getServerUrl()
}
