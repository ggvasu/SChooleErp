package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.FeeAssignment
import com.example.data.models.NotificationItem
import com.example.data.models.Payment
import com.example.data.models.Student
import com.example.data.models.Attendance
import com.example.data.models.AcademicPerformance
import com.example.ui.utils.PdfGenerator
import com.example.ui.utils.QrCodeGenerator
import com.example.ui.components.LottieLoader
import com.example.ui.components.UniversityTopAppBar
import com.example.ui.viewmodel.ERPViewModel
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    viewModel: ERPViewModel,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val students by viewModel.students.collectAsState()
    val feeAssignments by viewModel.assignedFees.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val attendance by viewModel.attendance.collectAsState()
    val academicPerformance by viewModel.academicPerformance.collectAsState()

    val currentStudentId = viewModel.sessionManager.username
    val activeStudent = remember(students) { students.find { it.StudentID == currentStudentId } }

    // Dues and lists filtered by student
    val studentFees = remember(feeAssignments) { feeAssignments.filter { it.StudentID == currentStudentId } }
    val studentPayments = remember(payments) { payments.filter { it.StudentID == currentStudentId } }
    val studentAttendance = remember(attendance) { attendance.filter { it.StudentID == currentStudentId } }
    val studentPerformance = remember(academicPerformance) { academicPerformance.filter { it.StudentID == currentStudentId } }

    val totalDues = remember(studentFees) {
        studentFees.sumOf { it.TotalAmount.toDoubleOrNull() ?: 0.0 }
    }
    val totalPaid = remember(studentPayments) {
        studentPayments.sumOf { it.Amount.toDoubleOrNull() ?: 0.0 }
    }
    val outstanding = remember(totalDues, totalPaid) {
        val diff = totalDues - totalPaid
        if (diff > 0) diff else 0.0
    }

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showCertificateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            UniversityTopAppBar(
                title = "Student Portal",
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshAllData() },
                role = "Student",
                userName = activeStudent?.Name ?: "Student User",
                userId = currentStudentId,
                onNavigateToSettings = onNavigateToSettings,
                onLogout = onLogout,
                additionalActions = {
                    IconButton(onClick = { showPasswordDialog = true }) {
                        Icon(Icons.Default.VpnKey, "Change Password", tint = Color.White)
                    }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            val isWideScreen = maxWidth >= 600.dp

            if (isWideScreen) {
                // Dual Column Responsive Tablet/Landscape Layout!
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column (width 340.dp) for profile & quick metrics/announcements
                    Column(
                        modifier = Modifier
                            .width(340.dp)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Profile Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2563EB).copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, null, tint = Color(0xFF2563EB), modifier = Modifier.size(36.dp))
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(activeStudent?.Name ?: "Active Student", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text("ID: ${activeStudent?.StudentID ?: currentStudentId}", fontSize = 12.sp, color = Color.Gray)
                                    Text("Course: ${activeStudent?.Course ?: "B.Tech CSE"}", fontSize = 12.sp, color = Color.Gray)
                                    val semVal = activeStudent?.Semester ?: "1"
                                    val semDisp = if (semVal.startsWith("Sem") || semVal.startsWith("Year")) semVal else "Semester $semVal"
                                    Text("Semester: $semDisp", fontSize = 12.sp, color = Color.Gray)
                                }

                                IconButton(onClick = { showQrDialog = true }) {
                                    Icon(Icons.Default.QrCode2, "Show QR Code", tint = Color(0xFF2563EB), modifier = Modifier.size(28.dp))
                                }
                            }
                        }

                        // Quick Stats Bar
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Brush.linearGradient(colors = listOf(Color(0xFF2563EB), Color(0xFF4338CA))))
                                        .padding(12.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                        Text("ASSIGNED", fontSize = 8.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("$${totalDues.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    }
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Brush.linearGradient(colors = listOf(Color(0xFF10B981), Color(0xFF0F766E))))
                                        .padding(12.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                        Text("PAID", fontSize = 8.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("$${totalPaid.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    }
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Brush.linearGradient(colors = listOf(Color(0xFFF43F5E), Color(0xFFBE185D))))
                                        .padding(12.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                        Text("DUE", fontSize = 8.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("$${outstanding.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    }
                                }
                            }
                        }

                        // Academic Actions
                        Text("Student Actions", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.DarkGray)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { showCertificateDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.WorkspacePremium, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Certificate", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { showQrDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.QrCode, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ID Code", fontSize = 11.sp)
                            }
                        }

                        // Campus Announcements list
                        Text("Campus Announcements", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.DarkGray)
                        if (notifications.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Text("No announcements posted yet.", modifier = Modifier.padding(16.dp), color = Color.Gray, fontSize = 13.sp)
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                notifications.forEach { item ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(item.Title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2563EB), modifier = Modifier.weight(1f))
                                                Text(item.Date, fontSize = 9.sp, color = Color.Gray)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(item.Message, fontSize = 11.sp, color = Color.DarkGray)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Right Column (fill weight 1f) for details: Attendance, Academic, Fees & Receipts
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Horizontal section split for attendance & performance charts
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Attendance Area
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Attendance Status", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.DarkGray)
                                if (studentAttendance.isNotEmpty()) {
                                    AttendanceTrendChart(studentAttendance = studentAttendance)
                                }
                                if (studentAttendance.isEmpty()) {
                                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                                        Text("No attendance records found.", modifier = Modifier.padding(16.dp), color = Color.Gray, fontSize = 13.sp)
                                    }
                                } else {
                                    studentAttendance.forEach { att ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(att.Subject, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2563EB))
                                                    Text("${att.Percentage}%", fontWeight = FontWeight.Black, color = if ((att.Percentage.toDoubleOrNull() ?: 0.0) >= 75) Color(0xFF16A34A) else Color(0xFFDC2626))
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("Attended ${att.ClassesAttended} / ${att.TotalClasses} classes", fontSize = 11.sp, color = Color.DarkGray)
                                                Text("Last Updated: ${att.LastUpdated}", fontSize = 9.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                }
                            }

                            // Performance Area
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Academic Performance", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.DarkGray)
                                if (studentPerformance.isNotEmpty()) {
                                    AcademicPerformanceChart(studentPerformance = studentPerformance)
                                }
                                if (studentPerformance.isEmpty()) {
                                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                                        Text("No performance records found.", modifier = Modifier.padding(16.dp), color = Color.Gray, fontSize = 13.sp)
                                    }
                                } else {
                                    studentPerformance.forEach { perf ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(perf.Subject, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text(perf.Grade, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("Score: ${perf.Marks} / ${perf.TotalMarks}", fontSize = 11.sp, color = Color.DarkGray)
                                                if (perf.Remarks.isNotEmpty()) {
                                                    Text("Remarks: ${perf.Remarks}", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Fees & Receipts
                        Text("Assigned Dues & Semester Plans", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.DarkGray)
                        if (studentFees.isEmpty()) {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                                Text("No fee assignments generated.", modifier = Modifier.padding(16.dp), color = Color.Gray, fontSize = 13.sp)
                            }
                        } else {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                studentFees.forEach { fee ->
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                val sDisp = if (fee.Semester.startsWith("Sem") || fee.Semester.startsWith("Year")) fee.Semester else "Semester ${fee.Semester}"
                                                Text("$sDisp Fee Base", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("$${fee.TotalAmount}", fontWeight = FontWeight.Black, color = Color(0xFFDC2626))
                                            }
                                            Text("Timeline Due Date: ${fee.DueDate}", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                            Text("Tuition: $${fee.TuitionFee} | Admission: $${fee.AdmissionFee} | Exam: $${fee.ExamFee}", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }

                        Text("Receipts & Payment Log", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.DarkGray)
                        if (studentPayments.isEmpty()) {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                                Text("No payments processed under this Student ID.", modifier = Modifier.padding(16.dp), color = Color.Gray, fontSize = 13.sp)
                            }
                        } else {
                            studentPayments.forEach { pay ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(pay.ReceiptNumber, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), fontSize = 13.sp)
                                                Text("Date: ${pay.Date} | Mode: ${pay.PaymentMode}", fontSize = 10.sp, color = Color.Gray)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("$${pay.Amount}", fontWeight = FontWeight.Black, color = Color(0xFF16A34A), fontSize = 14.sp)
                                                TextButton(
                                                    onClick = {
                                                        val pdf = PdfGenerator.generatePaymentReceipt(context, pay)
                                                        if (pdf != null) {
                                                            PdfGenerator.sharePdf(context, pdf)
                                                        }
                                                    },
                                                    contentPadding = PaddingValues(0.dp),
                                                    modifier = Modifier.height(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Download, null, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Receipt PDF", fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Classic Compact Layout for phone
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Card with Avatar Placeholder
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2563EB).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, null, tint = Color(0xFF2563EB), modifier = Modifier.size(36.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(activeStudent?.Name ?: "Active Student", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("ID: ${activeStudent?.StudentID ?: currentStudentId}", fontSize = 12.sp, color = Color.Gray)
                                Text("Course: ${activeStudent?.Course ?: "B.Tech CSE"}", fontSize = 12.sp, color = Color.Gray)
                                val semVal = activeStudent?.Semester ?: "1"
                                val semDisp = if (semVal.startsWith("Sem") || semVal.startsWith("Year")) semVal else "Semester $semVal"
                                Text("Semester: $semDisp", fontSize = 12.sp, color = Color.Gray)
                            }

                            IconButton(onClick = { showQrDialog = true }) {
                                Icon(Icons.Default.QrCode2, "Show QR Code", tint = Color(0xFF2563EB), modifier = Modifier.size(28.dp))
                            }
                        }
                    }

                    // Quick Stats Bar
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(28.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Brush.linearGradient(colors = listOf(Color(0xFF2563EB), Color(0xFF4338CA))))
                                    .padding(14.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Text("ASSIGNED", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$${totalDues.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(28.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Brush.linearGradient(colors = listOf(Color(0xFF10B981), Color(0xFF0F766E))))
                                    .padding(14.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Text("PAID", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$${totalPaid.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(28.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Brush.linearGradient(colors = listOf(Color(0xFFF43F5E), Color(0xFFBE185D))))
                                    .padding(14.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Text("DUE", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$${outstanding.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }
                    }

                    // Academic Actions
                    Text("Student Actions", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { showCertificateDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.WorkspacePremium, null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clearance Cert")
                        }

                        Button(
                            onClick = { showQrDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.QrCode, null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Identity Code")
                        }
                    }

                    // Attendance Data
                    Text("Attendance Status", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (studentAttendance.isNotEmpty()) {
                        AttendanceTrendChart(studentAttendance = studentAttendance)
                    }
                    if (studentAttendance.isEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Text("No attendance records found.", modifier = Modifier.padding(16.dp), color = Color.Gray, fontSize = 13.sp)
                        }
                    } else {
                        studentAttendance.forEach { att ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(att.Subject, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2563EB))
                                        Text("${att.Percentage}%", fontWeight = FontWeight.Black, color = if ((att.Percentage.toDoubleOrNull() ?: 0.0) >= 75) Color(0xFF16A34A) else Color(0xFFDC2626))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Attended ${att.ClassesAttended} / ${att.TotalClasses} classes", fontSize = 12.sp, color = Color.DarkGray)
                                    Text("Last Updated: ${att.LastUpdated}", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    // Academic Performance
                    Text("Academic Performance", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (studentPerformance.isNotEmpty()) {
                        AcademicPerformanceChart(studentPerformance = studentPerformance)
                    }
                    if (studentPerformance.isEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Text("No performance records found.", modifier = Modifier.padding(16.dp), color = Color.Gray, fontSize = 13.sp)
                        }
                    } else {
                        studentPerformance.forEach { perf ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(perf.Subject, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(perf.Grade, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Score: ${perf.Marks} / ${perf.TotalMarks}", fontSize = 12.sp, color = Color.DarkGray)
                                    if (perf.Remarks.isNotEmpty()) {
                                        Text("Remarks: ${perf.Remarks}", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }

                    // Campus Announcements list
                    Text("Campus Announcements", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (notifications.isEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Text("No announcements posted yet.", modifier = Modifier.padding(16.dp), color = Color.Gray, fontSize = 13.sp)
                        }
                    } else {
                        notifications.forEach { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(item.Title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2563EB))
                                        Text(item.Date, fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(item.Message, fontSize = 12.sp, color = Color.DarkGray)
                                }
                            }
                        }
                    }

                    // Assigned Fees
                    Text("Assigned Dues & Semester Plans", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (studentFees.isEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Text("No fee assignments generated.", modifier = Modifier.padding(16.dp), color = Color.Gray, fontSize = 13.sp)
                        }
                    } else {
                        studentFees.forEach { fee ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        val sDisp = if (fee.Semester.startsWith("Sem") || fee.Semester.startsWith("Year")) fee.Semester else "Semester ${fee.Semester}"
                                        Text("$sDisp Fee Base", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("$${fee.TotalAmount}", fontWeight = FontWeight.Black, color = Color(0xFFDC2626))
                                    }
                                    Text("Timeline Due Date: ${fee.DueDate}", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                    Text("Tuition: $${fee.TuitionFee} | Admission: $${fee.AdmissionFee} | Exam: $${fee.ExamFee}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    // Payment receipts History
                    Text("Receipts & Payment Log", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (studentPayments.isEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Text("No payments processed under this Student ID.", modifier = Modifier.padding(16.dp), color = Color.Gray, fontSize = 13.sp)
                        }
                    } else {
                        studentPayments.forEach { pay ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(pay.ReceiptNumber, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                            Text("Date: ${pay.Date} | Mode: ${pay.PaymentMode}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("$${pay.Amount}", fontWeight = FontWeight.Black, color = Color(0xFF16A34A))
                                            TextButton(
                                                onClick = {
                                                    val pdf = PdfGenerator.generatePaymentReceipt(context, pay)
                                                    if (pdf != null) {
                                                        PdfGenerator.sharePdf(context, pdf)
                                                    }
                                                },
                                                contentPadding = PaddingValues(0.dp),
                                                modifier = Modifier.height(24.dp)
                                            ) {
                                                Icon(Icons.Default.Download, null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Receipt PDF", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS ---

    // 1. PASSWORD CHANGE DIALOG
    if (showPasswordDialog) {
        var newPass by remember { mutableStateOf("") }
        var confirmPass by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showPasswordDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Update Portal Password", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2563EB))
                    
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Secure Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showPasswordDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newPass.trim().isEmpty()) {
                                    Toast.makeText(context, "Password cannot be blank", Toast.LENGTH_SHORT).show()
                                } else if (newPass != confirmPass) {
                                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.changePassword(newPass.trim()) {
                                        showPasswordDialog = false
                                        Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) { Text("Save Changes") }
                    }
                }
            }
        }
    }

    // 2. IDENTITY BARCODE QR POPUP
    if (showQrDialog) {
        Dialog(onDismissRequest = { showQrDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Digital Student Identity", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(activeStudent?.Name ?: "Active Student", fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB))
                    Text("ID Token: $currentStudentId", fontSize = 12.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(16.dp))

                    val qr = remember { QrCodeGenerator.generate("CAMPUS_ONE_STUDENT_IDENTITY:$currentStudentId") }
                    if (qr != null) {
                        Image(
                            bitmap = qr.asImageBitmap(),
                            contentDescription = "Identity QR",
                            modifier = Modifier.size(220.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showQrDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))) {
                        Text("Dismiss Token")
                    }
                }
            }
        }
    }

    // 3. LIVE CLEARANCE CERTIFICATE
    if (showCertificateDialog) {
        Dialog(onDismissRequest = { showCertificateDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.WorkspacePremium, "Cert", tint = Color(0xFFF59E0B), modifier = Modifier.size(64.dp))
                    Text("Institutional Academic Clearance", fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)

                    if (outstanding > 0) {
                        Text(
                            "Clearance Status: HOLD \nYou have outstanding dues of $$outstanding. Please clear all dues at the Accounts section to generate clearance credentials.",
                            color = Color.Red,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            "Clearance Status: APPROVED \nCongratulations! This Student Account is fully cleared of all library, hostel, administrative, and tuition dues. You are eligible for examinations and certificate generation.",
                            color = Color(0xFF16A34A),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = { showCertificateDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Close View")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AttendanceTrendChart(
    studentAttendance: List<Attendance>,
    modifier: Modifier = Modifier
) {
    if (studentAttendance.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Attendance Trend Line",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E3A8A)
                )
                Text(
                    "Tap points to inspect",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(studentAttendance) {
                            detectTapGestures { offset ->
                                val leftPaddingPx = 100f
                                val rightPaddingPx = 50f
                                val topPaddingPx = 30f
                                val bottomPaddingPx = 80f
                                
                                val graphWidth = this.size.width.toFloat() - leftPaddingPx - rightPaddingPx
                                val stepX = if (studentAttendance.size > 1) graphWidth / (studentAttendance.size - 1) else graphWidth
                                
                                var bestIndex: Int? = null
                                var minDistance = Float.MAX_VALUE
                                
                                studentAttendance.forEachIndexed { index, att ->
                                    val x = leftPaddingPx + index * stepX
                                    val pct = att.Percentage.toDoubleOrNull() ?: 0.0
                                    val graphHeight = this.size.height.toFloat() - topPaddingPx - bottomPaddingPx
                                    val y = topPaddingPx + graphHeight - (pct.toFloat() / 100f * graphHeight)
                                    
                                    val dist = (offset.x - x) * (offset.x - x) + (offset.y - y) * (offset.y - y)
                                    if (dist < 1000f && dist < minDistance) { // Tap within ~30px radius
                                        minDistance = dist
                                        bestIndex = index
                                    }
                                }
                                selectedIndex = if (bestIndex == selectedIndex) null else bestIndex
                            }
                        }
                ) {
                    val leftPaddingPx = 100f
                    val rightPaddingPx = 50f
                    val topPaddingPx = 30f
                    val bottomPaddingPx = 80f
                    
                    val canvasWidth = this.size.width
                    val canvasHeight = this.size.height
                    
                    val graphWidth = canvasWidth - leftPaddingPx - rightPaddingPx
                    val graphHeight = canvasHeight - topPaddingPx - bottomPaddingPx
                    
                    // 1. Draw grid lines (0%, 25%, 50%, 75%, 100%) and Y-axis labels
                    val gridPercentages = listOf(0, 25, 50, 75, 100)
                    gridPercentages.forEach { pct ->
                        val y = topPaddingPx + graphHeight - (pct.toFloat() / 100f * graphHeight)
                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(leftPaddingPx, y),
                            end = Offset(canvasWidth - rightPaddingPx, y),
                            strokeWidth = 1f
                        )
                        val pctTextResult = textMeasurer.measure(
                            text = "$pct%",
                            style = TextStyle(color = Color.Gray, fontSize = 10.sp)
                        )
                        drawText(
                            textLayoutResult = pctTextResult,
                            topLeft = Offset(15f, y - 8f)
                        )
                    }
                    
                    if (studentAttendance.isEmpty()) return@Canvas
                    
                    // 2. Draw line and area under line
                    val stepX = if (studentAttendance.size > 1) graphWidth / (studentAttendance.size - 1) else graphWidth
                    
                    val points = studentAttendance.mapIndexed { index, att ->
                        val x = leftPaddingPx + index * stepX
                        val pct = att.Percentage.toDoubleOrNull() ?: 0.0
                        val y = topPaddingPx + graphHeight - (pct.toFloat() / 100f * graphHeight)
                        Offset(x, y)
                    }
                    
                    // Create beautiful path
                    val path = Path()
                    val fillPath = Path()
                    
                    if (points.isNotEmpty()) {
                        path.moveTo(points[0].x, points[0].y)
                        fillPath.moveTo(points[0].x, points[0].y)
                        
                        for (i in 1 until points.size) {
                            val prevPoint = points[i - 1]
                            val currentPoint = points[i]
                            val controlX = (prevPoint.x + currentPoint.x) / 2
                            path.quadraticTo(controlX, prevPoint.y, controlX, currentPoint.y)
                            path.lineTo(currentPoint.x, currentPoint.y)
                            
                            fillPath.quadraticTo(controlX, prevPoint.y, controlX, currentPoint.y)
                            fillPath.lineTo(currentPoint.x, currentPoint.y)
                        }
                        
                        // Close fill path
                        fillPath.lineTo(points.last().x, topPaddingPx + graphHeight)
                        fillPath.lineTo(points.first().x, topPaddingPx + graphHeight)
                        fillPath.close()
                        
                        // Draw gradient fill
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF2563EB).copy(alpha = 0.3f), Color.Transparent),
                                startY = topPaddingPx,
                                endY = topPaddingPx + graphHeight
                            )
                        )
                        
                        // Draw line path
                        drawPath(
                            path = path,
                            color = Color(0xFF2563EB),
                            style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                    
                    // 3. Draw nodes and label X-axis
                    points.forEachIndexed { index, point ->
                        val att = studentAttendance[index]
                        
                        // Draw node circle outer shadow-like background
                        drawCircle(
                            color = Color.White,
                            radius = 10f,
                            center = point
                        )
                        
                        // Draw node circle
                        drawCircle(
                            color = Color(0xFF2563EB),
                            radius = 6f,
                            center = point
                        )
                        
                        // Label X-axis (Subjects)
                        val subjectAbbr = att.Subject.take(6) + (if (att.Subject.length > 6) ".." else "")
                        val textLayoutResult = textMeasurer.measure(
                            text = subjectAbbr,
                            style = TextStyle(color = Color.DarkGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        )
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(point.x - textLayoutResult.size.width / 2, topPaddingPx + graphHeight + 10f)
                        )
                    }
                    
                    // 4. Draw selection tooltip
                    selectedIndex?.let { index ->
                        if (index in points.indices) {
                            val point = points[index]
                            val att = studentAttendance[index]
                            
                            // Highlight node
                            drawCircle(
                                color = Color(0xFF10B981),
                                radius = 9f,
                                center = point
                            )
                            
                            // Tooltip text
                            val tooltipText = "${att.Subject}\n${att.Percentage}% (${att.ClassesAttended}/${att.TotalClasses} classes)"
                            val tooltipLayout = textMeasurer.measure(
                                text = tooltipText,
                                style = TextStyle(color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            )
                            
                            val tooltipWidth = tooltipLayout.size.width + 16f
                            val tooltipHeight = tooltipLayout.size.height + 12f
                            
                            // Coordinates for tooltip box
                            var tooltipX = point.x - tooltipWidth / 2
                            var tooltipY = point.y - tooltipHeight - 20f
                            
                            // Keep within bounds
                            if (tooltipX < 10f) tooltipX = 10f
                            if (tooltipX + tooltipWidth > canvasWidth - 10f) tooltipX = canvasWidth - tooltipWidth - 10f
                            if (tooltipY < 5f) tooltipY = point.y + 15f
                            
                            // Tooltip background
                            drawRoundRect(
                                color = Color(0xFF1E293B),
                                topLeft = Offset(tooltipX, tooltipY),
                                size = Size(tooltipWidth, tooltipHeight),
                                cornerRadius = CornerRadius(12f, 12f)
                            )
                            
                            // Tooltip text drawing
                            drawText(
                                textLayoutResult = tooltipLayout,
                                topLeft = Offset(tooltipX + 8f, tooltipY + 6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AcademicPerformanceChart(
    studentPerformance: List<AcademicPerformance>,
    modifier: Modifier = Modifier
) {
    if (studentPerformance.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Academic Progress",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E3A8A)
                )
                Text(
                    "Tap bars to inspect",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(studentPerformance) {
                            detectTapGestures { offset ->
                                val leftPaddingPx = 100f
                                val rightPaddingPx = 50f
                                val topPaddingPx = 30f
                                val bottomPaddingPx = 80f
                                
                                val graphWidth = this.size.width.toFloat() - leftPaddingPx - rightPaddingPx
                                val barCount = studentPerformance.size
                                val barSpacing = 30f
                                val totalSpaces = maxOf(1, barCount - 1)
                                val availableBarWidth = graphWidth - (totalSpaces * barSpacing)
                                val barWidth = availableBarWidth / barCount
                                
                                var tappedIdx: Int? = null
                                studentPerformance.forEachIndexed { index, _ ->
                                    val barLeft = leftPaddingPx + index * (barWidth + barSpacing)
                                    val barRight = barLeft + barWidth
                                    if (offset.x in barLeft..barRight) {
                                        tappedIdx = index
                                    }
                                }
                                selectedIndex = if (tappedIdx == selectedIndex) null else tappedIdx
                            }
                        }
                ) {
                    val leftPaddingPx = 100f
                    val rightPaddingPx = 50f
                    val topPaddingPx = 30f
                    val bottomPaddingPx = 80f
                    
                    val canvasWidth = this.size.width
                    val canvasHeight = this.size.height
                    
                    val graphWidth = canvasWidth - leftPaddingPx - rightPaddingPx
                    val graphHeight = canvasHeight - topPaddingPx - bottomPaddingPx
                    
                    // 1. Draw horizontal grid lines
                    val marksPercentages = listOf(0, 25, 50, 75, 100)
                    marksPercentages.forEach { pct ->
                        val y = topPaddingPx + graphHeight - (pct.toFloat() / 100f * graphHeight)
                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(leftPaddingPx, y),
                            end = Offset(canvasWidth - rightPaddingPx, y),
                            strokeWidth = 1f
                        )
                        val pctTextResult = textMeasurer.measure(
                            text = "$pct%",
                            style = TextStyle(color = Color.Gray, fontSize = 10.sp)
                        )
                        drawText(
                            textLayoutResult = pctTextResult,
                            topLeft = Offset(15f, y - 8f)
                        )
                    }
                    
                    if (studentPerformance.isEmpty()) return@Canvas
                    
                    // 2. Draw bars
                    val barCount = studentPerformance.size
                    val barSpacing = 30f
                    val totalSpaces = maxOf(1, barCount - 1)
                    val availableBarWidth = graphWidth - (totalSpaces * barSpacing)
                    val barWidth = availableBarWidth / barCount
                    
                    studentPerformance.forEachIndexed { index, perf ->
                        val marksValue = perf.Marks.toDoubleOrNull() ?: 0.0
                        val totalMarksValue = perf.TotalMarks.toDoubleOrNull() ?: 100.0
                        val ratio = if (totalMarksValue > 0) marksValue / totalMarksValue else 0.0
                        val barHeight = (ratio * graphHeight).toFloat()
                        
                        val barLeft = leftPaddingPx + index * (barWidth + barSpacing)
                        val barRight = barLeft + barWidth
                        val barTop = topPaddingPx + graphHeight - barHeight
                        val barBottom = topPaddingPx + graphHeight
                        
                        val isSelected = selectedIndex == index
                        val barColorBrush = if (isSelected) {
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                            )
                        }
                        
                        // Draw rounded rect bar
                        drawRoundRect(
                            brush = barColorBrush,
                            topLeft = Offset(barLeft, barTop),
                            size = Size(barWidth, maxOf(5f, barHeight)),
                            cornerRadius = CornerRadius(12f, 12f)
                        )
                        
                        // Label X-axis (Subjects)
                        val subjectAbbr = perf.Subject.take(6) + (if (perf.Subject.length > 6) ".." else "")
                        val textLayoutResult = textMeasurer.measure(
                            text = subjectAbbr,
                            style = TextStyle(color = Color.DarkGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        )
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(barLeft + (barWidth - textLayoutResult.size.width) / 2, topPaddingPx + graphHeight + 10f)
                        )
                        
                        // Render tooltips for active selection
                        if (isSelected) {
                            val tooltipText = "${perf.Subject}\nMarks: ${perf.Marks}/${perf.TotalMarks}\nGrade: ${perf.Grade}\n${perf.Remarks}"
                            val tooltipLayout = textMeasurer.measure(
                                text = tooltipText,
                                style = TextStyle(color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            )
                            
                            val tooltipWidth = tooltipLayout.size.width + 16f
                            val tooltipHeight = tooltipLayout.size.height + 12f
                            
                            var tooltipX = barLeft + barWidth / 2 - tooltipWidth / 2
                            var tooltipY = barTop - tooltipHeight - 20f
                            
                            if (tooltipX < 10f) tooltipX = 10f
                            if (tooltipX + tooltipWidth > canvasWidth - 10f) tooltipX = canvasWidth - tooltipWidth - 10f
                            if (tooltipY < 5f) tooltipY = barTop + 15f
                            
                            // Tooltip box
                            drawRoundRect(
                                color = Color(0xFF1E293B),
                                topLeft = Offset(tooltipX, tooltipY),
                                size = Size(tooltipWidth, tooltipHeight),
                                cornerRadius = CornerRadius(12f, 12f)
                            )
                            
                            // Tooltip text
                            drawText(
                                textLayoutResult = tooltipLayout,
                                topLeft = Offset(tooltipX + 8f, tooltipY + 6f)
                            )
                        }
                    }
                }
            }
        }
    }
}
