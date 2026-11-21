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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import com.example.HrisApplication
import com.example.core.format.Formatters
import com.example.core.ui.components.CardSurface
import com.example.core.ui.components.EmptyState
import com.example.core.ui.components.ResponsiveContainer
import com.example.data.api.model.AnnouncementDto
import com.example.data.repo.Resource
import com.example.ui.theme.BadgeCornerShape
import com.example.ui.theme.Bg
import com.example.ui.theme.ButtonCornerShape
import com.example.ui.theme.DialogCornerShape
import com.example.ui.theme.Ink
import com.example.ui.theme.Ink2
import com.example.ui.theme.InputCornerShape
import com.example.ui.theme.Line
import com.example.ui.theme.LineStrong
import com.example.ui.theme.Muted
import com.example.ui.theme.Navy900
import com.example.ui.theme.Teal50
import com.example.ui.theme.Teal500
import com.example.ui.theme.Teal600
import com.example.ui.theme.Teal700
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementScreen(
    initialAnnouncementId: Int? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HrisApplication
    val scope = rememberCoroutineScope()

    var announcements by remember { mutableStateOf<List<AnnouncementDto>>(emptyList()) }
    var selectedItem by remember { mutableStateOf<AnnouncementDto?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var onlyUnread by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val reload = {
        scope.launch {
            isLoading = true
            when (val res = app.repository.getAnnouncements()) {
                is Resource.Success -> {
                    announcements = res.data
                    if (initialAnnouncementId != null) {
                        selectedItem = res.data.find { it.id == initialAnnouncementId }
                        if (selectedItem != null) {
                            app.repository.markAnnouncementRead(selectedItem!!.id)
                        }
                    }
                }
                else -> {}
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        reload()
    }

    val filteredList = announcements.filter { item ->
        val matchesQuery = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.excerpt.contains(searchQuery, ignoreCase = true)
        val matchesUnread = !onlyUnread || !item.isRead
        matchesQuery && matchesUnread
    }

    BackHandler(enabled = selectedItem == null) { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengumuman Perusahaan", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Navy900, maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
            // Search & Filter Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari pengumuman...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Muted) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Hapus", tint = Muted)
                            }
                        }
                    },
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = onlyUnread,
                        onClick = { onlyUnread = !onlyUnread },
                        label = { Text("Belum dibaca saja", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Teal50,
                            selectedLabelColor = Teal700
                        )
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Teal600)
                }
            } else if (filteredList.isEmpty()) {
                EmptyState(
                    title = "Tidak Ada Pengumuman",
                    body = if (searchQuery.isNotBlank()) "Tidak ditemukan pengumuman dengan kata kunci \"$searchQuery\"." else "Belum ada informasi terbaru untuk Anda.",
                    icon = Icons.Default.Campaign
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList) { item ->
                        CardSurface(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                selectedItem = item
                                scope.launch {
                                    app.repository.markAnnouncementRead(item.id)
                                    reload()
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!item.isRead) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Teal600)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    if (item.isPinned) {
                                        Text("📌 ", fontSize = 12.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(BadgeCornerShape)
                                            .background(Teal50)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.category,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Teal700
                                        )
                                    }
                                }
                                Text(
                                    text = Formatters.formatDateShort(item.publishedAt?.take(10)),
                                    fontSize = 11.sp,
                                    color = Muted
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = item.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = item.excerpt,
                                fontSize = 13.sp,
                                color = Muted,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        }
    }

    // Announcement Full Detail Reader Dialog
    if (selectedItem != null) {
        val item = selectedItem!!
        val parsedBody = remember(item.body) {
            if (!item.body.isNullOrBlank()) {
                HtmlCompat.fromHtml(item.body, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
            } else {
                item.excerpt
            }
        }

        AlertDialog(
            onDismissRequest = { selectedItem = null },
            shape = DialogCornerShape,
            containerColor = Color.White,
            title = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(BadgeCornerShape)
                                .background(Teal50)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(item.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Teal700)
                        }
                        Text(Formatters.formatDateShort(item.publishedAt?.take(10)), fontSize = 11.sp, color = Muted)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(item.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy900)
                    Text("Oleh: ${item.author ?: "HR Corporate"}", fontSize = 12.sp, color = Muted)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    HorizontalDivider(color = Line)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = parsedBody,
                        fontSize = 13.sp,
                        color = Ink,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedItem = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900, contentColor = Color.White),
                    shape = ButtonCornerShape
                ) {
                    Text("Tutup", fontSize = 13.sp, color = Color.White)
                }
            }
        )
    }
}
