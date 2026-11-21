package com.example.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiEnvelope<T>(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: T? = null,
    @Json(name = "code") val code: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "errors") val errors: Any? = null,
    @Json(name = "meta") val meta: MetaDto? = null
)

@JsonClass(generateAdapter = true)
data class MetaDto(
    @Json(name = "request_id") val requestId: String? = null,
    @Json(name = "server_time") val serverTime: String? = null,
    @Json(name = "version") val version: String? = null,
    @Json(name = "unread_count") val unreadCount: Int? = null,
    @Json(name = "has_open_request") val hasOpenRequest: Boolean? = null,
    @Json(name = "idempotent_replay") val idempotentReplay: Boolean? = null,
    @Json(name = "years") val years: List<Int>? = null,
    @Json(name = "total_net") val totalNet: Double? = null,
    @Json(name = "counts") val counts: Map<String, Int>? = null,
    @Json(name = "pagination") val pagination: PaginationDto? = null
)

@JsonClass(generateAdapter = true)
data class PaginationDto(
    @Json(name = "page") val page: Int = 1,
    @Json(name = "per_page") val perPage: Int = 20,
    @Json(name = "total") val total: Int = 0,
    @Json(name = "total_pages") val totalPages: Int = 1,
    @Json(name = "has_more") val hasMore: Boolean = false
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "identifier") val identifier: String,
    @Json(name = "password") val password: String,
    @Json(name = "device_name") val deviceName: String = "Android Device",
    @Json(name = "platform") val platform: String = "android",
    @Json(name = "app_version") val appVersion: String = "1.0.0"
)

@JsonClass(generateAdapter = true)
data class RefreshRequest(
    @Json(name = "refresh_token") val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class TokenPairDto(
    @Json(name = "token_type") val tokenType: String = "Bearer",
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "expires_at") val expiresAt: String? = null,
    @Json(name = "expires_in") val expiresIn: Int? = null,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    @Json(name = "refresh_expires_at") val refreshExpiresAt: String? = null
)

@JsonClass(generateAdapter = true)
data class LoginResponseData(
    @Json(name = "tokens") val tokens: TokenPairDto,
    @Json(name = "employee") val employee: EmployeeDto
)

@JsonClass(generateAdapter = true)
data class RefreshResponseData(
    @Json(name = "tokens") val tokens: TokenPairDto
)

@JsonClass(generateAdapter = true)
data class ManagerDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "name") val name: String? = null
)

@JsonClass(generateAdapter = true)
data class EmployeeDto(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "employee_code") val employeeCode: String = "",
    @Json(name = "full_name") val fullName: String = "",
    @Json(name = "email") val email: String = "",
    @Json(name = "username") val username: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "role") val role: String = "KARYAWAN",
    @Json(name = "flag") val flag: String? = "ACTIVE",
    @Json(name = "join_date") val joinDate: String? = null,
    @Json(name = "position") val position: String? = null,
    @Json(name = "department") val department: String? = null,
    @Json(name = "business_unit") val businessUnit: String? = null,
    @Json(name = "manager") val manager: ManagerDto? = null,
    @Json(name = "photo_url") val photoUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class CompanyDto(
    @Json(name = "name") val name: String = "PT Asia Raya Group",
    @Json(name = "legal") val legal: String? = null,
    @Json(name = "address") val address: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "logo_url") val logoUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthMeData(
    @Json(name = "employee") val employee: EmployeeDto,
    @Json(name = "counters") val counters: Map<String, Int>? = null,
    @Json(name = "attendance_today") val attendanceToday: AttendanceTodayDto? = null,
    @Json(name = "permissions") val permissions: PermissionsDto? = null
)

@JsonClass(generateAdapter = true)
data class PermissionsDto(
    @Json(name = "can_approve") val canApprove: Boolean = false,
    @Json(name = "role") val role: String = "KARYAWAN"
)

