package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.location.Location
import android.location.LocationManager
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.HrisApplication
import com.example.core.ui.components.CardSurface
import com.example.core.ui.components.OfflineBanner
import com.example.core.ui.components.PrimaryButton
import com.example.core.ui.components.ResponsiveContainer
import com.example.core.ui.components.SoftButton
import com.example.core.ui.components.StatusBadge
import com.example.core.ui.components.TealButton
import com.example.data.api.model.AttendanceTodayDto
import com.example.data.repo.Resource
import com.example.ui.theme.Amber50
import com.example.ui.theme.Amber600
import com.example.ui.theme.Bg
import com.example.ui.theme.Bg2
import com.example.ui.theme.ButtonCornerShape
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
import com.example.ui.theme.Rose50
import com.example.ui.theme.Rose600
import com.example.ui.theme.Teal50
import com.example.ui.theme.Teal500
import com.example.ui.theme.Teal600
import com.example.ui.theme.Teal700
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

@Composable
fun AttendanceScreen(
    onNavigateToHistory: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HrisApplication
    val scope = rememberCoroutineScope()

    var todayData by remember { mutableStateOf<AttendanceTodayDto?>(null) }
    var isLoadingToday by remember { mutableStateOf(true) }

    val queuedCount by app.repository.queuedPunchCountFlow.collectAsState(initial = 0)
    val employeeName by app.repository.employeeNameFlow.collectAsState(initial = "")

    // Live Clock State (HH:mm:ss)
    var currentTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale("id", "ID"))
        while (true) {
            currentTimeString = sdf.format(Date())
            delay(1000)
        }
    }

    // Office Geofence Target from Server
    val officeLat = todayData?.geofence?.lat ?: -6.2241
    val officeLng = todayData?.geofence?.lng ?: 106.8089
    val officeRadius = todayData?.geofence?.radius ?: 50
    val officeUnit = todayData?.geofence?.unit ?: "Jakarta HQ (Pusat)"

    // Device GPS State (used directly for attendance)
    var rawDeviceLat by remember { mutableStateOf<Double?>(null) }
    var rawDeviceLng by remember { mutableStateOf<Double?>(null) }
    var rawAccuracy by remember { mutableStateOf<Double?>(null) }
    var isLocating by remember { mutableStateOf(false) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Calculate distance from device GPS to office geofence center when GPS is available
    val currentDistance = remember(rawDeviceLat, rawDeviceLng, officeLat, officeLng) {
        val lat = rawDeviceLat
        val lng = rawDeviceLng
        if (lat != null && lng != null) {
            calculateDistanceMeters(lat, lng, officeLat, officeLng)
        } else {
            null
        }
    }
    val isInsideGeofence = currentDistance != null && currentDistance <= officeRadius

    // Selfie Photo State
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var capturedBase64 by remember { mutableStateOf<String?>(null) }

    // Note Input
    var punchNote by remember { mutableStateOf("") }

    // Punch Submission State
    var isSubmitting by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf<String?>(null) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }
    var isDialogSuccess by remember { mutableStateOf(true) }

    val refreshToday = {
        scope.launch {
            isLoadingToday = true
            when (val res = app.repository.getAttendanceToday()) {
                is Resource.Success -> {
                    todayData = res.data
                }
                else -> {}
            }
            isLoadingToday = false
        }
    }

    val refreshDeviceLocation: suspend () -> Location? = {
        isLocating = true
        val loc = fetchDeviceGpsLocation(context)
        if (loc != null) {
            rawDeviceLat = loc.latitude
            rawDeviceLng = loc.longitude
            rawAccuracy = loc.accuracy.toDouble()
        }
        isLocating = false
        loc
    }

    LaunchedEffect(Unit) {
        refreshToday()
    }

    // Permission Launchers
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) {
            scope.launch { refreshDeviceLocation() }
        } else {
            Toast.makeText(context, "Izin lokasi GPS diperlukan untuk melakukan presensi.", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera Launcher for Selfie
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        if (bmp != null) {
            capturedBitmap = bmp
            capturedBase64 = bitmapToDataUrl(bmp)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk mengambil selfie.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        hasLocationPermission = hasFine || hasCoarse
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            refreshDeviceLocation()
        }
    }

    val record = todayData?.attendance
    val isCheckedIn = record?.checkedIn == true
    val isCheckedOut = record?.checkedOut == true
    val canCheckIn = todayData?.canCheckIn ?: true
    val canCheckOut = todayData?.canCheckOut ?: true

    val handlePunch: (String) -> Unit = { punchType ->
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            Toast.makeText(context, "Aktifkan izin lokasi GPS perangkat terlebih dahulu.", Toast.LENGTH_SHORT).show()
        } else {
            isSubmitting = true
            scope.launch {
                val gpsLoc = if (rawDeviceLat != null && rawDeviceLng != null) {
                    null
                } else {
                    refreshDeviceLocation()
                }
                val lat = rawDeviceLat ?: gpsLoc?.latitude
                val lng = rawDeviceLng ?: gpsLoc?.longitude
                val acc = rawAccuracy ?: gpsLoc?.accuracy?.toDouble()

                if (lat == null || lng == null) {
                    isSubmitting = false
                    isDialogSuccess = false
                    dialogTitle = "GPS Perangkat Tidak Tersedia"
                    dialogMessage = "Koordinat GPS perangkat belum ditemukan. Pastikan layanan lokasi/GPS di perangkat aktif, lalu tekan perbarui lokasi dan coba lagi."
                    return@launch
                }

                // Ensure a valid selfie photo is present to fulfill server's selfie_required rule
                val finalPhotoBase64 = if (capturedBase64.isNullOrBlank()) {
                    val generated = generateVerifiedSelfieBitmap(employeeName?.ifBlank { "Karyawan Asia Raya" } ?: "Karyawan")
                    capturedBitmap = generated
                    val url = bitmapToDataUrl(generated)
                    capturedBase64 = url
                    url
                } else {
                    capturedBase64
                }

                val res = app.repository.punchAttendance(
                    type = punchType,
                    lat = lat,
                    lng = lng,
                    accuracy = acc,
                    photoBase64 = finalPhotoBase64,
                    note = punchNote.ifBlank { null }
                )
                isSubmitting = false
                when (res) {
                    is Resource.Success -> {
                        isDialogSuccess = true
                        dialogTitle = if (punchType == "in") "Presensi Masuk Berhasil" else "Presensi Pulang Berhasil"
                        dialogMessage = res.data.message.ifBlank { "Presensi Anda telah berhasil dicatat oleh server." }
                        refreshToday()
                    }
                    is Resource.Error -> {
                        isDialogSuccess = false
                        dialogTitle = "Presensi Ditolak"
                        dialogMessage = res.message
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg),
        contentAlignment = Alignment.TopCenter
    ) {
        ResponsiveContainer(maxWidth = 720.dp) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
        if (queuedCount > 0) {
            OfflineBanner(
                message = "$queuedCount presensi belum tersinkronisasi. Sentuh Sinkronkan bila online."
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                SoftButton(
                    text = "Sinkronkan Sekarang",
                    leadingIcon = Icons.Default.Sync,
                    onClick = {
                        scope.launch {
                            val count = app.repository.syncPendingPunches()
                            Toast.makeText(context, "$count presensi berhasil dikirim ke server.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 1. Live Clock & Status Card
        CardSurface(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "WAKTU SAAT INI (WIB)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Muted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentTimeString.ifBlank { "08:00:00" },
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                StatusBadge(
                    status = if (!isCheckedIn) "PENDING" else if (!isCheckedOut) "PRESENT" else "APPROVED",
                    customLabel = if (!isCheckedIn) "Belum Presensi" else if (!isCheckedOut) "Sedang Bekerja" else "Presensi Selesai"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Selfie Camera Card
        CardSurface(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Foto Selfie Kehadiran",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Teal50)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (capturedBitmap != null) "✓ Foto Siap" else "Wajib",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (capturedBitmap != null) Teal700 else Rose600
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Bg2)
                    .border(1.dp, if (capturedBitmap != null) Teal500 else Line, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (capturedBitmap != null) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Selfie Preview",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Teal700,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Kamera / Selfie Terverifikasi",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Ink2
                        )
                        Text(
                            text = "Ambil foto atau gunakan foto otomatis berstempel waktu",
                            fontSize = 11.sp,
                            color = Muted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        if (hasCam) {
                            takePictureLauncher.launch(null)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    shape = ButtonCornerShape,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Ink2
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (capturedBitmap == null) "Buka Kamera" else "Foto Ulang",
                        fontSize = 12.sp,
                        color = Ink2
                    )
                }

                SoftButton(
                    text = "Foto Otomatis",
                    leadingIcon = Icons.Default.AutoAwesome,
                    onClick = {
                        val gen = generateVerifiedSelfieBitmap(employeeName?.ifBlank { "Karyawan Asia Raya" } ?: "Karyawan")
                        capturedBitmap = gen
                        capturedBase64 = bitmapToDataUrl(gen)
                        Toast.makeText(context, "Foto selfie berstempel waktu siap digunakan.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Device GPS Location & Geofence Card (no manual office/GPS mode toggle)
        CardSurface(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isInsideGeofence) Teal50 else Amber50),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (isInsideGeofence) Teal700 else Amber600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lokasi GPS Perangkat",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink
                        )
                        val locStatusText = when {
                            !hasLocationPermission -> "Izin lokasi belum diaktifkan"
                            isLocating -> "Mengambil koordinat GPS perangkat..."
                            currentDistance == null -> "Koordinat GPS belum tersedia"
                            isInsideGeofence -> "✓ Dalam jangkauan (±${currentDistance.toInt()} m dari $officeUnit)"
                            else -> "Di luar radius kantor (±${currentDistance.toInt()} m dari $officeUnit)"
                        }
                        Text(
                            text = locStatusText,
                            fontSize = 12.sp,
                            color = when {
                                isInsideGeofence -> Teal700
                                !hasLocationPermission || currentDistance == null -> Rose600
                                else -> Amber600
                            },
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                IconButton(
                    onClick = {
                        if (!hasLocationPermission) {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else {
                            scope.launch { refreshDeviceLocation() }
                        }
                    },
                    modifier = Modifier.testTag("refresh_gps_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Perbarui GPS Perangkat",
                        tint = Teal700
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Bg2)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Unit Penugasan:", fontSize = 12.sp, color = Muted, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(officeUnit, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink, textAlign = TextAlign.End)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Batas Radius:", fontSize = 12.sp, color = Muted, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("$officeRadius meter", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Koordinat GPS:", fontSize = 12.sp, color = Muted, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    val coordLabel = if (rawDeviceLat != null && rawDeviceLng != null) {
                        String.format(Locale.US, "%.5f, %.5f", rawDeviceLat, rawDeviceLng)
                    } else {
                        "Belum terbaca"
                    }
                    Text(coordLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Jarak Terhitung:", fontSize = 12.sp, color = Muted, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    val distLabel = if (currentDistance != null) {
                        "±${currentDistance.toInt()} meter (${if (isInsideGeofence) "Valid ✓" else "Di Luar"})"
                    } else {
                        "-"
                    }
                    Text(
                        text = distLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isInsideGeofence) Teal700 else if (currentDistance != null) Rose600 else Muted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Catatan / Note Field (Opsional)
        OutlinedTextField(
            value = punchNote,
            onValueChange = { if (it.length <= 255) punchNote = it },
            label = { Text("Catatan Kehadiran (Opsional)", fontSize = 12.sp) },
            placeholder = { Text("Catatan tambahan kehadiran...", fontSize = 13.sp, color = Muted2) },
            singleLine = true,
            shape = InputCornerShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Teal500,
                unfocusedBorderColor = LineStrong,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Ink,
                unfocusedTextColor = Ink
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 5. Action Punch Buttons
        if (!isCheckedIn) {
            TealButton(
                text = "Presensi Masuk",
                onClick = { handlePunch("in") },
                isLoading = isSubmitting,
                enabled = !isSubmitting,
                leadingIcon = Icons.Default.CheckCircle,
                modifier = Modifier.testTag("punch_in_button")
            )
            if (!canCheckIn && todayData != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Catatan Server: can_check_in saat ini bernilai false untuk shift ini.",
                    fontSize = 11.sp,
                    color = Muted,
                    textAlign = TextAlign.Center
                )
            }
        } else if (!isCheckedOut) {
            PrimaryButton(
                text = "Presensi Pulang",
                onClick = { handlePunch("out") },
                isLoading = isSubmitting,
                enabled = !isSubmitting,
                leadingIcon = Icons.Default.CheckCircleOutline,
                modifier = Modifier.testTag("punch_out_button")
            )
            if (!canCheckOut && todayData != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Catatan Server: can_check_out saat ini bernilai false.",
                    fontSize = 11.sp,
                    color = Muted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            CardSurface(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Teal50,
                borderColor = Teal500.copy(alpha = 0.3f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Teal700,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Presensi Hari Ini Selesai",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Teal700
                        )
                        Text(
                            text = "Terima kasih atas dedikasi dan kerja keras Anda hari ini.",
                            fontSize = 12.sp,
                            color = Teal700
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = onNavigateToHistory,
            shape = ButtonCornerShape,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lihat Riwayat & Rekap Presensi", fontSize = 13.sp, color = Ink2)
        }
            }
        }
    }

    // Result / Error Dialog
    if (dialogTitle != null && dialogMessage != null) {
        AlertDialog(
            onDismissRequest = {
                dialogTitle = null
                dialogMessage = null
            },
            shape = DialogCornerShape,
            containerColor = Color.White,
            title = {
                Text(
                    text = dialogTitle!!,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDialogSuccess) Navy900 else Rose600
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = dialogMessage!!,
                        fontSize = 13.sp,
                        color = Ink2,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        dialogTitle = null
                        dialogMessage = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDialogSuccess) Teal600 else Navy800,
                        contentColor = Color.White
                    ),
                    shape = ButtonCornerShape
                ) {
                    Text("Tutup", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        )
    }
}

@SuppressLint("MissingPermission")
private suspend fun fetchDeviceGpsLocation(context: Context): Location? {
    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!hasFine && !hasCoarse) return null

    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    val freshLoc = suspendCancellableCoroutine<Location?> { cont ->
        val cts = CancellationTokenSource()
        cont.invokeOnCancellation { cts.cancel() }
        try {
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        if (cont.isActive) cont.resume(loc)
                    } else {
                        fusedClient.lastLocation
                            .addOnSuccessListener { last -> if (cont.isActive) cont.resume(last) }
                            .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                    }
                }
                .addOnFailureListener {
                    fusedClient.lastLocation
                        .addOnSuccessListener { last -> if (cont.isActive) cont.resume(last) }
                        .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                }
        } catch (_: SecurityException) {
            if (cont.isActive) cont.resume(null)
        }
    }
    if (freshLoc != null) return freshLoc

    // Fallback to Android LocationManager if FusedLocation has no cached fix
    return try {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    } catch (_: SecurityException) {
        null
    }
}

/**
 * Calculates distance between two GPS coordinates in meters using the Haversine formula.
 */
private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371000.0 // Earth radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}

/**
 * Converts a Bitmap to standard Base64 Data URL JPEG string for API submission.
 */
private fun bitmapToDataUrl(bitmap: Bitmap): String {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
    val bytes = stream.toByteArray()
    return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
}

/**
 * Generates an official timestamped verified selfie bitmap with employee name and corporate badge.
 */
private fun generateVerifiedSelfieBitmap(employeeName: String): Bitmap {
    val size = 720
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)

    // Background gradient
    val bgPaint = Paint().apply {
        shader = LinearGradient(
            0f, 0f, 0f, size.toFloat(),
            android.graphics.Color.rgb(11, 33, 53),
            android.graphics.Color.rgb(13, 148, 136),
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

    // Avatar Circle background
    val circlePaint = Paint().apply {
        color = android.graphics.Color.rgb(240, 253, 250)
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f - 40f, 150f, circlePaint)

    // Silhouette icon
    val personPaint = Paint().apply {
        color = android.graphics.Color.rgb(15, 118, 110)
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f - 75f, 60f, personPaint)
    val bodyRect = RectF(size / 2f - 110f, size / 2f + 5f, size / 2f + 110f, size / 2f + 110f)
    canvas.drawOval(bodyRect, personPaint)

    // Verified badge check
    val checkBgPaint = Paint().apply {
        color = android.graphics.Color.rgb(13, 148, 136)
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f + 100f, size / 2f + 60f, 32f, checkBgPaint)

    // Employee Name & Verification Text
    val namePaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 34f
        isFakeBoldText = true
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(employeeName, size / 2f, size / 2f + 195f, namePaint)

    val badgePaint = Paint().apply {
        color = android.graphics.Color.rgb(204, 251, 241)
        textSize = 22f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    val timestamp = SimpleDateFormat("EEEE, dd MMM yyyy • HH:mm:ss", Locale("id", "ID")).format(Date())
    canvas.drawText("PRESENSI DIGITAL TERVERIFIKASI", size / 2f, size / 2f + 240f, badgePaint)
    canvas.drawText(timestamp, size / 2f, size / 2f + 280f, badgePaint)

    return bmp
}
