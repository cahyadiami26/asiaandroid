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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ReceiptLong
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
import com.example.core.ui.components.HrisDropdown
import com.example.core.ui.components.KpiTile
import com.example.core.ui.components.ResponsiveContainer
import com.example.core.ui.components.SoftButton
import com.example.core.ui.components.StatusBadge
import com.example.data.api.model.ClaimCategoryDto
import com.example.data.api.model.ClaimDto
import com.example.data.api.model.CreateClaimPayload
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
fun ClaimScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HrisApplication
    val scope = rememberCoroutineScope()

    var claims by remember { mutableStateOf<List<ClaimDto>>(emptyList()) }
    var categories by remember { mutableStateOf<List<ClaimCategoryDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Dialog Create Claim
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<ClaimCategoryDto?>(null) }
    var claimDate by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var hasReceipt by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val reload = {
        scope.launch {
            isLoading = true
            when (val res = app.repository.getClaims()) {
                is Resource.Success -> claims = res.data
                else -> {}
            }
            when (val catRes = app.repository.getClaimCategories()) {
                is Resource.Success -> {
                    categories = catRes.data
                    if (selectedCategory == null && catRes.data.isNotEmpty()) {
                        selectedCategory = catRes.data.first()
                    }
                }
                else -> {}
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        claimDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        reload()
    }

    val totalAmount = claims.sumOf { it.amount }
    val approvedAmount = claims.filter { it.status == "APPROVED" || it.status == "PAID" }.sumOf { it.amount }

    BackHandler(enabled = !showCreateDialog) { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Klaim Biaya", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Navy900, maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
            // Stats Row (adaptive for narrow screens)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (maxWidth < 360.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KpiTile(label = "Total Klaim", value = "${claims.size}", subValue = "Pengajuan", modifier = Modifier.weight(1f))
                            KpiTile(label = "Diajukan", value = Formatters.formatRupiahCompact(totalAmount), accentColor = Amber600, modifier = Modifier.weight(1f))
                        }
                        KpiTile(label = "Disetujui", value = Formatters.formatRupiahCompact(approvedAmount), accentColor = Teal600, modifier = Modifier.fillMaxWidth())
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiTile(label = "Total Klaim", value = "${claims.size}", subValue = "Pengajuan", modifier = Modifier.weight(1f))
                        KpiTile(label = "Diajukan", value = Formatters.formatRupiahCompact(totalAmount), accentColor = Amber600, modifier = Modifier.weight(1f))
                        KpiTile(label = "Disetujui", value = Formatters.formatRupiahCompact(approvedAmount), accentColor = Teal600, modifier = Modifier.weight(1f))
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Teal600)
                }
            } else if (claims.isEmpty()) {
                EmptyState(
                    title = "Belum Ada Pengajuan Klaim",
                    body = "Ajukan penggantian biaya operasional medis, transport, atau kebutuhan dinas lainnya.",
                    ctaText = "Ajukan Klaim",
                    onCtaClick = { showCreateDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(claims) { claim ->
                        CardSurface(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = claim.category?.name ?: "Klaim Biaya",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = Formatters.formatRupiah(claim.amount),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Teal700
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Tanggal: ${Formatters.formatDateShort(claim.claimDate)} • ${claim.claimNumber}",
                                        fontSize = 12.sp,
                                        color = Muted
                                    )
                                    if (!claim.description.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = claim.description,
                                            fontSize = 12.sp,
                                            color = Ink2
                                        )
                                    }
                                }
                                StatusBadge(status = claim.status, customLabel = claim.statusLabel)
                            }

                            if (claim.status == "PENDING" || claim.status == "MANAGER_APPROVED") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            scope.launch {
                                                app.repository.cancelClaim(claim.id)
                                                reload()
                                                Toast.makeText(context, "Klaim dibatalkan.", Toast.LENGTH_SHORT).show()
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

    // Modal Create Claim Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showCreateDialog = false },
            shape = DialogCornerShape,
            containerColor = Color.White,
            title = {
                Text("Ajukan Klaim Biaya", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy900)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    HrisDropdown(
                        label = "Kategori Reimbursement",
                        items = categories,
                        selectedItem = selectedCategory,
                        onItemSelected = { selectedCategory = it },
                        itemLabel = {
                            val max = if (it.maxAmount != null) " (Plafon: ${Formatters.formatRupiahCompact(it.maxAmount)})" else ""
                            "${it.name}$max"
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Tanggal Nota/Transaksi (YYYY-MM-DD)", fontSize = 12.sp, color = Ink2)
                    OutlinedTextField(
                        value = claimDate,
                        onValueChange = { claimDate = it },
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

                    Text("Nominal Penggantian (Rupiah)", fontSize = 12.sp, color = Ink2)
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it.filter { char -> char.isDigit() } },
                        placeholder = { Text("Contoh: 150000", fontSize = 13.sp, color = Muted2) },
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

                    Text("Keterangan Pengeluaran (Maks 255)", fontSize = 12.sp, color = Ink2)
                    OutlinedTextField(
                        value = description,
                        onValueChange = { if (it.length <= 255) description = it },
                        placeholder = { Text("Misal: Pembelian obat resep klinik mitra", fontSize = 13.sp, color = Muted2) },
                        shape = InputCornerShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Teal500,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (hasReceipt) "Bukti: nota_pembayaran.jpg" else "Bukti Pembayaran / Nota (Wajib)",
                            fontSize = 12.sp,
                            color = if (hasReceipt) Teal700 else Rose600,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { hasReceipt = !hasReceipt }) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (hasReceipt) "Hapus" else "Lampirkan", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountVal = amountInput.toDoubleOrNull() ?: 0.0
                        if (amountVal <= 0) {
                            Toast.makeText(context, "Masukkan nominal klaim yang valid.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (selectedCategory == null) {
                            Toast.makeText(context, "Pilih kategori klaim.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSubmitting = true
                        scope.launch {
                            val res = app.repository.createClaim(
                                CreateClaimPayload(
                                    categoryId = selectedCategory!!.id,
                                    claimDate = claimDate,
                                    amount = amountVal,
                                    description = description,
                                    receipt = if (hasReceipt) "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA=" else null
                                )
                            )
                            isSubmitting = false
                            showCreateDialog = false
                            reload()
                            when (res) {
                                is Resource.Success -> Toast.makeText(context, res.message ?: "Klaim diajukan.", Toast.LENGTH_SHORT).show()
                                is Resource.Error -> Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800, contentColor = Color.White),
                    shape = ButtonCornerShape,
                    enabled = !isSubmitting
                ) {
                    Text("Kirim Klaim", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
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
