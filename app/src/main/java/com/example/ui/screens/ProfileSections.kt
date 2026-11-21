package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.format.Formatters
import com.example.core.ui.components.CardSurface
import com.example.core.ui.components.EmployeeAvatar
import com.example.core.ui.components.SectionCard
import com.example.core.ui.components.StatusBadge
import com.example.data.api.model.EmployeeProfileData
import com.example.ui.theme.Amber50
import com.example.ui.theme.Amber600
import com.example.ui.theme.BadgeCornerShape
import com.example.ui.theme.Bg2
import com.example.ui.theme.Indigo50
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Ink
import com.example.ui.theme.Ink2
import com.example.ui.theme.Line
import com.example.ui.theme.Muted
import com.example.ui.theme.Muted2
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Rose50
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate600
import com.example.ui.theme.Teal50
import com.example.ui.theme.Teal600
import com.example.ui.theme.Teal700

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileHeroCard(
    data: EmployeeProfileData,
    resolvedPhotoUrl: String
) {
    val p = data.profile
    val org = data.organization
    var imageFailed by remember(resolvedPhotoUrl) { mutableStateOf(false) }

    CardSurface(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Teal50)
                    .border(2.dp, Teal600.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (resolvedPhotoUrl.isNotBlank() && !imageFailed) {
                    AsyncImage(
                        model = resolvedPhotoUrl,
                        contentDescription = "Foto Profil ${p.fullName}",
                        contentScale = ContentScale.Crop,
                        onError = { imageFailed = true },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    EmployeeAvatar(
                        name = p.fullName,
                        size = 68,
                        backgroundColor = Teal600
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = p.fullName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
                if (!p.nickname.isNullOrBlank()) {
                    Text(
                        text = "Panggilan: ${p.nickname}",
                        fontSize = 12.sp,
                        color = Muted
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "NIK: ${p.employeeCode}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Teal700
                )
                val posDept = listOf(org.position.name, org.department)
                    .filter { it.isNotBlank() && it != "-" }
                    .joinToString(" • ")
                if (posDept.isNotBlank()) {
                    Text(
                        text = posDept,
                        fontSize = 12.sp,
                        color = Ink2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Line)
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ProfileBadgePill(
                text = if (p.roleLabel != p.role) "${p.roleLabel} (${p.role})" else p.roleLabel,
                bgColor = Indigo50,
                textColor = Indigo600
            )
            StatusBadge(
                status = p.employmentStatus,
                customLabel = p.employmentStatusLabel
            )
            ProfileBadgePill(
                text = "Tahun Data: ${data.metaYear}",
                bgColor = Bg2,
                textColor = Ink2
            )
        }
    }
}

@Composable
fun EmploymentAndOrgCards(data: EmployeeProfileData) {
    val emp = data.employment
    val org = data.organization

    SectionCard(
        title = "Kepegawaian & Kontrak",
        subtitle = "Status hubungan kerja dan masa bakti"
    ) {
        ProfileKeyValueRow("Jenis Karyawan", emp.contractType)
        ProfileKeyValueRow("Tanggal Bergabung", Formatters.formatDateLong(emp.joinDate))
        ProfileKeyValueRow(
            "Masa Kerja (Tenure)",
            "${emp.tenure.label} (${emp.tenure.years} thn, ${emp.tenure.months} bln, ${emp.tenure.days} hr)"
        )
        emp.contract?.let { contract ->
            if (!contract.endDate.isNullOrBlank()) {
                ProfileKeyValueRow("Akhir Kontrak", Formatters.formatDateLong(contract.endDate))
            }
            if (contract.remainingDays != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sisa Masa Kontrak",
                        fontSize = 12.sp,
                        color = Muted,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (contract.expiringSoon) {
                            ProfileBadgePill(
                                text = "Segera Berakhir (${contract.remainingDays} hari)",
                                bgColor = Amber50,
                                textColor = Amber600
                            )
                        } else {
                            Text(
                                text = "${contract.remainingDays} hari",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Teal700
                            )
                        }
                    }
                }
            }
        }
        if (!emp.resignDate.isNullOrBlank()) {
            ProfileKeyValueRow("Tanggal Resign", Formatters.formatDateLong(emp.resignDate), valueColor = Rose600)
        }
    }

    SectionCard(
        title = "Organisasi & Atasan",
        subtitle = "Struktur unit kerja dan pelaporan"
    ) {
        ProfileIconRow(Icons.Default.Business, "Unit Bisnis", org.businessUnit)
        ProfileIconRow(Icons.Default.Work, "Departemen", org.department)
        val jabatanDetail = buildString {
            append(org.position.name)
            val meta = listOfNotNull(
                org.position.code?.let { "Kode: $it" },
                org.position.id?.let { "ID: #$it" }
            )
            if (meta.isNotEmpty()) append(" (${meta.joinToString(", ")})")
        }
        ProfileIconRow(Icons.Default.Badge, "Jabatan", jabatanDetail)

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Line)
        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.SupervisorAccount,
                contentDescription = null,
                tint = Navy800,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Atasan Langsung",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Navy900
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        val mgr = data.manager
        if (mgr == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(BadgeCornerShape)
                    .background(Slate50)
                    .padding(12.dp)
            ) {
                Text(
                    text = "Belum ditetapkan",
                    fontSize = 12.sp,
                    color = Slate600,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(BadgeCornerShape)
                    .background(Bg2)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = mgr.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ProfileBadgePill(
                        text = mgr.statusLabel ?: if (mgr.isActive) "Aktif" else "Nonaktif",
                        bgColor = if (mgr.isActive) Teal50 else Rose50,
                        textColor = if (mgr.isActive) Teal700 else Rose600
                    )
                }
                val mgrPosDept = listOfNotNull(mgr.position, mgr.department)
                    .filter { it.isNotBlank() }
                    .joinToString(" • ")
                if (mgrPosDept.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(text = mgrPosDept, fontSize = 12.sp, color = Ink2)
                }
                if (!mgr.email.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = mgr.email, fontSize = 12.sp, color = Muted)
                }
            }
        }
    }
}