@JsonClass(generateAdapter = true)
data class ShiftDto(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "code") val code: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "date") val date: String? = null,
    @Json(name = "start_time") val startTime: String = "08:00",
    @Json(name = "end_time") val endTime: String = "17:00",
    @Json(name = "break_minutes") val breakMinutes: Int = 60,
    @Json(name = "late_tolerance") val lateTolerance: Int = 15,
    @Json(name = "operational_type") val operationalType: String = "NORMAL"
)

@JsonClass(generateAdapter = true)
data class AttendanceRecordDto(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "date") val date: String = "",
    @Json(name = "status") val status: String? = null,
    @Json(name = "checked_in") val checkedIn: Boolean = false,
    @Json(name = "checked_out") val checkedOut: Boolean = false,
    @Json(name = "check_in_at") val checkInAt: String? = null,
    @Json(name = "check_out_at") val checkOutAt: String? = null,
    @Json(name = "check_in_status") val checkInStatus: String? = null,
    @Json(name = "late_minutes") val lateMinutes: Int = 0,
    @Json(name = "early_leave_minutes") val earlyLeaveMinutes: Int = 0,
    @Json(name = "work_minutes") val workMinutes: Int = 0,
    @Json(name = "work_hours") val workHours: Double = 0.0,
    @Json(name = "check_in_photo_url") val checkInPhotoUrl: String? = null,
    @Json(name = "check_in_distance") val checkInDistance: Int? = null,
    @Json(name = "note") val note: String? = null,
    @Json(name = "source") val source: String? = "ANDROID"
)

@JsonClass(generateAdapter = true)
data class GeofenceDto(
    @Json(name = "required") val required: Boolean = true,
    @Json(name = "selfie_required") val selfieRequired: Boolean = true,
    @Json(name = "unit") val unit: String? = "Jakarta HQ (Pusat)",
    @Json(name = "lat") val lat: Double? = -6.2241,
    @Json(name = "lng") val lng: Double? = 106.8089,
    @Json(name = "radius") val radius: Int = 50
)

@JsonClass(generateAdapter = true)
data class AttendanceTodayDto(
    @Json(name = "date") val date: String = "",
    @Json(name = "shift") val shift: ShiftDto? = null,
    @Json(name = "shift_start") val shiftStart: String? = "08:00",
    @Json(name = "shift_end") val shiftEnd: String? = "17:00",
    @Json(name = "late_tolerance") val lateTolerance: Int = 15,
    @Json(name = "operational_type") val operationalType: String = "NORMAL",
    @Json(name = "can_check_in") val canCheckIn: Boolean = false,
    @Json(name = "can_check_out") val canCheckOut: Boolean = false,
    @Json(name = "attendance") val attendance: AttendanceRecordDto? = null,
    @Json(name = "geofence") val geofence: GeofenceDto? = null
)

@JsonClass(generateAdapter = true)
data class PunchRequest(
    @Json(name = "type") val type: String, // in or out
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double,
    @Json(name = "accuracy") val accuracy: Double? = null,
    @Json(name = "photo") val photo: String? = null,
    @Json(name = "note") val note: String? = null,
    @Json(name = "device") val device: String? = "Android",
    @Json(name = "captured_at") val capturedAt: String? = null,
    @Json(name = "offline") val offline: Boolean? = false
)

@JsonClass(generateAdapter = true)
data class PunchDataDto(
    @Json(name = "type") val type: String,
    @Json(name = "status") val status: String? = null,
    @Json(name = "late_minutes") val lateMinutes: Int = 0,
    @Json(name = "work_minutes") val workMinutes: Int = 0,
    @Json(name = "distance") val distance: Double? = null,
    @Json(name = "geofence") val geofence: String? = null,
    @Json(name = "at") val at: String? = null
)

