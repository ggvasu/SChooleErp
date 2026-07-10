package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.viewmodel.LoginViewModel
import com.example.ui.components.LottieLoader

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (String) -> Unit,
    onConfigureUrl: () -> Unit
) {
    val context = LocalContext.current
    val statusMsg by viewModel.statusMessage.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Admin") } // "Admin" or "Student"
    var rememberMe by remember { mutableStateOf(viewModel.sessionManager.rememberMe) }
    var showPassword by remember { mutableStateOf(false) }

    // Prefill username if remembered
    LaunchedEffect(Unit) {
        if (viewModel.sessionManager.rememberMe) {
            username = viewModel.sessionManager.username
        }
    }

    LaunchedEffect(statusMsg) {
        statusMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearStatusMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAF7F0), // Ivory Sandalwood
                        Color(0xFFF3EDE2), // Creamy Traditional Ivory
                        Color(0xFFE9DEC4)  // Pale Ochre Accent
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant Traditional Banner Image
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.dp, Color(0xFFDFD1B8))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_south_indian_login_banner_1782908329236),
                    contentDescription = "University Gateway Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(18.dp))


            Text(
                text = "Center for Distance Education",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C1B11), // Deep Charcoal Bronze
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Bharathidasan University",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFC2410C), // Elegant Traditional Saffron
                textAlign = TextAlign.Center,
                letterSpacing = 0.25.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login Panel Card with Saffron Gold frame
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.2.dp, Color(0xFFDFD1B8)), // Sandalwood border
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Portal Authentication",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C1B11)
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    // Tab Role Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF7F4EB))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { selectedRole = "Admin" },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedRole == "Admin") Color(0xFFC2410C) else Color.Transparent,
                                contentColor = if (selectedRole == "Admin") Color.White else Color(0xFF2C1B11).copy(alpha = 0.6f)
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, "Admin", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Admin", fontSize = 13.sp)
                        }

                        Button(
                            onClick = { selectedRole = "Student" },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedRole == "Student") Color(0xFFC2410C) else Color.Transparent,
                                contentColor = if (selectedRole == "Student") Color.White else Color(0xFF2C1B11).copy(alpha = 0.6f)
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Person, "Student", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Student", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Username Input
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text(if (selectedRole == "Admin") "Username" else "Student ID") },
                        leadingIcon = { Icon(Icons.Default.AccountCircle, null, tint = Color(0xFF8B5A2B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC2410C),
                            focusedLabelColor = Color(0xFFC2410C),
                            unfocusedBorderColor = Color(0xFFDFD1B8)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF8B5A2B)) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    tint = Color(0xFF8B5A2B),
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC2410C),
                            focusedLabelColor = Color(0xFFC2410C),
                            unfocusedBorderColor = Color(0xFFDFD1B8)
                        ),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Remember Me & Forgot Password Layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFC2410C))
                            )
                            Text("Remember Me", fontSize = 13.sp, color = Color(0xFF2C1B11))
                        }

                        TextButton(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Please contact the administration office or your department coordinator to reset your security credentials.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        ) {
                            Text("Forgot Password?", color = Color(0xFFC2410C), fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Button
                    if (isRefreshing) {
                        LottieLoader(modifier = Modifier.size(48.dp))
                    } else {
                        Button(
                            onClick = {
                                if (username.trim().isEmpty() || password.trim().isEmpty()) {
                                    Toast.makeText(context, "Please enter both credentials", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.handleLogin(username.trim(), selectedRole, password, rememberMe) {
                                        onLoginSuccess(selectedRole)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2410C))
                        ) {
                            Text("Authenticate Account", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onConfigureUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFC2410C)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC2410C))
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Configure Live Sheets Link", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
