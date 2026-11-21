package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.HrisApplication
import com.example.core.ui.components.CardSurface
import com.example.core.ui.components.PrimaryButton
import com.example.core.ui.components.SoftButton
import com.example.data.api.model.LoginRequest
import com.example.data.repo.Resource
import com.example.ui.theme.Bg
import com.example.ui.theme.Bg2
import com.example.ui.theme.CardCornerShape
import com.example.ui.theme.Ink
import com.example.ui.theme.Ink2
import com.example.ui.theme.InputCornerShape
import com.example.ui.theme.Line
import com.example.ui.theme.LineStrong
import com.example.ui.theme.Muted
import com.example.ui.theme.Muted2
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Rose600
import com.example.ui.theme.Teal500
import com.example.ui.theme.Teal600
import com.example.ui.theme.Teal700
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HrisApplication
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val currentServerUrl by app.repository.serverUrlFlow.collectAsState(initial = "https://hris.asiaraya.co.id")

    var serverUrlInput by remember(currentServerUrl) { mutableStateOf(currentServerUrl) }
    var showServerConfig by remember { mutableStateOf(false) }

    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var serverTestMessage by remember { mutableStateOf<String?>(null) }

    val handleLogin: (String, String) -> Unit = { idInput, pwdInput ->
        if (idInput.isBlank()) {
            errorMessage = "Email, NIK, atau Username wajib diisi."
        } else if (pwdInput.isBlank()) {
            errorMessage = "Kata sandi wajib diisi."
        } else {
            isLoading = true
            errorMessage = null
            scope.launch {
                val res = app.repository.login(
                    LoginRequest(
                        identifier = idInput.trim(),
                        password = pwdInput,
                        deviceName = "Android Device",
                        appVersion = "1.0.0"
                    )
                )
                isLoading = false
                when (res) {
                    is Resource.Success -> {
                        Toast.makeText(context, "Selamat datang, ${res.data.fullName}", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    }
                    is Resource.Error -> {
                        errorMessage = res.message
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

        // Branding Logo
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Navy900),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AR",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Teal500
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "HRIS Asia Raya",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Navy900,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = "Silakan masuk dengan akun karyawan Anda",
            fontSize = 13.sp,
            color = Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Main Login Card
        CardSurface(modifier = Modifier.fillMaxWidth()) {
            // Identifier Input
            Text(
                text = "Email / NIK / Username",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Ink2,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            OutlinedTextField(
                value = identifier,
                onValueChange = {
                    identifier = it
                    errorMessage = null
                },
                placeholder = { Text("Email, NIK, atau username", fontSize = 14.sp, color = Muted2) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                shape = InputCornerShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Teal500,
                    unfocusedBorderColor = LineStrong,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("identifier_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password Input
            Text(
                text = "Kata Sandi",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Ink2,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                placeholder = { Text("Masukkan kata sandi", fontSize = 14.sp, color = Muted2) },
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    handleLogin(identifier, password)
                }),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible) "Sembunyikan sandi" else "Tampilkan sandi",
                            tint = Muted
                        )
                    }
                },
                shape = InputCornerShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Teal500,
                    unfocusedBorderColor = LineStrong,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input")
            )

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage!!,
                    fontSize = 12.sp,
                    color = Rose600,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            PrimaryButton(
                text = "Masuk",
                onClick = {
                    focusManager.clearFocus()
                    handleLogin(identifier, password)
                },
                isLoading = isLoading,
                modifier = Modifier.testTag("login_button")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Collapsible Server Address Setting
        Card(
            shape = CardCornerShape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Line),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showServerConfig = !showServerConfig },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = Teal700,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Alamat Server",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink2
                            )
                            Text(
                                text = currentServerUrl,
                                fontSize = 11.sp,
                                color = Muted,
                                maxLines = 1
                            )
                        }
                    }
                    Icon(
                        imageVector = if (showServerConfig) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Muted
                    )
                }

                AnimatedVisibility(visible = showServerConfig) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        OutlinedTextField(
                            value = serverUrlInput,
                            onValueChange = {
                                serverUrlInput = it
                                serverTestMessage = null
                            },
                            singleLine = true,
                            placeholder = { Text("https://hris.asiaraya.co.id", fontSize = 13.sp) },
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        app.repository.updateServerUrl("https://hris.asiaraya.co.id")
                                        serverUrlInput = "https://hris.asiaraya.co.id"
                                        serverTestMessage = "Diatur ke server bawaan."
                                    }
                                }
                            ) {
                                Text("Bawaan", fontSize = 12.sp, color = Muted)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            SoftButton(
                                text = "Simpan Server",
                                onClick = {
                                    scope.launch {
                                        app.repository.updateServerUrl(serverUrlInput)
                                        serverTestMessage = "Alamat server diperbarui."
                                    }
                                }
                            )
                        }
                        if (!serverTestMessage.isNullOrBlank()) {
                            Text(
                                text = serverTestMessage!!,
                                fontSize = 11.sp,
                                color = Teal700,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(16.dp))

        // Version metadata
        Text(
            text = "HRIS v2.17.5 • Android App v1.0.0\nPT Asia Raya Group",
            fontSize = 11.sp,
            color = Muted,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
        }
    }
}