@Composable
fun ContactAndPersonalCards(data: EmployeeProfileData) {
    val c = data.contact
    val per = data.personal

    SectionCard(
        title = "Kontak & Akun",
        subtitle = "Informasi komunikasi dan penautan Google"
    ) {
        ProfileIconRow(Icons.Default.Email, "Email Perusahaan", c.companyEmail)
        ProfileIconRow(Icons.Default.Person, "Username", c.username ?: "-")
        ProfileIconRow(Icons.Default.Phone, "Telepon", c.phone ?: "-")
        ProfileIconRow(Icons.Default.Home, "Alamat", c.address ?: "-")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Muted, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Akun Google", fontSize = 12.sp, color = Muted)
            }
            ProfileBadgePill(
                text = if (c.googleLinked) "Terhubung" else "Belum Terhubung",
                bgColor = if (c.googleLinked) Teal50 else Slate50,
                textColor = if (c.googleLinked) Teal700 else Slate600
            )
        }
    }

    SectionCard(
        title = "Data Pribadi",
        subtitle = "Identitas demografis karyawan"
    ) {
        ProfileKeyValueRow("Tanggal Lahir", Formatters.formatDateLong(per.birthDate))
        ProfileKeyValueRow("Umur", per.age?.let { "$it tahun" } ?: "-")
        ProfileKeyValueRow("Jenis Kelamin", per.gender ?: "-")
        ProfileKeyValueRow("Status Pernikahan", per.maritalStatus ?: "-")
        ProfileKeyValueRow("Agama", per.religion ?: "-")
    }
}

