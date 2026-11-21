package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.HrisApplication
import com.example.core.format.Formatters
import com.example.core.ui.components.CardSurface
import com.example.core.ui.components.EmptyState
import com.example.core.ui.components.KpiTile
import com.example.core.ui.components.ResponsiveContainer
import com.example.core.ui.components.StatusBadge
import com.example.data.api.model.AttendanceRecordDto
import com.example.data.api.model.AttendanceSummaryData
import com.example.data.repo.Resource
import com.example.ui.theme.Amber600
import com.example.ui.theme.Bg
import com.example.ui.theme.Bg2
import com.example.ui.theme.Ink
import com.example.ui.theme.Line
import com.example.ui.theme.Muted
import com.example.ui.theme.Navy900
import com.example.ui.theme.Rose600
import com.example.ui.theme.Teal600
import com.example.ui.theme.Teal700
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

internal data class WeekRange(
    val startDateStr: String,
    val endDateStr: String,
    val startMonthStr: String,
    val endMonthStr: String,
    val label: String
)

internal fun getCurrentWeekRange(reference: Calendar = Calendar.getInstance()): WeekRange {
    val start = (reference.clone() as Calendar).apply {
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        val dow = get(Calendar.DAY_OF_WEEK)
        val diffFromMonday = if (dow == Calendar.SUNDAY) -6 else Calendar.MONDAY - dow
        add(Calendar.DAY_OF_MONTH, diffFromMonday)
    }
    val end = (start.clone() as Calendar).apply {
        add(Calendar.DAY_OF_MONTH, 6)
    }

    val sdfIso = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val sdfMonth = SimpleDateFormat("yyyy-MM", Locale.US)
    val localeId = Locale("id", "ID")
    val sdfDayMonth = SimpleDateFormat("dd MMM", localeId)
    val sdfFull = SimpleDateFormat("dd MMM yyyy", localeId)

    return WeekRange(
        startDateStr = sdfIso.format(start.time),
        endDateStr = sdfIso.format(end.time),
        startMonthStr = sdfMonth.format(start.time),
        endMonthStr = sdfMonth.format(end.time),
        label = "${sdfDayMonth.format(start.time)} – ${sdfFull.format(end.time)}"
    )
}

