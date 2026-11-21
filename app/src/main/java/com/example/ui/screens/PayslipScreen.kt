package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import com.example.core.ui.components.KpiTile
import com.example.core.ui.components.ResponsiveContainer
import com.example.core.ui.components.SoftButton
import com.example.core.ui.components.StatusBadge
import com.example.core.ui.components.TealButton
import com.example.data.api.model.PayslipDto
import java.util.Calendar
import com.example.data.repo.Resource
import com.example.ui.theme.Bg
import com.example.ui.theme.Bg2
import com.example.ui.theme.ButtonCornerShape
import com.example.ui.theme.DialogCornerShape
import com.example.ui.theme.Ink
import com.example.ui.theme.Ink2
import com.example.ui.theme.Line
import com.example.ui.theme.Muted
import com.example.ui.theme.Muted2
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Rose600
import com.example.ui.theme.Teal50
import com.example.ui.theme.Teal600
import com.example.ui.theme.Teal700
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayslipScreen(
    onBack: () -> Unit,
    onViewPdf: (String, String) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HrisApplication
    val scope = rememberCoroutineScope()

    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    var payslips by remember { mutableStateOf<List<PayslipDto>>(emptyList()) }
    var selectedPayslip by remember { mutableStateOf<PayslipDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    BackHandler(enabled = selectedPayslip == null) { onBack() }

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            when (val res = app.repository.getPayslips(currentYear)) {
                is Resource.Success -> payslips = res.data
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
                        text = "Slip Gaji Karyawan",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Navy900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("payslips_back_button")
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
            } else if (payslips.isEmpty()) {
                EmptyState(
                    title = "Belum Ada Slip Gaji",
                    body = "Slip gaji diterbitkan secara otomatis setelah proses persetujuan payroll bulanan selesai."
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
                            text = "DAFTAR SLIP GAJI FINAL (24 BULAN TERAKHIR)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Muted,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(payslips) { slip ->
                        CardSurface(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { selectedPayslip = slip }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = slip.period?.label ?: "Periode Gaji",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = slip.slipNumber,
                                        fontSize = 12.sp,
                                        color = Muted
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Take Home Pay: ${Formatters.formatRupiah(slip.takeHomePay)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Teal700
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusBadge(status = slip.period?.status ?: "PAID", customLabel = "Lunas")
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

    // Detail Payslip Modal Sheet / Dialog
    if (selectedPayslip != null) {
        val slip = selectedPayslip!!
        AlertDialog(
            onDismissRequest = { selectedPayslip = null },
            shape = DialogCornerShape,
            containerColor = Color.White,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(slip.period?.label ?: "Slip Gaji", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy900)
                        Text(slip.slipNumber, fontSize = 11.sp, color = Muted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(status = "PAID", customLabel = "Lunas")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Summary Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Teal50, DialogCornerShape)
                            .padding(12.dp)
                    ) {
                        Text("TAKE HOME PAY (NETO)", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Teal700)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Formatters.formatRupiah(slip.takeHomePay),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Teal700
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gaji Bruto:", fontSize = 12.sp, color = Muted, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Formatters.formatRupiah(slip.grossSalary), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Potongan:", fontSize = 12.sp, color = Muted, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("- ${Formatters.formatRupiah(slip.totalDeduction)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Rose600)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("PPh 21:", fontSize = 12.sp, color = Muted, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Formatters.formatRupiah(slip.pphAmount), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Line)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Rincian Komponen Gaji", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink2)
                    Spacer(modifier = Modifier.height(6.dp))

                    val components = slip.components ?: emptyList()
                    components.forEach { comp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(comp.name, fontSize = 11.sp, color = Ink, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            val isDeduction = comp.type == "DEDUCTION"
                            Text(
                                text = if (isDeduction) "- ${Formatters.formatRupiah(comp.amount)}" else Formatters.formatRupiah(comp.amount),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDeduction) Rose600 else Teal700,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val title = "Slip Gaji ${slip.period?.label ?: ""}"
                        selectedPayslip = null
                        onViewPdf(title, slip.slipNumber)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Navy800,
                        contentColor = Color.White
                    ),
                    shape = ButtonCornerShape,
                    modifier = Modifier.testTag("download_payslip_pdf_button")
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
                TextButton(onClick = { selectedPayslip = null }) {
                    Text("Tutup", fontSize = 13.sp, color = Muted)
                }
            }
        )
    }
}