@JsonClass(generateAdapter = true)
data class PunchResponseData(
    @Json(name = "punch") val punch: PunchDataDto? = null,
    @Json(name = "attendance") val attendance: AttendanceRecordDto? = null,
    @Json(name = "today") val today: AttendanceTodayDto? = null,
    @Json(name = "message") val message: String = ""
)

@JsonClass(generateAdapter = true)
data class AttendanceSummaryData(
    @Json(name = "month") val month: String = "",
    @Json(name = "present") val present: Int = 0,
    @Json(name = "late") val late: Int = 0,
    @Json(name = "absent") val absent: Int = 0,
    @Json(name = "late_minutes") val lateMinutes: Int = 0,
    @Json(name = "work_hours") val workHours: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class ScheduleDayDto(
    @Json(name = "date") val date: String,
    @Json(name = "dow") val dow: Int? = null,
    @Json(name = "weekend") val weekend: Boolean = false,
    @Json(name = "is_work") val isWork: Boolean = true,
    @Json(name = "shift") val shift: ShiftDto? = null
)

@JsonClass(generateAdapter = true)
data class ScheduleTodayData(
    @Json(name = "date") val date: String,
    @Json(name = "shift") val shift: ShiftDto? = null,
    @Json(name = "next_days") val nextDays: List<ScheduleDayDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ScheduleRequestDto(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "start_date") val startDate: String = "",
    @Json(name = "end_date") val endDate: String = "",
    @Json(name = "reason") val reason: String? = null,
    @Json(name = "status") val status: String = "PENDING",
    @Json(name = "status_label") val statusLabel: String? = null,
    @Json(name = "current_shift") val currentShift: ShiftDto? = null,
    @Json(name = "requested_shift") val requestedShift: ShiftDto? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateScheduleRequestPayload(
    @Json(name = "requested_shift_id") val requestedShiftId: Int,
    @Json(name = "start_date") val startDate: String,
    @Json(name = "end_date") val endDate: String,
    @Json(name = "reason") val reason: String? = null
)

@JsonClass(generateAdapter = true)
data class DecisionStepDto(
    @Json(name = "name") val name: String? = null,
    @Json(name = "at") val at: String? = null,
    @Json(name = "note") val note: String? = null
)

@JsonClass(generateAdapter = true)
data class DecisionDto(
    @Json(name = "manager") val manager: DecisionStepDto? = null,
    @Json(name = "hr") val hr: DecisionStepDto? = null
)

@JsonClass(generateAdapter = true)
data class LeaveTypeRef(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "name") val name: String? = null,
    @Json(name = "code") val code: String? = null
)

@JsonClass(generateAdapter = true)
data class LeaveDto(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "request_number") val requestNumber: String? = null,
    @Json(name = "type") val type: LeaveTypeRef? = null,
    @Json(name = "start_date") val startDate: String = "",
    @Json(name = "end_date") val endDate: String = "",
    @Json(name = "days") val days: Double = 1.0,
    @Json(name = "reason") val reason: String? = null,
    @Json(name = "status") val status: String = "PENDING",
    @Json(name = "status_label") val statusLabel: String? = null,
    @Json(name = "attachment_url") val attachmentUrl: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "decision") val decision: DecisionDto? = null
)

@JsonClass(generateAdapter = true)
data class LeaveTypeDto(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "code") val code: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "quota_days") val quotaDays: Double = 0.0,
    @Json(name = "is_paid") val isPaid: Boolean = true,
    @Json(name = "deduct_salary") val deductSalary: Boolean = false,
    @Json(name = "requires_attachment") val requiresAttachment: Boolean = false,
    @Json(name = "max_per_request") val maxPerRequest: Int = 12,
    @Json(name = "min_notice_days") val minNoticeDays: Int = 0,
    @Json(name = "color") val color: String? = null
)

