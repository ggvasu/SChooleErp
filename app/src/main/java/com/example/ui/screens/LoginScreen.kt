package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import kotlinx.coroutines.launch

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
                        Color(0xFFF8FAFC), // Slate 50 (BackgroundSlate)
                        Color(0xFFE2E8F0)  // Slate 200
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
            // Modern ERP Emblem / Icon Header
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0xFF2563EB).copy(alpha = 0.08f), shape = RoundedCornerShape(20.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "ERP Logo",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "CAMPUS ONE",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A), // Slate 900
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )
            Text(
                text = "Integrated ERP Resource Portal",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B), // Slate 500
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Center for Distance Education, BDU",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2563EB), // Accent Slate Blue
                textAlign = TextAlign.Center,
                letterSpacing = 0.25.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login Panel Card with Slate border and elegant shadows
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)), // Slate-200 border
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Portal Authentication",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    // Tab Role Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { selectedRole = "Admin" },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedRole == "Admin") Color(0xFF2563EB) else Color.Transparent,
                                contentColor = if (selectedRole == "Admin") Color.White else Color(0xFF64748B)
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, "Admin", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Admin Portal", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { selectedRole = "Student" },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedRole == "Student") Color(0xFF2563EB) else Color.Transparent,
                                contentColor = if (selectedRole == "Student") Color.White else Color(0xFF64748B)
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Person, "Student", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Student Portal", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Username Input
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text(if (selectedRole == "Admin") "Username / Login ID" else "Student Enrollment ID") },
                        leadingIcon = { Icon(Icons.Default.AccountCircle, null, tint = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            focusedLabelColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
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
                        label = { Text("Security Access Key") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF64748B)) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    tint = Color(0xFF64748B),
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            focusedLabelColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
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
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB))
                            )
                            Text("Remember Me", fontSize = 13.sp, color = Color(0xFF475569))
                        }

                        TextButton(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Please contact the distance education administration office or IT support desk to retrieve your security credentials.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        ) {
                            Text("Forgot Credentials?", color = Color(0xFF2563EB), fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                                    Toast.makeText(context, "Please enter all authentication credentials", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.handleLogin(username.trim(), selectedRole, password, rememberMe) {
                                        onLoginSuccess(selectedRole)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Text("Sign In to Portal", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Embedded direct Google Sheets URL Configurator
                    var showDbConfig by remember { mutableStateOf(false) }
                    var configUrl by remember { mutableStateOf(viewModel.sessionManager.scriptUrl) }
                    var isTestingConn by remember { mutableStateOf(false) }
                    var testSuccess by remember { mutableStateOf<Boolean?>(null) }
                    val scope = rememberCoroutineScope()

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDbConfig = !showDbConfig }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Live Database Connection",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                            Icon(
                                imageVector = if (showDbConfig) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        }

                        // Current connection status badge
                        Row(
                            modifier = Modifier.padding(top = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isUrlValid = configUrl.startsWith("https://script.google.com")
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (isUrlValid) Color(0xFF16A34A) else Color(0xFFDC2626),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isUrlValid) "Live Sheets Synced" else "Configuration Required",
                                fontSize = 11.sp,
                                color = if (isUrlValid) Color(0xFF16A34A) else Color(0xFFDC2626),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        AnimatedVisibility(visible = showDbConfig) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Configure your Google Apps Script Web App URL below to connect this client to your live spreadsheet:",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )

                                OutlinedTextField(
                                    value = configUrl,
                                    onValueChange = { 
                                        configUrl = it
                                        testSuccess = null
                                    },
                                    label = { Text("Google Apps Script URL") },
                                    placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                                    leadingIcon = { Icon(Icons.Default.Link, null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF2563EB),
                                        focusedLabelColor = Color(0xFF2563EB),
                                        unfocusedBorderColor = Color(0xFFE2E8F0)
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Test button
                                    OutlinedButton(
                                        onClick = {
                                            if (!configUrl.startsWith("https://script.google.com")) {
                                                Toast.makeText(context, "Please enter a valid Google Apps Script URL starting with https://script.google.com", Toast.LENGTH_LONG).show()
                                                return@OutlinedButton
                                            }
                                            scope.launch {
                                                isTestingConn = true
                                                testSuccess = null
                                                try {
                                                    val savedUrl = viewModel.sessionManager.scriptUrl
                                                    viewModel.sessionManager.scriptUrl = configUrl.trim()
                                                    val response = viewModel.repository.login(
                                                        com.example.data.models.GenericRequest("login", "test_ping", "Admin", "")
                                                    )
                                                    testSuccess = true
                                                    Toast.makeText(context, "Database Connection Verified Successfully!", Toast.LENGTH_SHORT).show()
                                                } catch (e: Exception) {
                                                    testSuccess = false
                                                    Toast.makeText(context, "Connection Failed: Cannot reach Apps Script server", Toast.LENGTH_LONG).show()
                                                } finally {
                                                    isTestingConn = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFF2563EB)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2563EB)),
                                        enabled = !isTestingConn,
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        if (isTestingConn) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFF2563EB))
                                        } else {
                                            Icon(Icons.Default.CloudSync, null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Test Ping", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Save button
                                    Button(
                                        onClick = {
                                            viewModel.sessionManager.scriptUrl = configUrl.trim()
                                            Toast.makeText(context, "Connection Settings Saved Successfully!", Toast.LENGTH_SHORT).show()
                                            showDbConfig = false
                                        },
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Save Link", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }

                                testSuccess?.let { success ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (success) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                                            contentDescription = null,
                                            tint = if (success) Color(0xFF16A34A) else Color(0xFFDC2626),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (success) "Verification Succeeded: Active live synchronization enabled." else "Verification Failed: Check URL layout and access permissions.",
                                            fontSize = 11.sp,
                                            color = if (success) Color(0xFF15803D) else Color(0xFF991B1B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