internal fun filterCurrentWeekAttendance(
    records: List<AttendanceRecordDto>,
    weekRange: WeekRange = getCurrentWeekRange()
): List<AttendanceRecordDto> {
    return records
        .filter { rec ->
            val d = rec.date.take(10)
            d.length == 10 && d >= weekRange.startDateStr && d <= weekRange.endDateStr
        }
        .distinctBy { it.date.take(10) }
        .sortedByDescending { it.date.take(10) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceHistoryScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HrisApplication
    val scope = rememberCoroutineScope()

    val calendar = remember { Calendar.getInstance() }
    var currentMonthStr by remember {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        mutableStateOf(sdf.format(calendar.time))
    }
    var currentMonthLabel by remember {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        mutableStateOf(sdf.format(calendar.time))
    }

    val weekRange = remember { getCurrentWeekRange() }
    var weeklyHistoryList by remember { mutableStateOf<List<AttendanceRecordDto>>(emptyList()) }
    var summaryData by remember { mutableStateOf<AttendanceSummaryData?>(null) }
    var isLoadingWeekly by remember { mutableStateOf(true) }

    val loadMonthSummary = { month: String ->
        scope.launch {
            when (val sumRes = app.repository.getAttendanceSummary(month)) {
                is Resource.Success -> summaryData = sumRes.data
                else -> summaryData = null
            }
        }
    }

    LaunchedEffect(currentMonthStr) {
        loadMonthSummary(currentMonthStr)
    }

    LaunchedEffect(weekRange.startDateStr, weekRange.endDateStr) {
        isLoadingWeekly = true
        val combined = mutableListOf<AttendanceRecordDto>()
        when (val resStart = app.repository.getAttendanceHistory(weekRange.startMonthStr)) {
            is Resource.Success -> combined.addAll(resStart.data)
            else -> {}
        }
        if (weekRange.endMonthStr != weekRange.startMonthStr) {
            when (val resEnd = app.repository.getAttendanceHistory(weekRange.endMonthStr)) {
                is Resource.Success -> combined.addAll(resEnd.data)
                else -> {}
            }
        }
        weeklyHistoryList = filterCurrentWeekAttendance(combined, weekRange)
        isLoadingWeekly = false
    }

    val changeMonth: (Int) -> Unit = { delta ->
        calendar.add(Calendar.MONTH, delta)
        val sdfVal = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val sdfLbl = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        currentMonthStr = sdfVal.format(calendar.time)
        currentMonthLabel = sdfLbl.format(calendar.time)
    }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat & Rekap Presensi", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Navy900, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Navy900)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Bg
    ) { innerPadding ->
        ResponsiveContainer(
            maxWidth = 760.dp,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
            // Month Picker Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { changeMonth(-1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Bulan Sebelumnya", tint = Ink)
                }
                Text(
                    text = currentMonthLabel,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Navy900
                )
                IconButton(onClick = { changeMonth(1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Bulan Berikutnya", tint = Ink)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 KPI Summary Grid (2x2 on compact phones, 4 cols on tablet)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                val presentVal = "${summaryData?.present ?: 0}"
                val lateVal = "${summaryData?.late ?: 0}"
                val lateSub = "${summaryData?.lateMinutes ?: 0} mnt"
                val absentVal = "${summaryData?.absent ?: 0}"
                val workHoursVal = "${(summaryData?.workHours ?: 0.0).toInt()}j"

                if (maxWidth >= 560.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KpiTile(label = "Hadir", value = presentVal, subValue = "hari kerja", modifier = Modifier.weight(1f))
                        KpiTile(label = "Terlambat", value = lateVal, subValue = lateSub, accentColor = Amber600, modifier = Modifier.weight(1f))
                        KpiTile(label = "Absen", value = absentVal, subValue = "hari", accentColor = Rose600, modifier = Modifier.weight(1f))
                        KpiTile(label = "Jam Kerja", value = workHoursVal, subValue = "total", modifier = Modifier.weight(1f))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KpiTile(label = "Hadir", value = presentVal, subValue = "hari kerja", modifier = Modifier.weight(1f))
                            KpiTile(label = "Terlambat", value = lateVal, subValue = lateSub, accentColor = Amber600, modifier = Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KpiTile(label = "Absen", value = absentVal, subValue = "hari", accentColor = Rose600, modifier = Modifier.weight(1f))
                            KpiTile(label = "Jam Kerja", value = workHoursVal, subValue = "total", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Section Header: Presensi Minggu Berjalan
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRESENSI MINGGU BERJALAN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Muted
                )
                Text(
                    text = weekRange.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Teal700
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Current Week Attendance List Only
            if (isLoadingWeekly) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Teal600)
                }
            } else if (weeklyHistoryList.isEmpty()) {
                EmptyState(
                    title = "Belum Ada Presensi Minggu Ini",
                    body = "Belum ada catatan presensi pada minggu berjalan (${weekRange.label})."
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(weeklyHistoryList) { item ->
                        CardSurface(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = Formatters.formatDateLong(item.date),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Ink
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Masuk: ${Formatters.formatTimeOnly(item.checkInAt)}",
                                            fontSize = 12.sp,
                                            color = Muted
                                        )
                                        Text(" • ", fontSize = 12.sp, color = Muted)
                                        Text(
                                            text = "Pulang: ${Formatters.formatTimeOnly(item.checkOutAt)}",
                                            fontSize = 12.sp,
                                            color = Muted
                                        )
                                    }
                                    if (item.lateMinutes > 0) {
                                        Text(
                                            text = "Terlambat ${item.lateMinutes} menit",
                                            fontSize = 11.sp,
                                            color = Amber600,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(status = item.status)
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            }
        }
    }
}
