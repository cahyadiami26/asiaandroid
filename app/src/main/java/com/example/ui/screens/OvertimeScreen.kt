package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.HrisApplication
import com.example.core.format.Formatters
import com.example.core.ui.components.CardSurface
import com.example.core.ui.components.EmptyState
import com.example.core.ui.components.KpiTile
import com.example.core.ui.components.ResponsiveContainer
import com.example.core.ui.components.SoftButton
import com.example.core.ui.components.StatusBadge
import com.example.core.ui.components.TealButton
import com.example.data.api.model.CreateOvertimePayload
import com.example.data.api.model.OvertimeDto
import com.example.data.repo.Resource
import com.example.ui.theme.Amber600
import com.example.ui.theme.Bg
import com.example.ui.theme.ButtonCornerShape
import com.example.ui.theme.DialogCornerShape
import com.example.ui.theme.Ink
import com.example.ui.theme.Ink2
import com.example.ui.theme.InputCornerShape
import com.example.ui.theme.Muted
import com.example.ui.theme.Muted2
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Rose600
import com.example.ui.theme.Teal500
import com.example.ui.theme.Teal600
import com.example.ui.theme.Teal700
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OvertimeScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HrisApplication
    val scope = rememberCoroutineScope()

    var overtimes by remember { mutableStateOf<List<OvertimeDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Dialog Create Overtime
    var showCreateDialog by remember { mutableStateOf(false) }
    var workDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("18:00") }
    var endTime by remember { mutableStateOf("21:00") }
    var reason by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val reload = {
        scope.launch {
            isLoading = true
            when (val res = app.repository.getOvertimes()) {
                is Resource.Success -> overtimes = res.data
                else -> {}
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        workDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        reload()
    }

    val totalHours = overtimes.filter { it.status == "APPROVED" }.sumOf { it.actualHours ?: it.plannedHours }
    val totalComp = overtimes.filter { it.status == "APPROVED" }.sumOf { it.compensationAmount }

    BackHandler(enabled = !showCreateDialog) { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengajuan Lembur", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Navy900, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Navy900)
                    }
                },
                actions = {
                    SoftButton(
                        text = "Ajukan",
                        leadingIcon = Icons.Default.Add,
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.padding(end = 12.dp)
                    )
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
            // KPI Summary Row (adaptive for narrow screens)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (maxWidth < 360.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KpiTile(label = "Pengajuan", value = "${overtimes.size}x", subValue = "Bulan ini", modifier = Modifier.weight(1f))
                            KpiTile(label = "Disetujui", value = Formatters.formatHours(totalHours), accentColor = Teal600, modifier = Modifier.weight(1f))
                        }
                        KpiTile(label = "Estimasi", value = Formatters.formatRupiahCompact(totalComp), accentColor = Amber600, modifier = Modifier.fillMaxWidth())
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiTile(label = "Pengajuan", value = "${overtimes.size}x", subValue = "Bulan ini", modifier = Modifier.weight(1f))
                        KpiTile(label = "Disetujui", value = Formatters.formatHours(totalHours), accentColor = Teal600, modifier = Modifier.weight(1f))
                        KpiTile(label = "Estimasi", value = Formatters.formatRupiahCompact(totalComp), accentColor = Amber600, modifier = Modifier.weight(1f))
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Teal600)
                }
            } else if (overtimes.isEmpty()) {
                EmptyState(
                    title = "Belum Ada Pengajuan Lembur",
                    body = "Ajukan lembur sebelum melaksanakan pekerjaan di luar jam operasional standar.",
                    ctaText = "Ajukan Lembur",
                    onCtaClick = { showCreateDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(overtimes) { ot ->
                        CardSurface(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = Formatters.formatDateLong(ot.workDate),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Waktu: ${ot.plannedStart} - ${ot.plannedEnd} (${Formatters.formatHours(ot.actualHours ?: ot.plannedHours)})",
                                        fontSize = 13.sp,
                                        color = Teal700,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (ot.compensationAmount > 0) {
                                        Text(
                                            text = "Kompensasi: ${Formatters.formatRupiah(ot.compensationAmount)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Ink2
                                        )
                                    }
                                    if (!ot.reason.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Alasan: ${ot.reason}",
                                            fontSize = 12.sp,
                                            color = Muted
                                        )
                                    }
                                }
                                StatusBadge(status = ot.status, customLabel = ot.statusLabel)
                            }

                            if (ot.status == "PENDING" || ot.status == "MANAGER_APPROVED") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            scope.launch {
                                                app.repository.cancelOvertime(ot.id)
                                                reload()
                                                Toast.makeText(context, "Pengajuan lembur dibatalkan.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Text("Batalkan", fontSize = 12.sp, color = Rose600)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
        }
    }

    // Modal Create Overtime Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showCreateDialog = false },
            shape = DialogCornerShape,
            containerColor = Color.White,
            title = {
                Text("Form Pengajuan Lembur", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy900)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Tanggal Kerja (YYYY-MM-DD)", fontSize = 12.sp, color = Ink2)
                    OutlinedTextField(
                        value = workDate,
                        onValueChange = { workDate = it },
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

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Jam Mulai", fontSize = 12.sp, color = Ink2)
                            OutlinedTextField(
                                value = startTime,
                                onValueChange = { startTime = it },
                                placeholder = { Text("18:00") },
                                singleLine = true,
                                shape = InputCornerShape,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Teal500,
                                    focusedTextColor = Ink,
                                    unfocusedTextColor = Ink
                                )
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Jam Selesai", fontSize = 12.sp, color = Ink2)
                            OutlinedTextField(
                                value = endTime,
                                onValueChange = { endTime = it },
                                placeholder = { Text("21:00") },
                                singleLine = true,
                                shape = InputCornerShape,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Teal500,
                                    focusedTextColor = Ink,
                                    unfocusedTextColor = Ink
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Alasan Lembur (Maks 255)", fontSize = 12.sp, color = Ink2)
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { if (it.length <= 255) reason = it },
                        placeholder = { Text("Pekerjaan atau deployment yang dilakukan", fontSize = 13.sp, color = Muted2) },
                        shape = InputCornerShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Teal500,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Kebijakan: Minimal 30 menit, maksimal 4 jam per hari. Estimasi kompensasi dihitung otomatis oleh sistem payroll.",
                        fontSize = 11.sp,
                        color = Muted,
                        lineHeight = 15.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reason.isBlank()) {
                            Toast.makeText(context, "Alasan lembur wajib diisi.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSubmitting = true
                        scope.launch {
                            val res = app.repository.createOvertime(
                                CreateOvertimePayload(
                                    workDate = workDate,
                                    plannedStart = startTime,
                                    plannedEnd = endTime,
                                    reason = reason
                                )
                            )
                            isSubmitting = false
                            showCreateDialog = false
                            reload()
                            when (res) {
                                is Resource.Success -> Toast.makeText(context, res.message ?: "Pengajuan lembur terkirim.", Toast.LENGTH_SHORT).show()
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