@Composable
fun PayrollCard(data: EmployeeProfileData) {
    val pay = data.payroll

    SectionCard(
        title = "Payroll, Pajak & BPJS",
        subtitle = "Kompensasi dasar dan administrasi kepesertaan"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(BadgeCornerShape)
                .background(Teal50)
                .padding(12.dp)
        ) {
            Text(
                text = "GAJI POKOK",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = Teal700
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = pay.basicSalaryLabel,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Teal700
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        ProfileKeyValueRow("Status Pajak (PTKP)", pay.taxStatus ?: "-")
        ProfileKeyValueRow("NPWP", pay.npwp ?: "-")
        ProfileKeyValueRow("BPJS Kesehatan", pay.bpjsKesehatan ?: "-")
        ProfileKeyValueRow("BPJS Ketenagakerjaan", pay.bpjsKetenagakerjaan ?: "-")

        val bankDisplay = listOfNotNull(
            pay.bankName?.takeIf { it.isNotBlank() },
            pay.bankAccountMasked?.takeIf { it.isNotBlank() }
        ).joinToString(" • ").ifEmpty { "-" }
        ProfileIconRow(Icons.Default.AccountBalance, "Rekening Bank", bankDisplay)
    }
}

@Composable
fun LeaveAndDocumentsCards(
    data: EmployeeProfileData,
    selectedYear: Int,
    onYearChange: (Int) -> Unit
) {
    val leave = data.leave
    val docs = data.documents

    SectionCard(
        title = "Ringkasan Cuti (${data.metaYear})",
        subtitle = "Saldo dan penggunaan cuti tahun berjalan",
        action = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onYearChange((selectedYear - 1).coerceIn(2000, 2100)) },
                    enabled = selectedYear > 2000
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Tahun Sebelumnya", tint = Navy900)
                }
                Text(
                    text = "$selectedYear",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
                IconButton(
                    onClick = { onYearChange((selectedYear + 1).coerceIn(2000, 2100)) },
                    enabled = selectedYear < 2100
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Tahun Berikutnya", tint = Navy900)
                }
            }
        }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isWide = maxWidth >= 520.dp
            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniStatTile("Total", formatNum(leave.summary.total), Bg2, Ink, Modifier.weight(1f))
                    MiniStatTile("Disetujui", formatNum(leave.summary.approvedDays), Indigo50, Indigo600, Modifier.weight(1f))
                    MiniStatTile("Menunggu", formatNum(leave.summary.open), Amber50, Amber600, Modifier.weight(1f))
                    MiniStatTile("Sisa", formatNum(leave.summary.remaining), Teal50, Teal700, Modifier.weight(1f))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MiniStatTile("Total", formatNum(leave.summary.total), Bg2, Ink, Modifier.weight(1f))
                        MiniStatTile("Disetujui", formatNum(leave.summary.approvedDays), Indigo50, Indigo600, Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MiniStatTile("Menunggu", formatNum(leave.summary.open), Amber50, Amber600, Modifier.weight(1f))
                        MiniStatTile("Sisa", formatNum(leave.summary.remaining), Teal50, Teal700, Modifier.weight(1f))
                    }
                }
            }
        }

        if (leave.balances.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Line)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Saldo Per Jenis Cuti",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink2
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                leave.balances.forEach { bal ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(BadgeCornerShape)
                            .background(Bg2)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bal.typeName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (bal.unlimited) "Tak Terbatas" else "Sisa ${Formatters.formatDays(bal.remaining)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Teal700
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { bal.progress.toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(BadgeCornerShape),
                            color = Teal600,
                            trackColor = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kuota: ${formatNum(bal.quota)}", fontSize = 11.sp, color = Muted)
                            Text("Carry: ${formatNum(bal.carryOver)}", fontSize = 11.sp, color = Muted)
                            Text("Terpakai: ${formatNum(bal.used)}", fontSize = 11.sp, color = Muted)
                        }
                    }
                }
            }
        }
    }

    SectionCard(
        title = "Dokumen Kepegawaian",
        subtitle = "Status verifikasi dan masa berlaku berkas"
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cols = if (maxWidth >= 520.dp) 5 else 2
            if (cols == 5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniStatTile("Total", "${docs.total}", Bg2, Ink, Modifier.weight(1f))
                    MiniStatTile("Terverifikasi", "${docs.verified}", Teal50, Teal700, Modifier.weight(1f))
                    MiniStatTile("Belum", "${docs.pending}", Amber50, Amber600, Modifier.weight(1f))
                    MiniStatTile("Ditolak", "${docs.rejected}", Rose50, Rose600, Modifier.weight(1f))
                    MiniStatTile("Kedaluwarsa", "${docs.expiringSoon}", Amber50, Amber600, Modifier.weight(1f))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniStatTile("Total Dokumen", "${docs.total}", Bg2, Ink, Modifier.weight(1f))
                        MiniStatTile("Terverifikasi", "${docs.verified}", Teal50, Teal700, Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniStatTile("Belum Diverifikasi", "${docs.pending}", Amber50, Amber600, Modifier.weight(1f))
                        MiniStatTile("Ditolak", "${docs.rejected}", Rose50, Rose600, Modifier.weight(1f))
                    }
                    MiniStatTile("Akan Kedaluwarsa", "${docs.expiringSoon}", Amber50, Amber600, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PermissionsCard(data: EmployeeProfileData) {
    val perm = data.permissions

    SectionCard(
        title = "Hak Akses & Kebijakan Edit",
        subtitle = "Otorisasi akun dan bidang yang dapat diubah"
    ) {
        ProfileIconRow(Icons.Default.Security, "Peran Akun", perm.role)
        ProfileKeyValueRow("Dapat Menyetujui (Approver)", if (perm.canApprove) "Ya" else "Tidak", if (perm.canApprove) Teal700 else Muted)
        ProfileKeyValueRow("Status Manajer", if (perm.isManager) "Ya" else "Tidak", if (perm.isManager) Teal700 else Muted)
        ProfileKeyValueRow(
            "Ubah Email Memerlukan HR",
            if (perm.emailChangeRequiresHr) "Ya, Wajib Persetujuan HR" else "Mandiri",
            if (perm.emailChangeRequiresHr) Amber600 else Teal700
        )

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Line)
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Bidang Dapat Diedit Sendiri",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink2
        )
        Spacer(modifier = Modifier.height(6.dp))
        if (perm.editableFields.isEmpty()) {
            Text(
                text = "Tidak ada bidang yang dapat diubah secara mandiri. Hubungi HR untuk pembaruan data.",
                fontSize = 12.sp,
                color = Muted
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                perm.editableFields.forEach { field ->
                    ProfileBadgePill(
                        text = field,
                        bgColor = Teal50,
                        textColor = Teal700
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniStatTile(
    label: String,
    value: String,
    bgColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(BadgeCornerShape)
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ProfileKeyValueRow(
    label: String,
    value: String,
    valueColor: Color = Ink
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Muted,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value.ifBlank { "-" },
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.2f)
        )
    }
}

@Composable
fun ProfileIconRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Muted,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Muted,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value.ifBlank { "-" },
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.2f)
        )
    }
}

@Composable
fun ProfileBadgePill(
    text: String,
    bgColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(BadgeCornerShape)
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatNum(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString() else String.format("%.1f", value)
}
