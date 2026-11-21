package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.HrisApplication
import com.example.core.format.Formatters
import com.example.core.ui.components.CardSurface
import com.example.core.ui.components.EmptyState
import com.example.core.ui.components.ResponsiveContainer
import com.example.core.ui.components.StatusBadge
import com.example.data.api.model.TaxSlipDto
import java.util.Calendar
import com.example.data.repo.Resource
import com.example.ui.theme.Bg
import com.example.ui.theme.ButtonCornerShape
import com.example.ui.theme.DialogCornerShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxSlipScreen(
    onBack: () -> Unit,
    onViewPdf: (String, String) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HrisApplication
    val scope = rememberCoroutineScope()

    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    var taxSlips by remember { mutableStateOf<List<TaxSlipDto>>(emptyList()) }
    var selectedTaxSlip by remember { mutableStateOf<TaxSlipDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    BackHandler(enabled = selectedTaxSlip == null) { onBack() }

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            when (val res = app.repository.getTaxSlips(currentYear)) {
                is Resource.Success -> taxSlips = res.data
                else -> {}
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bukti Potong Pajak 1721-A1",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Navy900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("tax_slips_back_button")
                    ) {
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
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Teal600)
                }
            } else if (taxSlips.isEmpty()) {
                EmptyState(
                    title = "Bukti Potong Belum Diterbitkan",
                    body = "Bukti potong Formulir 1721-A1 untuk pelaporan SPT Tahunan sedang dalam proses verifikasi tim HR & Tax.",
                    icon = Icons.Default.Description
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "BUKTI POTONG PPh PASAL 21 RESMI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Muted,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(taxSlips) { slip ->
                        CardSurface(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { selectedTaxSlip = slip }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Tahun Pajak ${slip.taxYear}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "No: ${slip.slipNumber} • ${slip.monthsCount} Bulan",
                                        fontSize = 12.sp,
                                        color = Muted
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "PPh Dipotong: ${Formatters.formatRupiah(slip.taxWithheld)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Teal700
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusBadge(status = "APPROVED", customLabel = "Diterbitkan")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = Muted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Detail Tax Slip Dialog
    if (selectedTaxSlip != null) {
        val slip = selectedTaxSlip!!
        AlertDialog(
            onDismissRequest = { selectedTaxSlip = null },
            shape = DialogCornerShape,
            containerColor = Color.White,
            title = {
                Column {
                    Text("Formulir 1721-A1 (${slip.taxYear})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy900)
                    Text("No. Bukti: ${slip.slipNumber}", fontSize = 11.sp, color = Muted)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Nama:", fontSize = 12.sp, color = Muted, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(slip.employeeName ?: "-", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink, textAlign = TextAlign.End)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("NPWP:", fontSize = 12.sp, color = Muted, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(slip.npwp ?: "-", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink, textAlign = TextAlign.End)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Status PTKP:", fontSize = 12.sp, color = Muted, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(slip.taxStatus ?: "-", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink, textAlign = TextAlign.End)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Line)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Rincian Penghasilan & Pajak", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink2)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Penghasilan Bruto", fontSize = 11.sp, color = Ink, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Formatters.formatRupiah(slip.grossIncome), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Ink)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Penghasilan Netto", fontSize = 11.sp, color = Ink, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Formatters.formatRupiah(slip.netto), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Ink)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("PKP (Penghasilan Kena Pajak)", fontSize = 11.sp, color = Ink, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Formatters.formatRupiah(slip.pkp), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Ink)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("PPh 21 Terutang", fontSize = 11.sp, color = Ink, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Formatters.formatRupiah(slip.taxDue), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Teal700)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("PPh Telah Dipotong", fontSize = 11.sp, color = Ink, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Formatters.formatRupiah(slip.taxWithheld), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Teal700)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Selisih Pajak (Nihil)", fontSize = 11.sp, color = Muted, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Formatters.formatRupiah(slip.taxDiff), fontSize = 11.sp, color = Muted)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val title = "Bukti Potong 1721-A1 (${slip.taxYear})"
                        selectedTaxSlip = null
                        onViewPdf(title, slip.slipNumber)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Navy800,
                        contentColor = Color.White
                    ),
                    shape = ButtonCornerShape,
                    modifier = Modifier.testTag("download_taxslip_pdf_button")
                ) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Unduh PDF", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTaxSlip = null }) {
                    Text("Tutup", fontSize = 13.sp, color = Muted)
                }
            }
        )
    }
}
