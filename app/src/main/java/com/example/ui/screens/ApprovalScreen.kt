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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.HrisApplication
import com.example.core.format.Formatters
import com.example.core.ui.components.CardSurface
import com.example.core.ui.components.DangerButton
import com.example.core.ui.components.EmployeeAvatar
import com.example.core.ui.components.EmptyState
import com.example.core.ui.components.ResponsiveContainer
import com.example.core.ui.components.StatusBadge
import com.example.core.ui.components.TealButton
import com.example.data.api.model.ApprovalItemDto
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
import com.example.ui.theme.Navy900
import com.example.ui.theme.Rose50
import com.example.ui.theme.Rose600
import com.example.ui.theme.Teal50
import com.example.ui.theme.Teal500
import com.example.ui.theme.Teal600
import com.example.ui.theme.Teal700
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HrisApplication
    val scope = rememberCoroutineScope()

    val tabKeys = listOf("all", "leave", "overtime", "schedule", "claim")
    val tabLabels = listOf("Semua", "Cuti", "Lembur", "Jadwal", "Klaim")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    var approvals by remember { mutableStateOf<List<ApprovalItemDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Decision Dialog
    var pendingDecisionItem by remember { mutableStateOf<ApprovalItemDto?>(null) }
    var pendingDecisionType by remember { mutableStateOf("APPROVED") } // APPROVED or REJECTED
    var decisionNote by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val reload = {
        scope.launch {
            isLoading = true
            val currentKey = tabKeys[selectedTabIndex]
            when (val res = app.repository.getApprovals(currentKey)) {
                is Resource.Success -> approvals = res.data
                else -> approvals = emptyList()
            }
            isLoading = false
        }
    }

    LaunchedEffect(selectedTabIndex) {
        reload()
    }

    val submitDecision: () -> Unit = {
        val item = pendingDecisionItem
        if (item != null) {
            isSubmitting = true
            scope.launch {
                val res = app.repository.decideApproval(
                    type = item.type,
                    id = item.id,
                    decision = pendingDecisionType,
                    note = decisionNote.ifBlank { null }
                )
                isSubmitting = false
                pendingDecisionItem = null
                decisionNote = ""
                reload()
                when (res) {
                    is Resource.Success -> Toast.makeText(context, res.message ?: "Keputusan tersimpan.", Toast.LENGTH_SHORT).show()
                    is Resource.Error -> Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    BackHandler(enabled = pendingDecisionItem == null) { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kotak Masuk Persetujuan", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Navy900, maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
            // Scrollable Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Navy900,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Teal600
                    )
                }
            ) {
                tabLabels.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                label,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Teal600)
                }
            } else if (approvals.isEmpty()) {
                EmptyState(
                    title = "Tidak Ada Pengajuan Menunggu",
                    body = "Semua pengajuan dari tim Anda telah selesai diproses.",
                    icon = Icons.Default.AssignmentTurnedIn
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(approvals) { item ->
                        CardSurface(modifier = Modifier.fillMaxWidth()) {
                            // Employee Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    EmployeeAvatar(
                                        name = item.employee?.fullName,
                                        size = 38,
                                        backgroundColor = Teal600
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = item.employee?.fullName ?: "Karyawan",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Ink
                                        )
                                        Text(
                                            text = "${item.employee?.employeeCode ?: ""} • ${item.employee?.department ?: ""}",
                                            fontSize = 11.sp,
                                            color = Muted
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(BadgeCornerShape)
                                        .background(Teal50)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = item.typeLabel,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Teal700
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Line)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Detail Info Box
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(BadgeCornerShape)
                                    .background(Bg2)
                                    .padding(10.dp)
                            ) {
                                item.detail?.forEach { (k, v) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 1.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = k, fontSize = 11.sp, color = Muted, modifier = Modifier.weight(1f))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = v.toString(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Ink, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                                    }
                                }
                            }

                            if (!item.reason.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Alasan: ${item.reason}",
                                    fontSize = 12.sp,
                                    color = Ink2
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        pendingDecisionItem = item
                                        pendingDecisionType = "REJECTED"
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Rose50,
                                        contentColor = Rose600
                                    ),
                                    shape = ButtonCornerShape,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tolak", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = {
                                        pendingDecisionItem = item
                                        pendingDecisionType = "APPROVED"
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Teal600,
                                        contentColor = Color.White
                                    ),
                                    shape = ButtonCornerShape,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Setujui", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }

    // Modal Decision Note Dialog
    if (pendingDecisionItem != null) {
        val isApprove = pendingDecisionType == "APPROVED"
        AlertDialog(
            onDismissRequest = { if (!isSubmitting) pendingDecisionItem = null },
            shape = DialogCornerShape,
            containerColor = Color.White,
            title = {
                Text(
                    text = if (isApprove) "Setujui ${pendingDecisionItem?.typeLabel}?" else "Tolak ${pendingDecisionItem?.typeLabel}?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isApprove) Teal700 else Rose600
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Pengajuan oleh ${pendingDecisionItem?.employee?.fullName}. Tambahkan catatan persetujuan (opsional):",
                        fontSize = 13.sp,
                        color = Ink2,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = decisionNote,
                        onValueChange = { if (it.length <= 255) decisionNote = it },
                        placeholder = { Text("Misal: Disetujui sesuai rencana kerja", fontSize = 13.sp, color = Muted2) },
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
                    onClick = submitDecision,
                    colors = ButtonDefaults.buttonColors(containerColor = if (isApprove) Teal600 else Rose600, contentColor = Color.White),
                    shape = ButtonCornerShape,
                    enabled = !isSubmitting
                ) {
                    Text(if (isApprove) "Ya, Setujui" else "Ya, Tolak", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingDecisionItem = null },
                    enabled = !isSubmitting
                ) {
                    Text("Batal", fontSize = 13.sp, color = Muted)
                }
            }
        )
    }
}