@JsonClass(generateAdapter = true)
data class LeaveBalanceDto(
    @Json(name = "leave_type_id") val leaveTypeId: Int = 0,
    @Json(name = "type_name") val typeName: String = "",
    @Json(name = "type_code") val typeCode: String = "",
    @Json(name = "year") val year: Int = 2026,
    @Json(name = "quota") val quota: Double = 0.0,
    @Json(name = "carry_over") val carryOver: Double = 0.0,
    @Json(name = "used") val used: Double = 0.0,
    @Json(name = "remaining") val remaining: Double = 0.0,
    @Json(name = "unlimited") val unlimited: Boolean = false,
    @Json(name = "progress") val progress: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class CreateLeavePayload(
    @Json(name = "leave_type_id") val leaveTypeId: Int,
    @Json(name = "start_date") val startDate: String,
    @Json(name = "end_date") val endDate: String,
    @Json(name = "reason") val reason: String,
    @Json(name = "attachment") val attachment: String? = null
)

@JsonClass(generateAdapter = true)
data class OvertimeDto(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "work_date") val workDate: String = "",
    @Json(name = "planned_start") val plannedStart: String = "",
    @Json(name = "planned_end") val plannedEnd: String = "",
    @Json(name = "planned_hours") val plannedHours: Double = 0.0,
    @Json(name = "actual_hours") val actualHours: Double? = null,
    @Json(name = "type") val type: String? = "REQUEST",
    @Json(name = "reason") val reason: String? = null,
    @Json(name = "status") val status: String = "PENDING",
    @Json(name = "status_label") val statusLabel: String? = null,
    @Json(name = "compensation_amount") val compensationAmount: Double = 0.0,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "decision") val decision: DecisionDto? = null
)

@JsonClass(generateAdapter = true)
data class CreateOvertimePayload(
    @Json(name = "work_date") val workDate: String,
    @Json(name = "planned_start") val plannedStart: String,
    @Json(name = "planned_end") val plannedEnd: String,
    @Json(name = "reason") val reason: String
)

@JsonClass(generateAdapter = true)
data class PayslipPeriodDto(
    @Json(name = "year") val year: Int = 2026,
    @Json(name = "month") val month: Int = 10,
    @Json(name = "label") val label: String = "",
    @Json(name = "pay_date") val payDate: String? = null,
    @Json(name = "status") val status: String = "PAID"
)

@JsonClass(generateAdapter = true)
data class PayslipComponentDto(
    @Json(name = "name") val name: String,
    @Json(name = "type") val type: String, // ALLOWANCE or DEDUCTION
    @Json(name = "amount") val amount: Double
)

@JsonClass(generateAdapter = true)
data class PayslipDto(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "slip_number") val slipNumber: String = "",
    @Json(name = "period") val period: PayslipPeriodDto? = null,
    @Json(name = "business_unit") val businessUnit: String? = null,
    @Json(name = "gross_salary") val grossSalary: Double = 0.0,
    @Json(name = "total_deduction") val totalDeduction: Double = 0.0,
    @Json(name = "pph_amount") val pphAmount: Double = 0.0,
    @Json(name = "net_salary") val netSalary: Double = 0.0,
    @Json(name = "take_home_pay") val takeHomePay: Double = 0.0,
    @Json(name = "download_url") val downloadUrl: String? = null,
    @Json(name = "verify_url") val verifyUrl: String? = null,
    @Json(name = "components") val components: List<PayslipComponentDto>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class ClaimCategoryDto(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "code") val code: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "description") val description: String? = null,
    @Json(name = "max_amount") val maxAmount: Double? = null,
    @Json(name = "requires_receipt") val requiresReceipt: Boolean = true,
    @Json(name = "color") val color: String? = null
)

