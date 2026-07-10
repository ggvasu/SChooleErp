package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ERPViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ERPViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val sessionManager = viewModel.sessionManager

    var scriptUrl by remember { mutableStateOf(sessionManager.scriptUrl) }
    var isDarkMode by remember { mutableStateOf(sessionManager.isDarkMode) }
    var showSetupGuide by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Connection") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dark Display Theme", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Configure eye-strain prevention", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = {
                            isDarkMode = it
                            sessionManager.isDarkMode = it
                            Toast.makeText(context, "Theme mode modified successfully", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }

            // Google Sheets Connection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudSync, "Sync", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google Sheets Cloud Sync", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Text(
                        "Synchronize student admissions, fee logs, courses, payments, and audit logs with your custom Google Sheet database live.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    OutlinedTextField(
                        value = scriptUrl,
                        onValueChange = { scriptUrl = it },
                        label = { Text("Google Apps Script Web App URL") },
                        placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Link, null) }
                    )

                    Button(
                        onClick = {
                            sessionManager.scriptUrl = scriptUrl.trim()
                            Toast.makeText(context, "Connection URL configured. Refreshing database...", Toast.LENGTH_SHORT).show()
                            viewModel.refreshAllData()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save URL Link")
                    }

                    HorizontalDivider()

                    TextButton(
                        onClick = { showSetupGuide = !showSetupGuide }
                    ) {
                        Icon(
                            if (showSetupGuide) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Need Setup Help? Read Deployment Instructions", fontWeight = FontWeight.SemiBold)
                    }

                    AnimatedVisibility(visible = showSetupGuide) {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Deployment Steps:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("1. Create a Google Sheet and copy its ID from the URL browser tab.", fontSize = 12.sp)
                            Text("2. Open Extensions > Apps Script in your sheet.", fontSize = 12.sp)
                            Text("3. Copy & paste the code found inside `/GoogleAppsScript.js` file of this project.", fontSize = 12.sp)
                            Text("4. Edit YOUR_SPREADSHEET_ID_HERE with your Sheet's ID (or leave it empty to bind active Sheet).", fontSize = 12.sp)
                            Text("5. Click Deploy > New Deployment. Choose Web App. Configure: 'Execute as Me' and 'Who has access: Anyone'.", fontSize = 12.sp)
                            Text("6. Paste the deployed URL into the input field above, and press Save!", fontSize = 12.sp)
                        }
                    }
                }
            }

            // About ERP Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("About ERP System", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Campus One ERP Client App", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("Client Version: 1.0.0 (Production Build)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text("Android Target SDK: 36 (Android 16)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text("Developer: Google AI Studio Coding Assistant", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        }
    }
}
