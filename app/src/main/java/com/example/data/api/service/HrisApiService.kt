package com.example.data.api.service

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
import com.example.data.api.model.LeaveBalanceDto
import com.example.data.api.model.LeaveDto
import com.example.data.api.model.LeaveTypeDto
import com.example.data.api.model.LoginRequest
import com.example.data.api.model.LoginResponseData
import com.example.data.api.model.NotificationDto
import com.example.data.api.model.OvertimeDto
import com.example.data.api.model.PayslipDto
import com.example.data.api.model.PunchRequest
import com.example.data.api.model.PunchResponseData
import com.example.data.api.model.RefreshRequest
import com.example.data.api.model.RefreshResponseData
import com.example.data.api.model.ScheduleDayDto
import com.example.data.api.model.ScheduleRequestDto
import com.example.data.api.model.ScheduleTodayData
import com.example.data.api.model.ShiftDto
import com.example.data.api.model.TaxSlipDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface HrisApiService {

    // Auth
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiEnvelope<LoginResponseData>>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): Response<ApiEnvelope<RefreshResponseData>>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<ApiEnvelope<Map<String, Any>>>

    @GET("api/v1/auth/me")
    suspend fun getMe(): Response<ApiEnvelope<AuthMeData>>

    @GET("api/v1/me/profile")
    suspend fun getMyProfile(@Query("year") year: Int? = null): Response<ResponseBody>

    // Config
    @GET("api/v1/app/config")
    suspend fun getAppConfig(): Response<ApiEnvelope<AppConfigDto>>

    // Announcements
    @GET("api/v1/announcements")
    suspend fun getAnnouncements(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("category") category: String? = null,
        @Query("q") query: String? = null,
        @Query("unread") unread: Boolean? = null
    ): Response<ApiEnvelope<List<AnnouncementDto>>>

    @GET("api/v1/announcements/{id}")
    suspend fun getAnnouncementDetail(@Path("id") id: Int): Response<ApiEnvelope<AnnouncementDto>>

    @POST("api/v1/announcements/{id}/read")
    suspend fun markAnnouncementRead(@Path("id") id: Int): Response<ApiEnvelope<Map<String, Any>>>

    // Schedule
    @GET("api/v1/schedule/today")
    suspend fun getScheduleToday(): Response<ApiEnvelope<ScheduleTodayData>>

    @GET("api/v1/schedule")
    suspend fun getScheduleCalendar(
        @Query("from") from: String,
        @Query("to") to: String
    ): Response<ApiEnvelope<List<ScheduleDayDto>>>

    @GET("api/v1/shifts")
    suspend fun getShifts(): Response<ApiEnvelope<List<ShiftDto>>>

    @GET("api/v1/schedule-requests")
    suspend fun getScheduleRequests(): Response<ApiEnvelope<List<ScheduleRequestDto>>>

    @POST("api/v1/schedule-requests")
    suspend fun createScheduleRequest(@Body payload: CreateScheduleRequestPayload): Response<ApiEnvelope<Map<String, Any>>>

    @POST("api/v1/schedule-requests/{id}/cancel")
    suspend fun cancelScheduleRequest(@Path("id") id: Int): Response<ApiEnvelope<Map<String, Any>>>

    // Attendance
    @GET("api/v1/attendance/today")
    suspend fun getAttendanceToday(): Response<ApiEnvelope<AttendanceTodayDto>>

    @POST("api/v1/attendance/punch")
    suspend fun punchAttendance(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: PunchRequest
    ): Response<ApiEnvelope<PunchResponseData>>

    @GET("api/v1/attendance/history")
    suspend fun getAttendanceHistory(@Query("month") month: String? = null): Response<ApiEnvelope<List<AttendanceRecordDto>>>

    @GET("api/v1/attendance/summary")
    suspend fun getAttendanceSummary(@Query("month") month: String? = null): Response<ApiEnvelope<AttendanceSummaryData>>

    // Overtime
    @GET("api/v1/overtimes")
    suspend fun getOvertimes(
        @Query("month") month: String? = null,
        @Query("status") status: String? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<ApiEnvelope<List<OvertimeDto>>>

    @POST("api/v1/overtimes")
    suspend fun createOvertime(@Body payload: CreateOvertimePayload): Response<ApiEnvelope<Map<String, Any>>>

    @GET("api/v1/overtimes/policy")
    suspend fun getOvertimePolicy(): Response<ApiEnvelope<Map<String, Any>>>

    @POST("api/v1/overtimes/{id}/cancel")
    suspend fun cancelOvertime(
        @Path("id") id: Int,
        @Body notePayload: Map<String, String>? = null
    ): Response<ApiEnvelope<Map<String, Any>>>

    // Leave
    @GET("api/v1/leave/types")
    suspend fun getLeaveTypes(): Response<ApiEnvelope<List<LeaveTypeDto>>>

    @GET("api/v1/leave/balances")
    suspend fun getLeaveBalances(@Query("year") year: Int? = null): Response<ApiEnvelope<List<LeaveBalanceDto>>>

    @GET("api/v1/leaves")
    suspend fun getLeaves(
        @Query("status") status: String? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<ApiEnvelope<List<LeaveDto>>>

    @GET("api/v1/leaves/{id}")
    suspend fun getLeaveDetail(@Path("id") id: Int): Response<ApiEnvelope<LeaveDto>>

    @POST("api/v1/leaves")
    suspend fun createLeave(@Body payload: CreateLeavePayload): Response<ApiEnvelope<Map<String, Any>>>

    @POST("api/v1/leaves/{id}/cancel")
    suspend fun cancelLeave(@Path("id") id: Int): Response<ApiEnvelope<Map<String, Any>>>

    // Payslip
    @GET("api/v1/payslips")
    suspend fun getPayslips(@Query("year") year: Int? = null): Response<ApiEnvelope<List<PayslipDto>>>

    @GET("api/v1/payslips/{id}")
    suspend fun getPayslipDetail(@Path("id") id: Int): Response<ApiEnvelope<PayslipDto>>

    @Streaming
    @GET("api/v1/payslips/{id}/pdf")
    suspend fun downloadPayslipPdf(
        @Path("id") id: Int,
        @Query("employee") employee: Int? = null,
        @Query("expires") expires: Long? = null,
        @Query("signature") signature: String? = null,
        @Query("download") download: Boolean = true
    ): Response<ResponseBody>

    // Claims
    @GET("api/v1/claim-categories")
    suspend fun getClaimCategories(): Response<ApiEnvelope<List<ClaimCategoryDto>>>

    @GET("api/v1/claims")
    suspend fun getClaims(
        @Query("status") status: String? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<ApiEnvelope<List<ClaimDto>>>

    @GET("api/v1/claims/{id}")
    suspend fun getClaimDetail(@Path("id") id: Int): Response<ApiEnvelope<ClaimDto>>

    @POST("api/v1/claims")
    suspend fun createClaim(@Body payload: CreateClaimPayload): Response<ApiEnvelope<Map<String, Any>>>

    @POST("api/v1/claims/{id}/cancel")
    suspend fun cancelClaim(@Path("id") id: Int): Response<ApiEnvelope<Map<String, Any>>>

    // Tax Slips
    @GET("api/v1/tax-slips")
    suspend fun getTaxSlips(@Query("year") year: Int? = null): Response<ApiEnvelope<List<TaxSlipDto>>>

    @GET("api/v1/tax-slips/{year}")
    suspend fun getTaxSlipDetail(@Path("year") year: Int): Response<ApiEnvelope<Map<String, Any>>>

    @Streaming
    @GET("api/v1/tax-slips/{year}/pdf")
    suspend fun downloadTaxSlipPdf(
        @Path("year") year: Int,
        @Query("employee") employee: Int? = null,
        @Query("expires") expires: Long? = null,
        @Query("signature") signature: String? = null
    ): Response<ResponseBody>

    // Notifications
    @GET("api/v1/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("unread") unread: Boolean? = null,
        @Query("type") type: String? = null
    ): Response<ApiEnvelope<List<NotificationDto>>>

    @POST("api/v1/notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<ApiEnvelope<Map<String, Any>>>

    @POST("api/v1/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: Int): Response<ApiEnvelope<Map<String, Any>>>

    // Approvals
    @GET("api/v1/approvals")
    suspend fun getApprovals(
        @Query("type") type: String? = "all",
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<ApiEnvelope<List<ApprovalItemDto>>>

    @GET("api/v1/approvals/summary")
    suspend fun getApprovalsSummary(): Response<ApiEnvelope<Map<String, Any>>>

    @POST("api/v1/approvals/{type}/{id}/decide")
    suspend fun decideApproval(
        @Path("type") type: String,
        @Path("id") id: Int,
        @Body payload: DecidePayload
    ): Response<ApiEnvelope<Map<String, Any>>>
}
