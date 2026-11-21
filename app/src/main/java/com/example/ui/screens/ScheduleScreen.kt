package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.core.ui.components.DangerButton
import com.example.core.ui.components.EmptyState
import com.example.core.ui.components.HrisDropdown
import com.example.core.ui.components.ResponsiveContainer
import com.example.core.ui.components.SectionCard
import com.example.core.ui.components.SoftButton
import com.example.core.ui.components.StatusBadge
import com.example.core.ui.components.TealButton
import com.example.data.api.model.CreateScheduleRequestPayload
import com.example.data.api.model.ScheduleDayDto
import com.example.data.api.model.ScheduleRequestDto
import com.example.data.api.model.ScheduleTodayData
import com.example.data.api.model.ShiftDto
import com.example.data.repo.Resource
import com.example.ui.theme.BadgeCornerShape
import com.example.ui.theme.Bg
import com.example.ui.theme.Bg2
import com.example.ui.theme.ButtonCornerShape
import com.example.ui.theme.CardCornerShape
import com.example.ui.theme.DialogCornerShape
import com.example.ui.theme.Ink
import com.example.ui.theme.Ink2
import com.example.ui.theme.InputCornerShape
import com.example.ui.theme.Line
import com.example.ui.theme.LineStrong
import com.example.ui.theme.Muted
import com.example.ui.theme.Muted2
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate600
import com.example.ui.theme.Teal50
import com.example.ui.theme.Teal500
import com.example.ui.theme.Teal600
import com.example.ui.theme.Teal700
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScheduleScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as HrisApplication
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Jadwal Kerja, 1: Pengajuan Perubahan

    var scheduleTodayData by remember { mutableStateOf<ScheduleTodayData?>(null) }
    var availableShifts by remember { mutableStateOf<List<ShiftDto>>(emptyList()) }
    var scheduleRequests by remember { mutableStateOf<List<ScheduleRequestDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Dialog Create Request
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedShift by remember { mutableStateOf<ShiftDto?>(null) }
    var startDateInput by remember { mutableStateOf("") }
    var endDateInput by remember { mutableStateOf("") }
    var reasonInput by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val reloadAll = {
        scope.launch {
            isLoading = true
            when (val res = app.repository.getScheduleToday()) {
                is Resource.Success -> scheduleTodayData = res.data
                else -> {}
            }
            when (val sftRes = app.repository.getShifts()) {
                is Resource.Success -> {
                    availableShifts = sftRes.data
                    if (selectedShift == null && sftRes.data.isNotEmpty()) {
                        selectedShift = sftRes.data.firstOrNull { it.id == 2 } ?: sftRes.data.first()
                    }
                }
                else -> {}
            }
            when (val reqRes = app.repository.getScheduleRequests()) {
                is Resource.Success -> scheduleRequests = reqRes.data
                else -> {}
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        val nowStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        startDateInput = nowStr
        endDateInput = nowStr
        reloadAll()
    }

    BackHandler(enabled = selectedTab != 0 && !showCreateDialog) {
        selectedTab = 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        // Tab Header
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Navy900,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Teal600
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "Jadwal Kerja",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "Pengajuan (${scheduleRequests.size})",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Teal600)
            }
        } else if (selectedTab == 0) {
            // Tab 0: Jadwal Kerja
            ResponsiveContainer(maxWidth = 760.dp, modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Today Shift Hero Card
                item {
                    val shift = scheduleTodayData?.shift
                    CardSurface(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SHIFT HARI INI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Muted
                            )
                            Box(
                                modifier = Modifier
                                    .clip(BadgeCornerShape)
                                    .background(Teal50)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = shift?.operationalType ?: "NORMAL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Teal700
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = shift?.name ?: "Shift Pagi Regular",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Jam Kerja: ${shift?.startTime ?: "08:00"} - ${shift?.endTime ?: "17:00"} WIB",
                            fontSize = 13.sp,
                            color = Ink2
                        )
                        Text(
                            text = "Istirahat: ${shift?.breakMinutes ?: 60} menit • Toleransi: ${shift?.lateTolerance ?: 15} menit",
                            fontSize = 12.sp,
                            color = Muted
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SoftButton(
                            text = "Ajukan Perubahan Jadwal",
                            leadingIcon = Icons.Default.CalendarMonth,
                            onClick = { showCreateDialog = true }
                        )
                    }
                }

                // 7 Days Schedule Strip
                item {
                    Text(
                        text = "JADWAL 7 HARI KE DEPAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Muted,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items(scheduleTodayData?.nextDays ?: emptyList()) { day ->
                    CardSurface(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = if (day.weekend) Slate50 else Color.White
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (day.weekend) LineStrong else Teal50),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = if (day.weekend) Slate600 else Teal700,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = day.date,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (day.weekend) Slate600 else Ink
                                    )
                                    Text(
                                        text = if (day.weekend) "Hari Libur" else "${day.shift?.name ?: "Shift Pagi"} (${day.shift?.startTime} - ${day.shift?.endTime})",
                                        fontSize = 12.sp,
                                        color = Muted
                                    )
                                }
                            }
                            StatusBadge(
                                status = if (day.weekend) "OFF" else "NORMAL",
                                customLabel = if (day.weekend) "Libur" else "Kerja"
                            )
                        }
                    }
                }
            }
            }
        } else {
            // Tab 1: Pengajuan Perubahan Jadwal
            ResponsiveContainer(maxWidth = 760.dp, modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Riwayat Pengajuan",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink
                    )
                    SoftButton(
                        text = "Ajukan Baru",
                        leadingIcon = Icons.Default.Add,
                        onClick = { showCreateDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (scheduleRequests.isEmpty()) {
                    EmptyState(
                        title = "Belum Ada Pengajuan",
                        body = "Anda belum memiliki riwayat pengajuan perubahan shift kerja.",
                        ctaText = "Buat Pengajuan",
                        onCtaClick = { showCreateDialog = true }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(scheduleRequests) { req ->
                            CardSurface(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Perubahan: ${req.startDate} s/d ${req.endDate}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Ink
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Shift Baru: ${req.requestedShift?.name ?: "Shift Siang"}",
                                            fontSize = 12.sp,
                                            color = Teal700,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (!req.reason.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Alasan: ${req.reason}",
                                                fontSize = 12.sp,
                                                color = Muted
                                            )
                                        }
                                    }
                                    StatusBadge(status = req.status, customLabel = req.statusLabel)
                                }

                                if (req.status == "PENDING") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = {
                                                scope.launch {
                                                    app.repository.cancelScheduleRequest(req.id)
                                                    reloadAll()
                                                    Toast.makeText(context, "Pengajuan dibatalkan.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        ) {
                                            Text("Batalkan", fontSize = 12.sp, color = com.example.ui.theme.Rose600)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }

    // Modal Create Schedule Request Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showCreateDialog = false },
            shape = DialogCornerShape,
            containerColor = Color.White,
            title = {
                Text("Ajukan Perubahan Jadwal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy900)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    HrisDropdown(
                        label = "Shift Pengganti",
                        items = availableShifts,
                        selectedItem = selectedShift,
                        onItemSelected = { selectedShift = it },
                        itemLabel = { "${it.name} (${it.startTime} - ${it.endTime})" }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Tanggal Mulai (YYYY-MM-DD)", fontSize = 12.sp, color = Ink2)
                    OutlinedTextField(
                        value = startDateInput,
                        onValueChange = { startDateInput = it },
                        singleLine = true,
                        shape = InputCornerShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Teal500,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Tanggal Selesai (YYYY-MM-DD)", fontSize = 12.sp, color = Ink2)
                    OutlinedTextField(
                        value = endDateInput,
                        onValueChange = { endDateInput = it },
                        singleLine = true,
                        shape = InputCornerShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Teal500,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Alasan Pengajuan (Maks 255)", fontSize = 12.sp, color = Ink2)
                    OutlinedTextField(
                        value = reasonInput,
                        onValueChange = { if (it.length <= 255) reasonInput = it },
                        placeholder = { Text("Misal: Penyesuaian tugas maintenance", fontSize = 13.sp, color = Muted2) },
                        shape = InputCornerShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Teal500,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedShift == null) {
                            Toast.makeText(context, "Pilih shift terlebih dahulu", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSubmitting = true
                        scope.launch {
                            val res = app.repository.createScheduleRequest(
                                CreateScheduleRequestPayload(
                                    requestedShiftId = selectedShift!!.id,
                                    startDate = startDateInput,
                                    endDate = endDateInput,
                                    reason = reasonInput.ifBlank { null }
                                )
                            )
                            isSubmitting = false
                            showCreateDialog = false
                            reloadAll()
                            when (res) {
                                is Resource.Success -> Toast.makeText(context, res.message ?: "Pengajuan dikirim.", Toast.LENGTH_SHORT).show()
                                is Resource.Error -> Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800, contentColor = Color.White),
                    shape = ButtonCornerShape,
                    enabled = !isSubmitting
                ) {
                    Text("Kirim Pengajuan", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCreateDialog = false },
                    enabled = !isSubmitting
                ) {
                    Text("Batal", fontSize = 13.sp, color = Muted)
                }
            }
        )
    }
}
