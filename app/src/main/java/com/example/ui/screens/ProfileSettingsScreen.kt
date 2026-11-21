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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.HrisApplication
import com.example.core.ui.components.CardSurface
import com.example.core.ui.components.ConfirmationDialog
import com.example.core.ui.components.DangerButton
import com.example.core.ui.components.ErrorState
import com.example.core.ui.components.LoadingSkeleton
import com.example.core.ui.components.OfflineBanner
import com.example.core.ui.components.ResponsiveContainer
import com.example.core.ui.components.SoftButton
import com.example.data.api.model.EmployeeProfileData
import com.example.data.repo.Resource
import com.example.ui.theme.Bg
import com.example.ui.theme.Bg2
import com.example.ui.theme.ButtonCornerShape
import com.example.ui.theme.DialogCornerShape
import com.example.ui.theme.Ink
import com.example.ui.theme.InputCornerShape
import com.example.ui.theme.LineStrong
import com.example.ui.theme.Muted
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Teal50
import com.example.ui.theme.Teal500
import com.example.ui.theme.Teal700
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    onBack: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HrisApplication
    val scope = rememberCoroutineScope()

    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR).coerceIn(2000, 2100) }
    var selectedYear by remember { mutableIntStateOf(currentYear) }

    var profileData by remember { mutableStateOf<EmployeeProfileData?>(null) }
    var resolvedPhotoUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentServerUrl by remember { mutableStateOf("https://hris.asiaraya.co.id") }

    var showChangeServerDialog by remember { mutableStateOf(false) }
    var newServerUrlInput by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = !showChangeServerDialog && !showLogoutDialog) {
        onBack()
    }

    val loadProfile: (Int) -> Unit = { targetYear ->
        scope.launch {
            isLoading = true
            errorMessage = null
            currentServerUrl = app.repository.getServerUrl()
            newServerUrlInput = currentServerUrl
            when (val res = app.repository.getMyProfile(targetYear)) {
                is Resource.Success -> {
                    profileData = res.data
                    resolvedPhotoUrl = app.repository.resolveMediaUrl(res.data.profile.photoUrl)
                }
                is Resource.Error -> {
                    errorMessage = res.message
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(selectedYear) {
        loadProfile(selectedYear)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profil Karyawan",
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
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Navy900
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { loadProfile(selectedYear) },
                        enabled = !isLoading,
                        modifier = Modifier.testTag("profile_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Muat Ulang Profil",
                            tint = Navy900
                        )
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (isLoading && profileData == null) {
                    LoadingSkeleton(cardCount = 5)
                } else if (profileData == null && errorMessage != null) {
                    CardSurface(modifier = Modifier.fillMaxWidth()) {
                        ErrorState(
                            message = errorMessage!!,
                            onRetry = { loadProfile(selectedYear) }
                        )
                    }
                } else {
                    if (!errorMessage.isNullOrBlank()) {
                        OfflineBanner(message = errorMessage!!)
                    }

                    profileData?.let { data ->
                        ProfileHeroCard(
                            data = data,
                            resolvedPhotoUrl = resolvedPhotoUrl
                        )
                        EmploymentAndOrgCards(data = data)
                        ContactAndPersonalCards(data = data)
                        PayrollCard(data = data)
                        LeaveAndDocumentsCards(
                            data = data,
                            selectedYear = selectedYear,
                            onYearChange = { newYear -> selectedYear = newYear }
                        )
                        PermissionsCard(data = data)
                    }
                }

                // Server Address Configuration Card
                CardSurface(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Teal50),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dns,
                                    contentDescription = null,
                                    tint = Teal700,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pengaturan Server",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink
                                )
                                Text(
                                    text = currentServerUrl,
                                    fontSize = 11.sp,
                                    color = Muted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        SoftButton(
                            text = "Ubah",
                            onClick = {
                                newServerUrlInput = currentServerUrl
                                showChangeServerDialog = true
                            },
                            modifier = Modifier.testTag("change_server_button")
                        )
                    }
                }

                // App & System Information Card
                CardSurface(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Bg2),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Muted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Tentang Aplikasi",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            Text(
                                text = "HRIS Asia Raya v1.0.0 (API v2.17.5)",
                                fontSize = 12.sp,
                                color = Muted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Aplikasi resmi karyawan PT Asia Raya Group untuk presensi daring GPS geofence, jadwal shift, cuti, lembur, klaim pengeluaran, slip gaji, dan persetujuan bertingkat.",
                        fontSize = 12.sp,
                        color = Muted,
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Logout Button
                DangerButton(
                    text = "Keluar dari Akun (Logout)",
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("logout_button")
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Change Server Dialog
    if (showChangeServerDialog) {
        AlertDialog(
            onDismissRequest = { showChangeServerDialog = false },
            shape = DialogCornerShape,
            containerColor = Color.White,
            title = {
                Text("Ubah Alamat Server", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy900)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Masukkan alamat host backend HRIS (contoh: https://hris.asiaraya.co.id). Mengganti server akan memperbarui koneksi aktif Anda.",
                        fontSize = 12.sp,
                        color = Muted,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newServerUrlInput,
                        onValueChange = { newServerUrlInput = it },
                        singleLine = true,
                        shape = InputCornerShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Teal500,
                            unfocusedBorderColor = LineStrong,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(
                            onClick = {
                                newServerUrlInput = "https://hris.asiaraya.co.id"
                            }
                        ) {
                            Text("Bawaan", fontSize = 12.sp, color = Muted)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            app.repository.updateServerUrl(newServerUrlInput)
                            showChangeServerDialog = false
                            Toast.makeText(context, "Alamat server diperbarui.", Toast.LENGTH_SHORT).show()
                            loadProfile(selectedYear)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Navy800,
                        contentColor = Color.White
                    ),
                    shape = ButtonCornerShape
                ) {
                    Text("Simpan", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeServerDialog = false }) {
                    Text("Batal", fontSize = 13.sp, color = Muted)
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        ConfirmationDialog(
            title = "Keluar dari Akun?",
            message = "Anda akan keluar dari sesi perangkat ini dan kembali ke layar login.",
            confirmText = "Keluar",
            cancelText = "Batal",
            isDanger = true,
            onConfirm = {
                scope.launch {
                    app.repository.logout()
                    onLogoutSuccess()
                }
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}
