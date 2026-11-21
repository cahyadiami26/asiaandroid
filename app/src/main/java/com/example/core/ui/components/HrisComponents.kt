package com.example.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.Amber50
import com.example.ui.theme.Amber600
import com.example.ui.theme.BadgeCornerShape
import com.example.ui.theme.Bg2
import com.example.ui.theme.ButtonCornerShape
import com.example.ui.theme.CardCornerShape
import com.example.ui.theme.DialogCornerShape
import com.example.ui.theme.Indigo50
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Ink
import com.example.ui.theme.Ink2
import com.example.ui.theme.InputCornerShape
import com.example.ui.theme.Line
import com.example.ui.theme.LineStrong
import com.example.ui.theme.Muted
import com.example.ui.theme.Muted2
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Rose50
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate600
import com.example.ui.theme.Teal100
import com.example.ui.theme.Teal50
import com.example.ui.theme.Teal500
import com.example.ui.theme.Teal600
import com.example.ui.theme.Teal700

// 1. CardSurface
@Composable
fun CardSurface(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = Line,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .clip(CardCornerShape)
            .clickable(onClick = onClick)
    } else {
        modifier
    }

    Card(
        modifier = cardModifier,
        shape = CardCornerShape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

// 2. SectionCard
@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    CardSurface(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (action != null) {
                Spacer(modifier = Modifier.width(8.dp))
                action()
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

// 3. StatusBadge
@Composable
fun StatusBadge(
    status: String?,
    modifier: Modifier = Modifier,
    customLabel: String? = null
) {
    val rawStatus = (status ?: "PENDING").uppercase()
    val (bgColor, textColor, defaultLabel) = when (rawStatus) {
        "PRESENT", "APPROVED", "PAID", "FINANCE_APPROVED" -> Triple(Teal50, Teal700, "Hadir")
        "MANAGER_APPROVED" -> Triple(Teal50, Teal700, "Disetujui Atasan")
        "PENDING", "LATE" -> Triple(Amber50, Amber600, if (rawStatus == "LATE") "Terlambat" else "Menunggu")
        "REJECTED", "ABSENT" -> Triple(Rose50, Rose600, if (rawStatus == "ABSENT") "Tidak Hadir" else "Ditolak")
        "LEAVE", "WFH", "SCHEDULED" -> Triple(Indigo50, Indigo600, if (rawStatus == "LEAVE") "Cuti" else if (rawStatus == "WFH") "WFH" else "Terjadwal")
        "CANCELLED", "OFF" -> Triple(Slate50, Slate600, if (rawStatus == "OFF") "Libur" else "Dibatalkan")
        else -> Triple(Slate50, Slate600, rawStatus)
    }

    val displayLabel = customLabel ?: when (rawStatus) {
        "APPROVED" -> "Disetujui"
        "PAID" -> "Lunas"
        "FINANCE_APPROVED" -> "Disetujui Finance"
        else -> defaultLabel
    }

    Box(
        modifier = modifier
            .clip(BadgeCornerShape)
            .background(bgColor)
            .border(0.5.dp, textColor.copy(alpha = 0.25f), BadgeCornerShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

// 0. ResponsiveContainer
@Composable
fun ResponsiveContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 720.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxWidth),
            content = content
        )
    }
}

// 4. KpiTile
@Composable
fun KpiTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    subValue: String? = null,
    icon: ImageVector? = null,
    accentColor: Color = Teal600
) {
    CardSurface(
        modifier = modifier,
        backgroundColor = Color.White
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (icon != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (!subValue.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subValue,
                fontSize = 11.sp,
                color = Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// 5. Buttons
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null
) {
    val isButtonEnabled = enabled && !isLoading
    val contentTint = if (isButtonEnabled) Color.White else Slate600
    Button(
        onClick = onClick,
        enabled = isButtonEnabled,
        shape = ButtonCornerShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Navy800,
            contentColor = Color.White,
            disabledContainerColor = Bg2,
            disabledContentColor = Slate600
        ),
        modifier = Modifier
            .testTag("primary_button")
            .then(modifier)
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = contentTint,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentTint
                )
            }
        }
    }
}

@Composable
fun TealButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null
) {
    val isButtonEnabled = enabled && !isLoading
    val contentTint = if (isButtonEnabled) Color.White else Slate600
    Button(
        onClick = onClick,
        enabled = isButtonEnabled,
        shape = ButtonCornerShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Teal600,
            contentColor = Color.White,
            disabledContainerColor = Bg2,
            disabledContentColor = Slate600
        ),
        modifier = Modifier
            .testTag("teal_button")
            .then(modifier)
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = contentTint,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentTint
                )
            }
        }
    }
}

