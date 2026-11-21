package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.HrisApplication
import com.example.core.ui.components.EmployeeAvatar
import com.example.ui.screens.AnnouncementScreen
import com.example.ui.screens.ApprovalScreen
import com.example.ui.screens.AttendanceHistoryScreen
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.ClaimScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaveScreen
import com.example.ui.screens.NotificationScreen
import com.example.ui.screens.OvertimeScreen
import com.example.ui.screens.PayslipScreen
import com.example.ui.screens.PdfViewerScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.TaxSlipScreen
import com.example.ui.theme.Amber600
import com.example.ui.theme.Bg
import com.example.ui.theme.CardCornerShape
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Ink
import com.example.ui.theme.Line
import com.example.ui.theme.Muted
import com.example.ui.theme.Navy900
import com.example.ui.theme.Rose600
import com.example.ui.theme.SheetCornerShape
import com.example.ui.theme.Teal50
import com.example.ui.theme.Teal600
import com.example.ui.theme.Teal700

sealed interface AppRoute {
    data class MainTab(val tabIndex: Int) : AppRoute // 0: Beranda, 1: Presensi, 2: Jadwal, 3: Cuti
    data object AttendanceHistory : AppRoute
    data class Announcements(val initialId: Int? = null) : AppRoute
    data object Notifications : AppRoute
    data object Approvals : AppRoute
    data object ProfileSettings : AppRoute
    data object Payslips : AppRoute
    data object Overtime : AppRoute
    data object Claims : AppRoute
    data object TaxSlips : AppRoute
    data class PdfViewer(val title: String, val referenceNumber: String) : AppRoute
}

internal fun pushRoute(stack: MutableList<AppRoute>, route: AppRoute) {
    if (stack.isEmpty()) {
        stack.add(AppRoute.MainTab(0))
    }
    if (stack.lastOrNull() == route) return
    stack.add(route)
    while (stack.size > 30) {
        stack.removeAt(1) // Keep MainTab(0) anchored at index 0
    }
}

