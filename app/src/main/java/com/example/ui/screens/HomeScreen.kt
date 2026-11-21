package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.HrisApplication
import com.example.R
import com.example.core.format.Formatters
import com.example.core.ui.components.CardSurface
import com.example.core.ui.components.EmployeeAvatar
import com.example.core.ui.components.KpiTile
import com.example.core.ui.components.OfflineBanner
import com.example.core.ui.components.PrimaryButton
import com.example.core.ui.components.ResponsiveContainer
import com.example.core.ui.components.SectionCard
import com.example.core.ui.components.StatusBadge
import com.example.core.ui.components.TealButton
import com.example.data.api.model.AnnouncementDto
import com.example.data.api.model.AttendanceSummaryData
import com.example.data.api.model.AttendanceTodayDto
import com.example.data.api.model.AuthMeData
import com.example.data.api.model.EmployeeProfileData
import com.example.data.api.model.LeaveBalanceDto
import com.example.data.api.model.ScheduleTodayData
import com.example.data.repo.Resource
import com.example.ui.theme.Amber50
import com.example.ui.theme.Amber600
import com.example.ui.theme.Bg
import com.example.ui.theme.Bg2
import com.example.ui.theme.Ink
import com.example.ui.theme.Ink2
import com.example.ui.theme.Line
import com.example.ui.theme.Muted
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Teal50
import com.example.ui.theme.Teal600
import com.example.ui.theme.Teal700
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal enum class HomeAttendanceAction {
    CHECK_IN,
    CHECK_OUT,
    NO_NEED_ALREADY_DONE,
    NO_NEED_NO_SCHEDULE
}

internal data class HomeAttendanceUiState(
    val action: HomeAttendanceAction,
    val badgeStatus: String,
    val badgeLabel: String,
    val shiftLabel: String,
    val lastPunchLabel: String,
    val buttonText: String,
    val buttonEnabled: Boolean
)

