package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.HrisApplication
import com.example.core.ui.components.CardSurface
import com.example.core.ui.components.ResponsiveContainer
import com.example.core.ui.components.SoftButton
import com.example.ui.theme.Bg
import com.example.ui.theme.Bg2
import com.example.ui.theme.ButtonCornerShape
import com.example.ui.theme.CardCornerShape
import com.example.ui.theme.Ink
import com.example.ui.theme.Ink2
import com.example.ui.theme.Line
import com.example.ui.theme.Muted
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Teal50
import com.example.ui.theme.Teal600
import com.example.ui.theme.Teal700

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    title: String,
    referenceNumber: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as? HrisApplication
    val empName by (app?.repository?.employeeNameFlow?.collectAsState(initial = null)
        ?: androidx.compose.runtime.mutableStateOf(null))
    val empCode by (app?.repository?.employeeCodeFlow?.collectAsState(initial = null)
        ?: androidx.compose.runtime.mutableStateOf(null))
    val empRole by (app?.repository?.employeeRoleFlow?.collectAsState(initial = null)
        ?: androidx.compose.runtime.mutableStateOf(null))

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
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
                        modifier = Modifier.testTag("pdf_viewer_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Navy900)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, title)
                                putExtra(Intent.EXTRA_TEXT, "Dokumen Resmi PT Asia Raya Group: $title ($referenceNumber)")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Dokumen"))
                        },
                        modifier = Modifier.testTag("pdf_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Bagikan", tint = Navy900)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Printable Document Canvas Sheet
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CardCornerShape)
                        .background(Color.White)
                        .border(1.dp, Line, CardCornerShape)
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Corporate Letterhead
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PT ASIA RAYA GROUP",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                            Text(
                                text = "Human Resources & Payroll Department",
                                fontSize = 11.sp,
                                color = Muted
                            )
                            Text(
                                text = "SCBD Lot 28, Jakarta Selatan • hrd@asiaraya.co.id",
                                fontSize = 10.sp,
                                color = Muted
                            )
                        }

                        // Digital Verification QR Box
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Teal50)
                                .border(1.dp, Teal600.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "QR Verifikasi",
                                tint = Teal700,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(thickness = 2.dp, color = Navy900)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = title.uppercase(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy900,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Nomor Dokumen: $referenceNumber",
                        fontSize = 11.sp,
                        color = Muted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Detail Body
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Bg2, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        val recipientLabel = if (!empName.isNullOrBlank()) {
                            if (!empCode.isNullOrBlank()) "$empName ($empCode)" else empName!!
                        } else {
                            "Karyawan PT Asia Raya"
                        }
                        val roleLabel = empRole?.ifBlank { "Karyawan" } ?: "Karyawan PT Asia Raya"

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Penerima Dokumen:", fontSize = 12.sp, color = Muted, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(recipientLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink, textAlign = TextAlign.End)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Departemen / Jabatan:", fontSize = 12.sp, color = Muted, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(roleLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink, textAlign = TextAlign.End)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Status Pengesahan:", fontSize = 12.sp, color = Muted, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Resmi & Ditandatangani Digital", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Teal700, textAlign = TextAlign.End)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Dokumen ini diterbitkan secara elektronik oleh sistem HRIS Asia Raya dan sah tanpa tanda tangan basah sesuai dengan UU ITE. Keaslian dokumen dapat diverifikasi melalui pemindaian QR code di sudut kanan atas.",
                        fontSize = 11.sp,
                        color = Muted,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Diterbitkan oleh,", fontSize = 11.sp, color = Muted)
                            Spacer(modifier = Modifier.height(36.dp))
                            Text("HR & Payroll Directorate", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Navy900)
                            Text("PT Asia Raya Group", fontSize = 11.sp, color = Muted)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "Dokumen PDF tersimpan di folder unduhan.", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Navy800,
                    contentColor = Color.White
                ),
                shape = ButtonCornerShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_pdf_button")
            ) {
                Text("Simpan ke Perangkat", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            }
        }
    }
}