internal fun popRoute(stack: MutableList<AppRoute>): Boolean {
    return if (stack.size > 1) {
        stack.removeAt(stack.lastIndex)
        true
    } else {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HrisApplication

    val backStack = remember { mutableStateListOf<AppRoute>(AppRoute.MainTab(0)) }
    val currentRoute = backStack.lastOrNull() ?: AppRoute.MainTab(0)

    val canApprove by app.repository.canApproveFlow.collectAsState(initial = false)
    val employeeName by app.repository.employeeNameFlow.collectAsState(initial = null)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showMoreSheet by remember { mutableStateOf(false) }

    val navigateTo: (AppRoute) -> Unit = { route ->
        pushRoute(backStack, route)
    }

    val navigateBack: () -> Unit = {
        if (showMoreSheet) {
            showMoreSheet = false
        } else {
            popRoute(backStack)
        }
    }

    // Always pop back through visited screens/tabs instead of closing or logging out
    BackHandler(enabled = showMoreSheet || backStack.size > 1) {
        navigateBack()
    }

    when (currentRoute) {
        AppRoute.AttendanceHistory -> {
            AttendanceHistoryScreen(onBack = navigateBack)
            return
        }
        is AppRoute.Announcements -> {
            AnnouncementScreen(
                initialAnnouncementId = currentRoute.initialId,
                onBack = navigateBack
            )
            return
        }
        AppRoute.Notifications -> {
            NotificationScreen(
                onBack = navigateBack,
                onNavigateDestination = { dest ->
                    when (dest) {
                        "attendance" -> navigateTo(AppRoute.MainTab(1))
                        "schedule" -> navigateTo(AppRoute.MainTab(2))
                        "leave" -> navigateTo(AppRoute.MainTab(3))
                        "payslips" -> navigateTo(AppRoute.Payslips)
                        "overtime" -> navigateTo(AppRoute.Overtime)
                        "approvals" -> navigateTo(AppRoute.Approvals)
                        else -> navigateTo(AppRoute.MainTab(0))
                    }
                }
            )
            return
        }
        AppRoute.Approvals -> {
            ApprovalScreen(onBack = navigateBack)
            return
        }
        AppRoute.ProfileSettings -> {
            ProfileSettingsScreen(
                onBack = navigateBack,
                onLogoutSuccess = onLogout
            )
            return
        }
        AppRoute.Payslips -> {
            PayslipScreen(
                onBack = navigateBack,
                onViewPdf = { title, ref ->
                    navigateTo(AppRoute.PdfViewer(title = title, referenceNumber = ref))
                }
            )
            return
        }
        AppRoute.Overtime -> {
            OvertimeScreen(onBack = navigateBack)
            return
        }
        AppRoute.Claims -> {
            ClaimScreen(onBack = navigateBack)
            return
        }
        AppRoute.TaxSlips -> {
            TaxSlipScreen(
                onBack = navigateBack,
                onViewPdf = { title, ref ->
                    navigateTo(AppRoute.PdfViewer(title = title, referenceNumber = ref))
                }
            )
            return
        }
        is AppRoute.PdfViewer -> {
            PdfViewerScreen(
                title = currentRoute.title,
                referenceNumber = currentRoute.referenceNumber,
                onBack = navigateBack
            )
            return
        }
        is AppRoute.MainTab -> Unit
    }

    val activeBottomTab = (currentRoute as? AppRoute.MainTab)?.tabIndex ?: 0

    val topBarTitle = when (activeBottomTab) {
        0 -> "Beranda"
        1 -> "Presensi Daring"
        2 -> "Jadwal Kerja"
        3 -> "Manajemen Cuti"
        else -> "HRIS Asia Raya"
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        val isWideLayout = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (isWideLayout) {
                val railItemColors = NavigationRailItemDefaults.colors(
                    indicatorColor = Teal50,
                    selectedIconColor = Teal700,
                    selectedTextColor = Teal700,
                    unselectedIconColor = Muted,
                    unselectedTextColor = Muted
                )
                NavigationRail(
                    containerColor = Color.White,
                    windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    NavigationRailItem(
                        selected = activeBottomTab == 0,
                        onClick = { navigateTo(AppRoute.MainTab(0)) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                        label = { Text("Beranda", fontSize = 11.sp, maxLines = 1) },
                        colors = railItemColors
                    )
                    NavigationRailItem(
                        selected = activeBottomTab == 1,
                        onClick = { navigateTo(AppRoute.MainTab(1)) },
                        icon = { Icon(Icons.Default.Fingerprint, contentDescription = "Presensi") },
                        label = { Text("Presensi", fontSize = 11.sp, maxLines = 1) },
                        colors = railItemColors
                    )
                    NavigationRailItem(
                        selected = activeBottomTab == 2,
                        onClick = { navigateTo(AppRoute.MainTab(2)) },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Jadwal") },
                        label = { Text("Jadwal", fontSize = 11.sp, maxLines = 1) },
                        colors = railItemColors
                    )
                    NavigationRailItem(
                        selected = activeBottomTab == 3,
                        onClick = { navigateTo(AppRoute.MainTab(3)) },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Cuti") },
                        label = { Text("Cuti", fontSize = 11.sp, maxLines = 1) },
                        colors = railItemColors
                    )
                    NavigationRailItem(
                        selected = showMoreSheet,
                        onClick = { showMoreSheet = true },
                        icon = { Icon(Icons.Default.Widgets, contentDescription = "Lainnya") },
                        label = { Text("Lainnya", fontSize = 11.sp, maxLines = 1) },
                        colors = railItemColors
                    )
                }
                VerticalDivider(color = Line, thickness = 1.dp)
            }

            Scaffold(
                topBar = {
                    if (activeBottomTab != 0) {
                        TopAppBar(
                            title = {
                                Text(
                                    text = topBarTitle,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            actions = {
                                IconButton(
                                    onClick = { navigateTo(AppRoute.Notifications) },
                                    modifier = Modifier.testTag("topbar_notifications_button")
                                ) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = Rose600, modifier = Modifier.size(6.dp))
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "Notifikasi",
                                            tint = Navy900
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { navigateTo(AppRoute.ProfileSettings) },
                                    modifier = Modifier.testTag("topbar_profile_button")
                                ) {
                                    EmployeeAvatar(name = employeeName, size = 32, backgroundColor = Teal600)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                        )
                    }
                },
                bottomBar = {
                    if (!isWideLayout) {
                        val navItemColors = NavigationBarItemDefaults.colors(
                            indicatorColor = Teal50,
                            selectedIconColor = Teal700,
                            selectedTextColor = Teal700,
                            unselectedIconColor = Muted,
                            unselectedTextColor = Muted
                        )
                        NavigationBar(
                            containerColor = Color.White,
                            tonalElevation = 4.dp,
                            windowInsets = NavigationBarDefaults.windowInsets,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            NavigationBarItem(
                                selected = activeBottomTab == 0,
                                onClick = { navigateTo(AppRoute.MainTab(0)) },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                                label = { Text("Beranda", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = activeBottomTab == 1,
                                onClick = { navigateTo(AppRoute.MainTab(1)) },
                                icon = { Icon(Icons.Default.Fingerprint, contentDescription = "Presensi") },
                                label = { Text("Presensi", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = activeBottomTab == 2,
                                onClick = { navigateTo(AppRoute.MainTab(2)) },
                                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Jadwal") },
                                label = { Text("Jadwal", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = activeBottomTab == 3,
                                onClick = { navigateTo(AppRoute.MainTab(3)) },
                                icon = { Icon(Icons.Default.DateRange, contentDescription = "Cuti") },
                                label = { Text("Cuti", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                colors = navItemColors
                            )
                            NavigationBarItem(
                                selected = showMoreSheet,
                                onClick = { showMoreSheet = true },
                                icon = { Icon(Icons.Default.Widgets, contentDescription = "Lainnya") },
                                label = { Text("Lainnya", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                colors = navItemColors
                            )
                        }
                    }
                },
                containerColor = Bg,
                modifier = Modifier.weight(1f)
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (activeBottomTab) {
                        0 -> HomeScreen(
                            onNavigateToAttendance = { navigateTo(AppRoute.MainTab(1)) },
                            onNavigateToSchedule = { navigateTo(AppRoute.MainTab(2)) },
                            onNavigateToLeave = { navigateTo(AppRoute.MainTab(3)) },
                            onNavigateToOvertime = { navigateTo(AppRoute.Overtime) },
                            onNavigateToClaims = { navigateTo(AppRoute.Claims) },
                            onNavigateToPayslips = { navigateTo(AppRoute.Payslips) },
                            onNavigateToApprovals = { navigateTo(AppRoute.Approvals) },
                            onNavigateToAnnouncements = { navigateTo(AppRoute.Announcements(null)) },
                            onNavigateToAnnouncementDetail = { id ->
                                navigateTo(AppRoute.Announcements(id))
                            },
                            onNavigateToNotifications = { navigateTo(AppRoute.Notifications) },
                            onNavigateToProfile = { navigateTo(AppRoute.ProfileSettings) }
                        )
                        1 -> AttendanceScreen(
                            onNavigateToHistory = { navigateTo(AppRoute.AttendanceHistory) }
                        )
                        2 -> ScheduleScreen()
                        3 -> LeaveScreen()
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: "Lainnya" Menu
    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = SheetCornerShape
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = "Menu Layanan Karyawan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
                Spacer(modifier = Modifier.height(14.dp))

                SheetMenuItem(
                    icon = Icons.Default.Payments,
                    title = "Slip Gaji",
                    subtitle = "Rincian take home pay, tunjangan & potongan",
                    color = Teal600,
                    onClick = {
                        showMoreSheet = false
                        navigateTo(AppRoute.Payslips)
                    }
                )

                SheetMenuItem(
                    icon = Icons.Default.WorkHistory,
                    title = "Pengajuan Lembur",
                    subtitle = "Riwayat jam lembur & estimasi kompensasi",
                    color = Amber600,
                    onClick = {
                        showMoreSheet = false
                        navigateTo(AppRoute.Overtime)
                    }
                )

                SheetMenuItem(
                    icon = Icons.Default.ReceiptLong,
                    title = "Klaim Biaya (Reimbursement)",
                    subtitle = "Penggantian biaya medis, transportasi dinas",
                    color = Teal700,
                    onClick = {
                        showMoreSheet = false
                        navigateTo(AppRoute.Claims)
                    }
                )

                SheetMenuItem(
                    icon = Icons.Default.Description,
                    title = "Bukti Potong Pajak 1721-A1",
                    subtitle = "Dokumen pajak penghasilan resmi tahunan",
                    color = Indigo600,
                    onClick = {
                        showMoreSheet = false
                        navigateTo(AppRoute.TaxSlips)
                    }
                )

                SheetMenuItem(
                    icon = Icons.Default.Campaign,
                    title = "Pengumuman Perusahaan",
                    subtitle = "Informasi dan surat edaran PT Asia Raya",
                    color = Teal600,
                    onClick = {
                        showMoreSheet = false
                        navigateTo(AppRoute.Announcements(null))
                    }
                )

                if (canApprove) {
                    SheetMenuItem(
                        icon = Icons.Default.AssignmentTurnedIn,
                        title = "Kotak Persetujuan (Manager/HR)",
                        subtitle = "Persetujuan cuti, lembur, jadwal & klaim",
                        color = Rose600,
                        onClick = {
                            showMoreSheet = false
                            navigateTo(AppRoute.Approvals)
                        }
                    )
                }

                HorizontalDivider(color = Line, modifier = Modifier.padding(vertical = 6.dp))

                SheetMenuItem(
                    icon = Icons.Default.Person,
                    title = "Profil & Pengaturan Server",
                    subtitle = "Identitas karyawan, alamat server, sesi akun",
                    color = Navy900,
                    onClick = {
                        showMoreSheet = false
                        navigateTo(AppRoute.ProfileSettings)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SheetMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardCornerShape)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Muted
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Muted
        )
    }
}