internal fun resolveHomeAttendanceState(
    today: AttendanceTodayDto?,
    scheduleToday: ScheduleTodayData?,
    isLoading: Boolean
): HomeAttendanceUiState {
    val rec = today?.attendance
    val hasCheckedIn = rec?.checkedIn == true || !rec?.checkInAt.isNullOrBlank()
    val hasCheckedOut = rec?.checkedOut == true || !rec?.checkOutAt.isNullOrBlank()
    val opType = today?.operationalType?.uppercase(Locale.US)
    val recStatus = rec?.status?.uppercase(Locale.US)
    val shift = today?.shift ?: scheduleToday?.shift

    val isOffOrLeave = opType in setOf("OFF", "HOLIDAY", "LEAVE", "LIBUR", "CUTI", "NO_SHIFT", "NONE") ||
        recStatus in setOf("OFF", "LEAVE")
    val hasNoShiftToday = (today != null && shift == null && !today.canCheckIn && !today.canCheckOut && !hasCheckedIn && !hasCheckedOut) ||
        (!isLoading && today == null && scheduleToday != null && scheduleToday.shift == null && !hasCheckedIn && !hasCheckedOut)
    val noSchedule = isOffOrLeave || hasNoShiftToday

    val shiftLabel = when {
        noSchedule && (recStatus == "LEAVE" || opType in setOf("LEAVE", "CUTI")) ->
            "Jadwal: Sedang Cuti / Izin"
        noSchedule ->
            "Tidak ada jadwal kerja hari ini"
        shift != null -> {
            val shiftTitle = shift.name.ifBlank { shift.code }.ifBlank { "Kerja" }
            "Shift $shiftTitle: ${shift.startTime} - ${shift.endTime} WIB"
        }
        !today?.shiftStart.isNullOrBlank() && !today?.shiftEnd.isNullOrBlank() ->
            "Shift: ${today?.shiftStart} - ${today?.shiftEnd} WIB"
        else ->
            "Memuat jadwal hari ini..."
    }

    return when {
        hasCheckedOut -> {
            val outTime = Formatters.formatTimeOnly(rec?.checkOutAt)
            HomeAttendanceUiState(
                action = HomeAttendanceAction.NO_NEED_ALREADY_DONE,
                badgeStatus = rec?.status ?: "APPROVED",
                badgeLabel = "Sudah Presensi",
                shiftLabel = shiftLabel,
                lastPunchLabel = "Presensi terakhir hari ini: Pulang ($outTime WIB)",
                buttonText = "Tidak Perlu Melakukan Presensi",
                buttonEnabled = false
            )
        }
        hasCheckedIn -> {
            val inTime = Formatters.formatTimeOnly(rec?.checkInAt)
            HomeAttendanceUiState(
                action = HomeAttendanceAction.CHECK_OUT,
                badgeStatus = rec?.status ?: "PRESENT",
                badgeLabel = "Sudah Masuk",
                shiftLabel = shiftLabel,
                lastPunchLabel = "Presensi terakhir hari ini: Masuk ($inTime WIB)",
                buttonText = "Presensi Pulang",
                buttonEnabled = true
            )
        }
        noSchedule -> {
            val badgeText = if (recStatus == "LEAVE" || opType in setOf("LEAVE", "CUTI")) "Cuti" else "Tanpa Jadwal"
            HomeAttendanceUiState(
                action = HomeAttendanceAction.NO_NEED_NO_SCHEDULE,
                badgeStatus = rec?.status ?: "OFF",
                badgeLabel = badgeText,
                shiftLabel = shiftLabel,
                lastPunchLabel = "Tidak ada jadwal presensi hari ini",
                buttonText = "Tidak Perlu Melakukan Presensi",
                buttonEnabled = false
            )
        }
        else -> {
            HomeAttendanceUiState(
                action = HomeAttendanceAction.CHECK_IN,
                badgeStatus = rec?.status ?: "PENDING",
                badgeLabel = "Belum Presensi",
                shiftLabel = shiftLabel,
                lastPunchLabel = "Presensi terakhir hari ini: Belum ada presensi",
                buttonText = "Presensi Masuk",
                buttonEnabled = true
            )
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateToAttendance: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToLeave: () -> Unit,
    onNavigateToOvertime: () -> Unit,
    onNavigateToClaims: () -> Unit,
    onNavigateToPayslips: () -> Unit,
    onNavigateToApprovals: () -> Unit,
    onNavigateToAnnouncements: () -> Unit,
    onNavigateToAnnouncementDetail: (Int) -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as HrisApplication
    val scope = rememberCoroutineScope()

    var authMeData by remember { mutableStateOf<AuthMeData?>(null) }
    var profileData by remember { mutableStateOf<EmployeeProfileData?>(null) }
    var attendanceToday by remember { mutableStateOf<AttendanceTodayDto?>(null) }
    var scheduleToday by remember { mutableStateOf<ScheduleTodayData?>(null) }
    var announcements by remember { mutableStateOf<List<AnnouncementDto>>(emptyList()) }
    var attendanceSummary by remember { mutableStateOf<AttendanceSummaryData?>(null) }
    var leaveBalances by remember { mutableStateOf<List<LeaveBalanceDto>>(emptyList()) }
    var resolvedPhotoUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val queuedPunchCount by app.repository.queuedPunchCountFlow.collectAsState(initial = 0)
    val cachedEmployeeName by app.repository.employeeNameFlow.collectAsState(initial = null)

    val currentMonth = remember { SimpleDateFormat("yyyy-MM", Locale.US).format(Date()) }
    val currentYear = remember { java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) }

    val refreshData = {
        scope.launch {
            isLoading = true
            when (val profileRes = app.repository.getMyProfile(currentYear)) {
                is Resource.Success -> profileData = profileRes.data
                else -> {}
            }
            when (val meRes = app.repository.getMe()) {
                is Resource.Success -> authMeData = meRes.data
                else -> {}
            }
            when (val todayRes = app.repository.getAttendanceToday()) {
                is Resource.Success -> attendanceToday = todayRes.data
                else -> {}
            }
            when (val schRes = app.repository.getScheduleToday()) {
                is Resource.Success -> scheduleToday = schRes.data
                else -> {}
            }
            when (val annRes = app.repository.getAnnouncements()) {
                is Resource.Success -> announcements = annRes.data.take(3)
                else -> {}
            }
            when (val attRes = app.repository.getAttendanceSummary(currentMonth)) {
                is Resource.Success -> attendanceSummary = attRes.data
                else -> {}
            }
            when (val leaveRes = app.repository.getLeaveBalances(currentYear)) {
                is Resource.Success -> leaveBalances = leaveRes.data
                else -> {}
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    val rawPhotoPath = profileData?.profile?.photoUrl ?: authMeData?.employee?.photoUrl
    LaunchedEffect(rawPhotoPath) {
        resolvedPhotoUrl = if (!rawPhotoPath.isNullOrBlank()) {
            app.repository.resolveMediaUrl(rawPhotoPath)
        } else {
            null
        }
    }

    val resolvedEmployeeName = profileData?.profile?.fullName?.takeIf { it.isNotBlank() && it != "-" }
        ?: authMeData?.employee?.fullName?.takeIf { it.isNotBlank() }
        ?: cachedEmployeeName?.takeIf { it.isNotBlank() }

    val resolvedPosition = profileData?.organization?.position?.name?.takeIf { it.isNotBlank() && it != "-" }
        ?: authMeData?.employee?.position?.takeIf { it.isNotBlank() }
    val resolvedDepartment = profileData?.organization?.department?.takeIf { it.isNotBlank() && it != "-" }
        ?: authMeData?.employee?.department?.takeIf { it.isNotBlank() }
    val resolvedRoleLabel = profileData?.profile?.roleLabel?.takeIf { it.isNotBlank() && it != "-" }
    val resolvedSubtitle = listOfNotNull(resolvedPosition, resolvedDepartment)
        .joinToString(" • ")
        .ifEmpty { resolvedRoleLabel ?: "PT Asia Raya Group" }

    val todayFormatted = remember {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        sdf.format(Date())
    }

    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..10 -> "Selamat pagi"
            in 11..14 -> "Selamat siang"
            in 15..18 -> "Selamat sore"
            else -> "Selamat malam"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        if (queuedPunchCount > 0) {
            OfflineBanner(
                message = "$queuedPunchCount presensi menunggu tersimpan di perangkat (offline)"
            )
        }

        // Header with Background Image from "Beranda" to Date
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_home_header_bg),
                contentDescription = "Latar Belakang Header Beranda",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                com.example.ui.theme.Navy950.copy(alpha = 0.90f),
                                Navy900.copy(alpha = 0.86f),
                                Navy800.copy(alpha = 0.82f)
                            )
                        )
                    )
            )
            ResponsiveContainer(maxWidth = 760.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 20.dp)
                ) {
                // Baris Atas: Teks "Beranda" & Ikon Aksi (Notifikasi & Profil)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Beranda",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateToNotifications) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = com.example.ui.theme.Rose600, modifier = Modifier.size(6.dp))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifikasi",
                                    tint = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = onNavigateToProfile) {
                            HomeProfileAvatar(
                                name = resolvedEmployeeName,
                                photoUrl = resolvedPhotoUrl,
                                sizeDp = 34
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sapaan & Nama Karyawan dari data profil
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$greeting,",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                        Text(
                            text = resolvedEmployeeName ?: "Memuat profil...",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("home_employee_name")
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = resolvedSubtitle,
                            fontSize = 12.sp,
                            color = Teal50,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    HomeProfileAvatar(
                        name = resolvedEmployeeName,
                        photoUrl = resolvedPhotoUrl,
                        sizeDp = 46
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Teks Tanggal Hari Ini
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = todayFormatted,
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            }
        }

        ResponsiveContainer(maxWidth = 760.dp) {
            Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(14.dp))

        // Main Attendance Punch Card
        val effectiveToday = attendanceToday ?: authMeData?.attendanceToday
        val todayAttendance = effectiveToday?.attendance
        val attendanceUi = resolveHomeAttendanceState(
            today = effectiveToday,
            scheduleToday = scheduleToday,
            isLoading = isLoading
        )

        CardSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Teal50),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = Teal700,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Presensi Hari Ini",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink
                        )
                        if (!resolvedEmployeeName.isNullOrBlank()) {
                            Text(
                                text = resolvedEmployeeName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Teal700,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = attendanceUi.shiftLabel,
                            fontSize = 12.sp,
                            color = Muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = attendanceUi.badgeStatus, customLabel = attendanceUi.badgeLabel)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(com.example.ui.theme.BadgeCornerShape)
                    .background(Bg2)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MASUK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Muted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Formatters.formatTimeOnly(todayAttendance?.checkInAt),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink
                    )
                }
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                        .background(Line)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PULANG", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Muted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Formatters.formatTimeOnly(todayAttendance?.checkOutAt),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = attendanceUi.lastPunchLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Ink2,
                modifier = Modifier.testTag("home_last_punch_label")
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (attendanceUi.action) {
                HomeAttendanceAction.CHECK_IN -> {
                    TealButton(
                        text = attendanceUi.buttonText,
                        onClick = onNavigateToAttendance,
                        leadingIcon = Icons.Default.Fingerprint,
                        modifier = Modifier.testTag("home_attendance_button")
                    )
                }
                HomeAttendanceAction.CHECK_OUT -> {
                    PrimaryButton(
                        text = attendanceUi.buttonText,
                        onClick = onNavigateToAttendance,
                        leadingIcon = Icons.Default.Fingerprint,
                        modifier = Modifier.testTag("home_attendance_button")
                    )
                }
                HomeAttendanceAction.NO_NEED_ALREADY_DONE,
                HomeAttendanceAction.NO_NEED_NO_SCHEDULE -> {
                    TealButton(
                        text = attendanceUi.buttonText,
                        onClick = {},
                        enabled = false,
                        leadingIcon = Icons.Default.CheckCircle,
                        modifier = Modifier.testTag("home_attendance_button")
                    )
                }
            }
        }

        // Pending Approvals Card (If user can approve)
        val pendingApprovalCount = authMeData?.counters?.get("approvals_pending") ?: 0
        if (authMeData?.permissions?.canApprove == true && pendingApprovalCount > 0) {
            Spacer(modifier = Modifier.height(14.dp))
            CardSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onNavigateToApprovals() },
                backgroundColor = Amber50,
                borderColor = Amber600.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Amber600.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AssignmentTurnedIn,
                                contentDescription = null,
                                tint = Amber600,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Kotak Persetujuan Anda",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink
                            )
                            Text(
                                text = "$pendingApprovalCount pengajuan menunggu keputusan",
                                fontSize = 12.sp,
                                color = Amber600,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Amber600
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Shortcuts Grid
        Text(
            text = "PINTASAN CEPAT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Muted,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        val fontScale = LocalDensity.current.fontScale
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            val useTwoByTwoShortcuts = maxWidth < 360.dp || fontScale > 1.15f
            if (useTwoByTwoShortcuts) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ShortcutItem(
                            title = "Slip Gaji",
                            icon = Icons.Default.Payments,
                            color = Teal600,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToPayslips
                        )
                        ShortcutItem(
                            title = "Ajukan Cuti",
                            icon = Icons.Default.DateRange,
                            color = Navy800,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToLeave
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ShortcutItem(
                            title = "Lembur",
                            icon = Icons.Default.WorkHistory,
                            color = Amber600,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToOvertime
                        )
                        ShortcutItem(
                            title = "Klaim",
                            icon = Icons.Default.ReceiptLong,
                            color = Teal700,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToClaims
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ShortcutItem(
                        title = "Slip Gaji",
                        icon = Icons.Default.Payments,
                        color = Teal600,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToPayslips
                    )
                    ShortcutItem(
                        title = "Ajukan Cuti",
                        icon = Icons.Default.DateRange,
                        color = Navy800,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToLeave
                    )
                    ShortcutItem(
                        title = "Lembur",
                        icon = Icons.Default.WorkHistory,
                        color = Amber600,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToOvertime
                    )
                    ShortcutItem(
                        title = "Klaim",
                        icon = Icons.Default.ReceiptLong,
                        color = Teal700,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToClaims
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // KPI Grid
        val annualLeave = leaveBalances.firstOrNull { it.typeCode.equals("ANNUAL", ignoreCase = true) }
            ?: leaveBalances.firstOrNull()
        val remainingLeaveDays = annualLeave?.remaining?.toInt()
            ?: authMeData?.counters?.get("leave_balance")
            ?: 0
        val quotaLeaveDays = annualLeave?.quota?.toInt()

        val activeRequestsCount = (authMeData?.counters?.get("leave_open") ?: 0) +
            (authMeData?.counters?.get("claims_open") ?: 0) +
            (authMeData?.counters?.get("overtime_open") ?: 0)

        Text(
            text = "RINGKASAN BULAN INI",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Muted,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            val useFourColKpi = maxWidth >= 600.dp && fontScale <= 1.15f
            if (useFourColKpi) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiTile(
                        label = "Hadir",
                        value = "${attendanceSummary?.present ?: 0} hari",
                        subValue = if ((attendanceSummary?.workHours ?: 0.0) > 0.0) "${attendanceSummary?.workHours} jam kerja" else null,
                        icon = Icons.Default.AccessTime,
                        modifier = Modifier.weight(1f)
                    )
                    KpiTile(
                        label = "Terlambat",
                        value = "${attendanceSummary?.late ?: 0}x",
                        subValue = if ((attendanceSummary?.lateMinutes ?: 0) > 0) "${attendanceSummary?.lateMinutes} menit" else null,
                        accentColor = Amber600,
                        modifier = Modifier.weight(1f)
                    )
                    KpiTile(
                        label = "Sisa Cuti",
                        value = "$remainingLeaveDays hari",
                        subValue = quotaLeaveDays?.let { "Dari kuota $it" },
                        modifier = Modifier.weight(1f)
                    )
                    KpiTile(
                        label = "Pengajuan",
                        value = "$activeRequestsCount aktif",
                        subValue = "Dalam proses",
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiTile(
                            label = "Hadir",
                            value = "${attendanceSummary?.present ?: 0} hari",
                            subValue = if ((attendanceSummary?.workHours ?: 0.0) > 0.0) "${attendanceSummary?.workHours} jam kerja" else null,
                            icon = Icons.Default.AccessTime,
                            modifier = Modifier.weight(1f)
                        )
                        KpiTile(
                            label = "Terlambat",
                            value = "${attendanceSummary?.late ?: 0}x",
                            subValue = if ((attendanceSummary?.lateMinutes ?: 0) > 0) "${attendanceSummary?.lateMinutes} menit" else null,
                            accentColor = Amber600,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiTile(
                            label = "Sisa Cuti",
                            value = "$remainingLeaveDays hari",
                            subValue = quotaLeaveDays?.let { "Dari kuota $it" },
                            modifier = Modifier.weight(1f)
                        )
                        KpiTile(
                            label = "Pengajuan",
                            value = "$activeRequestsCount aktif",
                            subValue = "Dalam proses",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Latest Announcements
        SectionCard(
            title = "Pengumuman Perusahaan",
            subtitle = "Informasi resmi PT Asia Raya Group",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            action = {
                TextButton(onClick = onNavigateToAnnouncements) {
                    Text("Semua", fontSize = 12.sp, color = Teal700, fontWeight = FontWeight.SemiBold)
                }
            }
        ) {
            if (announcements.isEmpty()) {
                Text(
                    text = "Tidak ada pengumuman baru.",
                    fontSize = 13.sp,
                    color = Muted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                announcements.forEachIndexed { index, item ->
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Line)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToAnnouncementDetail(item.id) }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (item.isPinned) {
                                    Text("📌 ", fontSize = 12.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(com.example.ui.theme.BadgeCornerShape)
                                        .background(Teal50)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.category,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Teal700
                                    )
                                }
                            }
                            Text(
                                text = Formatters.formatDateShort(item.publishedAt?.take(10)),
                                fontSize = 11.sp,
                                color = Muted
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
            }
        }
    }
}

@Composable
private fun HomeProfileAvatar(
    name: String?,
    photoUrl: String?,
    sizeDp: Int
) {
    if (!photoUrl.isNullOrBlank()) {
        AsyncImage(
            model = photoUrl,
            contentDescription = "Foto Profil ${name ?: ""}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(CircleShape)
                .background(Teal600)
        )
    } else {
        EmployeeAvatar(
            name = name,
            size = sizeDp,
            backgroundColor = Teal600
        )
    }
}

@Composable
fun ShortcutItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    CardSurface(
        modifier = modifier.clickable(onClick = onClick),
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
