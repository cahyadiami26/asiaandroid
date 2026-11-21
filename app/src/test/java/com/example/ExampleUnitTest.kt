package com.example

import com.example.data.api.model.AttendanceRecordDto
import com.example.data.api.model.AttendanceTodayDto
import com.example.data.api.model.EmployeeProfileParser
import com.example.data.api.model.ShiftDto
import com.example.ui.AppRoute
import com.example.ui.popRoute
import com.example.ui.pushRoute
import com.example.ui.screens.HomeAttendanceAction
import com.example.ui.screens.filterCurrentWeekAttendance
import com.example.ui.screens.getCurrentWeekRange
import com.example.ui.screens.resolveHomeAttendanceState
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Calendar
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {

    @Test
    fun testParseServerErrorBodyWithArrayErrors() {
        val jsonStr = """
            {
                "success": false,
                "code": "PUNCH_REJECTED",
                "message": "Anda berada 37.245 m dari Jakarta HQ (Pusat), di luar radius 50 m. Mendekatlah ke lokasi kantor untuk melakukan presensi.",
                "errors": [],
                "meta": {}
            }
        """.trimIndent()

        val json = JSONObject(jsonStr)
        val msg = json.optString("message")
        val code = json.optString("code")

        assertEquals("PUNCH_REJECTED", code)
        assertEquals("Anda berada 37.245 m dari Jakarta HQ (Pusat), di luar radius 50 m. Mendekatlah ke lokasi kantor untuk melakukan presensi.", msg)
    }

    @Test
    fun testOfficeGeofenceDistanceCalculation() {
        val officeLat = -6.2241
        val officeLng = 106.8089

        // Coordinates aligned inside office radius (~10 meters away)
        val punchLat = officeLat + 0.00008
        val punchLng = officeLng + 0.00005

        val r = 6371000.0
        val dLat = Math.toRadians(punchLat - officeLat)
        val dLon = Math.toRadians(punchLng - officeLng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(officeLat)) * cos(Math.toRadians(punchLat)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distanceMeters = r * c

        assertTrue("Distance should be under 50m radius", distanceMeters < 50.0)
        assertTrue("Distance should be approximately 10m", distanceMeters in 8.0..15.0)
    }

    @Test
    fun testParseEmployeeProfileAllTenBlocks() {
        val json = """
            {
              "success": true,
              "meta": { "year": 2026 },
              "data": {
                "profile": {
                  "nik": "AR-2024-0088",
                  "full_name": "Budi Santoso",
                  "nickname": "Budi",
                  "photo_url": "/storage/photos/budi.jpg",
                  "role": "SUPERVISOR",
                  "role_label": "Supervisor",
                  "employment_status": "ACTIVE",
                  "employment_status_label": "Aktif"
                },
                "employment": {
                  "type": "PKWT",
                  "join_date": "2024-03-01",
                  "tenure": { "years": 2, "months": 1, "days": 5, "label": "2 tahun 1 bulan" },
                  "contract": {
                    "remaining_days": 45,
                    "expiring_soon": true,
                    "end_date": "2026-06-30"
                  },
                  "resign_date": null
                },
                "organization": {
                  "business_unit": { "name": "PT Asia Raya Utama" },
                  "department": { "name": "Operations" },
                  "position": { "id": 14, "name": "Area Supervisor", "code": "OPS-SPV" }
                },
                "manager": {
                  "id": 3,
                  "name": "Rina Wijaya",
                  "email": "rina@asiaraya.co.id",
                  "position": "Operations Manager",
                  "department": "Operations",
                  "is_active": true
                },
                "contact": {
                  "company_email": "budi@asiaraya.co.id",
                  "username": "budi.santoso",
                  "phone": "08123456789",
                  "address": "Jl. Sudirman No. 10, Jakarta",
                  "google_linked": true
                },
                "personal": {
                  "birth_date": "1995-05-12",
                  "age": 31,
                  "gender_label": "Laki-laki",
                  "marital_status_label": "Menikah",
                  "religion_label": "Islam"
                },
                "payroll": {
                  "basic_salary": 12500000,
                  "basic_salary_label": "Rp 12.500.000",
                  "tax_status": "K/1",
                  "npwp": "12.345.678.9-012.000",
                  "bpjs_kesehatan": "0001234567890",
                  "bpjs_ketenagakerjaan": "19002345678",
                  "bank": { "name": "BCA", "account_number_masked": "****4821" }
                },
                "leave": {
                  "summary": { "total": 14, "approved_days": 4, "open": 1, "remaining": 10 },
                  "balances": [
                    {
                      "leave_type_id": 1,
                      "type_name": "Cuti Tahunan",
                      "type_code": "ANNUAL",
                      "quota": 12,
                      "carry_over": 2,
                      "used": 4,
                      "remaining": 10,
                      "progress": 0.285
                    }
                  ]
                },
                "documents": {
                  "total": 6,
                  "verified": 4,
                  "pending": 1,
                  "rejected": 0,
                  "expiring_soon": 1
                },
                "permissions": {
                  "role": "SUPERVISOR",
                  "can_approve": true,
                  "is_manager": true,
                  "editable_fields": ["nickname", "phone", "address"],
                  "email_change_requires_hr": true
                }
              }
            }
        """.trimIndent()

        val parsed = EmployeeProfileParser.parse(json, 2026)
        assertNotNull(parsed)
        parsed!!

        assertEquals(2026, parsed.metaYear)
        assertEquals("AR-2024-0088", parsed.profile.employeeCode)
        assertEquals("Budi Santoso", parsed.profile.fullName)
        assertEquals("Budi", parsed.profile.nickname)
        assertEquals("Supervisor", parsed.profile.roleLabel)
        assertEquals("PKWT", parsed.employment.contractType)
        assertEquals(45, parsed.employment.contract?.remainingDays)
        assertTrue(parsed.employment.contract?.expiringSoon == true)
        assertEquals("Operations", parsed.organization.department)
        assertEquals(14, parsed.organization.position.id)
        assertEquals("OPS-SPV", parsed.organization.position.code)
        assertNotNull(parsed.manager)
        assertEquals("Rina Wijaya", parsed.manager?.name)
        assertTrue(parsed.manager?.isActive == true)
        assertTrue(parsed.contact.googleLinked)
        assertEquals(31, parsed.personal.age)
        assertEquals("Rp 12.500.000", parsed.payroll.basicSalaryLabel)
        assertEquals("BCA", parsed.payroll.bankName)
        assertEquals("****4821", parsed.payroll.bankAccountMasked)
        assertEquals(14.0, parsed.leave.summary.total, 0.01)
        assertEquals(1, parsed.leave.balances.size)
        assertEquals(4, parsed.documents.verified)
        assertEquals(1, parsed.documents.expiringSoon)
        assertTrue(parsed.permissions.canApprove)
        assertTrue(parsed.permissions.isManager)
        assertEquals(listOf("nickname", "phone", "address"), parsed.permissions.editableFields)
        assertTrue(parsed.permissions.emailChangeRequiresHr)
    }

    @Test
    fun testParseEmployeeProfileNullManager() {
        val json = """
            {
              "success": true,
              "meta": { "year": 2025 },
              "data": {
                "profile": { "nik": "AR-01", "full_name": "Siti Aminah", "role": "STAFF" },
                "manager": null,
                "contact": { "company_email": "siti@asiaraya.co.id", "google_linked": false }
              }
            }
        """.trimIndent()

        val parsed = EmployeeProfileParser.parse(json, 2025)
        assertNotNull(parsed)
        assertEquals(2025, parsed!!.metaYear)
        assertNull(parsed.manager)
        assertEquals("siti@asiaraya.co.id", parsed.contact.companyEmail)
    }

    @Test
    fun testBackstackPushAndPopPreservesHistory() {
        val stack = mutableListOf<AppRoute>(AppRoute.MainTab(0))
        pushRoute(stack, AppRoute.Payslips)
        pushRoute(stack, AppRoute.PdfViewer("Slip Gaji", "SLP-01"))
        // Pushing the exact top route again should be a no-op
        pushRoute(stack, AppRoute.PdfViewer("Slip Gaji", "SLP-01"))

        assertEquals(3, stack.size)
        assertTrue(popRoute(stack))
        assertEquals(AppRoute.Payslips, stack.last())
        assertTrue(popRoute(stack))
        assertEquals(AppRoute.MainTab(0), stack.last())
        // Should not pop the root home anchor
        assertFalse(popRoute(stack))
        assertEquals(1, stack.size)
    }

    @Test
    fun testResolveHomeAttendanceStateAllCases() {
        val shift = ShiftDto(id = 1, code = "REG", name = "Reguler", startTime = "08:00", endTime = "17:00")

        // 1. Belum presensi + ada jadwal -> Presensi Masuk
        val notCheckedIn = AttendanceTodayDto(
            date = "2026-11-19",
            shift = shift,
            canCheckIn = true,
            canCheckOut = false,
            attendance = AttendanceRecordDto(date = "2026-11-19", checkedIn = false, checkedOut = false)
        )
        val s1 = resolveHomeAttendanceState(notCheckedIn, null, isLoading = false)
        assertEquals(HomeAttendanceAction.CHECK_IN, s1.action)
        assertEquals("Presensi Masuk", s1.buttonText)
        assertTrue(s1.buttonEnabled)

        // 2. Sudah presensi masuk + belum pulang -> Presensi Pulang
        val checkedInOnly = AttendanceTodayDto(
            date = "2026-11-19",
            shift = shift,
            canCheckIn = false,
            canCheckOut = true,
            attendance = AttendanceRecordDto(
                date = "2026-11-19",
                checkedIn = true,
                checkedOut = false,
                checkInAt = "2026-11-19T08:02:00+07:00"
            )
        )
        val s2 = resolveHomeAttendanceState(checkedInOnly, null, isLoading = false)
        assertEquals(HomeAttendanceAction.CHECK_OUT, s2.action)
        assertEquals("Presensi Pulang", s2.buttonText)
        assertTrue(s2.lastPunchLabel.contains("Masuk"))
        assertTrue(s2.buttonEnabled)

        // 3. Sudah presensi masuk & pulang -> Tidak Perlu Melakukan Presensi
        val checkedOut = AttendanceTodayDto(
            date = "2026-11-19",
            shift = shift,
            canCheckIn = false,
            canCheckOut = false,
            attendance = AttendanceRecordDto(
                date = "2026-11-19",
                checkedIn = true,
                checkedOut = true,
                checkInAt = "2026-11-19T08:02:00+07:00",
                checkOutAt = "2026-11-19T17:05:00+07:00"
            )
        )
        val s3 = resolveHomeAttendanceState(checkedOut, null, isLoading = false)
        assertEquals(HomeAttendanceAction.NO_NEED_ALREADY_DONE, s3.action)
        assertEquals("Tidak Perlu Melakukan Presensi", s3.buttonText)
        assertTrue(s3.lastPunchLabel.contains("Pulang"))
        assertFalse(s3.buttonEnabled)

        // 4. Tidak ada jadwal -> Tidak Perlu Melakukan Presensi
        val noSchedule = AttendanceTodayDto(
            date = "2026-11-22",
            shift = null,
            operationalType = "OFF",
            canCheckIn = false,
            canCheckOut = false,
            attendance = null
        )
        val s4 = resolveHomeAttendanceState(noSchedule, null, isLoading = false)
        assertEquals(HomeAttendanceAction.NO_NEED_NO_SCHEDULE, s4.action)
        assertEquals("Tidak Perlu Melakukan Presensi", s4.buttonText)
        assertFalse(s4.buttonEnabled)
    }

    @Test
    fun testFilterCurrentWeekAttendanceCrossMonthBoundary() {
        // Thursday, Dec 3, 2026 -> Week is Mon Nov 30, 2026 to Sun Dec 6, 2026
        val ref = Calendar.getInstance().apply {
            set(2026, Calendar.DECEMBER, 3, 10, 0, 0)
        }
        val range = getCurrentWeekRange(ref)
        assertEquals("2026-11-30", range.startDateStr)
        assertEquals("2026-12-06", range.endDateStr)
        assertEquals("2026-11", range.startMonthStr)
        assertEquals("2026-12", range.endMonthStr)

        val records = listOf(
            AttendanceRecordDto(id = 1, date = "2026-11-29"), // Sunday prev week -> excluded
            AttendanceRecordDto(id = 2, date = "2026-11-30"), // Monday -> included
            AttendanceRecordDto(id = 3, date = "2026-12-03"), // Thursday -> included
            AttendanceRecordDto(id = 4, date = "2026-12-06"), // Sunday -> included
            AttendanceRecordDto(id = 5, date = "2026-12-07")  // Monday next week -> excluded
        )

        val filtered = filterCurrentWeekAttendance(records, range)
        assertEquals(listOf("2026-12-06", "2026-12-03", "2026-11-30"), filtered.map { it.date })
    }
}
