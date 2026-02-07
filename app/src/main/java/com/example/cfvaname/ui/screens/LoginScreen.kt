package com.example.cfvaname.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cfvaname.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onLogin: (email: String, password: String) -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VenameBgLight)
            .verticalScroll(rememberScrollState())
    ) {
        // === HEADER ===
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(VenamePrimaryDark, VenamePrimary)
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 12.dp, bottom = 48.dp)
        ) {
            Column {
                // Back button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.offset(x = (-12).dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Logo
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Hub,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Selamat Datang",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Masuk ke akun Sistem Cerdas Vename",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        // === LOGIN FORM CARD ===
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-24).dp)
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Login",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    placeholder = { Text("masukkan@email.com") },
                    leadingIcon = {
                        Icon(Icons.Filled.Email, contentDescription = null, tint = VenamePrimary)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VenamePrimary,
                        cursorColor = VenamePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("••••••••") },
                    leadingIcon = {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = VenamePrimary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Sembunyikan" else "Tampilkan",
                                tint = TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (email.isNotBlank() && password.isNotBlank()) {
                                onLogin(email, password)
                            }
                        }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VenamePrimary,
                        cursorColor = VenamePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Error message
                AnimatedVisibility(visible = errorMessage != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = StatusError.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = StatusError,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = StatusError,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Login button
                Button(
                    onClick = { onLogin(email, password) },
                    enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VenamePrimary,
                        disabledContainerColor = VenamePrimary.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Memproses...")
                    } else {
                        Icon(
                            Icons.Filled.Login,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Masuk",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Info box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = VenamePrimaryLight.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = VenamePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Demo Accounts:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = VenamePrimaryDark
                            )
                            Text(
                                text = "Admin: admin@vename.com / admin123\nUser: user@vename.com / user123",
                                fontSize = 11.sp,
                                color = VenamePrimaryDark.copy(alpha = 0.8f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "© 2026 Sistem Cerdas Vename",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}