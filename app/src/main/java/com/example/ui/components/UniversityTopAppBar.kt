package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversityTopAppBar(
    title: String,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    role: String,
    userName: String,
    userId: String,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    onOpenDrawer: (() -> Unit)? = null,
    additionalActions: @Composable (RowScope.() -> Unit)? = null
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isSmallScreen = screenWidth < 600

    var showProfileMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // University logo (if not showing hamburger menu drawer)
                if (onOpenDrawer == null) {
                    Card(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_icon_1782835278641),
                            contentDescription = "University Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                // Brand text layout
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSmallScreen) {
                        // Compact screen styling
                        Text(
                            text = "BDU CDE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        // Desktop/Tablet rich styling
                        Text(
                            text = "Center for Distance Education",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Bharathidasan University • $title",
                            fontSize = 12.sp,
                            color = Color(0xFF93C5FD), // Light bright blue
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        navigationIcon = {
            if (onOpenDrawer != null) {
                IconButton(onClick = onOpenDrawer) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Drawer Menu",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                // Logo next to hamburger on mobile drawer topbars
                Card(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon_1782835278641),
                        contentDescription = "University Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                    )
                }
            }
        },
        actions = {
            // Additional custom actions (e.g., Campaign button for Admin, or Change Password for Student)
            if (additionalActions != null) {
                additionalActions()
            }

            // Standard Refresh Button
            IconButton(onClick = onRefresh) {
                if (isRefreshing) {
                    LottieLoader(modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Refresh, "Refresh Data", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Professional Profile Indicator Pill
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { showProfileMenu = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    // Profile Avatar Circle with first letter of user name
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF60A5FA), Color(0xFF2563EB))
                                )
                            )
                    ) {
                        val initial = if (userName.isNotEmpty()) userName.take(1).uppercase() else role.take(1).uppercase()
                        Text(
                            text = initial,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!isSmallScreen) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = userName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 100.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Profile Menu",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Dropdown menu for profile actions
                DropdownMenu(
                    expanded = showProfileMenu,
                    onDismissRequest = { showProfileMenu = false },
                    modifier = Modifier
                        .width(220.dp)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    // User info header inside dropdown
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (role == "Admin") "Administrator" else "Student ID: $userId",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Styled Role Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (role == "Admin") Color(0xFFFEE2E2) else Color(0xFFDBEAFE)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = role.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (role == "Admin") Color(0xFF991B1B) else Color(0xFF1E40AF)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

                    // Menu Item: Settings
                    DropdownMenuItem(
                        text = { Text("Settings Preference", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showProfileMenu = false
                            onNavigateToSettings()
                        }
                    )

                    // Menu Item: Refresh
                    DropdownMenuItem(
                        text = { Text("Sync & Refresh", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showProfileMenu = false
                            onRefresh()
                        }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

                    // Menu Item: Logout (Highlighted)
                    DropdownMenuItem(
                        text = { Text("Logout Account", fontSize = 14.sp, color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                        onClick = {
                            showProfileMenu = false
                            onLogout()
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF1E3A8A), // Deep Royal Navy
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}
