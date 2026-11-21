package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import com.example.core.ui.components.EmptyState
import com.example.core.ui.components.HrisDropdown
import com.example.core.ui.components.ResponsiveContainer
import com.example.core.ui.components.SoftButton
import com.example.core.ui.components.StatusBadge
import com.example.core.ui.components.TealButton
import com.example.data.api.model.CreateLeavePayload
import com.example.data.api.model.LeaveBalanceDto
import com.example.data.api.model.LeaveDto
import com.example.data.api.model.LeaveTypeDto
import com.example.data.repo.Resource
import com.example.ui.theme.BadgeCornerShape
import com.example.ui.theme.Bg
import com.example.ui.theme.Bg2
import com.example.ui.theme.ButtonCornerShape
import com.example.ui.theme.DialogCornerShape
import com.example.ui.theme.Ink
import com.example.ui.theme.Ink2
import com.example.ui.theme.InputCornerShape
import com.example.ui.theme.Line
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

@Composable
fun LeaveScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as HrisApplication
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Saldo Cuti, 1: Riwayat Cuti

    var balances by remember { mutableStateOf<List<LeaveBalanceDto>>(emptyList()) }
    var leaves by remember { mutableStateOf<List<LeaveDto>>(emptyList()) }
    var leaveTypes by remember { mutableStateOf<List<LeaveTypeDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Create Leave Dialog State
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf<LeaveTypeDto?>(null) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var hasAttachment by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val currentYear = remember { java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) }

    BackHandler(enabled = selectedTab != 0 && !showCreateDialog) {
        selectedTab = 0
    }

    val reload = {
        scope.launch {
            isLoading = true
            when (val bRes = app.repository.getLeaveBalances(currentYear)) {
                is Resource.Success -> balances = bRes.data
                else -> {}
            }
            when (val lRes = app.repository.getLeaves()) {
                is Resource.Success -> leaves = lRes.data
                else -> {}
            }
            when (val tRes = app.repository.getLeaveTypes()) {
                is Resource.Success -> {
                    leaveTypes = tRes.data
                    if (selectedType == null && tRes.data.isNotEmpty()) {
                        selectedType = tRes.data.first()
                    }
                }
                else -> {}
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        val nowStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        startDate = nowStr
        endDate = nowStr
        reload()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Teal600,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajukan Cuti")
            }
        },
        containerColor = Bg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row
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
                            "Saldo Cuti",
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
                            "Riwayat (${leaves.size})",
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
                // Tab 0: Saldo Cuti
                ResponsiveContainer(maxWidth = 760.dp, modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "SALDO CUTI PERIODE TAHUN $currentYear",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Muted,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(balances) { bal ->
                        CardSurface(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = bal.typeName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink
                                )
                                Text(
                                    text = "Sisa: ${Formatters.formatDays(bal.remaining)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Teal700
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Progress Bar
                            LinearProgressIndicator(
                                progress = { bal.progress.toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(BadgeCornerShape),
                                color = Teal600,
                                trackColor = Bg2
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Kuota: ${bal.quota.toInt()} hari",
                                    fontSize = 12.sp,
                                    color = Muted
                                )
                                Text(
                                    text = "Terpakai: ${bal.used.toInt()} hari",
                                    fontSize = 12.sp,
                                    color = Muted
                                )
                                Text(
                                    text = "Carry Over: ${bal.carryOver.toInt()} hari",
                                    fontSize = 12.sp,
                                    color = Muted
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(60.dp)) // Space for FAB
                    }
                }
                }
            } else {
                // Tab 1: Riwayat Cuti
                ResponsiveContainer(maxWidth = 760.dp, modifier = Modifier.fillMaxSize()) {
                if (leaves.isEmpty()) {
                    EmptyState(
                        title = "Belum Ada Pengajuan Cuti",
                        body = "Sentuh tombol tambah (+) di bawah untuk mengajukan cuti baru.",
                        ctaText = "Ajukan Cuti",
                        onCtaClick = { showCreateDialog = true }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(leaves) { leave ->
                            CardSurface(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = leave.type?.name ?: "Cuti Tahunan",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Ink
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${Formatters.formatDateShort(leave.startDate)} s/d ${Formatters.formatDateShort(leave.endDate)} (${Formatters.formatDays(leave.days)})",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Teal700
                                        )
                                        if (!leave.reason.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Alasan: ${leave.reason}",
                                                fontSize = 12.sp,
                                                color = Muted
                                            )
                                        }
                                        if (!leave.requestNumber.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "No. Ref: ${leave.requestNumber}",
                                                fontSize = 11.sp,
                                                color = Muted2
                                            )
                                        }
                                    }
                                    StatusBadge(status = leave.status, customLabel = leave.statusLabel)
                                }

                                if (leave.status == "PENDING" || leave.status == "MANAGER_APPROVED") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = {
                                                scope.launch {
                                                    app.repository.cancelLeave(leave.id)
                                                    reload()
                                                    Toast.makeText(context, "Pengajuan cuti dibatalkan.", Toast.LENGTH_SHORT).show()
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
                            Spacer(modifier = Modifier.height(60.dp)) // Space for FAB
                        }
                    }
                }
                }
            }
        }
    }

    // Modal Dialog Form: Ajukan Cuti
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showCreateDialog = false },
            shape = DialogCornerShape,
            containerColor = Color.White,
            title = {
                Text("Form Pengajuan Cuti", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy900)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    HrisDropdown(
                        label = "Jenis Cuti",
                        items = leaveTypes,
                        selectedItem = selectedType,
                        onItemSelected = { selectedType = it },
                        itemLabel = { "${it.name} (Kuota: ${it.quotaDays.toInt()} hr)" }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Tanggal Mulai (YYYY-MM-DD)", fontSize = 12.sp, color = Ink2)
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
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
                        value = endDate,
                        onValueChange = { endDate = it },
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

                    Text("Alasan Cuti (Wajib diisi)", fontSize = 12.sp, color = Ink2)
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { if (it.length <= 255) reason = it },
                        placeholder = { Text("Misal: Keperluan keluarga mendesak", fontSize = 13.sp, color = Muted2) },
                        shape = InputCornerShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Teal500,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Attachment Toggle / Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (hasAttachment) "Lampiran: surat_keterangan.pdf" else "Lampiran Dokumen (Opsional)",
                            fontSize = 12.sp,
                            color = if (hasAttachment) Teal700 else Muted,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { hasAttachment = !hasAttachment }) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (hasAttachment) "Hapus" else "Unggah", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedType == null) {
                            Toast.makeText(context, "Pilih jenis cuti", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (reason.isBlank()) {
                            Toast.makeText(context, "Alasan cuti wajib diisi", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSubmitting = true
                        scope.launch {
                            val res = app.repository.createLeave(
                                CreateLeavePayload(
                                    leaveTypeId = selectedType!!.id,
                                    startDate = startDate,
                                    endDate = endDate,
                                    reason = reason,
                                    attachment = if (hasAttachment) "data:application/pdf;base64,JVBERi0xLjQKJcOkw7zDtsOfCjEgMCBvYmoKPDwKL1R5cGUgL0NhdGFsb2cKL1BhZ2VzIDIgMCBSCj4+CmVuZG9iamkyIDAgb2JqCjw8Ci9UeXBlIC9QYWdlcwovS2lkcyBbMyAwIFJdCi9Db3VudCAxCj4+CmVuZG9iamkKMyAwIG9iago8PAovVHlwZSAvUGFnZQovUGFyZW50IDIgMCBSCj4+CmVuZG9iamkKeHJlZgowIDQKMDAwMDAwMDAwMCA2NTUzNSBmIAowMDAwMDAwMDE4IDAwMDAwIG4gCjAwMDAwMDAwNjggMDAwMDAgbiAKMDAwMDAwMDExOCAwMDAwMCBuIAp0cmFpbGVyCjw8Ci9TaXplIDQKL1Jvb3QgMSAwIFIKPj4Kc3RhcnR4cmVmCjE2NAolJUVPRgo=" else null
                                )
                            )
                            isSubmitting = false
                            showCreateDialog = false
                            reload()
                            when (res) {
                                is Resource.Success -> Toast.makeText(context, res.message ?: "Pengajuan cuti terkirim.", Toast.LENGTH_SHORT).show()
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