@JsonClass(generateAdapter = true)
data class ClaimDto(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "claim_number") val claimNumber: String = "",
    @Json(name = "category") val category: ClaimCategoryDto? = null,
    @Json(name = "claim_date") val claimDate: String = "",
    @Json(name = "amount") val amount: Double = 0.0,
    @Json(name = "description") val description: String? = null,
    @Json(name = "status") val status: String = "PENDING",
    @Json(name = "status_label") val statusLabel: String? = null,
    @Json(name = "receipt_url") val receiptUrl: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateClaimPayload(
    @Json(name = "category_id") val categoryId: Int,
    @Json(name = "claim_date") val claimDate: String,
    @Json(name = "amount") val amount: Double,
    @Json(name = "description") val description: String,
    @Json(name = "receipt") val receipt: String? = null
)

@JsonClass(generateAdapter = true)
data class TaxSlipDto(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "tax_year") val taxYear: Int = 2026,
    @Json(name = "slip_number") val slipNumber: String = "",
    @Json(name = "months_count") val monthsCount: Int = 12,
    @Json(name = "employee_name") val employeeName: String? = null,
    @Json(name = "npwp") val npwp: String? = null,
    @Json(name = "tax_status") val taxStatus: String? = null,
    @Json(name = "gross_income") val grossIncome: Double = 0.0,
    @Json(name = "netto") val netto: Double = 0.0,
    @Json(name = "pkp") val pkp: Double = 0.0,
    @Json(name = "tax_due") val taxDue: Double = 0.0,
    @Json(name = "tax_withheld") val taxWithheld: Double = 0.0,
    @Json(name = "tax_diff") val taxDiff: Double = 0.0,
    @Json(name = "issued_at") val issuedAt: String? = null,
    @Json(name = "download_url") val downloadUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class AnnouncementDto(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "title") val title: String = "",
    @Json(name = "category") val category: String = "Umum",
    @Json(name = "audience") val audience: String? = "ALL",
    @Json(name = "is_pinned") val isPinned: Boolean = false,
    @Json(name = "author") val author: String? = null,
    @Json(name = "excerpt") val excerpt: String = "",
    @Json(name = "body") val body: String? = null,
    @Json(name = "published_at") val publishedAt: String? = null,
    @Json(name = "attachment_url") val attachmentUrl: String? = null,
    @Json(name = "is_read") val isRead: Boolean = false,
    @Json(name = "read_at") val readAt: String? = null
)

@JsonClass(generateAdapter = true)
data class NotificationDto(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "title") val title: String = "",
    @Json(name = "message") val message: String = "",
    @Json(name = "type") val type: String = "info", // info, success, warning, danger
    @Json(name = "link") val link: String? = null,
    @Json(name = "is_read") val isRead: Boolean = false,
    @Json(name = "read_at") val readAt: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ApprovalItemDto(
    @Json(name = "type") val type: String, // leave, overtime, schedule, claim
    @Json(name = "type_label") val typeLabel: String = "",
    @Json(name = "id") val id: Int = 0,
    @Json(name = "employee") val employee: EmployeeDto? = null,
    @Json(name = "status") val status: String = "PENDING",
    @Json(name = "status_label") val statusLabel: String? = null,
    @Json(name = "reason") val reason: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "decide_url") val decideUrl: String? = null,
    @Json(name = "detail") val detail: Map<String, Any>? = null
)

@JsonClass(generateAdapter = true)
data class DecidePayload(
    @Json(name = "decision") val decision: String, // APPROVED or REJECTED
    @Json(name = "note") val note: String? = null
)

@JsonClass(generateAdapter = true)
data class AppConfigDto(
    @Json(name = "app") val app: Map<String, Any>? = null,
    @Json(name = "company") val company: CompanyDto? = null,
    @Json(name = "employee") val employee: EmployeeDto? = null,
    @Json(name = "features") val features: Map<String, Boolean>? = null,
    @Json(name = "attendance") val attendance: Map<String, Any>? = null,
    @Json(name = "leave") val leave: Map<String, Any>? = null,
    @Json(name = "overtime") val overtime: Map<String, Any>? = null,
    @Json(name = "claims") val claims: Map<String, Any>? = null,
    @Json(name = "limits") val limits: Map<String, Any>? = null
)
