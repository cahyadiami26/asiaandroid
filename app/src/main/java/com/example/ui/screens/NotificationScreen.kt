package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.core.format.Formatters
import com.example.core.ui.components.CardSurface
import com.example.core.ui.components.EmptyState
import com.example.core.ui.components.ResponsiveContainer
import com.example.data.api.model.NotificationDto
import com.example.data.repo.Resource
import com.example.ui.theme.Amber50
import com.example.ui.theme.Amber600
import com.example.ui.theme.Bg
import com.example.ui.theme.Ink
import com.example.ui.theme.Muted
import com.example.ui.theme.Navy900
import com.example.ui.theme.Rose50
import com.example.ui.theme.Rose600
import com.example.ui.theme.Teal50
import com.example.ui.theme.Teal600
import com.example.ui.theme.Teal700
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    onNavigateDestination: (String) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HrisApplication
    val scope = rememberCoroutineScope()

    var notifications by remember { mutableStateOf<List<NotificationDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val reload = {
        scope.launch {
            isLoading = true
            when (val res = app.repository.getNotifications()) {
                is Resource.Success -> notifications = res.data
                else -> {}
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        reload()
    }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notifikasi",
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
                        modifier = Modifier.testTag("notifications_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Navy900)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                app.repository.markAllNotificationsRead()
                                reload()
                            }
                        },
                        modifier = Modifier.testTag("mark_all_notifications_read_button")
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp), tint = Teal700)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tandai Semua", fontSize = 12.sp, color = Teal700, fontWeight = FontWeight.SemiBold)
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
            } else if (notifications.isEmpty()) {
                EmptyState(
                    title = "Tidak Ada Notifikasi",
                    body = "Semua notifikasi pengajuan dan persetujuan akan muncul di sini.",
                    icon = Icons.Default.Notifications
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notifications) { notif ->
                        val (icon, iconColor, iconBg) = when (notif.type) {
                            "success" -> Triple(Icons.Default.CheckCircle, Teal700, Teal50)
                            "warning" -> Triple(Icons.Default.Warning, Amber600, Amber50)
                            "danger" -> Triple(Icons.Default.ErrorOutline, Rose600, Rose50)
                            else -> Triple(Icons.Default.Info, Teal700, Teal50)
                        }

                        CardSurface(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                scope.launch {
                                    app.repository.markNotificationRead(notif.id)
                                    reload()
                                }
                                val dest = when (notif.link) {
                                    "/me/attendance" -> "attendance"
                                    "/me/schedule" -> "schedule"
                                    "/me/leave" -> "leave"
                                    "/me/overtime" -> "overtime"
                                    "/me/payslip" -> "payslips"
                                    "/approvals" -> "approvals"
                                    else -> "home"
                                }
                                onNavigateDestination(dest)
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(iconBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = notif.title,
                                            fontSize = 14.sp,
                                            fontWeight = if (!notif.isRead) FontWeight.Bold else FontWeight.SemiBold,
                                            color = Ink
                                        )
                                        if (!notif.isRead) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Teal600)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = notif.message,
                                        fontSize = 13.sp,
                                        color = if (!notif.isRead) Ink else Muted,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = Formatters.formatIsoDateTime(notif.createdAt),
                                        fontSize = 11.sp,
                                        color = Muted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