@Composable
fun SoftButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    val contentTint = if (enabled) Teal700 else Muted2
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonCornerShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Teal50,
            contentColor = Teal700,
            disabledContainerColor = Bg2,
            disabledContentColor = Muted2
        ),
        modifier = modifier.heightIn(min = 44.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentTint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentTint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val contentTint = if (enabled) Color.White else Muted2
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonCornerShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Rose600,
            contentColor = Color.White,
            disabledContainerColor = LineStrong,
            disabledContentColor = Muted2
        ),
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentTint
        )
    }
}

@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val contentTint = if (enabled) Ink2 else Muted2
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonCornerShape,
        border = BorderStroke(1.dp, LineStrong),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Ink2
        ),
        modifier = modifier.heightIn(min = 44.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = contentTint
        )
    }
}

// 6. Text Field
@Composable
fun HrisTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    errorText: String? = null,
    isPassword: Boolean = false,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    enabled: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Ink2,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 14.sp, color = Muted2) },
            isError = !errorText.isNullOrBlank(),
            singleLine = singleLine,
            maxLines = maxLines,
            enabled = enabled,
            shape = InputCornerShape,
            trailingIcon = trailingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Teal500,
                unfocusedBorderColor = LineStrong,
                errorBorderColor = Rose600,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                errorContainerColor = Color.White,
                focusedTextColor = Ink,
                unfocusedTextColor = Ink
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (!errorText.isNullOrBlank()) {
            Text(
                text = errorText,
                fontSize = 11.sp,
                color = Rose600,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

// 7. Dropdown Field
@Composable
fun <T> HrisDropdown(
    label: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    itemLabel: (T) -> String,
    modifier: Modifier = Modifier,
    placeholder: String = "Pilih item...",
    errorText: String? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Ink2,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(InputCornerShape)
                .background(Color.White)
                .border(1.dp, if (!errorText.isNullOrBlank()) Rose600 else LineStrong, InputCornerShape)
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedItem != null) itemLabel(selectedItem) else placeholder,
                    fontSize = 14.sp,
                    color = if (selectedItem != null) Ink else Muted2
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Muted
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(itemLabel(item), fontSize = 14.sp, color = Ink) },
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        }
        if (!errorText.isNullOrBlank()) {
            Text(
                text = errorText,
                fontSize = 11.sp,
                color = Rose600,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

// 8. EmployeeAvatar
@Composable
fun EmployeeAvatar(
    name: String?,
    modifier: Modifier = Modifier,
    size: Int = 40,
    backgroundColor: Color = Teal600
) {
    val initials = remember(name) {
        val parts = name?.trim()?.split(" ")?.filter { it.isNotEmpty() } ?: emptyList()
        when {
            parts.size >= 2 -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "AR"
        }
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            fontSize = (size * 0.36).sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

// 9. EmptyState
@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    icon: ImageVector = Icons.Default.Inbox,
    ctaText: String? = null,
    onCtaClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Bg2),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Muted2,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink,
            textAlign = TextAlign.Center
        )
        if (!body.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                fontSize = 13.sp,
                color = Muted,
                textAlign = TextAlign.Center
            )
        }
        if (!ctaText.isNullOrBlank() && onCtaClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            SoftButton(
                text = ctaText,
                onClick = onCtaClick
            )
        }
    }
}

// 10. ErrorState
@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = Rose600,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Terjadi Kesalahan",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            fontSize = 13.sp,
            color = Muted,
            textAlign = TextAlign.Center
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = onRetry,
                shape = ButtonCornerShape,
                border = BorderStroke(1.dp, LineStrong)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Coba Lagi", fontSize = 13.sp, color = Ink2)
            }
        }
    }
}

// 11. LoadingSkeleton
@Composable
fun LoadingSkeleton(
    modifier: Modifier = Modifier,
    cardCount: Int = 3
) {
    Column(modifier = modifier.fillMaxWidth()) {
        repeat(cardCount) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .height(84.dp)
                    .clip(CardCornerShape)
                    .background(Color.White)
                    .border(1.dp, Line, CardCornerShape)
                    .padding(16.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(16.dp)
                            .clip(BadgeCornerShape)
                            .background(Bg2)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(12.dp)
                            .clip(BadgeCornerShape)
                            .background(Bg2)
                    )
                }
            }
        }
    }
}

// 12. OfflineBanner
@Composable
fun OfflineBanner(
    modifier: Modifier = Modifier,
    message: String = "Anda sedang offline. Menampilkan data lokal terakhir."
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Amber50,
        border = BorderStroke(1.dp, Amber600.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                tint = Amber600,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                fontSize = 12.sp,
                color = Amber600,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// 13. ConfirmationDialog
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Ya, Lanjutkan",
    cancelText: String = "Batal",
    isDanger: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = DialogCornerShape,
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = Muted,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(cancelText, fontSize = 13.sp, color = Muted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDanger) Rose600 else Teal600,
                            contentColor = Color.White
                        ),
                        shape = ButtonCornerShape
                    ) {
                        Text(confirmText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }
    }
}
