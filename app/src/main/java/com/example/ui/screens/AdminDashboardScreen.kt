package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.example.data.models.*
import com.example.R
import com.example.ui.utils.PdfGenerator
import com.example.ui.utils.QrCodeGenerator
import com.example.ui.components.LottieLoader
import com.example.ui.components.UniversityTopAppBar
import com.example.ui.viewmodel.ERPViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: ERPViewModel,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Core states
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val reports by viewModel.reportData.collectAsState()
    val students by viewModel.students.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val departments by viewModel.departments.collectAsState()
    val batches by viewModel.batches.collectAsState()
    val semesters by viewModel.semesters.collectAsState()
    val feeAssignments by viewModel.assignedFees.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val attendance by viewModel.attendance.collectAsState()
    val academicPerformance by viewModel.academicPerformance.collectAsState()

    var activeView by remember { mutableStateOf("Dashboard") }

    // Dialog trigger states
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var selectedStudentForEdit by remember { mutableStateOf<Student?>(null) }
    var showQrDialogStudent by remember { mutableStateOf<Student?>(null) }

    // Courses Dialog State
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var selectedCourseForEdit by remember { mutableStateOf<Course?>(null) }

    // Department Dialog State
    var showAddDeptDialog by remember { mutableStateOf(false) }
    var selectedDeptForEdit by remember { mutableStateOf<Department?>(null) }

    // Batch Dialog State
    var showAddBatchDialog by remember { mutableStateOf(false) }
    var selectedBatchForEdit by remember { mutableStateOf<Batch?>(null) }

    // Semester Dialog State
    var showAddSemDialog by remember { mutableStateOf(false) }
    var selectedSemForEdit by remember { mutableStateOf<Semester?>(null) }

    // Fee Assignment State
    var showAssignFeeDialog by remember { mutableStateOf(false) }
    
    // Receive Payment State
    var showReceivePaymentDialog by remember { mutableStateOf(false) }
    var selectedPaymentForEdit by remember { mutableStateOf<Payment?>(null) }

    // Announcement Dialog State
    var showAnnounceDialog by remember { mutableStateOf(false) }

    val drawerItems = listOf(
        "Dashboard" to Icons.Default.Dashboard,
        "Students" to Icons.Default.People,
        "Bulk Import" to Icons.Default.CloudUpload,
        "Courses" to Icons.Default.School,
        "Departments" to Icons.Default.Business,
        "Batches" to Icons.Default.CalendarMonth,
        "Semesters" to Icons.Default.MenuBook,
        "Fees" to Icons.Default.Payments,
        "Results" to Icons.Default.Assignment,
        "Attendance" to Icons.Default.CalendarToday,
        "Fee Assignments" to Icons.Default.RequestQuote,
        "Payments" to Icons.Default.CreditCard,
        "Reports" to Icons.Default.Analytics,
        "Users" to Icons.Default.ManageAccounts,
        "Settings" to Icons.Default.Settings,
        "Logout" to Icons.Default.Logout
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Left permanent elegant M3 side bar
                Card(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header Drawer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))))
                                .padding(24.dp)
                        ) {
                            Column {
                                Icon(Icons.Default.School, "Logo", tint = Color.White, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("CAMPUS ONE ADMIN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("erp.admin@college.edu", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(drawerItems) { item ->
                                val isSelected = activeView == item.first
                                NavigationDrawerItem(
                                    icon = { Icon(item.second, contentDescription = null, tint = if (isSelected) Color(0xFF2563EB) else Color.Gray) },
                                    label = { Text(item.first, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                    selected = isSelected,
                                    onClick = {
                                        when (item.first) {
                                            "Logout" -> onLogout()
                                            "Settings" -> onNavigateToSettings()
                                            else -> activeView = item.first
                                        }
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                                    colors = NavigationDrawerItemDefaults.colors(
                                        selectedContainerColor = Color(0xFFEFF6FF),
                                        selectedTextColor = Color(0xFF2563EB),
                                        unselectedTextColor = Color.DarkGray
                                    )
                                )
                            }
                        }
                    }
                }

                // Right side main content Scaffold
                Scaffold(
                    topBar = {
                        UniversityTopAppBar(
                            title = activeView,
                            isRefreshing = isRefreshing,
                            onRefresh = { viewModel.refreshAllData() },
                            role = "Admin",
                            userName = "Administrator",
                            userId = "admin",
                            onNavigateToSettings = onNavigateToSettings,
                            onLogout = onLogout,
                            additionalActions = {
                                IconButton(onClick = { showAnnounceDialog = true }) {
                                    Icon(Icons.Default.Campaign, "Announce", tint = Color.White)
                                }
                            }
                        )
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        when (activeView) {
                            "Dashboard" -> DashboardView(
                                reports = reports,
                                students = students,
                                payments = payments,
                                onQuickAction = { action ->
                                    when (action) {
                                        "Add Student" -> showAddStudentDialog = true
                                        "Assign Fees" -> showAssignFeeDialog = true
                                        "Receive Payment" -> showReceivePaymentDialog = true
                                        "Reports" -> activeView = "Reports"
                                        "Users" -> activeView = "Users"
                                    }
                                }
                            )
                            "Students" -> StudentsView(
                                students = students,
                                courses = courses,
                                semesters = semesters,
                                feeAssignments = feeAssignments,
                                payments = payments,
                                onAddStudent = { showAddStudentDialog = true },
                                onBulkImport = { activeView = "Bulk Import" },
                                onEditStudent = { student ->
                                    selectedStudentForEdit = student
                                    showAddStudentDialog = true
                                },
                                onDeleteStudent = { studentId -> viewModel.removeStudent(studentId) },
                                onShowQr = { student -> showQrDialogStudent = student }
                            )
                            "Bulk Import" -> BulkImportView(
                                viewModel = viewModel,
                                onNavigateToStudents = { activeView = "Students" }
                            )
                            "Courses" -> CoursesView(
                                courses = courses,
                                onAddCourse = { showAddCourseDialog = true },
                                onEditCourse = { course ->
                                    selectedCourseForEdit = course
                                    showAddCourseDialog = true
                                },
                                onDeleteCourse = { code -> viewModel.removeCourse(code) }
                            )
                            "Departments" -> DepartmentsView(
                                departments = departments,
                                onAddDept = { showAddDeptDialog = true },
                                onEditDept = { dept ->
                                    selectedDeptForEdit = dept
                                    showAddDeptDialog = true
                                },
                                onDeleteDept = { code -> viewModel.removeDepartment(code) }
                            )
                            "Batches" -> BatchesView(
                                batches = batches,
                                onAddBatch = { showAddBatchDialog = true },
                                onEditBatch = { batch ->
                                    selectedBatchForEdit = batch
                                    showAddBatchDialog = true
                                },
                                onDeleteBatch = { name -> viewModel.removeBatch(name) }
                            )
                            "Semesters" -> SemestersView(
                                semesters = semesters,
                                courses = courses,
                                onAddSem = { showAddSemDialog = true },
                                onEditSem = { sem ->
                                    selectedSemForEdit = sem
                                    showAddSemDialog = true
                                },
                                onDeleteSem = { no, cName -> viewModel.removeSemester(no, cName) }
                            )
                            "Fee Assignments" -> FeeAssignmentsView(
                                feeAssignments = feeAssignments,
                                onAssign = { showAssignFeeDialog = true }
                            )
                            "Payments" -> PaymentsView(
                                payments = payments,
                                students = students,
                                courses = courses,
                                semesters = semesters,
                                onReceive = {
                                    selectedPaymentForEdit = null
                                    showReceivePaymentDialog = true
                                },
                                onEditPayment = { payment ->
                                    selectedPaymentForEdit = payment
                                    showReceivePaymentDialog = true
                                },
                                onDeletePayment = { paymentId ->
                                    viewModel.deletePayment(paymentId) {
                                        Toast.makeText(context, "Payment deleted successfully", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                            "Reports" -> ReportsView(
                                reports = reports,
                                students = students,
                                feeAssignments = feeAssignments,
                                payments = payments,
                                courses = courses,
                                departments = departments,
                                batches = batches
                            )
                            "Fees" -> FeesView(
                                students = students,
                                feeAssignments = feeAssignments,
                                payments = payments,
                                courses = courses,
                                onCollectFee = { student ->
                                    selectedPaymentForEdit = Payment(
                                        PaymentID = "",
                                        StudentID = student.StudentID,
                                        Course = student.Course,
                                        Semester = student.Semester,
                                        FeeType = "Tuition Fee",
                                        Amount = "",
                                        Fine = "0",
                                        Discount = "0",
                                        Balance = "",
                                        PaymentMode = "UPI",
                                        TransactionNumber = "",
                                        ReceiptNumber = "",
                                        Date = "",
                                        Remarks = "Fees Collection"
                                    )
                                    showReceivePaymentDialog = true
                                }
                            )
                            "Results" -> ResultsView(
                                students = students,
                                academicPerformance = academicPerformance,
                                courses = courses,
                                semesters = semesters,
                                onSavePerformance = { ap ->
                                    viewModel.saveAcademicPerformance(ap) {
                                        Toast.makeText(context, "Academic record updated!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                            "Attendance" -> AttendanceView(
                                students = students,
                                attendanceList = attendance,
                                courses = courses,
                                semesters = semesters,
                                onSaveAttendance = { att ->
                                    viewModel.saveAttendance(att) {
                                        Toast.makeText(context, "Attendance record updated!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                            "Users" -> UsersView(students = students, auditLogs = auditLogs)
                        }
                    }
                }
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = false,
                drawerContent = {
                    ModalDrawerSheet {
                        // Header Drawer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))))
                                .padding(24.dp)
                        ) {
                            Column {
                                Icon(Icons.Default.School, "Logo", tint = Color.White, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("CAMPUS ONE ADMIN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("erp.admin@college.edu", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn {
                            items(drawerItems) { item ->
                                NavigationDrawerItem(
                                    icon = { Icon(item.second, contentDescription = null) },
                                    label = { Text(item.first) },
                                    selected = activeView == item.first,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        when (item.first) {
                                            "Logout" -> onLogout()
                                            "Settings" -> onNavigateToSettings()
                                            else -> activeView = item.first
                                        }
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        UniversityTopAppBar(
                            title = activeView,
                            isRefreshing = isRefreshing,
                            onRefresh = { viewModel.refreshAllData() },
                            role = "Admin",
                            userName = "Administrator",
                            userId = "admin",
                            onNavigateToSettings = onNavigateToSettings,
                            onLogout = onLogout,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            additionalActions = {
                                IconButton(onClick = { showAnnounceDialog = true }) {
                                    Icon(Icons.Default.Campaign, "Announce", tint = Color.White)
                                }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color.White,
                            tonalElevation = 8.dp
                        ) {
                            val navItems = listOf(
                                Triple("Dashboard", Icons.Default.Dashboard, "Dashboard"),
                                Triple("Students", Icons.Default.People, "Students"),
                                Triple("Payments", Icons.Default.Payments, "Payments"),
                                Triple("Reports", Icons.Default.Analytics, "Reports"),
                                Triple("More", Icons.Default.Menu, "More")
                            )
                            navItems.forEach { (label, icon, viewKey) ->
                                val isSelected = if (viewKey == "More") {
                                    activeView !in listOf("Dashboard", "Students", "Payments", "Reports")
                                } else {
                                    activeView == viewKey
                                }
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        if (viewKey == "More") {
                                            scope.launch { drawerState.open() }
                                        } else {
                                            activeView = viewKey
                                        }
                                    },
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF2563EB),
                                        selectedTextColor = Color(0xFF2563EB),
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray,
                                        indicatorColor = Color(0xFFEFF6FF)
                                    )
                                )
                            }
                        }
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        when (activeView) {
                            "Dashboard" -> DashboardView(
                                reports = reports,
                                students = students,
                                payments = payments,
                                onQuickAction = { action ->
                                    when (action) {
                                        "Add Student" -> showAddStudentDialog = true
                                        "Assign Fees" -> showAssignFeeDialog = true
                                        "Receive Payment" -> showReceivePaymentDialog = true
                                        "Reports" -> activeView = "Reports"
                                        "Users" -> activeView = "Users"
                                    }
                                }
                            )
                            "Students" -> StudentsView(
                                students = students,
                                courses = courses,
                                semesters = semesters,
                                feeAssignments = feeAssignments,
                                payments = payments,
                                onAddStudent = { showAddStudentDialog = true },
                                onBulkImport = { activeView = "Bulk Import" },
                                onEditStudent = { student ->
                                    selectedStudentForEdit = student
                                    showAddStudentDialog = true
                                },
                                onDeleteStudent = { studentId -> viewModel.removeStudent(studentId) },
                                onShowQr = { student -> showQrDialogStudent = student }
                            )
                            "Bulk Import" -> BulkImportView(
                                viewModel = viewModel,
                                onNavigateToStudents = { activeView = "Students" }
                            )
                            "Courses" -> CoursesView(
                                courses = courses,
                                onAddCourse = { showAddCourseDialog = true },
                                onEditCourse = { course ->
                                    selectedCourseForEdit = course
                                    showAddCourseDialog = true
                                },
                                onDeleteCourse = { code -> viewModel.removeCourse(code) }
                            )
                            "Departments" -> DepartmentsView(
                                departments = departments,
                                onAddDept = { showAddDeptDialog = true },
                                onEditDept = { dept ->
                                    selectedDeptForEdit = dept
                                    showAddDeptDialog = true
                                },
                                onDeleteDept = { code -> viewModel.removeDepartment(code) }
                            )
                            "Batches" -> BatchesView(
                                batches = batches,
                                onAddBatch = { showAddBatchDialog = true },
                                onEditBatch = { batch ->
                                    selectedBatchForEdit = batch
                                    showAddBatchDialog = true
                                },
                                onDeleteBatch = { name -> viewModel.removeBatch(name) }
                            )
                            "Semesters" -> SemestersView(
                                semesters = semesters,
                                courses = courses,
                                onAddSem = { showAddSemDialog = true },
                                onEditSem = { sem ->
                                    selectedSemForEdit = sem
                                    showAddSemDialog = true
                                },
                                onDeleteSem = { no, cName -> viewModel.removeSemester(no, cName) }
                            )
                            "Fee Assignments" -> FeeAssignmentsView(
                                feeAssignments = feeAssignments,
                                onAssign = { showAssignFeeDialog = true }
                            )
                            "Payments" -> PaymentsView(
                                payments = payments,
                                students = students,
                                courses = courses,
                                semesters = semesters,
                                onReceive = {
                                    selectedPaymentForEdit = null
                                    showReceivePaymentDialog = true
                                },
                                onEditPayment = { payment ->
                                    selectedPaymentForEdit = payment
                                    showReceivePaymentDialog = true
                                },
                                onDeletePayment = { paymentId ->
                                    viewModel.deletePayment(paymentId) {
                                        Toast.makeText(context, "Payment deleted successfully", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                            "Reports" -> ReportsView(
                                reports = reports,
                                students = students,
                                feeAssignments = feeAssignments,
                                payments = payments,
                                courses = courses,
                                departments = departments,
                                batches = batches
                            )
                            "Fees" -> FeesView(
                                students = students,
                                feeAssignments = feeAssignments,
                                payments = payments,
                                courses = courses,
                                onCollectFee = { student ->
                                    selectedPaymentForEdit = Payment(
                                        PaymentID = "",
                                        StudentID = student.StudentID,
                                        Course = student.Course,
                                        Semester = student.Semester,
                                        FeeType = "Tuition Fee",
                                        Amount = "",
                                        Fine = "0",
                                        Discount = "0",
                                        Balance = "",
                                        PaymentMode = "UPI",
                                        TransactionNumber = "",
                                        ReceiptNumber = "",
                                        Date = "",
                                        Remarks = "Fees Collection"
                                    )
                                    showReceivePaymentDialog = true
                                }
                            )
                            "Results" -> ResultsView(
                                students = students,
                                academicPerformance = academicPerformance,
                                courses = courses,
                                semesters = semesters,
                                onSavePerformance = { ap ->
                                    viewModel.saveAcademicPerformance(ap) {
                                        Toast.makeText(context, "Academic record updated!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                            "Attendance" -> AttendanceView(
                                students = students,
                                attendanceList = attendance,
                                courses = courses,
                                semesters = semesters,
                                onSaveAttendance = { att ->
                                    viewModel.saveAttendance(att) {
                                        Toast.makeText(context, "Attendance record updated!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                            "Users" -> UsersView(students = students, auditLogs = auditLogs)
                        }
                    }
                }
            }
        }
    }

    // --- POPUP DIALOGS ---

    // 1. ADD / EDIT STUDENT DIALOG
    if (showAddStudentDialog) {
        StudentFormDialog(
            student = selectedStudentForEdit,
            courses = courses,
            departments = departments,
            batches = batches,
            semesters = semesters,
            onDismiss = {
                showAddStudentDialog = false
                selectedStudentForEdit = null
            },
            onSave = { stu ->
                viewModel.saveStudent(stu, selectedStudentForEdit != null) {
                    showAddStudentDialog = false
                    selectedStudentForEdit = null
                }
            }
        )
    }

    // 2. STUDENT QR DIALOG
    showQrDialogStudent?.let { stu ->
        Dialog(onDismissRequest = { showQrDialogStudent = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Student Identity Token", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(stu.Name, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Text("ID: ${stu.StudentID} | Reg No: ${stu.RegNo}", fontSize = 11.sp, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val qrBitmap = remember(stu) { QrCodeGenerator.generate("CAMPUS_ONE_STUDENT_VERIFICATION:${stu.StudentID}") }
                    if (qrBitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Verification",
                            modifier = Modifier.size(240.dp)
                        )
                    } else {
                        Text("Failed to build QR Code token", color = Color.Red, fontSize = 12.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showQrDialogStudent = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Dismiss Token")
                    }
                }
            }
        }
    }

    // 3. COURSE DIALOG
    if (showAddCourseDialog) {
        CourseFormDialog(
            course = selectedCourseForEdit,
            onDismiss = {
                showAddCourseDialog = false
                selectedCourseForEdit = null
            },
            onSave = { crs ->
                viewModel.saveCourse(crs, selectedCourseForEdit != null) {
                    showAddCourseDialog = false
                    selectedCourseForEdit = null
                }
            }
        )
    }

    // 4. DEPARTMENT DIALOG
    if (showAddDeptDialog) {
        DeptFormDialog(
            dept = selectedDeptForEdit,
            onDismiss = {
                showAddDeptDialog = false
                selectedDeptForEdit = null
            },
            onSave = { dep ->
                viewModel.saveDepartment(dep, selectedDeptForEdit != null) {
                    showAddDeptDialog = false
                    selectedDeptForEdit = null
                }
            }
        )
    }

    // 5. BATCH DIALOG
    if (showAddBatchDialog) {
        BatchFormDialog(
            batch = selectedBatchForEdit,
            onDismiss = {
                showAddBatchDialog = false
                selectedBatchForEdit = null
            },
            onSave = { bat ->
                viewModel.saveBatch(bat, selectedBatchForEdit != null) {
                    showAddBatchDialog = false
                    selectedBatchForEdit = null
                }
            }
        )
    }

    // 6. SEMESTER DIALOG
    if (showAddSemDialog) {
        SemesterFormDialog(
            semester = selectedSemEditOrBuild(selectedSemForEdit),
            courses = courses,
            onDismiss = {
                showAddSemDialog = false
                selectedSemForEdit = null
            },
            onSave = { sem ->
                viewModel.saveSemester(sem, selectedSemForEdit != null) {
                    showAddSemDialog = false
                    selectedSemForEdit = null
                }
            }
        )
    }

    // 7. FEE ASSIGNMENT DIALOG (Supports bulk)
    if (showAssignFeeDialog) {
        FeeAssignmentFormDialog(
            courses = courses,
            batches = batches,
            students = students,
            feeAssignments = feeAssignments,
            onDismiss = { showAssignFeeDialog = false },
            onSave = { fee, isBulk, batchFilter ->
                viewModel.assignFees(fee, isBulk, batchFilter) {
                    showAssignFeeDialog = false
                }
            }
        )
    }

    // 8. RECEIVE PAYMENT DIALOG
    if (showReceivePaymentDialog) {
        ReceivePaymentFormDialog(
            students = students,
            feeAssignments = feeAssignments,
            payments = payments,
            courses = courses,
            paymentToEdit = selectedPaymentForEdit,
            onDismiss = {
                showReceivePaymentDialog = false
                selectedPaymentForEdit = null
            },
            onSave = { payment, onConfirm ->
                if (selectedPaymentForEdit != null) {
                    viewModel.updatePayment(payment) {
                        onConfirm(payment)
                        showReceivePaymentDialog = false
                        selectedPaymentForEdit = null
                    }
                } else {
                    viewModel.collectPayment(payment) { payId, recNo ->
                        val targetPayment = payment.copy(PaymentID = payId, ReceiptNumber = recNo)
                        onConfirm(targetPayment)
                    }
                }
            }
        )
    }

    // 9. ANNOUNCEMENT BROADCAST DIALOG
    if (showAnnounceDialog) {
        AnnounceDialog(
            onDismiss = { showAnnounceDialog = false },
            onPost = { title, msg, target ->
                viewModel.postNotification(title, msg, target) {
                    showAnnounceDialog = false
                    Toast.makeText(context, "Announcement broadcast successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

fun selectedSemEditOrBuild(sem: Semester?): Semester {
    return sem ?: Semester("", "", "", "", "Active")
}

// ==========================================
// VIEW RENDERING LOGIC
// ==========================================

@Composable
fun DashboardView(
    reports: ReportResponse,
    students: List<Student>,
    payments: List<Payment>,
    onQuickAction: (String) -> Unit
) {
    val maleCount = remember(students) { students.count { it.Gender.equals("Male", ignoreCase = true) } }
    val femaleCount = remember(students) { students.count { it.Gender.equals("Female", ignoreCase = true) } }
    val otherCount = remember(students) { students.count { !it.Gender.equals("Male", ignoreCase = true) && !it.Gender.equals("Female", ignoreCase = true) && it.Gender.isNotBlank() } }
    val totalBatches = remember(students) {
        val count = students.map { it.Batch }.distinct().filter { it.isNotBlank() }.size
        if (count == 0) 3 else count
    }
    
    var activityTab by remember { mutableStateOf(0) } // 0 = Admissions, 1 = Payments

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Clean slate gray
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))))
                    .padding(20.dp)
            ) {
                Column {
                    Text("Welcome Back, Admin!", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    
                }
            }
        }

        // Metrics Grid (Rounded gradient cards)
        Text("Key Performance Metrics", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "Total Students",
                value = reports.totalStudents.toString(),
                icon = Icons.Default.Group,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFF2563EB), Color(0xFF4338CA))),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Assigned Dues",
                value = "$${reports.totalFeesAssigned.toInt()}",
                icon = Icons.Default.Receipt,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFFF59E0B), Color(0xFFEA580C))),
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "Paid Collections",
                value = "$${reports.totalPaid.toInt()}",
                icon = Icons.Default.Paid,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFF10B981), Color(0xFF0F766E))),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Pending Fees",
                value = "$${reports.totalPending.toInt()}",
                icon = Icons.Default.Warning,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFFF43F5E), Color(0xFFBE185D))),
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "Today Admissions",
                value = reports.totalStudents.toString(), // Simplified counts
                icon = Icons.Default.HowToReg,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFF0F172A), Color(0xFF334155))),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Today's Cash",
                value = "$${reports.todayCollection.toInt()}",
                icon = Icons.Default.CurrencyExchange,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFF0EA5E9), Color(0xFF2563EB))),
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "Gender Count",
                value = "M: $maleCount | F: $femaleCount" + (if (otherCount > 0) " | O: $otherCount" else ""),
                icon = Icons.Default.Wc,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFF8B5CF6), Color(0xFFD946EF))),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Total Batches",
                value = "$totalBatches",
                icon = Icons.Default.School,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFF14B8A6), Color(0xFF0D9488))),
                modifier = Modifier.weight(1f)
            )
        }

        // Quick Actions
        Text("Quick ERP Admin Actions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionButton("Admit Student", Icons.Default.PersonAdd, Color(0xFF2563EB)) { onQuickAction("Add Student") }
            QuickActionButton("Assign Dues", Icons.Default.RequestQuote, Color(0xFFF59E0B)) { onQuickAction("Assign Fees") }
            QuickActionButton("Payment Desk", Icons.Default.Payments, Color(0xFF16A34A)) { onQuickAction("Receive Payment") }
            QuickActionButton("Fin Reports", Icons.Default.Analytics, Color(0xFFDC2626)) { onQuickAction("Reports") }
        }

        // Recent Campus Activity
        Text("Recent Campus Activity", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Real-time Activities",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B)
                    )
                    // Custom pill-selector for tabs
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (activityTab == 0) Color(0xFF2563EB) else Color.Transparent)
                                .clickable { activityTab = 0 }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "New Students",
                                color = if (activityTab == 0) Color.White else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (activityTab == 1) Color(0xFF2563EB) else Color.Transparent)
                                .clickable { activityTab = 1 }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "New Payments",
                                color = if (activityTab == 1) Color.White else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (activityTab == 0) {
                    val displayStudents = remember(students) { students.reversed().take(5) }
                    if (displayStudents.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Group, null, modifier = Modifier.size(36.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No recent admissions found.", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            displayStudents.forEach { student ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF8FAFC))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEFF6FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(student.Name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                                        Text("Reg: ${student.RegNo} | ${student.Course}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFECFDF5))
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                student.Status,
                                                color = Color(0xFF059669),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(student.Batch, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val displayPayments = remember(payments) { payments.reversed().take(5) }
                    if (displayPayments.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Paid, null, modifier = Modifier.size(36.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No recent payments found.", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            displayPayments.forEach { p ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF8FAFC))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFECFDF5)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        val studentName = remember(students) {
                                            students.find { s -> s.StudentID == p.StudentID }?.Name ?: "Student: ${p.StudentID}"
                                        }
                                        Text(studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                                        Text("${p.FeeType} · Receipt: ${p.ReceiptNumber}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("$${p.Amount}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF059669))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(p.Date, fontSize = 10.sp, color = Color.Gray)
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

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title.uppercase(),
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
        }
    }
}

// ==========================================
// SUB MODULES & FORMS IMPLEMENTATION
// ==========================================

// --- STUDENTS SCREEN ---
@Composable
fun StudentsView(
    students: List<Student>,
    courses: List<Course>,
    semesters: List<Semester>,
    feeAssignments: List<FeeAssignment>,
    payments: List<Payment>,
    onAddStudent: () -> Unit,
    onBulkImport: () -> Unit,
    onEditStudent: (Student) -> Unit,
    onDeleteStudent: (String) -> Unit,
    onShowQr: (Student) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCourseFilter by remember { mutableStateOf("All Courses") }
    var selectedSemFilter by remember { mutableStateOf("All Semesters") }
    var selectedStudentForDetail by remember { mutableStateOf<Student?>(null) }

    val filteredStudents = students.filter { stu ->
        val matchesSearch = stu.Name.contains(searchQuery, ignoreCase = true) || 
                            stu.StudentID.contains(searchQuery, ignoreCase = true) ||
                            stu.Mobile.contains(searchQuery, ignoreCase = true) ||
                            stu.RegNo.contains(searchQuery, ignoreCase = true)
        val matchesCourse = selectedCourseFilter == "All Courses" || stu.Course == selectedCourseFilter
        val matchesSem = selectedSemFilter == "All Semesters" || stu.Semester == selectedSemFilter
        matchesSearch && matchesCourse && matchesSem
    }

    val totalStudents = students.size
    val activeStudents = students.count { it.Status == "Active" }
    val completedStudents = students.count { it.Status == "Completed" }
    val discontinuedStudents = students.count { it.Status == "Discontinued" }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    title = "Total Students",
                    value = "$totalStudents",
                    icon = Icons.Default.People,
                    gradient = Brush.linearGradient(colors = listOf(Color(0xFF2563EB), Color(0xFF6366F1))),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Active Students",
                    value = "$activeStudents",
                    icon = Icons.Default.Check,
                    gradient = Brush.linearGradient(colors = listOf(Color(0xFF059669), Color(0xFF10B981))),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    title = "Completed",
                    value = "$completedStudents",
                    icon = Icons.Default.School,
                    gradient = Brush.linearGradient(colors = listOf(Color(0xFF7C3AED), Color(0xFF8B5CF6))),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Discontinued",
                    value = "$discontinuedStudents",
                    icon = Icons.Default.Close,
                    gradient = Brush.linearGradient(colors = listOf(Color(0xFFDC2626), Color(0xFFEF4444))),
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick Actions (Admit and Bulk Import)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAddStudent,
                    modifier = Modifier.weight(1f).height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Admit Student", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick = onBulkImport,
                    modifier = Modifier.weight(1f).height(46.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2563EB)),
                    border = BorderStroke(1.5.dp, Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bulk Import", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // Search Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Student ID, Name, Mobile, Reg No", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.Gray)
                    }
                }
            }

            if (filteredStudents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FolderOpen, "No records", modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No students found matching filter parameters.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredStudents) { stu ->
                        val statusAccentColor = when (stu.Status) {
                            "Active" -> Color(0xFF10B981)
                            "Completed" -> Color(0xFF8B5CF6)
                            else -> Color(0xFFEF4444)
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStudentForDetail = stu },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Status colored left-edge stripe
                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .fillMaxHeight()
                                        .align(Alignment.CenterVertically)
                                        .background(statusAccentColor)
                                )
                                
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Dynamic Name Initial Avatar
                                    val avatarBgColors = listOf(
                                        Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFF8B5CF6),
                                        Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF06B6D4), Color(0xFFEC4899)
                                    )
                                    val nameHash = kotlin.math.abs(stu.Name.hashCode())
                                    val avatarBg = avatarBgColors[nameHash % avatarBgColors.size]
                                    val initial = stu.Name.firstOrNull()?.uppercase() ?: "?"
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(avatarBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = initial,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                stu.StudentID,
                                                fontSize = 12.sp,
                                                color = Color(0xFF2563EB),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            val tagColor = if (stu.Status == "Active") Color(0xFF16A34A) else Color(0xFFF59E0B)
                                            val tagBgColor = if (stu.Status == "Active") Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                                            Text(
                                                stu.Status,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = tagColor,
                                                modifier = Modifier
                                                    .background(tagBgColor, RoundedCornerShape(12.dp))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(stu.Name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("${stu.Course} · Sem ${stu.Semester}", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                                        Text(stu.Mobile, fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedStudentForDetail != null) {
        StudentDetailsDialog(
            student = selectedStudentForDetail!!,
            feeAssignments = feeAssignments,
            payments = payments,
            onDismiss = { selectedStudentForDetail = null },
            onEdit = { onEditStudent(it); selectedStudentForDetail = null },
            onDelete = { onDeleteStudent(it.StudentID); selectedStudentForDetail = null },
            onShowQr = { onShowQr(it); selectedStudentForDetail = null }
        )
    }
}

// --- BULK IMPORT VIEW & UTILITIES ---

fun readUriText(context: android.content.Context, uri: android.net.Uri): String {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().use { it.readText() }
        } ?: ""
    } catch (e: Exception) {
        android.util.Log.e("BulkImport", "Error reading URI text", e)
        ""
    }
}

fun parseStudentsFromCsv(csvText: String): List<Student> {
    val lines = csvText.lines().map { it.trim() }.filter { it.isNotEmpty() }
    if (lines.size < 2) return emptyList()
    
    val header = parseCsvLine(lines[0])
    val nameIdx = header.indexOfFirst { it.equals("name", ignoreCase = true) }
    val genderIdx = header.indexOfFirst { it.equals("gender", ignoreCase = true) }
    val dobIdx = header.indexOfFirst { it.equals("dob", ignoreCase = true) || it.equals("date of birth", ignoreCase = true) }
    val mobileIdx = header.indexOfFirst { it.equals("mobile", ignoreCase = true) || it.equals("phone", ignoreCase = true) }
    val emailIdx = header.indexOfFirst { it.equals("email", ignoreCase = true) }
    val addressIdx = header.indexOfFirst { it.equals("address", ignoreCase = true) }
    val courseIdx = header.indexOfFirst { it.equals("course", ignoreCase = true) }
    val deptIdx = header.indexOfFirst { it.equals("department", ignoreCase = true) || it.equals("dept", ignoreCase = true) }
    val semIdx = header.indexOfFirst { it.equals("semester", ignoreCase = true) || it.equals("sem", ignoreCase = true) }
    val batchIdx = header.indexOfFirst { it.equals("batch", ignoreCase = true) }
    val statusIdx = header.indexOfFirst { it.equals("status", ignoreCase = true) }
    val passwordIdx = header.indexOfFirst { it.equals("password", ignoreCase = true) }

    val list = mutableListOf<Student>()
    for (i in 1 until lines.size) {
        val row = parseCsvLine(lines[i])
        if (row.isEmpty()) continue
        
        fun getValue(idx: Int, default: String = ""): String {
            return if (idx in row.indices) row[idx] else default
        }
        
        val nameValue = getValue(nameIdx)
        if (nameValue.isEmpty()) continue
        
        list.add(
            Student(
                StudentID = "",
                RegNo = "",
                Name = nameValue,
                Gender = getValue(genderIdx, "Male"),
                DOB = getValue(dobIdx, "2000-01-01"),
                Mobile = getValue(mobileIdx),
                Email = getValue(emailIdx),
                Address = getValue(addressIdx),
                Course = getValue(courseIdx, "B.Tech"),
                Department = getValue(deptIdx, "CSE"),
                Semester = getValue(semIdx, "Semester 1"),
                Batch = getValue(batchIdx, "2026"),
                Status = getValue(statusIdx, "Active"),
                Password = getValue(passwordIdx, "stu123")
            )
        )
    }
    return list
}

private fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    var current = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
        val c = line[i]
        if (c == '\"') {
            inQuotes = !inQuotes
        } else if (c == ',' && !inQuotes) {
            result.add(current.toString().trim())
            current = StringBuilder()
        } else {
            current.append(c)
        }
        i++
    }
    result.add(current.toString().trim())
    return result
}

fun parseStudentsFromJson(jsonText: String): List<Student>? {
    return try {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val type = Types.newParameterizedType(List::class.java, Student::class.java)
        val adapter = moshi.adapter<List<Student>>(type)
        adapter.fromJson(jsonText)
    } catch (e: Exception) {
        android.util.Log.e("Parser", "JSON parse error", e)
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BulkImportView(
    viewModel: com.example.ui.viewmodel.ERPViewModel,
    onNavigateToStudents: () -> Unit
) {
    val context = LocalContext.current
    var parsedStudents by remember { mutableStateOf<List<Student>>(emptyList()) }
    var dragOver by remember { mutableStateOf(false) }
    var fileName by remember { mutableStateOf("") }
    
    val importProgress by viewModel.bulkImportProgress.collectAsState()
    val importStatus by viewModel.bulkImportStatus.collectAsState()
    
    var showSuccessDialog by remember { mutableStateOf(false) }
    var finalSuccessCount by remember { mutableStateOf(0) }
    var finalFailedCount by remember { mutableStateOf(0) }
    
    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                val content = readUriText(context, uri)
                fileName = uri.lastPathSegment ?: "Imported File"
                if (content.trim().startsWith("[")) {
                    val list = parseStudentsFromJson(content)
                    if (list != null) {
                        parsedStudents = list
                        Toast.makeText(context, "Successfully parsed ${list.size} students from JSON", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to parse JSON file", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val list = parseStudentsFromCsv(content)
                    if (list.isNotEmpty()) {
                        parsedStudents = list
                        Toast.makeText(context, "Successfully parsed ${list.size} students from CSV", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to parse CSV file (or no students found)", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    )
    
    val dragAndDropTargetValue = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val androidEvent = event.toAndroidDragEvent()
                val clipData = androidEvent.clipData
                if (clipData != null && clipData.itemCount > 0) {
                    val uri = clipData.getItemAt(0).uri
                    if (uri != null) {
                        fileName = clipData.getItemAt(0).text?.toString() ?: "Imported File"
                        val content = readUriText(context, uri)
                        if (content.trim().startsWith("[")) {
                            val list = parseStudentsFromJson(content)
                            if (list != null) {
                                parsedStudents = list
                                Toast.makeText(context, "Successfully parsed ${list.size} students from JSON", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to parse JSON file", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val list = parseStudentsFromCsv(content)
                            if (list.isNotEmpty()) {
                                parsedStudents = list
                                Toast.makeText(context, "Successfully parsed ${list.size} students from CSV", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to parse CSV file (or no students found)", Toast.LENGTH_SHORT).show()
                            }
                        }
                        return true
                    }
                }
                return false
            }
            override fun onStarted(event: DragAndDropEvent) {
                dragOver = true
            }
            override fun onEntered(event: DragAndDropEvent) {
                dragOver = true
            }
            override fun onExited(event: DragAndDropEvent) {
                dragOver = false
            }
            override fun onEnded(event: DragAndDropEvent) {
                dragOver = false
            }
        }
    }
    
    val sampleCsv = """
Name,Gender,DOB,Mobile,Email,Address,Course,Department,Semester,Batch,Status,Password
"Alice Smith","Female","2002-04-12","9876543201","alice@college.edu","Campus Block A","B.Tech","CSE","Semester 1","2026","Active","pwd123"
"Bob Jones","Male","2001-08-23","9876543202","bob@college.edu","Campus Block B","B.Tech","CSE","Semester 1","2026","Active","pwd123"
    """.trimIndent()
    
    val sampleJson = """
[
  {
    "Name": "Alice Smith",
    "Gender": "Female",
    "DOB": "2002-04-12",
    "Mobile": "9876543201",
    "Email": "alice@college.edu",
    "Address": "Campus Block A",
    "Course": "B.Tech",
    "Department": "CSE",
    "Semester": "Semester 1",
    "Batch": "2026",
    "Status": "Active",
    "Password": "pwd123"
  },
  {
    "Name": "Bob Jones",
    "Gender": "Male",
    "DOB": "2001-08-23",
    "Mobile": "9876543202",
    "Email": "bob@college.edu",
    "Address": "Campus Block B",
    "Course": "B.Tech",
    "Department": "CSE",
    "Semester": "Semester 1",
    "Batch": "2026",
    "Status": "Active",
    "Password": "pwd123"
  }
]
    """.trimIndent()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateToStudents) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Bulk Import Students",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }
            
            if (parsedStudents.isEmpty()) {
                // Info Section & Samples Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Instructions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            "Bulk import supports CSV and JSON formats. Ensure column/property names match the Student fields precisely. Click below to copy formatting templates to clipboard:",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clipData = android.content.ClipData.newPlainText("Sample CSV", sampleCsv)
                                    clipboardManager.setPrimaryClip(clipData)
                                    Toast.makeText(context, "Sample CSV copied to Clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("CSV Template", fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clipData = android.content.ClipData.newPlainText("Sample JSON", sampleJson)
                                    clipboardManager.setPrimaryClip(clipData)
                                    Toast.makeText(context, "Sample JSON copied to Clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("JSON Template", fontSize = 12.sp)
                            }
                        }
                    }
                }
                
                // Drag and Drop Zone
                val activeBgColor = if (dragOver) Color(0xFFEFF6FF) else Color.White
                val activeBorderColor = if (dragOver) Color(0xFF2563EB) else Color(0xFFCBD5E1)
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(activeBgColor, RoundedCornerShape(20.dp))
                        .border(
                            border = BorderStroke(2.dp, activeBorderColor),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .dragAndDropTarget(
                            shouldStartDragAndDrop = { true },
                            target = dragAndDropTargetValue
                        )
                        .clickable { openFileLauncher.launch(arrayOf("*/*")) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload",
                            modifier = Modifier.size(72.dp),
                            tint = if (dragOver) Color(0xFF2563EB) else Color(0xFF64748B)
                        )
                        Text(
                            text = if (dragOver) "Drop File Here!" else "Drag & Drop CSV or JSON File",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dragOver) Color(0xFF2563EB) else Color(0xFF1E293B)
                        )
                        Text(
                            text = "or tap here to browse local storage",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Preview Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Preview (${parsedStudents.size} Records Found)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E293B)
                    )
                    TextButton(
                        onClick = {
                            parsedStudents = emptyList()
                            fileName = ""
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear List")
                    }
                }
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(parsedStudents) { stu ->
                        val isValid = stu.Name.isNotEmpty() && stu.Email.isNotEmpty()
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isValid) Color(0xFFE2E8F0) else Color(0xFFFCA5A5))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(if (isValid) Color(0xFFDCFCE7) else Color(0xFFFEE2E2), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (isValid) Color(0xFF16A34A) else Color(0xFFDC2626)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        stu.Name.ifEmpty { "[Missing Name]" },
                                        fontWeight = FontWeight.Bold,
                                        color = if (stu.Name.isNotEmpty()) Color(0xFF1E293B) else Color.Red,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Course: ${stu.Course} · Dept: ${stu.Department} · Sem: ${stu.Semester}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        "Email: ${stu.Email.ifEmpty { "[Missing Email]" }} · Mobile: ${stu.Mobile}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
                
                Button(
                    onClick = {
                        viewModel.bulkImportStudents(parsedStudents) { success, failed ->
                            finalSuccessCount = success
                            finalFailedCount = failed
                            showSuccessDialog = true
                            parsedStudents = emptyList()
                            fileName = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Bulk Import", fontWeight = FontWeight.Bold)
                }
            }
        }
        
        // Progress Overlay
        if (importProgress != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(0.85f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { importProgress ?: 0f },
                            modifier = Modifier.size(56.dp),
                            color = Color(0xFF2563EB),
                            strokeWidth = 5.dp
                        )
                        Text(
                            "Importing Records",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E293B)
                        )
                        LinearProgressIndicator(
                            progress = { importProgress ?: 0f },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = Color(0xFF2563EB),
                            trackColor = Color(0xFFEFF6FF)
                        )
                        Text(
                            text = importStatus ?: "Preparing import...",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        // Import Complete Success Dialog
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSuccessDialog = false
                    viewModel.resetBulkImportProgress()
                },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFFDCFCE7), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                },
                title = { Text("Import Completed", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Your student records have been synchronized successfully with the Google Sheets backend.",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "$finalSuccessCount",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                                Text("Succeeded", fontSize = 12.sp, color = Color.Gray)
                            }
                            Box(modifier = Modifier.height(40.dp).width(1.dp).background(Color.LightGray))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "$finalFailedCount",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                                Text("Failed", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            viewModel.resetBulkImportProgress()
                            onNavigateToStudents()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Done")
                    }
                }
            )
        }
    }
}

// --- STUDENT DETAILS DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailsDialog(
    student: Student,
    feeAssignments: List<FeeAssignment>,
    payments: List<Payment>,
    onDismiss: () -> Unit,
    onEdit: (Student) -> Unit = {},
    onDelete: (Student) -> Unit = {},
    onShowQr: (Student) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("Profile") }
    
    val studentFeeAssignments = feeAssignments.filter { it.StudentID == student.StudentID }
    val studentPayments = payments.filter { it.StudentID == student.StudentID }

    val totalFee = studentFeeAssignments.sumOf { it.TotalAmount.toDoubleOrNull() ?: 0.0 }
    val totalPaid = studentPayments.sumOf { it.Amount.toDoubleOrNull() ?: 0.0 }
    val totalUnpaid = maxOf(0.0, totalFee - totalPaid)
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF8FAFC)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top AppBar
                TopAppBar(
                    title = { Text("Student Details", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                    },
                    actions = {
                        IconButton(onClick = { onEdit(student) }) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White) }
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White) }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(text = { Text("Delete Student", color = Color.Red) }, onClick = { showMenu = false; onDelete(student) })
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E3A8A))
                )
                
                // Header Details
                Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.size(80.dp).background(Color(0xFFE2E8F0), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(student.Name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                val tagColor = if (student.Status == "Active") Color(0xFF16A34A) else Color(0xFFF59E0B)
                                val tagBgColor = if (student.Status == "Active") Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                                Text(
                                    student.Status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = tagColor,
                                    modifier = Modifier
                                        .background(tagBgColor, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(student.StudentID, fontSize = 14.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Medium)
                            Text("Reg No : ${student.RegNo}", fontSize = 13.sp, color = Color.Gray)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("${student.Course} - ${student.Semester}", fontSize = 13.sp, color = Color.Gray)
                                IconButton(onClick = { onShowQr(student) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
                
                // Tabs
                val tabs = listOf("Profile", "Fees", "Payments", "Documents", "More")
                ScrollableTabRow(
                    selectedTabIndex = tabs.indexOf(selectedTab),
                    containerColor = Color.White,
                    contentColor = Color(0xFF2563EB),
                    edgePadding = 0.dp,
                    divider = { HorizontalDivider(color = Color(0xFFE2E8F0)) }
                ) {
                    tabs.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = { Text(tab, fontSize = 13.sp, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium) },
                            unselectedContentColor = Color.Gray
                        )
                    }
                }
                
                // Tab Content
                Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                    when (selectedTab) {
                        "Profile" -> ProfileTab(student, onEdit)
                        "Fees" -> FeesTab(studentFeeAssignments, studentPayments, totalFee, totalPaid, totalUnpaid)
                        "Payments" -> PaymentsTab(studentPayments, totalPaid)
                        "Documents" -> DocumentsTab()
                        else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Coming Soon") }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileTab(student: Student, onEdit: (Student) -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Personal Information
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Personal Information", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2563EB))
                }
                Spacer(modifier = Modifier.height(16.dp))
                DetailRowItem("Gender", student.Gender)
                DetailRowItem("Date of Birth", student.DOB)
                DetailRowItem("Mobile", student.Mobile, hasAction = true, actionIcon = Icons.Default.Phone)
                DetailRowItem("Email", student.Email, hasAction = true, actionIcon = Icons.Default.Email)
                DetailRowItem("Blood Group", "O+")
                DetailRowItem("Aadhar Number", "1234 5678 9012")
                DetailRowItem("Address", student.Address)
            }
        }
        
        // Academic Information
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Academic Information", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2563EB))
                }
                Spacer(modifier = Modifier.height(16.dp))
                DetailRowItem("Course", student.Course)
                DetailRowItem("Department", student.Department)
                DetailRowItem("Semester", student.Semester)
                DetailRowItem("Batch", student.Batch)
                DetailRowItem("Joining Date", "10-Feb-2025")
                DetailRowItem("Status", student.Status, isStatus = true)
            }
        }
        
        // Bottom Actions
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionButton(Icons.Default.Phone, "Call", modifier = Modifier.weight(1f))
            ActionButton(Icons.Default.Chat, "WhatsApp", modifier = Modifier.weight(1f))
            ActionButton(Icons.Default.Edit, "Edit", modifier = Modifier.weight(1f), onClick = { onEdit(student) })
            ActionButton(Icons.Default.MoreHoriz, "More", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun DetailRowItem(label: String, value: String, hasAction: Boolean = false, actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null, isStatus: Boolean = false) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.weight(1f))
            if (isStatus) {
                val tagColor = if (value == "Active") Color(0xFF16A34A) else Color(0xFFF59E0B)
                val tagBgColor = if (value == "Active") Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                Text(
                    value,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = tagColor,
                    modifier = Modifier
                        .background(tagBgColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(2f), horizontalArrangement = Arrangement.End) {
                    Text(value.ifEmpty { "N/A" }, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B), textAlign = TextAlign.End)
                    if (hasAction && actionIcon != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.size(24.dp).background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                            Icon(actionIcon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2E8F0))
    }
}

@Composable
fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
        }
    }
}

@Composable
fun FeesTab(feeAssignments: List<FeeAssignment>, payments: List<Payment>, totalFee: Double, totalPaid: Double, totalUnpaid: Double) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Fee Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2563EB))
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeeSummaryBox("Total Fees", "₹ ${totalFee.toInt()}", Color(0xFFEFF6FF), Color(0xFF2563EB), modifier = Modifier.weight(1f))
            FeeSummaryBox("Paid Amount", "₹ ${totalPaid.toInt()}", Color(0xFFDCFCE7), Color(0xFF16A34A), modifier = Modifier.weight(1f))
            FeeSummaryBox("Balance", "₹ ${totalUnpaid.toInt()}", Color(0xFFFEE2E2), Color(0xFFDC2626), modifier = Modifier.weight(1f))
            FeeSummaryBox("Due Date", "30-Aug-2025", Color(0xFFF3E8FF), Color(0xFF8B5CF6), modifier = Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Fee Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2563EB))
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Semester", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray, modifier = Modifier.weight(1f))
                    Text("Total Fees", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text("Paid", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text("Balance", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text("Status", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }
                
                val mockFees = listOf(
                    Triple("I Semester", Triple(45000, 45000, 0), "Paid"),
                    Triple("II Semester", Triple(46000, 25000, 21000), "Partial"),
                    Triple("III Semester", Triple(46000, 0, 46000), "Pending"),
                    Triple("IV Semester", Triple(46000, 0, 46000), "Pending")
                )
                
                mockFees.forEach { (sem, amounts, status) ->
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(sem, fontSize = 11.sp, color = Color(0xFF1E293B), modifier = Modifier.weight(1f))
                        Text("₹ ${amounts.first}", fontSize = 11.sp, color = Color(0xFF1E293B), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text("₹ ${amounts.second}", fontSize = 11.sp, color = Color(0xFF1E293B), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text("₹ ${amounts.third}", fontSize = 11.sp, color = Color(0xFF1E293B), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                            val tagColor = when(status) { "Paid" -> Color(0xFF16A34A); "Partial" -> Color(0xFFF59E0B); else -> Color(0xFFDC2626) }
                            val tagBg = when(status) { "Paid" -> Color(0xFFDCFCE7); "Partial" -> Color(0xFFFEF3C7); else -> Color(0xFFFEE2E2) }
                            Text(status, fontSize = 10.sp, color = tagColor, modifier = Modifier.background(tagBg, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color(0xFF2563EB))) {
            Text("View Fee Assignment", color = Color(0xFF2563EB))
        }
    }
}

@Composable
fun FeeSummaryBox(title: String, amount: String, bgColor: Color, contentColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(bgColor, RoundedCornerShape(8.dp)).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, fontSize = 10.sp, color = contentColor.copy(alpha = 0.8f), maxLines = 1)
        Spacer(modifier = Modifier.height(4.dp))
        Text(amount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = contentColor, maxLines = 1)
    }
}

@Composable
fun PaymentsTab(payments: List<Payment>, totalPaid: Double) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Payment Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2563EB))
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeeSummaryBox("Total Paid", "₹ ${totalPaid.toInt()}", Color(0xFFDCFCE7), Color(0xFF16A34A), modifier = Modifier.weight(1f))
            FeeSummaryBox("Total Transactions", "${payments.size.coerceAtLeast(2)}", Color(0xFFEFF6FF), Color(0xFF2563EB), modifier = Modifier.weight(1f))
            FeeSummaryBox("Last Payment", "15-May-2025", Color(0xFFF3E8FF), Color(0xFF8B5CF6), modifier = Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Payment History", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2563EB))
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("RC25080018", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                        Text("II Semester - Tuition Fee", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("UPI", fontSize = 10.sp, color = Color(0xFF16A34A), modifier = Modifier.background(Color(0xFFDCFCE7), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("15-May-2025", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("₹ 25,000", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF16A34A))
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("RC25070010", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                        Text("I Semester - Full Payment", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Cash", fontSize = 10.sp, color = Color(0xFF16A34A), modifier = Modifier.background(Color(0xFFDCFCE7), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("10-Feb-2025", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("₹ 45,000", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF16A34A))
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color(0xFF2563EB))) {
            Text("View All Payments", color = Color(0xFF2563EB))
        }
    }
}

@Composable
fun DocumentsTab() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Uploaded Documents", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2563EB))
        Spacer(modifier = Modifier.height(12.dp))
        
        val docs = listOf(
            Triple("Aadhar Card", "aadhar_vasu.pdf", "12-Feb-2025"),
            Triple("10th Marksheet", "10th_marksheet.pdf", "12-Feb-2025"),
            Triple("12th Marksheet", "12th_marksheet.pdf", "12-Feb-2025"),
            Triple("Transfer Certificate", "tc_vasu.pdf", "12-Feb-2025"),
            Triple("Passport Photo", "photo_vasu.jpg", "12-Feb-2025")
        )
        
        docs.forEach { doc ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(doc.first, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                        Text(doc.second, fontSize = 12.sp, color = Color.Gray)
                    }
                    Text(doc.third, fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF2563EB))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color(0xFF2563EB))) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload New Document", color = Color(0xFF2563EB))
        }
    }
}

// --- STUDENT FORM DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormDialog(
    student: Student?,
    courses: List<Course>,
    departments: List<Department>,
    batches: List<Batch>,
    semesters: List<Semester>,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    val isEdit = student != null
    var name by remember { mutableStateOf(student?.Name ?: "") }
    var gender by remember { mutableStateOf(student?.Gender ?: "Male") }
    var dob by remember { mutableStateOf(student?.DOB ?: "2006-01-01") }
    var mobile by remember { mutableStateOf(student?.Mobile ?: "") }
    var email by remember { mutableStateOf(student?.Email ?: "") }
    var address by remember { mutableStateOf(student?.Address ?: "") }
    var course by remember { mutableStateOf(student?.Course ?: (courses.firstOrNull()?.CourseCode ?: "")) }
    var department by remember { mutableStateOf(student?.Department ?: (departments.firstOrNull()?.DepartmentCode ?: "")) }
    var semester by remember { mutableStateOf(student?.Semester ?: "1") }
    var batch by remember { mutableStateOf(student?.Batch ?: (batches.firstOrNull()?.BatchName ?: "")) }
    var status by remember { mutableStateOf(student?.Status ?: "Active") }
    var password by remember { mutableStateOf(student?.Password ?: "stu123") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isEdit) "Modify Admission Details" else "New ERP Admission Form",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF2563EB)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Student Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    var genderExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = !genderExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Gender") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = genderExpanded,
                            onDismissRequest = { genderExpanded = false }
                        ) {
                            listOf("Male", "Female", "Other").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        gender = option
                                        genderExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = { Text("DOB (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Institutional Email ID") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Residential Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                var courseExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = courseExpanded,
                    onExpandedChange = { courseExpanded = !courseExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = course,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Course") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = courseExpanded,
                        onDismissRequest = { courseExpanded = false }
                    ) {
                        if (courses.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No courses available") },
                                onClick = { courseExpanded = false }
                            )
                        } else {
                            courses.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text("${c.CourseCode} - ${c.CourseName}") },
                                    onClick = {
                                        course = c.CourseCode
                                        courseExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                var deptExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = deptExpanded,
                    onExpandedChange = { deptExpanded = !deptExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Department") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = deptExpanded,
                        onDismissRequest = { deptExpanded = false }
                    ) {
                        if (departments.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No departments available") },
                                onClick = { deptExpanded = false }
                            )
                        } else {
                            departments.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text("${d.DepartmentCode} - ${d.DepartmentName}") },
                                    onClick = {
                                        department = d.DepartmentCode
                                        deptExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    var semesterExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = semesterExpanded,
                        onExpandedChange = { semesterExpanded = !semesterExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = semester,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Semester") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = semesterExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = semesterExpanded,
                            onDismissRequest = { semesterExpanded = false }
                        ) {
                            val availableSemesters = if (semesters.isNotEmpty()) {
                                semesters.filter { it.Course.equals(course, ignoreCase = true) }
                                    .map { it.SemesterNo }
                                    .distinct()
                                    .sortedBy { it.toIntOrNull() ?: 0 }
                            } else emptyList()

                            val displayList = if (availableSemesters.isNotEmpty()) {
                                availableSemesters
                            } else {
                                listOf("Sem/Year 1", "Sem/Year 2", "Sem/Year 3", "Sem 4")
                            }

                            displayList.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(if (s.startsWith("Sem") || s.startsWith("Year")) s else "Semester $s") },
                                    onClick = {
                                        semester = s
                                        semesterExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    var batchExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = batchExpanded,
                        onExpandedChange = { batchExpanded = !batchExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = batch,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Batch") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = batchExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = batchExpanded,
                            onDismissRequest = { batchExpanded = false }
                        ) {
                            if (batches.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No batches available") },
                                    onClick = { batchExpanded = false }
                                )
                            } else {
                                batches.forEach { b ->
                                    DropdownMenuItem(
                                        text = { Text(b.BatchName) },
                                        onClick = {
                                            batch = b.BatchName
                                            batchExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Portal Password") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isEmpty() || email.isEmpty()) {
                                // Simplified toast trigger
                            } else {
                                onSave(
                                    Student(
                                        StudentID = student?.StudentID ?: "",
                                        RegNo = student?.RegNo ?: "",
                                        Name = name,
                                        Gender = gender,
                                        DOB = dob,
                                        Mobile = mobile,
                                        Email = email,
                                        Address = address,
                                        Course = course,
                                        Department = department,
                                        Semester = semester,
                                        Batch = batch,
                                        JoiningDate = student?.JoiningDate ?: "",
                                        Status = status,
                                        Password = password
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Process Admission")
                    }
                }
            }
        }
    }
}

// --- ACADEMIC CONFIGURATION STEP SYSTEM & HERITAGE UTILS ---
@Composable
fun AdministrativeSetupStepper(
    currentStep: Int // 1 = Course, 2 = Department, 3 = Batch
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFBF7)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFDFD1B8))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ACADEMIC CONFIGURATION WORKFLOW",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC2410C),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step 1: Courses
                StepIndicatorItem(
                    stepNo = "1",
                    label = "Courses",
                    isActive = currentStep == 1,
                    isCompleted = currentStep > 1,
                    modifier = Modifier.weight(1f)
                )

                // Divider
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(2.dp)
                        .background(if (currentStep > 1) Color(0xFFC2410C) else Color(0xFFDFD1B8))
                )

                // Step 2: Departments
                StepIndicatorItem(
                    stepNo = "2",
                    label = "Departments",
                    isActive = currentStep == 2,
                    isCompleted = currentStep > 2,
                    modifier = Modifier.weight(1f)
                )

                // Divider
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(2.dp)
                        .background(if (currentStep > 2) Color(0xFFC2410C) else Color(0xFFDFD1B8))
                )

                // Step 3: Batches
                StepIndicatorItem(
                    stepNo = "3",
                    label = "Batches",
                    isActive = currentStep == 3,
                    isCompleted = currentStep > 3,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StepIndicatorItem(
    stepNo: String,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isActive -> Color(0xFFC2410C)
        isCompleted -> Color(0xFFC2410C).copy(alpha = 0.15f)
        else -> Color(0xFFF1F5F9)
    }
    val contentColor = when {
        isActive -> Color.White
        isCompleted -> Color(0xFFC2410C)
        else -> Color.Gray
    }
    val labelColor = if (isActive) Color(0xFF2C1B11) else Color.Gray
    val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(bgColor, CircleShape)
                .border(
                    width = 1.dp,
                    color = if (isActive) Color(0xFFC2410C) else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color(0xFFC2410C),
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Text(
                    text = stepNo,
                    color = contentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = fontWeight,
            color = labelColor
        )
    }
}

@Composable
fun HeritageViewHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    actionButtonLabel: String,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFDFD1B8))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_south_indian_login_banner_1782908329236),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                alpha = 0.3f
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF2C1B11).copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFC2410C), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Normal
                    )
                }

                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2410C)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(actionButtonLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// --- COURSES VIEW ---
@Composable
fun CoursesView(
    courses: List<Course>,
    onAddCourse: () -> Unit,
    onEditCourse: (Course) -> Unit,
    onDeleteCourse: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF7F0))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AdministrativeSetupStepper(currentStep = 1)

        HeritageViewHeader(
            title = "Academic Courses",
            subtitle = "Manage university degrees, durations, and fee models",
            icon = Icons.Default.School,
            actionButtonLabel = "Add Course",
            onActionClick = onAddCourse
        )

        if (courses.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFDFD1B8))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFFC2410C), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No Academic Courses Available", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2C1B11))
                    Text("Click 'Add Course' above to register the first degree.", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(courses) { crs ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.2.dp, Color(0xFFDFD1B8).copy(alpha = 0.8f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(Color(0xFFFFF1EB), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = crs.CourseCode.take(2).uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC2410C)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = crs.CourseName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF2C1B11)
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFFF1EB), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = crs.CourseCode,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC2410C)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Duration: ${crs.Duration} (${crs.TotalSemesters} Semesters)", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Annual Fees: ₹ ${crs.CourseFees}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF16A34A),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = { onEditCourse(crs) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, "Edit", tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { onDeleteCourse(crs.CourseCode) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                            
                            if (crs.Description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = crs.Description,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- COURSE FORM DIALOG ---
@Composable
fun CourseFormDialog(
    course: Course?,
    onDismiss: () -> Unit,
    onSave: (Course) -> Unit
) {
    val isEdit = course != null
    var code by remember { mutableStateOf(course?.CourseCode ?: "") }
    var name by remember { mutableStateOf(course?.CourseName ?: "") }
    var duration by remember { mutableStateOf(course?.Duration ?: "3 Years") }
    var semestersNo by remember { mutableStateOf(course?.TotalSemesters ?: "6") }
    var fees by remember { mutableStateOf(course?.CourseFees ?: "") }
    var desc by remember { mutableStateOf(course?.Description ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF7F0)),
            border = BorderStroke(1.2.dp, Color(0xFFDFD1B8)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isEdit) "Modify Academic Course" else "Register New Course",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF2C1B11)
                )
                
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Course Code (e.g. BCOM)") },
                    enabled = !isEdit,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC2410C),
                        focusedLabelColor = Color(0xFFC2410C),
                        unfocusedBorderColor = Color(0xFFDFD1B8)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Course Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC2410C),
                        focusedLabelColor = Color(0xFFC2410C),
                        unfocusedBorderColor = Color(0xFFDFD1B8)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC2410C),
                            focusedLabelColor = Color(0xFFC2410C),
                            unfocusedBorderColor = Color(0xFFDFD1B8)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = semestersNo,
                        onValueChange = { semestersNo = it },
                        label = { Text("Total Semesters") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC2410C),
                            focusedLabelColor = Color(0xFFC2410C),
                            unfocusedBorderColor = Color(0xFFDFD1B8)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                OutlinedTextField(
                    value = fees,
                    onValueChange = { fees = it },
                    label = { Text("Annual Fees (₹)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC2410C),
                        focusedLabelColor = Color(0xFFC2410C),
                        unfocusedBorderColor = Color(0xFFDFD1B8)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC2410C),
                        focusedLabelColor = Color(0xFFC2410C),
                        unfocusedBorderColor = Color(0xFFDFD1B8)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF2C1B11))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(Course(code, name, duration, semestersNo, fees, desc, "Active")) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2410C)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Course", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// --- DEPARTMENTS VIEW ---
@Composable
fun DepartmentsView(
    departments: List<Department>,
    onAddDept: () -> Unit,
    onEditDept: (Department) -> Unit,
    onDeleteDept: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF7F0))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AdministrativeSetupStepper(currentStep = 2)

        HeritageViewHeader(
            title = "Academic Departments",
            subtitle = "Administer faculties, departments, and academic heads",
            icon = Icons.Default.Business,
            actionButtonLabel = "Add Dept",
            onActionClick = onAddDept
        )

        if (departments.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFDFD1B8))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFFC2410C), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No Departments Registered", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2C1B11))
                    Text("Click 'Add Dept' above to configure the first academic department.", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(departments) { dept ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.2.dp, Color(0xFFDFD1B8).copy(alpha = 0.8f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(Color(0xFFFFF1EB), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dept.DepartmentCode.take(2).uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC2410C)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = dept.DepartmentName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF2C1B11)
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFFF1EB), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = dept.DepartmentCode,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC2410C)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Head of Department (HOD): ${dept.HOD}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF2C1B11),
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = { onEditDept(dept) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, "Edit", tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { onDeleteDept(dept.DepartmentCode) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
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

// --- DEPARTMENT FORM DIALOG ---
@Composable
fun DeptFormDialog(
    dept: Department?,
    onDismiss: () -> Unit,
    onSave: (Department) -> Unit
) {
    val isEdit = dept != null
    var code by remember { mutableStateOf(dept?.DepartmentCode ?: "") }
    var name by remember { mutableStateOf(dept?.DepartmentName ?: "") }
    var hod by remember { mutableStateOf(dept?.HOD ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF7F0)),
            border = BorderStroke(1.2.dp, Color(0xFFDFD1B8)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isEdit) "Modify Department" else "New Department Form",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF2C1B11)
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Dept Code (e.g. CS)") },
                    enabled = !isEdit,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC2410C),
                        focusedLabelColor = Color(0xFFC2410C),
                        unfocusedBorderColor = Color(0xFFDFD1B8)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Department Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC2410C),
                        focusedLabelColor = Color(0xFFC2410C),
                        unfocusedBorderColor = Color(0xFFDFD1B8)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = hod,
                    onValueChange = { hod = it },
                    label = { Text("Head of Department (HOD)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC2410C),
                        focusedLabelColor = Color(0xFFC2410C),
                        unfocusedBorderColor = Color(0xFFDFD1B8)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF2C1B11))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(Department(code, name, hod, "Active")) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2410C)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Department", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// --- BATCHES VIEW ---
@Composable
fun BatchesView(
    batches: List<Batch>,
    onAddBatch: () -> Unit,
    onEditBatch: (Batch) -> Unit,
    onDeleteBatch: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF7F0))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AdministrativeSetupStepper(currentStep = 3)

        HeritageViewHeader(
            title = "Academic Batches",
            subtitle = "Define student intake cohorts, timelines, and academic cycles",
            icon = Icons.Default.CalendarMonth,
            actionButtonLabel = "Add Batch",
            onActionClick = onAddBatch
        )

        if (batches.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFDFD1B8))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFFC2410C), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No Academic Batches Configured", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2C1B11))
                    Text("Click 'Add Batch' above to initialize the first intake cohort.", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(batches) { batch ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.2.dp, Color(0xFFDFD1B8).copy(alpha = 0.8f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = Color(0xFFC2410C),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = batch.BatchName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF2C1B11)
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFFF1EB), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = batch.AcademicYear,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC2410C)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Timeline: ${batch.StartDate} to ${batch.EndDate}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = { onEditBatch(batch) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, "Edit", tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { onDeleteBatch(batch.BatchName) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
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

// --- BATCH FORM DIALOG ---
@Composable
fun BatchFormDialog(
    batch: Batch?,
    onDismiss: () -> Unit,
    onSave: (Batch) -> Unit
) {
    val isEdit = batch != null
    var name by remember { mutableStateOf(batch?.BatchName ?: "") }
    var year by remember { mutableStateOf(batch?.AcademicYear ?: "2026-2030") }
    var start by remember { mutableStateOf(batch?.StartDate ?: "2026-08-01") }
    var end by remember { mutableStateOf(batch?.EndDate ?: "2030-06-30") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF7F0)),
            border = BorderStroke(1.2.dp, Color(0xFFDFD1B8)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isEdit) "Modify Batch" else "New Batch Form",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF2C1B11)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Batch Name (e.g. Batch 2026)") },
                    enabled = !isEdit,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC2410C),
                        focusedLabelColor = Color(0xFFC2410C),
                        unfocusedBorderColor = Color(0xFFDFD1B8)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Academic Years Timeline") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC2410C),
                        focusedLabelColor = Color(0xFFC2410C),
                        unfocusedBorderColor = Color(0xFFDFD1B8)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = start,
                        onValueChange = { start = it },
                        label = { Text("Start Date") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC2410C),
                            focusedLabelColor = Color(0xFFC2410C),
                            unfocusedBorderColor = Color(0xFFDFD1B8)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = end,
                        onValueChange = { end = it },
                        label = { Text("End Date") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFC2410C),
                            focusedLabelColor = Color(0xFFC2410C),
                            unfocusedBorderColor = Color(0xFFDFD1B8)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF2C1B11))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(Batch(name, year, start, end, "Active")) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2410C)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Batch", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// --- SEMESTERS VIEW ---
@Composable
fun SemestersView(
    semesters: List<Semester>,
    courses: List<Course>,
    onAddSem: () -> Unit,
    onEditSem: (Semester) -> Unit,
    onDeleteSem: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Semester Schemas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Button(
                onClick = onAddSem,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Sem Schema")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(semesters) { sem ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val semLabel = if (sem.SemesterNo.startsWith("Sem") || sem.SemesterNo.startsWith("Year")) sem.SemesterNo else "Semester ${sem.SemesterNo}"
                            Text("$semLabel - ${sem.Course}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Row {
                                IconButton(onClick = { onEditSem(sem) }) { Icon(Icons.Default.Edit, null, tint = Color(0xFFF59E0B)) }
                                IconButton(onClick = { onDeleteSem(sem.SemesterNo, sem.Course) }) { Icon(Icons.Default.Delete, null, tint = Color(0xFFDC2626)) }
                            }
                        }
                        Text("Semester Base Fees: $${sem.SemesterFees}", fontSize = 12.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                        Text("Subjects: ${sem.Subjects}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// --- SEMESTER FORM DIALOG ---
@Composable
fun SemesterFormDialog(
    semester: Semester,
    courses: List<Course>,
    onDismiss: () -> Unit,
    onSave: (Semester) -> Unit
) {
    var semNo by remember { mutableStateOf(semester.SemesterNo) }
    var courseCode by remember { mutableStateOf(semester.Course.ifEmpty { courses.firstOrNull()?.CourseCode ?: "" }) }
    var fees by remember { mutableStateOf(semester.SemesterFees) }
    var subjects by remember { mutableStateOf(semester.Subjects) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Configure Semester Schema", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                
                OutlinedTextField(
                    value = semNo,
                    onValueChange = { semNo = it },
                    label = { Text("Semester Number (e.g. 1)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = courseCode,
                    onValueChange = { courseCode = it },
                    label = { Text("Target Course Code") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fees,
                    onValueChange = { fees = it },
                    label = { Text("Semester Fee Allocation ($)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subjects,
                    onValueChange = { subjects = it },
                    label = { Text("Subjects (Comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(Semester(semNo, courseCode, fees, subjects, "Active")) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) { Text("Save Schema") }
                }
            }
        }
    }
}

// --- FEE ASSIGNMENTS VIEW ---
@Composable
fun FeeAssignmentsView(
    feeAssignments: List<FeeAssignment>,
    onAssign: () -> Unit
) {
    val totalAssigned = remember(feeAssignments) { feeAssignments.sumOf { it.TotalAmount.toDoubleOrNull() ?: 0.0 }.toInt() }
    val uniqueStudentsCount = remember(feeAssignments) { feeAssignments.map { it.StudentID }.distinct().size }
    val avgFee = remember(feeAssignments) { if (feeAssignments.isEmpty()) 0 else totalAssigned / feeAssignments.size }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dues Assigned", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1E293B))
            Button(
                onClick = onAssign,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddCard, null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Assign Fee Dues")
            }
        }

        // Color-rich Stats Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "Total Assigned",
                value = "₹$totalAssigned",
                icon = Icons.Default.TrendingUp,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFFEA580C), Color(0xFFF97316))),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Target Students",
                value = "$uniqueStudentsCount",
                icon = Icons.Default.People,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFF2563EB), Color(0xFF60A5FA))),
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "Average Allocated",
                value = "₹$avgFee",
                icon = Icons.Default.Assessment,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text("All Assigned Fee Dues", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(feeAssignments) { assign ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Orange indicator stripe on the left to indicate outstanding due obligation
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .fillMaxHeight()
                                .align(Alignment.CenterVertically)
                                .background(Color(0xFFEA580C))
                        )
                        
                        Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Student ID: ${assign.StudentID}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                Text(
                                    "₹ ${assign.TotalAmount}",
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFEF4444),
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Course: ${assign.Course} | Semester: ${assign.Semester}", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text("Fee Breakup:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Tuition: ₹${assign.TuitionFee}", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                                Text("Exam: ₹${assign.ExamFee}", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                                Text("Library: ₹${assign.LibraryFee}", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = Color(0xFFEA580C),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Due Date: ${assign.DueDate}", fontSize = 11.sp, color = Color(0xFFEA580C), fontWeight = FontWeight.Bold)
                                }
                                if (assign.Remarks.isNotEmpty()) {
                                    Text("Remarks: ${assign.Remarks}", fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- FEE ASSIGNMENT FORM DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeeAssignmentFormDialog(
    courses: List<Course>,
    batches: List<Batch>,
    students: List<Student>,
    feeAssignments: List<FeeAssignment>,
    onDismiss: () -> Unit,
    onSave: (FeeAssignment, Boolean, String) -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) }

    var studentId by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf(courses.firstOrNull()?.CourseCode ?: "") }
    var semesterNo by remember { mutableStateOf("1") }
    var studentQuery by remember { mutableStateOf("") }

    var admissionFee by remember { mutableStateOf("0") }
    var tuitionFee by remember { mutableStateOf("0") }
    var examFee by remember { mutableStateOf("0") }
    var libraryFee by remember { mutableStateOf("0") }
    var labFee by remember { mutableStateOf("0") }
    var transportFee by remember { mutableStateOf("0") }
    var hostelFee by remember { mutableStateOf("0") }
    var otherFee by remember { mutableStateOf("0") }
    var scholarship by remember { mutableStateOf("0") }
    var discount by remember { mutableStateOf("0") }
    var fine by remember { mutableStateOf("0") }
    var dueDate by remember { mutableStateOf("30-Sep-2025") }
    var remarks by remember { mutableStateOf("") }

    val getSemesterName = { no: Int ->
        when (no) {
            1 -> "I Semester"
            2 -> "II Semester"
            3 -> "III Semester"
            4 -> "IV Semester"
            5 -> "V Semester"
            6 -> "VI Semester"
            7 -> "VII Semester"
            8 -> "VIII Semester"
            else -> "$no Semester"
        }
    }

    val selectedStudent = remember(studentId, students) { students.find { it.StudentID == studentId } }

    LaunchedEffect(selectedStudent, semesterNo) {
        selectedStudent?.let { s ->
            val courseObj = courses.find { it.CourseCode == s.Course || it.CourseName == s.Course }
            if (courseObj != null) {
                val totalSems = courseObj.TotalSemesters.toIntOrNull() ?: 4
                val totalCourseFeesVal = courseObj.CourseFees.toDoubleOrNull() ?: 155000.0
                val semesterFeeVal = totalCourseFeesVal / totalSems
                
                tuitionFee = (semesterFeeVal * 0.70).toInt().toString()
                examFee = (semesterFeeVal * 0.10).toInt().toString()
                libraryFee = (semesterFeeVal * 0.05).toInt().toString()
                labFee = (semesterFeeVal * 0.05).toInt().toString()
                hostelFee = "0"
                transportFee = "0"
                otherFee = (semesterFeeVal * 0.10).toInt().toString()
                
                val isFirstSem = semesterNo.contains("1") || semesterNo.startsWith("I ") || semesterNo.equals("I Semester", ignoreCase = true)
                admissionFee = if (isFirstSem) "5000" else "0"
                
                scholarship = "0"
                discount = "0"
                fine = "0"
                remarks = "Fees assigned for $semesterNo - ${s.Course}"
            }
        }
    }

    val totalAmountComputed = remember(admissionFee, tuitionFee, examFee, libraryFee, labFee, transportFee, hostelFee, otherFee, scholarship, discount, fine) {
        val add = admissionFee.toDoubleOrNull() ?: 0.0
        val tui = tuitionFee.toDoubleOrNull() ?: 0.0
        val exm = examFee.toDoubleOrNull() ?: 0.0
        val lib = libraryFee.toDoubleOrNull() ?: 0.0
        val lab = labFee.toDoubleOrNull() ?: 0.0
        val tra = transportFee.toDoubleOrNull() ?: 0.0
        val hos = hostelFee.toDoubleOrNull() ?: 0.0
        val oth = otherFee.toDoubleOrNull() ?: 0.0
        val sch = scholarship.toDoubleOrNull() ?: 0.0
        val dsc = discount.toDoubleOrNull() ?: 0.0
        val fn = fine.toDoubleOrNull() ?: 0.0
        
        val sum = (add + tui + exm + lib + lab + tra + hos + oth + fn) - (sch + dsc)
        if (sum > 0) sum.toInt().toString() else "0"
    }
    
    val totalFeesSum = remember(totalAmountComputed) {
        (totalAmountComputed.toDoubleOrNull() ?: 0.0) + (discount.toDoubleOrNull() ?: 0.0) + (scholarship.toDoubleOrNull() ?: 0.0) - (fine.toDoubleOrNull() ?: 0.0)
    }

    Dialog(
        onDismissRequest = { if (currentStep == 5) onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints {
            val isCompact = maxWidth < 550.dp
            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF8FAFC)) {
                Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("Assign Fees", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        if (currentStep < 5) {
                            IconButton(onClick = { if (currentStep > 1) currentStep-- else onDismiss() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E3A8A))
                )

                if (currentStep < 5) {
                    val steps = listOf("Student", "Semester", "Fees", "Review")
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 16.dp, horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        steps.forEachIndexed { index, stepName ->
                            val stepNum = index + 1
                            val isCompleted = currentStep > stepNum
                            val isCurrent = currentStep == stepNum
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier.size(24.dp).background(
                                        color = if (isCompleted || isCurrent) Color(0xFF2563EB) else Color(0xFFE2E8F0),
                                        shape = CircleShape
                                    ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    } else {
                                        Text(stepNum.toString(), color = if (isCurrent) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(stepName, fontSize = 10.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal, color = if (isCurrent || isCompleted) Color(0xFF2563EB) else Color.Gray)
                            }
                            
                            if (index < steps.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp).offset(y = (-8).dp),
                                    thickness = 1.dp,
                                    color = if (currentStep > stepNum) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFE2E8F0))
                }

                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    when (currentStep) {
                        1 -> {
                            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                Text("Select Student", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                OutlinedTextField(
                                    value = studentQuery,
                                    onValueChange = { studentQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Search by Student ID, Reg No, Name, Mobile", fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                val filtered = students.filter {
                                    it.Name.contains(studentQuery, ignoreCase = true) || it.StudentID.contains(studentQuery, ignoreCase = true)
                                }
                                
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(filtered) { student ->
                                        val isSelected = student.StudentID == studentId
                                        Card(
                                            modifier = Modifier.fillMaxWidth().clickable { studentId = student.StudentID; courseCode = student.Course },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White),
                                            border = if (isSelected) BorderStroke(1.dp, Color(0xFF2563EB)) else BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(50.dp).background(Color(0xFFE2E8F0), CircleShape), contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                                                }
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(student.StudentID, fontSize = 12.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold)
                                                    Text(student.Name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                                    Text("${student.Course} - ${student.Semester}", fontSize = 12.sp, color = Color.Gray)
                                                }
                                                if (isSelected) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2563EB))
                                                }
                                            }
                                        }
                                    }
                                    item {
                                        TextButton(onClick = { }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                            Icon(Icons.Default.Add, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Add New Student")
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                if (selectedStudent != null) {
                                    Text("Student Information", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(50.dp).background(Color(0xFFE2E8F0), CircleShape), contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Text(selectedStudent.Name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                                    Text("Active", fontSize = 10.sp, color = Color(0xFF16A34A), modifier = Modifier.background(Color(0xFFDCFCE7), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 2.dp))
                                                }
                                                Text("${selectedStudent.StudentID} | ${selectedStudent.Course}", fontSize = 12.sp, color = Color.Gray)
                                                Text("${selectedStudent.Semester} | 2024-2027", fontSize = 12.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))
                                }

                                Text("Select Semester", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                val studentCourseObj = courses.find { it.CourseCode == selectedStudent?.Course || it.CourseName == selectedStudent?.Course }
                                val maxSemesters = studentCourseObj?.TotalSemesters?.toIntOrNull() ?: 4
                                val studentAssignments = feeAssignments.filter { it.StudentID == selectedStudent?.StudentID }
                                
                                val semesters = (1..maxSemesters).map { semNo ->
                                    val semName = getSemesterName(semNo)
                                    val assignment = studentAssignments.find { it.Semester == semName }
                                    if (assignment != null) {
                                        Triple(
                                            semName,
                                            "Completed",
                                            "Assigned On: ${assignment.DueDate.ifEmpty { "10-Feb-2025" }}\nTotal Fees: ₹ ${assignment.TotalAmount}"
                                        )
                                    } else {
                                        val defaultFee = studentCourseObj?.CourseFees?.toDoubleOrNull()?.let { (it / maxSemesters).toInt().toString() } ?: "45000"
                                        Triple(
                                            semName,
                                            "Pending",
                                            "Not Assigned\nTotal Fees: ₹ $defaultFee"
                                        )
                                    }
                                }
                                
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(semesters) { sem ->
                                        val isSelected = semesterNo == sem.first
                                        Card(
                                            modifier = Modifier.fillMaxWidth().clickable { semesterNo = sem.first },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White),
                                            border = if (isSelected) BorderStroke(1.dp, Color(0xFF2563EB)) else BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                                RadioButton(selected = isSelected, onClick = { semesterNo = sem.first }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB)))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                        Text(sem.first, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                                        val tagColor = if (sem.second == "Completed") Color(0xFF16A34A) else Color(0xFFF59E0B)
                                                        val tagBgColor = if (sem.second == "Completed") Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                                                        Text(sem.second, fontSize = 10.sp, color = tagColor, modifier = Modifier.background(tagBgColor, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 2.dp))
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    val parts = sem.third.split("\n")
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text(parts.getOrNull(1)?.split(":")?.first() ?: "", fontSize = 12.sp, color = Color.Gray)
                                                        Text(parts.getOrNull(0)?.split(":")?.first() ?: "", fontSize = 12.sp, color = Color.Gray)
                                                    }
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text(parts.getOrNull(1)?.split(":")?.getOrNull(1)?.trim() ?: "", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                                        Text(parts.getOrNull(0)?.split(":")?.getOrNull(1)?.trim() ?: "", fontSize = 12.sp, color = Color.Gray)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        3 -> {
                            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                                Text("Fee Components", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        FeeCompInput("Admission Fee", admissionFee, Icons.Default.Person, Color(0xFF3B82F6)) { admissionFee = it }
                                        FeeCompInput("Tuition Fee", tuitionFee, Icons.Default.MenuBook, Color(0xFF3B82F6)) { tuitionFee = it }
                                        FeeCompInput("Exam Fee", examFee, Icons.Default.Assignment, Color(0xFF3B82F6)) { examFee = it }
                                        FeeCompInput("Library Fee", libraryFee, Icons.Default.LibraryBooks, Color(0xFF3B82F6)) { libraryFee = it }
                                        FeeCompInput("Lab Fee", labFee, Icons.Default.Build, Color(0xFF3B82F6)) { labFee = it }
                                        FeeCompInput("Transport Fee", transportFee, Icons.Default.DirectionsBus, Color(0xFF3B82F6)) { transportFee = it }
                                        FeeCompInput("Hostel Fee", hostelFee, Icons.Default.Domain, Color(0xFF3B82F6)) { hostelFee = it }
                                        FeeCompInput("Other Fee", otherFee, Icons.Default.MoreHoriz, Color(0xFF3B82F6)) { otherFee = it }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Adjustments", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                if (isCompact) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(value = discount, onValueChange = { discount = it }, label = { Text("Discount (₹)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White))
                                        OutlinedTextField(value = scholarship, onValueChange = { scholarship = it }, label = { Text("Scholarship (₹)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White))
                                        OutlinedTextField(value = fine, onValueChange = { fine = it }, label = { Text("Fine (₹)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White))
                                    }
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(value = discount, onValueChange = { discount = it }, label = { Text("Discount (₹)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White))
                                        OutlinedTextField(value = scholarship, onValueChange = { scholarship = it }, label = { Text("Scholarship (₹)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White))
                                        OutlinedTextField(value = fine, onValueChange = { fine = it }, label = { Text("Fine (₹)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = dueDate,
                                    onValueChange = { dueDate = it },
                                    label = { Text("Due Date *") },
                                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Remarks", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = remarks,
                                    onValueChange = { remarks = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                                )
                            }
                        }
                        4 -> {
                            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                                Text("Review & Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Student Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(50.dp).background(Color(0xFFE2E8F0), CircleShape), contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Text(selectedStudent?.Name ?: "", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                                    Text("Active", fontSize = 10.sp, color = Color(0xFF16A34A), modifier = Modifier.background(Color(0xFFDCFCE7), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 2.dp))
                                                }
                                                Text(selectedStudent?.StudentID ?: "", fontSize = 12.sp, color = Color.Gray)
                                                Text("${selectedStudent?.Course ?: ""} - $semesterNo", fontSize = 12.sp, color = Color.Gray)
                                                Text("Batch : 2024-2027", fontSize = 12.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Fee Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Total Fees", fontSize = 14.sp, color = Color.Gray)
                                            Text("₹ ${totalFeesSum.toInt()}", fontSize = 14.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Medium)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Discount", fontSize = 14.sp, color = Color.Gray)
                                            Text("- ₹ $discount", fontSize = 14.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Medium)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Scholarship", fontSize = 14.sp, color = Color.Gray)
                                            Text("- ₹ $scholarship", fontSize = 14.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Medium)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Fine", fontSize = 14.sp, color = Color.Gray)
                                            Text("+ ₹ $fine", fontSize = 14.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Medium)
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        HorizontalDivider(thickness = 1.dp, color = Color(0xFFE2E8F0))
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Total Amount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                            Text("₹ $totalAmountComputed", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF2563EB))
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Assignment Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Text("Academic Year", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                                            Text("2024-2025", fontSize = 13.sp, color = Color(0xFF1E293B), modifier = Modifier.weight(2f))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Text("Semester", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                                            Text(semesterNo, fontSize = 13.sp, color = Color(0xFF1E293B), modifier = Modifier.weight(2f))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Text("Due Date", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                                            Text(dueDate, fontSize = 13.sp, color = Color(0xFF1E293B), modifier = Modifier.weight(2f))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Text("Remarks", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                                            Text(remarks, fontSize = 13.sp, color = Color(0xFF1E293B), modifier = Modifier.weight(2f))
                                        }
                                    }
                                }
                            }
                        }
                        5 -> {
                            Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Spacer(modifier = Modifier.height(32.dp))
                                
                                Box(modifier = Modifier.size(80.dp).background(Color(0xFF16A34A), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Fees Assigned Successfully!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text("Fees has been assigned for", fontSize = 14.sp, color = Color.Gray)
                                Text("${selectedStudent?.Name} (${selectedStudent?.StudentID})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Text(semesterNo, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Total Amount", fontSize = 14.sp, color = Color.Gray)
                                            Text("₹ $totalAmountComputed", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        HorizontalDivider(thickness = 1.dp, color = Color(0xFFE2E8F0))
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Assigned On", fontSize = 13.sp, color = Color.Gray)
                                            Text(SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(Date()), fontSize = 13.sp, color = Color(0xFF1E293B))
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Due Date", fontSize = 13.sp, color = Color.Gray)
                                            Text(dueDate, fontSize = 13.sp, color = Color(0xFF1E293B))
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Assigned By", fontSize = 13.sp, color = Color.Gray)
                                            Text("Admin", fontSize = 13.sp, color = Color(0xFF1E293B))
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(modifier = Modifier.size(48.dp).background(Color.White, RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF2563EB))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("View Details", fontSize = 12.sp, color = Color(0xFF1E293B))
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { studentId = ""; currentStep = 1 }) {
                                        Box(modifier = Modifier.size(48.dp).background(Color.White, RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF2563EB))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Assign Another", fontSize = 12.sp, color = Color(0xFF1E293B))
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(modifier = Modifier.size(48.dp).background(Color.White, RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Print, contentDescription = null, tint = Color(0xFF2563EB))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Print", fontSize = 12.sp, color = Color(0xFF1E293B))
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(modifier = Modifier.size(48.dp).background(Color.White, RoundedCornerShape(8.dp)).border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF2563EB))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Share", fontSize = 12.sp, color = Color(0xFF1E293B))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                Button(
                                    onClick = onDismiss,
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Go to Dashboard", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (currentStep < 5) {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (currentStep > 1) {
                            Button(
                                onClick = { currentStep-- },
                                modifier = Modifier.weight(1f).height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF2563EB)),
                                border = BorderStroke(1.dp, Color(0xFF2563EB)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Back", fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Button(
                            onClick = { 
                                if (currentStep < 4) {
                                    if (currentStep == 1 && studentId.isEmpty()) {
                                        Toast.makeText(context, "Please select a student", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    currentStep++
                                } else {
                                    onSave(
                                        FeeAssignment(
                                            StudentID = studentId,
                                            Course = courseCode,
                                            Semester = semesterNo,
                                            AdmissionFee = admissionFee,
                                            TuitionFee = tuitionFee,
                                            ExamFee = examFee,
                                            LibraryFee = libraryFee,
                                            HostelFee = hostelFee,
                                            TransportFee = transportFee,
                                            Scholarship = scholarship,
                                            Discount = discount,
                                            TotalAmount = totalAmountComputed,
                                            DueDate = dueDate,
                                            Remarks = remarks
                                        ),
                                        false,
                                        ""
                                    )
                                    currentStep = 5
                                }
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (currentStep < 4) {
                                Text("Next", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                            } else {
                                Text("Confirm & Assign", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun FeeCompInput(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, onValueChange: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 14.sp, color = Color(0xFF1E293B), modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            prefix = { Text("₹ ") },
            modifier = Modifier.width(130.dp).height(50.dp),
            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.End),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF8FAFC),
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color(0xFFE2E8F0)
            )
        )
    }
}

// --- PAYMENTS LIST & TRANSACTION SCREEN ---
@Composable
fun PaymentsView(
    payments: List<Payment>,
    students: List<Student>,
    courses: List<Course>,
    semesters: List<Semester>,
    onReceive: () -> Unit,
    onEditPayment: (Payment) -> Unit = {},
    onDeletePayment: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Paid", "Pending", "Failed")

    val totalAmt = remember(payments) { payments.sumOf { it.Amount.toDoubleOrNull() ?: 0.0 }.toInt() }
    val cashAmt = remember(payments) { payments.filter { it.PaymentMode.equals("Cash", ignoreCase = true) }.sumOf { it.Amount.toDoubleOrNull() ?: 0.0 }.toInt() }
    val onlineAmt = remember(payments) { payments.filter { !it.PaymentMode.equals("Cash", ignoreCase = true) }.sumOf { it.Amount.toDoubleOrNull() ?: 0.0 }.toInt() }
    val totalTransactions = payments.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Payment History", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1E293B))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onReceive, modifier = Modifier.background(Color.White, CircleShape).size(40.dp)) {
                    Icon(Icons.Default.Add, "Process Payment", tint = Color(0xFF2563EB))
                }
                IconButton(onClick = {}, modifier = Modifier.background(Color.White, CircleShape).size(40.dp)) {
                    Icon(Icons.Default.FilterList, "Filter", tint = Color(0xFF64748B))
                }
            }
        }

        // Color-rich Stats Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "Total Paid",
                value = "₹$totalAmt",
                icon = Icons.Default.CheckCircle,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Cash Paid",
                value = "₹$cashAmt",
                icon = Icons.Default.Payments,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "Online/UPI",
                value = "₹$onlineAmt",
                icon = Icons.Default.QrCodeScanner,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFF10B981), Color(0xFF059669))),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Transactions",
                value = "$totalTransactions",
                icon = Icons.Default.ReceiptLong,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED))),
                modifier = Modifier.weight(1f)
            )
        }

        ScrollableTabRow(
            selectedTabIndex = filters.indexOf(selectedFilter),
            containerColor = Color.Transparent,
            contentColor = Color(0xFF2563EB),
            edgePadding = 0.dp,
            indicator = {},
            divider = {}
        ) {
            filters.forEach { filter ->
                val isSelected = selectedFilter == filter
                Tab(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        filter,
                        modifier = Modifier
                            .background(if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(20.dp))
                            .border(1.dp, if (isSelected) Color(0xFFE2E8F0) else Color.Transparent, RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        color = if (isSelected) Color(0xFF1E293B) else Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        val filteredPayments = remember(payments, selectedFilter) {
            when (selectedFilter) {
                "Paid" -> payments
                "Pending" -> emptyList()
                "Failed" -> emptyList()
                else -> payments
            }
        }

        if (filteredPayments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFFEFF6FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Empty",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Transactions Found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "There are no records matching the '$selectedFilter' status in the active Google Sheets ledger.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPayments) { pay ->
                    val (modeColor, modeBg, modeIcon) = when {
                        pay.PaymentMode.equals("Cash", ignoreCase = true) -> 
                            Triple(Color(0xFFD97706), Color(0xFFFEF3C7), Icons.Default.Payments)
                        pay.PaymentMode.contains("UPI", ignoreCase = true) || pay.PaymentMode.contains("Online", ignoreCase = true) -> 
                            Triple(Color(0xFF0D9488), Color(0xFFCCFBF1), Icons.Default.QrCodeScanner)
                        else -> 
                            Triple(Color(0xFF7C3AED), Color(0xFFF3E8FF), Icons.Default.CreditCard)
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Colored status stripe based on payment mode
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .fillMaxHeight()
                                    .align(Alignment.CenterVertically)
                                    .background(modeColor)
                            )
                            
                            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(pay.ReceiptNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Student ID: ${pay.StudentID}", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                                        Text("${pay.Semester} - ${pay.FeeType}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(pay.Date, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "₹ ${pay.Amount.toDoubleOrNull()?.toInt() ?: 0}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(modeBg, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            modeIcon,
                                            contentDescription = null,
                                            tint = modeColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            pay.PaymentMode,
                                            fontSize = 11.sp,
                                            color = modeColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = {
                                            try {
                                                val file = PdfGenerator.generatePaymentReceipt(context, pay)
                                                if (file != null) {
                                                    PdfGenerator.sharePdf(context, file)
                                                } else {
                                                    Toast.makeText(context, "Failed to generate receipt PDF", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }, modifier = Modifier.size(32.dp).background(Color(0xFFEFF6FF), CircleShape)) {
                                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(onClick = { onEditPayment(pay) }, modifier = Modifier.size(32.dp).background(Color(0xFFFFFBEB), CircleShape)) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(onClick = { onDeletePayment(pay.PaymentID) }, modifier = Modifier.size(32.dp).background(Color(0xFFFEF2F2), CircleShape)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
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

// --- RECEIVE PAYMENT FORM DIALOG ---
// --- RECEIVE PAYMENT FORM DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivePaymentFormDialog(
    students: List<Student>,
    feeAssignments: List<FeeAssignment>,
    payments: List<Payment>,
    courses: List<Course>,
    paymentToEdit: Payment? = null,
    onDismiss: () -> Unit,
    onSave: (Payment, (Payment) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Helper to format semester names
    val getSemesterName = { no: Int ->
        when (no) {
            1 -> "I Semester"
            2 -> "II Semester"
            3 -> "III Semester"
            4 -> "IV Semester"
            5 -> "V Semester"
            6 -> "VI Semester"
            7 -> "VII Semester"
            8 -> "VIII Semester"
            else -> "$no Semester"
        }
    }

    // 10-Step Wizard State
    var currentStep by remember { mutableStateOf(if (paymentToEdit != null) 5 else 1) }
    
    // Core selection states
    var studentId by remember { mutableStateOf(paymentToEdit?.StudentID ?: "") }
    var studentQuery by remember { mutableStateOf("") }
    
    // Active student object helper
    val selectedStudent = remember(studentId, students) { students.find { it.StudentID == studentId } }
    
    // Step 2 Student Details Active Tab
    var activeDetailsTab by remember { mutableStateOf(0) } // 0 = Profile, 1 = Fees, 2 = Payments, 3 = Documents
    
    // Dynamic Course & Semester calculations based on live Google Sheets values
    val coursesList = remember { students.map { it.Course }.distinct() }
    val studentCourse = selectedStudent?.Course ?: ""
    val studentSemester = selectedStudent?.Semester ?: ""
    val studentBatch = selectedStudent?.Batch ?: ""
    
    // Step 3 dynamic semester breakdown calculations
    val studentAssignments = remember(studentId, feeAssignments) {
        feeAssignments.filter { it.StudentID == studentId }
    }
    val studentCourseObj = remember(selectedStudent, courses) {
        courses.find { it.CourseCode == (selectedStudent?.Course ?: "") || it.CourseName == (selectedStudent?.Course ?: "") }
    }
    val totalCourseFee = remember(studentAssignments, studentCourseObj) {
        val sumAssigned = studentAssignments.sumOf { it.TotalAmount.toDoubleOrNull() ?: 0.0 }
        if (sumAssigned > 0) sumAssigned else (studentCourseObj?.CourseFees?.toDoubleOrNull() ?: 155000.0)
    }
    val studentPayments = remember(studentId, payments) {
        payments.filter { it.StudentID == studentId }
    }
    val totalPaidSoFar = remember(studentPayments) {
        studentPayments.sumOf { it.Amount.toDoubleOrNull() ?: 0.0 }
    }
    val nextDueDate = "30-Sep-2025"
    
    // Semester details selection for Step 4
    var selectedSemesterCode by remember { mutableStateOf("") }
    var selectedSemesterFee by remember { mutableDoubleStateOf(0.0) }
    var selectedSemesterPaid by remember { mutableDoubleStateOf(0.0) }
    var selectedSemesterBalance by remember { mutableDoubleStateOf(0.0) }
    
    // Form Input States (Step 4)
    var receiptNo by remember { mutableStateOf(paymentToEdit?.ReceiptNumber ?: "RC25080023") }
    var paymentDate by remember { mutableStateOf(paymentToEdit?.Date ?: SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date())) }
    var feeType by remember { mutableStateOf(paymentToEdit?.FeeType ?: "Tuition Fee") }
    var semesterNo by remember { mutableStateOf(paymentToEdit?.Semester ?: "II Semester") }
    var amountCollected by remember { mutableStateOf(paymentToEdit?.Amount ?: "") }
    var discountCollected by remember { mutableStateOf(paymentToEdit?.Discount ?: "0") }
    var fineCollected by remember { mutableStateOf(paymentToEdit?.Fine ?: "0") }
    var scholarshipCollected by remember { mutableStateOf("0") }
    var paymentMode by remember { mutableStateOf(paymentToEdit?.PaymentMode ?: "UPI") }
    var txnNumber by remember { mutableStateOf(paymentToEdit?.TransactionNumber ?: "UPI123456789012") }
    var remarks by remember { mutableStateOf(paymentToEdit?.Remarks ?: "Tuition Fee Payment") }
    
    // Financial calculation for balance after payment
    val currentSemesterBalance = selectedSemesterBalance
    val balanceRemaining = remember(currentSemesterBalance, amountCollected, fineCollected, discountCollected, scholarshipCollected) {
        val collected = amountCollected.toDoubleOrNull() ?: 0.0
        val fine = fineCollected.toDoubleOrNull() ?: 0.0
        val disc = discountCollected.toDoubleOrNull() ?: 0.0
        val schol = scholarshipCollected.toDoubleOrNull() ?: 0.0
        val result = currentSemesterBalance - collected + fine - disc - schol
        maxOf(0.0, result)
    }

    // Save outputs
    var confirmedPayment by remember { mutableStateOf<Payment?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }

    // Dynamic fee split calculations helper
    val tuiFee = (selectedSemesterFee * 0.60).toInt()
    val devFee = (selectedSemesterFee * 0.12).toInt()
    val libFee = (selectedSemesterFee * 0.06).toInt()
    val exmFee = (selectedSemesterFee * 0.10).toInt()
    val othFee = (selectedSemesterFee * 0.12).toInt()
    
    val tuiPaid = minOf(tuiFee, (selectedSemesterPaid * 0.60).toInt())
    val devPaid = minOf(devFee, (selectedSemesterPaid * 0.12).toInt())
    val libPaid = minOf(libFee, (selectedSemesterPaid * 0.06).toInt())
    val exmPaid = minOf(exmFee, (selectedSemesterPaid * 0.10).toInt())
    val othPaid = minOf(othFee, (selectedSemesterPaid * 0.12).toInt())
    
    val tuiDue = maxOf(0, tuiFee - tuiPaid)
    val devDue = maxOf(0, devFee - devPaid)
    val libDue = maxOf(0, libFee - libPaid)
    val exmDue = maxOf(0, exmFee - exmPaid)
    val othDue = maxOf(0, othFee - othPaid)

    // Trigger processing and save flow on entering step 7
    LaunchedEffect(currentStep) {
        if (currentStep == 7) {
            kotlinx.coroutines.delay(1500)
            val p = Payment(
                PaymentID = paymentToEdit?.PaymentID ?: "",
                ReceiptNumber = receiptNo.ifEmpty { "RC25000023" },
                StudentID = studentId,
                Course = studentCourse,
                Semester = semesterNo,
                FeeType = feeType,
                Amount = amountCollected,
                Fine = fineCollected,
                Discount = discountCollected,
                Balance = balanceRemaining.toInt().toString(),
                PaymentMode = paymentMode,
                TransactionNumber = txnNumber,
                Date = paymentDate,
                Remarks = remarks.ifEmpty { "Semester II Fee Payment" }
            )
            onSave(p) { confirmed ->
                confirmedPayment = confirmed
                currentStep = 8 // Move to step 8 (Payment Success)
            }
        }
    }

    Dialog(
        onDismissRequest = { if (currentStep != 7) onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF8FAFC)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                
                // STEP-BY-STEP HEADER (Beautiful top bar with step circles)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                        .border(BorderStroke(1.dp, Color(0xFFE2E8F0)))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            when (currentStep) {
                                1 -> onDismiss()
                                2 -> currentStep = 1
                                3 -> currentStep = 2
                                4 -> currentStep = 3
                                5 -> {
                                    if (paymentToEdit != null) onDismiss()
                                    else currentStep = 4
                                }
                                6 -> currentStep = 5
                                7 -> {} // Processing
                                8 -> onDismiss()
                                9 -> currentStep = 8
                                10 -> currentStep = 9
                                11 -> currentStep = 10
                                else -> onDismiss()
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E3A8A))
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Text(
                            text = "Fee Payment – Step by Step",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E3A8A),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        IconButton(onClick = { onDismiss() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // HORIZONTAL STEPS SCROLLER
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val steps = listOf(
                            "Select Student",
                            "Student Details",
                            "Select Semester",
                            "Fee Details",
                            "Payment Entry",
                            "Review & Confirm",
                            "Processing",
                            "Success",
                            "Receipt",
                            "History",
                            "Quick Actions"
                        )
                        
                        steps.forEachIndexed { index, name ->
                            val stepNum = index + 1
                            val isCurrent = stepNum == currentStep
                            val isCompleted = stepNum < currentStep
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable(enabled = isCompleted) {
                                    currentStep = stepNum
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .background(
                                            color = if (isCurrent) Color(0xFF1E3A8A) else if (isCompleted) Color(0xFF16A34A) else Color(0xFFEFF6FF),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stepNum.toString(),
                                        color = if (isCurrent || isCompleted) Color.White else Color(0xFF1E3A8A),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(6.dp))
                                
                                Text(
                                    text = name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCurrent) Color(0xFF1E3A8A) else if (isCompleted) Color(0xFF16A34A) else Color(0xFF94A3B8)
                                )
                                
                                if (index < steps.size - 1) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("→", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentStep) {
                        
                        // ================== STEP 1: STUDENT SEARCH ==================
                        1 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = studentQuery,
                                    onValueChange = { studentQuery = it },
                                    placeholder = { Text("Search by Student ID, Reg No, Name, Mobile...") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = Color(0xFF1E3A8A)
                                    )
                                )
                                
                                Text(
                                    text = "Recent Students",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF64748B)
                                )
                                
                                val filteredStudents = remember(studentQuery, students) {
                                    students.filter {
                                        it.Name.contains(studentQuery, ignoreCase = true) ||
                                        it.StudentID.contains(studentQuery, ignoreCase = true) ||
                                        it.RegNo.contains(studentQuery, ignoreCase = true) ||
                                        it.Mobile.contains(studentQuery, ignoreCase = true)
                                    }
                                }
                                
                                if (filteredStudents.isEmpty()) {
                                    Box(
                                        modifier = Modifier.weight(1f).fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No students found matching query.", color = Color.Gray)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(filteredStudents) { s ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        studentId = s.StudentID
                                                        currentStep = 2
                                                    },
                                                shape = RoundedCornerShape(16.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Styled Avatar with dynamic color
                                                    Box(
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .background(
                                                                Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFF1D4ED8))),
                                                                CircleShape
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = s.Name.take(1).uppercase(),
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 18.sp
                                                        )
                                                    }
                                                    
                                                    Spacer(modifier = Modifier.width(16.dp))
                                                    
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(s.Name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                                                        Text(s.StudentID, fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB), fontSize = 13.sp)
                                                        Text("${s.Course} - Semester ${s.Semester}", fontSize = 12.sp, color = Color.Gray)
                                                    }
                                                    
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowForward,
                                                        contentDescription = "Select Student",
                                                        tint = Color(0xFF94A3B8)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ================== STEP 2: STUDENT DETAILS ==================
                        2 -> {
                            selectedStudent?.let { s ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Avatar & Name Card
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .background(
                                                        Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))),
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(s.Name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 32.sp)
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .background(Color(0xFF10B981), CircleShape)
                                                        .border(2.dp, Color.White, CircleShape)
                                                        .align(Alignment.BottomEnd)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(s.Name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1E293B))
                                            Text(s.StudentID, color = Color(0xFF2563EB), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                            Text("${s.Course} - ${s.Semester} Semester", fontSize = 13.sp, color = Color.Gray)
                                            Text("Reg No: ${s.RegNo.ifEmpty { "REG2025001" }}", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }

                                    // Custom Tabs
                                    ScrollableTabRow(
                                        selectedTabIndex = activeDetailsTab,
                                        containerColor = Color.Transparent,
                                        contentColor = Color(0xFF2563EB),
                                        edgePadding = 0.dp,
                                        indicator = {},
                                        divider = {}
                                    ) {
                                        listOf("Profile", "Fees", "Payments", "Documents").forEachIndexed { idx, tab ->
                                            val isSelected = activeDetailsTab == idx
                                            Tab(
                                                selected = isSelected,
                                                onClick = { activeDetailsTab = idx }
                                            ) {
                                                Text(
                                                    text = tab,
                                                    modifier = Modifier
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                        .background(if (isSelected) Color(0xFFEFF6FF) else Color.Transparent, RoundedCornerShape(12.dp))
                                                        .border(1.dp, if (isSelected) Color(0xFFBFDBFE) else Color.Transparent, RoundedCornerShape(12.dp))
                                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                                    color = if (isSelected) Color(0xFF2563EB) else Color.Gray,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }
                                    }

                                    // Tab Content
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                            when (activeDetailsTab) {
                                                0 -> { // Profile Info
                                                    Text("Personal Information", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                                    DetailRowReceipt("Mobile", s.Mobile.ifEmpty { "9876543210" })
                                                    DetailRowReceipt("Email", s.Email.ifEmpty { "student@college.edu" })
                                                    DetailRowReceipt("DOB", s.DOB.ifEmpty { "15-Aug-2004" })
                                                    DetailRowReceipt("Gender", s.Gender.ifEmpty { "Male" })
                                                    DetailRowReceipt("Blood Group", "O+")
                                                    DetailRowReceipt("Address", s.Address.ifEmpty { "12, Anna Nagar, Coimbatore" })
                                                    
                                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                                    Text("Academic Information", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                                    DetailRowReceipt("Course", s.Course)
                                                    DetailRowReceipt("Department", s.Department.ifEmpty { "Computer Applications" })
                                                    DetailRowReceipt("Semester", s.Semester)
                                                    DetailRowReceipt("Batch", s.Batch)
                                                }
                                                1 -> { // Fees
                                                    Text("Fees Overview", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                                    Text("Total Course Assigned Fees: ₹$totalCourseFee", fontSize = 13.sp)
                                                    Text("Total Paid: ₹$totalPaidSoFar", color = Color(0xFF16A34A), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                    Text("Balance Remaining: ₹${totalCourseFee - totalPaidSoFar}", color = Color(0xFFDC2626), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                }
                                                2 -> { // Payments
                                                    Text("Payment History", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                                    if (studentPayments.isEmpty()) {
                                                        Text("No past transactions found.", color = Color.Gray, fontSize = 12.sp)
                                                    } else {
                                                        studentPayments.forEach { pay ->
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Column {
                                                                    Text(pay.ReceiptNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                                    Text("${pay.Semester} - ${pay.FeeType}", fontSize = 11.sp, color = Color.Gray)
                                                                }
                                                                Text("₹${pay.Amount}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                                            }
                                                        }
                                                    }
                                                }
                                                3 -> { // Documents
                                                    Text("Assigned Documents", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                                    listOf("Aadhaar Card", "HSC Marksheet", "Transfer Certificate", "Allotment Letter").forEach { doc ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(doc, fontSize = 13.sp, color = Color.DarkGray)
                                                            Icon(Icons.Default.Download, contentDescription = "Download", tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Bottom Navigation & Calls Quick Actions
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { Toast.makeText(context, "Calling ${s.Name}...", Toast.LENGTH_SHORT).show() },
                                            modifier = Modifier.size(48.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = "Call")
                                        }
                                        
                                        OutlinedButton(
                                            onClick = { Toast.makeText(context, "Opening WhatsApp...", Toast.LENGTH_SHORT).show() },
                                            modifier = Modifier.height(48.dp).weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("WhatsApp", color = Color(0xFF25D366))
                                        }
                                        
                                        Button(
                                            onClick = { currentStep = 3 },
                                            modifier = Modifier.height(48.dp).weight(1.5f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Pay Fees", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // ================== STEP 3: SEMESTER FEE BALANCE ==================
                        3 -> {
                            selectedStudent?.let { s ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Balance Header Card
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Column {
                                                    Text("Total Course Fee", fontSize = 12.sp, color = Color.Gray)
                                                    Text("₹ ${String.format("%,.0f", totalCourseFee)}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF1E3A8A))
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text("Total Paid", fontSize = 12.sp, color = Color.Gray)
                                                    Text("₹ ${String.format("%,.0f", totalPaidSoFar)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF16A34A))
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(12.dp))
                                            HorizontalDivider(color = Color(0xFFDBEAFE))
                                            Spacer(modifier = Modifier.height(12.dp))
                                            
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Column {
                                                    Text("Total Balance", fontSize = 12.sp, color = Color.Gray)
                                                    Text("₹ ${String.format("%,.0f", totalCourseFee - totalPaidSoFar)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFDC2626))
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text("Next Due Date", fontSize = 12.sp, color = Color.Gray)
                                                    Text(nextDueDate, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text("Payment Completion", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                                                val pct = (totalPaidSoFar / totalCourseFee) * 100
                                                Text(String.format("%.2f%%", pct), fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E3A8A))
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            LinearProgressIndicator(
                                                progress = { ((totalPaidSoFar / totalCourseFee).toFloat()).coerceIn(0f, 1f) },
                                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                                color = Color(0xFF2563EB),
                                                trackColor = Color(0xFFDBEAFE)
                                            )
                                        }
                                    }

                                    // Semester List Title
                                    Text("Select Semester to Pay", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))

                                    // Dynamic Semesters Cards
                                    val maxSems = studentCourseObj?.TotalSemesters?.toIntOrNull() ?: 4
                                    val semestersDef = (1..maxSems).map { semNo ->
                                        val semName = getSemesterName(semNo)
                                        val assignedFee = studentAssignments.find { it.Semester == semName }?.TotalAmount?.toDoubleOrNull()
                                            ?: (totalCourseFee / maxSems)
                                        val paidForSem = studentPayments.filter { it.Semester == semName }.sumOf { it.Amount.toDoubleOrNull() ?: 0.0 }
                                        Triple(semName, assignedFee, paidForSem)
                                    }
                                    
                                    semestersDef.forEach { (semName, fee, paid) ->
                                        val balance = maxOf(0.0, fee - paid)
                                        val status = if (balance == 0.0) "Paid" else if (paid > 0.0) "Partial" else "Pending"
                                        val badgeColor = if (status == "Paid") Color(0xFF16A34A) else if (status == "Partial") Color(0xFFD97706) else Color(0xFFDC2626)
                                        val badgeBg = if (status == "Paid") Color(0xFFDCFCE7) else if (status == "Partial") Color(0xFFFEF3C7) else Color(0xFFFEE2E2)
                                        
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (balance == 0.0) {
                                                        Toast.makeText(context, "This semester is already fully Paid!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        selectedSemesterCode = semName
                                                        selectedSemesterFee = fee
                                                        selectedSemesterPaid = paid
                                                        selectedSemesterBalance = balance
                                                        semesterNo = semName
                                                        amountCollected = balance.toInt().toString()
                                                        currentStep = 4
                                                    }
                                                },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Text(semName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                                                    Text(
                                                        text = status,
                                                        color = badgeColor,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier
                                                            .background(badgeBg, RoundedCornerShape(12.dp))
                                                            .padding(horizontal = 10.dp, vertical = 2.dp)
                                                    )
                                                }
                                                
                                                Spacer(modifier = Modifier.height(10.dp))
                                                
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Column {
                                                        Text("Total Fee", fontSize = 11.sp, color = Color.Gray)
                                                        Text("₹ ${fee.toInt()}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                    }
                                                    Column {
                                                        Text("Paid", fontSize = 11.sp, color = Color.Gray)
                                                        Text("₹ ${paid.toInt()}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF16A34A))
                                                    }
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text("Balance", fontSize = 11.sp, color = Color.Gray)
                                                        Text("₹ ${balance.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFDC2626))
                                                    }
                                                }
                                                
                                                val progress = if (fee > 0) (paid / fee).toFloat() else 0f
                                                if (progress > 0f) {
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    LinearProgressIndicator(
                                                        progress = { progress },
                                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                                        color = badgeColor,
                                                        trackColor = Color(0xFFF1F5F9)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ================== STEP 4: FEE DETAILS (SEMESTER WISE) ==================
                        4 -> {
                            selectedStudent?.let { s ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Fee Breakup Details",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )

                                    // Student Brief Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Student", fontSize = 11.sp, color = Color.Gray)
                                            Text("${s.Name} (${s.StudentID})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Semester", fontSize = 11.sp, color = Color.Gray)
                                            Text(selectedSemesterCode, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                                        }
                                    }

                                    // Summary Boxes
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FinancialSummaryCard("Total Fee", "₹ ${selectedSemesterFee.toInt()}", Color(0xFF2563EB), Color(0xFFEFF6FF), Modifier.weight(1f))
                                        FinancialSummaryCard("Paid", "₹ ${selectedSemesterPaid.toInt()}", Color(0xFF16A34A), Color(0xFFDCFCE7), Modifier.weight(1f))
                                        FinancialSummaryCard("Due", "₹ ${selectedSemesterBalance.toInt()}", Color(0xFFDC2626), Color(0xFFFEE2E2), Modifier.weight(1f))
                                    }

                                    // Fee Breakup Card Table
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Column {
                                            // Table Header
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFEFF6FF))
                                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Fee Type", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1D4ED8), modifier = Modifier.weight(2f))
                                                Text("Amount", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1D4ED8), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                                Text("Paid", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1D4ED8), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                                Text("Due", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1D4ED8), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                                            }
                                            
                                            // Table Body rows
                                            FeeBreakupRow("Tuition Fee", tuiFee, tuiPaid, tuiDue)
                                            HorizontalDivider(color = Color(0xFFF1F5F9))
                                            FeeBreakupRow("Development Fee", devFee, devPaid, devDue)
                                            HorizontalDivider(color = Color(0xFFF1F5F9))
                                            FeeBreakupRow("Library Fee", libFee, libPaid, libDue)
                                            HorizontalDivider(color = Color(0xFFF1F5F9))
                                            FeeBreakupRow("Exam Fee", exmFee, exmPaid, exmDue)
                                            HorizontalDivider(color = Color(0xFFF1F5F9))
                                            FeeBreakupRow("Other Fee", othFee, othPaid, othDue)
                                            
                                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 2.dp)
                                            
                                            // Total row
                                            FeeBreakupRow("Total", selectedSemesterFee.toInt(), selectedSemesterPaid.toInt(), selectedSemesterBalance.toInt(), isTotal = true)
                                        }
                                    }

                                    Button(
                                        onClick = { currentStep = 5 },
                                        modifier = Modifier.fillMaxWidth().height(48.dp).padding(top = 8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                            Text("Continue", fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("→", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // ================== STEP 5: PAYMENT ENTRY ==================
                        5 -> {
                            selectedStudent?.let { s ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Student Profile Banner
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(14.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier.size(44.dp).background(Color(0xFFEFF6FF), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1E3A8A))
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(s.Name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                                                Text("${s.StudentID} • ${selectedSemesterCode.ifEmpty { s.Semester + " Semester" }}", fontSize = 12.sp, color = Color.Gray)
                                            }
                                        }
                                    }

                                    // Selected Semester Totals Counters
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FinancialSummaryCard("Total Fee", "₹ ${selectedSemesterFee.toInt()}", Color(0xFF2563EB), Color(0xFFEFF6FF), Modifier.weight(1f))
                                        FinancialSummaryCard("Paid", "₹ ${selectedSemesterPaid.toInt()}", Color(0xFF16A34A), Color(0xFFDCFCE7), Modifier.weight(1f))
                                        FinancialSummaryCard("Balance", "₹ ${selectedSemesterBalance.toInt()}", Color(0xFFD97706), Color(0xFFFEF3C7), Modifier.weight(1f))
                                    }

                                    // Information Input Form
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text("Payment Information", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                            
                                            OutlinedTextField(
                                                value = receiptNo,
                                                onValueChange = { receiptNo = it },
                                                label = { Text("Receipt No") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White)
                                            )
                                            
                                            // Date Picker Input
                                            OutlinedTextField(
                                                value = paymentDate,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Payment Date") },
                                                trailingIcon = {
                                                    IconButton(onClick = {
                                                        val cal = Calendar.getInstance()
                                                        DatePickerDialog(
                                                            context,
                                                            { _, y, m, d ->
                                                                val out = Calendar.getInstance()
                                                                out.set(y, m, d)
                                                                paymentDate = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(out.time)
                                                            },
                                                            cal.get(Calendar.YEAR),
                                                            cal.get(Calendar.MONTH),
                                                            cal.get(Calendar.DAY_OF_MONTH)
                                                        ).show()
                                                    }) {
                                                        Icon(Icons.Default.DateRange, contentDescription = "Calendar")
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White)
                                            )

                                            // Fee Type Select
                                            var feeTypeExpanded by remember { mutableStateOf(false) }
                                            ExposedDropdownMenuBox(
                                                expanded = feeTypeExpanded,
                                                onExpandedChange = { feeTypeExpanded = !feeTypeExpanded },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                OutlinedTextField(
                                                    value = feeType,
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("Fee Type") },
                                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = feeTypeExpanded) },
                                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                ExposedDropdownMenu(
                                                    expanded = feeTypeExpanded,
                                                    onDismissRequest = { feeTypeExpanded = false }
                                                ) {
                                                    listOf("Tuition Fee", "Hostel Fee", "Exam Fee", "Library Fee", "Other Fees").forEach { t ->
                                                        DropdownMenuItem(text = { Text(t) }, onClick = { feeType = t; feeTypeExpanded = false })
                                                    }
                                                }
                                            }

                                            // Semester Select
                                            var semesterExpanded by remember { mutableStateOf(false) }
                                            ExposedDropdownMenuBox(
                                                expanded = semesterExpanded,
                                                onExpandedChange = { semesterExpanded = !semesterExpanded },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                OutlinedTextField(
                                                    value = semesterNo,
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("Semester") },
                                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = semesterExpanded) },
                                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                ExposedDropdownMenu(
                                                    expanded = semesterExpanded,
                                                    onDismissRequest = { semesterExpanded = false }
                                                ) {
                                                    listOf("I Semester", "II Semester", "III Semester", "IV Semester").forEach { s ->
                                                        DropdownMenuItem(text = { Text(s) }, onClick = { semesterNo = s; semesterExpanded = false })
                                                    }
                                                }
                                            }

                                            // Amount
                                            OutlinedTextField(
                                                value = amountCollected,
                                                onValueChange = { amountCollected = it },
                                                label = { Text("Amount") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp)
                                            )

                                            // Discount, Fine, Scholarship
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedTextField(value = discountCollected, onValueChange = { discountCollected = it }, label = { Text("Discount") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
                                                OutlinedTextField(value = fineCollected, onValueChange = { fineCollected = it }, label = { Text("Fine") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
                                                OutlinedTextField(value = scholarshipCollected, onValueChange = { scholarshipCollected = it }, label = { Text("Scholarship") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp))
                                            }

                                            // Payment Mode Select
                                            var modeExpanded by remember { mutableStateOf(false) }
                                            ExposedDropdownMenuBox(
                                                expanded = modeExpanded,
                                                onExpandedChange = { modeExpanded = !modeExpanded },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                OutlinedTextField(
                                                    value = paymentMode,
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("Payment Mode") },
                                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                ExposedDropdownMenu(
                                                    expanded = modeExpanded,
                                                    onDismissRequest = { modeExpanded = false }
                                                ) {
                                                    listOf("UPI", "Cash", "Bank Transfer", "Card").forEach { m ->
                                                        DropdownMenuItem(text = { Text(m) }, onClick = { paymentMode = m; modeExpanded = false })
                                                    }
                                                }
                                            }

                                            // Txn Number & Remarks
                                            OutlinedTextField(value = txnNumber, onValueChange = { txnNumber = it }, label = { Text("Transaction ID") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                                            OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))

                                            Spacer(modifier = Modifier.height(8.dp))
                                            HorizontalDivider(color = Color(0xFFF1F5F9))
                                            
                                            // Balance after payment
                                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text("Balance After Payment", color = Color.Gray, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                Text("₹ ${balanceRemaining.toInt()}", color = Color(0xFF16A34A), fontWeight = FontWeight.Black, fontSize = 18.sp)
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            if (amountCollected.isBlank() || (amountCollected.toDoubleOrNull() ?: 0.0) <= 0.0) {
                                                Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                                            } else {
                                                currentStep = 6 // Go to Step 6: Review & Confirm
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Proceed to Review", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // ================== STEP 6: REVIEW & CONFIRM ==================
                        6 -> {
                            selectedStudent?.let { s ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                            Text("Review Payment Summary", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF1E3A8A))
                                            HorizontalDivider(color = Color(0xFFF1F5F9))
                                            
                                            DetailRowReceipt("Student Name", s.Name, isBold = true)
                                            DetailRowReceipt("Student ID", s.StudentID)
                                            DetailRowReceipt("Semester", semesterNo)
                                            DetailRowReceipt("Fee Type", feeType)
                                            
                                            HorizontalDivider(color = Color(0xFFF1F5F9))
                                            
                                            DetailRowReceipt("Amount Collected", "₹ $amountCollected")
                                            DetailRowReceipt("Discount Applied", "- ₹ $discountCollected")
                                            DetailRowReceipt("Fine Added", "+ ₹ $fineCollected")
                                            DetailRowReceipt("Scholarship Credit", "- ₹ $scholarshipCollected")
                                            
                                            val netPayable = (amountCollected.toDoubleOrNull() ?: 0.0) + (fineCollected.toDoubleOrNull() ?: 0.0) - (discountCollected.toDoubleOrNull() ?: 0.0) - (scholarshipCollected.toDoubleOrNull() ?: 0.0)
                                            DetailRowReceipt("Net Payable Amount", "₹ ${netPayable.toInt()}", isBold = true)
                                            
                                            HorizontalDivider(color = Color(0xFFF1F5F9))
                                            
                                            DetailRowReceipt("Payment Method", paymentMode)
                                            DetailRowReceipt("Transaction ID", txnNumber)
                                            DetailRowReceipt("Remarks", remarks)
                                        }
                                    }

                                    // Warning Box Alert (matches image)
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Info, contentDescription = "Info", tint = Color(0xFF2563EB))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("Please review the details before confirming payment.", fontSize = 13.sp, color = Color(0xFF1E3A8A), fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Button(
                                        onClick = {
                                            currentStep = 7 // Run processing screen
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Confirm & Pay", fontWeight = FontWeight.Bold)
                                    }
                                    
                                    OutlinedButton(
                                        onClick = { currentStep = 5 },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Edit Details")
                                    }
                                }
                            }
                        }

                        // ================== STEP 7: PROCESSING SCREEN ==================
                        7 -> {
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(64.dp),
                                        color = Color(0xFF2563EB),
                                        strokeWidth = 6.dp
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text("Processing Payment", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF1E293B))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Please wait while we are processing your payment...", fontSize = 13.sp, color = Color.Gray)
                                }
                            }
                        }

                        // ================== STEP 8: PAYMENT SUCCESS ==================
                        8 -> {
                            Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
                                // Draw gorgeous confetti particles
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val colors = listOf(Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF8B5CF6))
                                    val random = java.util.Random(1234)
                                    for (i in 0 until 60) {
                                        val x = random.nextFloat() * size.width
                                        val y = random.nextFloat() * size.height
                                        val radius = random.nextFloat() * 10 + 6
                                        val color = colors[random.nextInt(colors.size)]
                                        drawCircle(color = color, radius = radius, center = Offset(x, y))
                                    }
                                }
                                
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(24.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .background(Color(0xFF10B981), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Success", tint = Color.White, modifier = Modifier.size(48.dp))
                                    }
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text("Payment Successful!", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFF10B981))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Your payment has been completed successfully.", fontSize = 14.sp, color = Color.Gray)
                                    
                                    Spacer(modifier = Modifier.height(32.dp))
                                    
                                    // Transaction Card Summary
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            DetailRowReceipt("Receipt No", receiptNo)
                                            DetailRowReceipt("Paid Amount", "₹ $amountCollected", isBold = true)
                                            DetailRowReceipt("Payment Mode", paymentMode)
                                            DetailRowReceipt("Date & Time", paymentDate)
                                            DetailRowReceipt("Transaction ID", txnNumber)
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(32.dp))
                                    
                                    Button(
                                        onClick = { currentStep = 9 },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("View Receipt", fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    OutlinedButton(
                                        onClick = { onDismiss() },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Back to Dashboard")
                                    }
                                }
                            }
                        }

                        // ================== STEP 9: RECEIPT INVOICE ==================
                        9 -> {
                            selectedStudent?.let { s ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            // College letterhead
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.School, contentDescription = "Logo", tint = Color(0xFF1E3A8A), modifier = Modifier.size(44.dp))
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text("Center for Distance Education", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF1E3A8A))
                                                    Text("Bharathidasan University", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                                    Text("Palkalaiperur, Tiruchirappalli - 620024", fontSize = 9.sp, color = Color.Gray)
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "PAYMENT RECEIPT",
                                                color = Color(0xFF16A34A),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp,
                                                modifier = Modifier
                                                    .background(Color(0xFFDCFCE7), RoundedCornerShape(12.dp))
                                                    .padding(horizontal = 14.dp, vertical = 4.dp)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            HorizontalDivider(color = Color(0xFFE2E8F0))
                                            Spacer(modifier = Modifier.height(16.dp))
                                            
                                            DetailRowReceipt("Receipt No", receiptNo)
                                            DetailRowReceipt("Date", paymentDate)
                                            
                                            Spacer(modifier = Modifier.height(12.dp))
                                            DetailRowReceipt("Student Name", s.Name)
                                            DetailRowReceipt("Student ID", s.StudentID)
                                            DetailRowReceipt("Course", s.Course)
                                            DetailRowReceipt("Semester", semesterNo)
                                            DetailRowReceipt("Batch", studentBatch.ifEmpty { "2024-2027" })
                                            
                                            Spacer(modifier = Modifier.height(12.dp))
                                            HorizontalDivider(color = Color(0xFFE2E8F0))
                                            Spacer(modifier = Modifier.height(12.dp))
                                            
                                            DetailRowReceipt("Fee Type", feeType)
                                            DetailRowReceipt("Amount", "₹ $amountCollected")
                                            DetailRowReceipt("Discount", "₹ $discountCollected")
                                            DetailRowReceipt("Fine", "₹ $fineCollected")
                                            DetailRowReceipt("Scholarship", "₹ $scholarshipCollected")
                                            
                                            val paidAmt = (amountCollected.toDoubleOrNull() ?: 0.0) + (fineCollected.toDoubleOrNull() ?: 0.0) - (discountCollected.toDoubleOrNull() ?: 0.0)
                                            DetailRowReceipt("Total Paid Amount", "₹ ${paidAmt.toInt()}", isBold = true)
                                            DetailRowReceipt("Payment Mode", paymentMode)
                                            DetailRowReceipt("Transaction ID", txnNumber)
                                            
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                                    Text("Remaining Balance : ", color = Color(0xFFDC2626), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                    Text("₹ ${balanceRemaining.toInt()}", fontWeight = FontWeight.Black, color = Color(0xFFDC2626), fontSize = 13.sp)
                                                }
                                            }
                                            
                                            // Dynamic QR Code Simulator (matches image step 8)
                                            Spacer(modifier = Modifier.height(24.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                                    .padding(8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Canvas(modifier = Modifier.size(80.dp)) {
                                                    // Simple stylized QR Code layout
                                                    val random = java.util.Random(receiptNo.hashCode().toLong())
                                                    for (x in 0..8) {
                                                        for (y in 0..8) {
                                                            if (random.nextBoolean() || (x in 0..2 && y in 0..2) || (x in 6..8 && y in 0..2) || (x in 0..2 && y in 6..8)) {
                                                                drawRect(
                                                                    color = Color.Black,
                                                                    topLeft = Offset(x * size.width / 9f, y * size.height / 9f),
                                                                    size = Size(size.width / 9f, size.height / 9f)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Thank You!", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF1E3A8A))
                                            Text("This is a computer generated receipt.", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }

                                    // Action buttons for sharing/saving
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(
                                            onClick = { Toast.makeText(context, "Downloading PDF Receipt...", Toast.LENGTH_SHORT).show() },
                                            modifier = Modifier.height(48.dp).weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = "Download")
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("DOWNLOAD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = { showShareDialog = true },
                                            modifier = Modifier.height(48.dp).weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = "Share")
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("SHARE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = { currentStep = 10 },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                            Icon(Icons.Default.History, contentDescription = "History", tint = Color.White)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("VIEW ALL PAYMENT HISTORY", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // ================== STEP 10: STUDENT PAYMENT HISTORY ==================
                        10 -> {
                            selectedStudent?.let { s ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Payment History Log",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )

                                    // Student Card Details
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(14.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier.size(44.dp).background(Color(0xFFEFF6FF), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1E3A8A))
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(s.Name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                                                Text("${s.StudentID} • ${s.Course}", fontSize = 12.sp, color = Color.Gray)
                                            }
                                        }
                                    }

                                    // History Log Table
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Column {
                                            // Table Header
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFF8FAFC))
                                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Date & Receipt", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1.5f))
                                                Text("Semester", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1.2f))
                                                Text("Method", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                                                Text("Amount", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                                            }

                                            val allHistory = studentPayments.sortedByDescending { it.Date }
                                            if (allHistory.isEmpty()) {
                                                Box(
                                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("No payments recorded yet.", color = Color.Gray, fontSize = 13.sp)
                                                }
                                            } else {
                                                allHistory.forEachIndexed { idx, p ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 12.dp, horizontal = 12.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1.5f)) {
                                                            Text(p.Date, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                                            Text(p.ReceiptNumber, fontSize = 10.sp, color = Color.Gray)
                                                        }
                                                        Text(p.Semester, fontSize = 12.sp, color = Color(0xFF475569), modifier = Modifier.weight(1.2f))
                                                        Text(p.PaymentMode, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB), modifier = Modifier.weight(1f))
                                                        Text("₹ ${p.Amount}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A), modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                                                    }
                                                    if (idx < allHistory.lastIndex) {
                                                        HorizontalDivider(color = Color(0xFFF1F5F9))
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Finish button
                                    Button(
                                        onClick = { currentStep = 11 },
                                        modifier = Modifier.fillMaxWidth().height(48.dp).padding(top = 8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Go to Quick Actions", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // ================== STEP 11: QUICK ACTIONS ==================
                        11 -> {
                            selectedStudent?.let { s ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Quick Actions",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )

                                    // Grid of Actions
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        val items = listOf(
                                            Triple("Pay Fees", Icons.Default.CreditCard, 1),
                                            Triple("Fee History", Icons.Default.History, 10),
                                            Triple("Print Receipt", Icons.Default.Receipt, 9),
                                            Triple("Due Fees", Icons.Default.DateRange, 4),
                                            Triple("Send Receipt", Icons.Default.Send, 9),
                                            Triple("Download", Icons.Default.Download, 9)
                                        )
                                        items.chunked(2).forEach { rowItems ->
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                rowItems.forEach { (title, icon, stepTarget) ->
                                                    Card(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(95.dp)
                                                            .clickable {
                                                                if (title == "Due Fees") {
                                                                    Toast.makeText(context, "Semester Outstanding Balance: ₹ ${selectedSemesterBalance.toInt()}", Toast.LENGTH_LONG).show()
                                                                } else if (title == "Send Receipt" || title == "Download") {
                                                                    Toast.makeText(context, "$title action triggered!", Toast.LENGTH_SHORT).show()
                                                                } else {
                                                                    currentStep = stepTarget
                                                                }
                                                            },
                                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                        shape = RoundedCornerShape(12.dp),
                                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.fillMaxSize().padding(8.dp),
                                                            verticalArrangement = Arrangement.Center,
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {
                                                            Icon(icon, contentDescription = title, tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
                                                            Spacer(modifier = Modifier.height(6.dp))
                                                            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = { onDismiss() },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Back to Dashboard", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ================== STEP 10: SHARE RECEIPT OVERLAY DIALOG ==================
    if (showShareDialog) {
        Dialog(onDismissRequest = { showShareDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Share Receipt via", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF1E293B))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val shareOptions = listOf(
                            Triple("WhatsApp", Icons.Default.Chat, Color(0xFF25D366)),
                            Triple("Email", Icons.Default.Email, Color(0xFFEA4335)),
                            Triple("Telegram", Icons.Default.Send, Color(0xFF0088CC)),
                            Triple("More", Icons.Default.Share, Color(0xFF64748B))
                        )
                        shareOptions.forEach { (name, icon, color) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    Toast.makeText(context, "Sharing receipt via $name...", Toast.LENGTH_SHORT).show()
                                    showShareDialog = false
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(color.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = name, tint = color, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                        }
                    }
                    
                    Button(
                        onClick = { showShareDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialSummaryCard(title: String, value: String, color: Color, bgColor: Color, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = bgColor), shape = RoundedCornerShape(8.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(title, fontSize = 9.sp, color = color, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun DetailRowReceipt(label: String, value: String, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = if (isBold) Color(0xFF1E293B) else Color.Gray, fontSize = 13.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF1E293B))
    }
}

@Composable
fun FeeBreakupRow(
    title: String,
    amount: Int,
    paid: Int,
    due: Int,
    isTotal: Boolean = false
) {
    val weightTitle = 2f
    val weightVal = 1f
    
    val textColor = if (isTotal) Color(0xFF1E3A8A) else Color(0xFF475569)
    val fontWeight = if (isTotal) FontWeight.Black else FontWeight.Medium
    val fontSize = if (isTotal) 13.sp else 12.sp
    val bg = if (isTotal) Color(0xFFEFF6FF) else Color.Transparent
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(vertical = 12.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontWeight = fontWeight,
            fontSize = fontSize,
            color = textColor,
            modifier = Modifier.weight(weightTitle)
        )
        Text(
            text = "₹ $amount",
            fontWeight = fontWeight,
            fontSize = fontSize,
            color = textColor,
            modifier = Modifier.weight(weightVal),
            textAlign = TextAlign.End
        )
        Text(
            text = "₹ $paid",
            fontWeight = fontWeight,
            fontSize = fontSize,
            color = if (isTotal) Color(0xFF16A34A) else textColor,
            modifier = Modifier.weight(weightVal),
            textAlign = TextAlign.End
        )
        Text(
            text = "₹ $due",
            fontWeight = if (due > 0 && !isTotal) FontWeight.Bold else fontWeight,
            fontSize = fontSize,
            color = if (due > 0) Color(0xFFDC2626) else textColor,
            modifier = Modifier.weight(weightVal),
            textAlign = TextAlign.End
        )
    }
}

// --- REPORTS VIEW ---
@Composable
fun ReportsView(
    reports: ReportResponse,
    students: List<Student>,
    feeAssignments: List<FeeAssignment>,
    payments: List<Payment>,
    courses: List<Course>,
    departments: List<Department>,
    batches: List<Batch>
) {
    val context = LocalContext.current
    var selectedReportType by remember { mutableStateOf(0) } // 0 = Assigned Fees, 1 = Payment History
    var selectedDimension by remember { mutableStateOf(0) } // 0 = Batch wise, 1 = Course wise, 2 = Department wise, 3 = Name wise
    
    // Group keys expanded state
    var expandedGroups by remember { mutableStateOf(setOf<String>()) }
    
    // Grouping logic
    val groupedData = remember(selectedReportType, selectedDimension, feeAssignments, payments, students) {
        if (selectedReportType == 0) {
            val result = mutableMapOf<String, MutableList<FeeAssignment>>()
            feeAssignments.forEach { assign ->
                val stu = students.find { it.StudentID == assign.StudentID }
                val key = when (selectedDimension) {
                    0 -> stu?.Batch?.takeIf { it.isNotBlank() } ?: "No Batch Assigned"
                    1 -> assign.Course.takeIf { it.isNotBlank() } ?: stu?.Course?.takeIf { it.isNotBlank() } ?: "General Course"
                    2 -> stu?.Department?.takeIf { it.isNotBlank() } ?: "General Department"
                    3 -> stu?.Name?.takeIf { it.isNotBlank() }?.let { "$it (${assign.StudentID})" } ?: "ID: ${assign.StudentID}"
                    else -> "General"
                }
                result.getOrPut(key) { mutableListOf() }.add(assign)
            }
            result
        } else {
            val result = mutableMapOf<String, MutableList<Payment>>()
            payments.forEach { pay ->
                val stu = students.find { it.StudentID == pay.StudentID }
                val key = when (selectedDimension) {
                    0 -> stu?.Batch?.takeIf { it.isNotBlank() } ?: "No Batch Assigned"
                    1 -> pay.Course.takeIf { it.isNotBlank() } ?: stu?.Course?.takeIf { it.isNotBlank() } ?: "General Course"
                    2 -> stu?.Department?.takeIf { it.isNotBlank() } ?: "General Department"
                    3 -> stu?.Name?.takeIf { it.isNotBlank() }?.let { "$it (${pay.StudentID})" } ?: "ID: ${pay.StudentID}"
                    else -> "General"
                }
                result.getOrPut(key) { mutableListOf() }.add(pay)
            }
            result
        }
    }

    val grandTotal = remember(groupedData, selectedReportType) {
        if (selectedReportType == 0) {
            groupedData.values.sumOf { list ->
                (list as List<FeeAssignment>).sumOf { it.TotalAmount.toDoubleOrNull() ?: 0.0 }
            }
        } else {
            groupedData.values.sumOf { list ->
                (list as List<Payment>).sumOf { it.Amount.toDoubleOrNull() ?: 0.0 }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // PDF Export Banner (Beautiful Emerald-Green Gradient Card)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(colors = listOf(Color(0xFF059669), Color(0xFF10B981))))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Assessment, null, tint = Color.White, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Outstanding Debtors & Collections", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        }
                        Text(
                            "Compile overall campus financial statistics and generate standard printable ledger documents safely.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                val pdf = PdfGenerator.generateCollectionReport(context, reports)
                                if (pdf != null) {
                                    PdfGenerator.sharePdf(context, pdf)
                                } else {
                                    Toast.makeText(context, "Failed to compile report", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Download, null, tint = Color(0xFF059669))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Ledger PDF Report", color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Select Report Category
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select Report Type", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF475569))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                        .padding(4.dp)
                ) {
                    // Tab 0: Assigned Fees
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedReportType == 0) Color.White else Color.Transparent)
                            .clickable { 
                                selectedReportType = 0 
                                expandedGroups = emptySet()
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AddCard,
                                null,
                                tint = if (selectedReportType == 0) Color(0xFF1E3A8A) else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Assigned Fees Dues",
                                fontWeight = if (selectedReportType == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedReportType == 0) Color(0xFF1E3A8A) else Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                    // Tab 1: Payment History
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedReportType == 1) Color.White else Color.Transparent)
                            .clickable { 
                                selectedReportType = 1 
                                expandedGroups = emptySet()
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Receipt,
                                null,
                                tint = if (selectedReportType == 1) Color(0xFF16A34A) else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Payment History",
                                fontWeight = if (selectedReportType == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedReportType == 1) Color(0xFF16A34A) else Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Section: Select Grouping Dimension
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Group Statistics By", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF475569))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val dimensions = listOf(
                        Triple("Batch", Icons.Default.School, Color(0xFF3B82F6)),
                        Triple("Course", Icons.Default.Category, Color(0xFF8B5CF6)),
                        Triple("Dept", Icons.Default.Domain, Color(0xFFF59E0B)),
                        Triple("Name", Icons.Default.Person, Color(0xFFEC4899))
                    )
                    dimensions.forEachIndexed { index, (dim, icon, color) ->
                        val isSelected = selectedDimension == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) color.copy(alpha = 0.15f) else Color.White)
                                .border(1.dp, if (isSelected) color else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .clickable { 
                                    selectedDimension = index 
                                    expandedGroups = emptySet()
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(icon, null, tint = if (isSelected) color else Color.Gray, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    dim,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) color else Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        // Grand Total Card for Selected Dimension and Mode (Very elegant styling)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val header = if (selectedReportType == 0) "Total Assigned Fees Amount" else "Total Payment Collection"
                        Text(header, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "₹ ${String.format("%,d", grandTotal.toInt())}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = if (selectedReportType == 0) Color(0xFF1E3A8A) else Color(0xFF16A34A)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                if (selectedReportType == 0) Color(0xFFEFF6FF) else Color(0xFFDCFCE7),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (selectedReportType == 0) Icons.Default.TrendingUp else Icons.Default.CheckCircle,
                            null,
                            tint = if (selectedReportType == 0) Color(0xFF2563EB) else Color(0xFF16A34A),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        // List of Group Items
        if (groupedData.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FolderOpen, null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No report transactions found.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(groupedData.keys.toList()) { groupKey ->
                val list = groupedData[groupKey]!!
                val isExpanded = expandedGroups.contains(groupKey)
                
                // Calculate total for this specific group
                val groupAmt = if (selectedReportType == 0) {
                    (list as List<FeeAssignment>).sumOf { it.TotalAmount.toDoubleOrNull() ?: 0.0 }
                } else {
                    (list as List<Payment>).sumOf { it.Amount.toDoubleOrNull() ?: 0.0 }
                }

                val percentage = if (grandTotal > 0) (groupAmt / grandTotal).toFloat() else 0f
                val colorTheme = when (selectedDimension) {
                    0 -> Color(0xFF3B82F6) // Batch Blue
                    1 -> Color(0xFF8B5CF6) // Course Purple
                    2 -> Color(0xFFF59E0B) // Dept Amber
                    else -> Color(0xFFEC4899) // Name Pink
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (isExpanded) colorTheme.copy(alpha = 0.5f) else Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Header Box containing Title, Count and Amount
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedGroups = if (isExpanded) {
                                        expandedGroups - groupKey
                                    } else {
                                        expandedGroups + groupKey
                                    }
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(colorTheme.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val letter = groupKey.replaceFirst("Batch:", "").replaceFirst("Course:", "").trim().firstOrNull()?.uppercase() ?: "?"
                                Text(letter.toString(), fontWeight = FontWeight.Bold, color = colorTheme, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = groupKey,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val label = if (selectedReportType == 0) "allocations" else "receipts"
                                Text("${list.size} $label", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "₹ ${String.format("%,d", groupAmt.toInt())}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = if (selectedReportType == 0) Color(0xFFEF4444) else Color(0xFF10B981)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${(percentage * 100).toInt()}% of grand total",
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Progress bar showing contribution percentage
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(Color(0xFFF1F5F9))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(percentage)
                                    .fillMaxHeight()
                                    .background(colorTheme)
                            )
                        }

                        // Expanded Area: Detailed breakdown & itemised list
                        if (isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // 1. Category Sub-Breakdown Boxes
                                Text(
                                    "COMPONENTS BREAKDOWN",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    letterSpacing = 1.sp
                                )
                                
                                if (selectedReportType == 0) {
                                    // Fee breakup sums
                                    val assignmentsList = list as List<FeeAssignment>
                                    val tuitionSum = assignmentsList.sumOf { it.TuitionFee.toDoubleOrNull() ?: 0.0 }.toInt()
                                    val examSum = assignmentsList.sumOf { it.ExamFee.toDoubleOrNull() ?: 0.0 }.toInt()
                                    val libSum = assignmentsList.sumOf { it.LibraryFee.toDoubleOrNull() ?: 0.0 }.toInt()
                                    val hostelSum = assignmentsList.sumOf { it.HostelFee.toDoubleOrNull() ?: 0.0 }.toInt()
                                    val otherSum = assignmentsList.sumOf { 
                                        (it.AdmissionFee.toDoubleOrNull() ?: 0.0) + 
                                        (it.TransportFee.toDoubleOrNull() ?: 0.0) +
                                        (it.Fine.toDoubleOrNull() ?: 0.0) -
                                        (it.Scholarship.toDoubleOrNull() ?: 0.0) -
                                        (it.Discount.toDoubleOrNull() ?: 0.0)
                                    }.toInt()

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        FinancialSummaryCard("Tuition", "₹$tuitionSum", Color(0xFF1E3A8A), Color.White, Modifier.weight(1f))
                                        FinancialSummaryCard("Exam", "₹$examSum", Color(0xFF7C3AED), Color.White, Modifier.weight(1f))
                                        FinancialSummaryCard("Library", "₹$libSum", Color(0xFF0D9488), Color.White, Modifier.weight(1f))
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        FinancialSummaryCard("Hostel", "₹$hostelSum", Color(0xFFEA580C), Color.White, Modifier.weight(1f))
                                        FinancialSummaryCard("Others/Net", "₹$otherSum", Color(0xFF475569), Color.White, Modifier.weight(1f))
                                    }
                                } else {
                                    // Payment Mode distribution
                                    val paymentsList = list as List<Payment>
                                    val cashSum = paymentsList.filter { it.PaymentMode.equals("Cash", true) }.sumOf { it.Amount.toDoubleOrNull() ?: 0.0 }.toInt()
                                    val upiSum = paymentsList.filter { it.PaymentMode.contains("UPI", true) || it.PaymentMode.contains("Online", true) }.sumOf { it.Amount.toDoubleOrNull() ?: 0.0 }.toInt()
                                    val otherSum = paymentsList.filter { !it.PaymentMode.equals("Cash", true) && !it.PaymentMode.contains("UPI", true) && !it.PaymentMode.contains("Online", true) }.sumOf { it.Amount.toDoubleOrNull() ?: 0.0 }.toInt()
                                    val fineSum = paymentsList.sumOf { it.Fine.toDoubleOrNull() ?: 0.0 }.toInt()
                                    val discountSum = paymentsList.sumOf { it.Discount.toDoubleOrNull() ?: 0.0 }.toInt()

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        FinancialSummaryCard("Cash Mode", "₹$cashSum", Color(0xFFD97706), Color.White, Modifier.weight(1f))
                                        FinancialSummaryCard("UPI/Online", "₹$upiSum", Color(0xFF0D9488), Color.White, Modifier.weight(1f))
                                        FinancialSummaryCard("Others", "₹$otherSum", Color(0xFF7C3AED), Color.White, Modifier.weight(1f))
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        FinancialSummaryCard("Fines", "₹$fineSum", Color(0xFFEF4444), Color.White, Modifier.weight(1f))
                                        FinancialSummaryCard("Discounts", "₹$discountSum", Color(0xFF10B981), Color.White, Modifier.weight(1f))
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                
                                // 2. Itemised Individual Records list (Table-like representation)
                                Text(
                                    "INDIVIDUAL RECORDS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    letterSpacing = 1.sp
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    list.take(15).forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.White, RoundedCornerShape(10.dp))
                                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (selectedReportType == 0) {
                                                val assignItem = item as FeeAssignment
                                                val stu = students.find { it.StudentID == assignItem.StudentID }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(stu?.Name ?: "ID: ${assignItem.StudentID}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                                                    Text("ID: ${assignItem.StudentID} | Sem: ${assignItem.Semester}", fontSize = 11.sp, color = Color.Gray)
                                                }
                                                Text(
                                                    "₹ ${assignItem.TotalAmount}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFFEF4444)
                                                )
                                            } else {
                                                val payItem = item as Payment
                                                val stu = students.find { it.StudentID == payItem.StudentID }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(stu?.Name ?: "ID: ${payItem.StudentID}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                                                    Text("Receipt: ${payItem.ReceiptNumber} | Date: ${payItem.Date}", fontSize = 11.sp, color = Color.Gray)
                                                }
                                                Text(
                                                    "+₹ ${payItem.Amount}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF10B981)
                                                )
                                            }
                                        }
                                    }
                                    if (list.size > 15) {
                                        Text(
                                            "Showing first 15 of ${list.size} records",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
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
}

// --- USERS MANAGEMENT & AUDIT LOG VIEW ---
@Composable
fun UsersView(students: List<Student>, auditLogs: List<AuditLog>) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Users, 1 = Audit Logs
    val context = LocalContext.current

    // Local user model representing portal users
    data class PortalUser(
        val name: String,
        val email: String,
        val role: String, // SUPER_ADMIN, ADMIN, STAFF, STUDENT
        val isActive: Boolean,
        val mobile: String = "",
        val regNo: String = "",
        val avatarColor: Color = Color(0xFF6366F1)
    )

    // Initial users seed matching the image
    val initialUsers = remember {
        mutableStateListOf(
            PortalUser("Admin", "admin@gmail.com", "SUPER_ADMIN", true, "9876543210", "REG10001", Color(0xFF6366F1)),
            PortalUser("Ramesh Kumar", "ramesh@gmail.com", "ADMIN", true, "9876543211", "REG10002", Color(0xFFEF4444)),
            PortalUser("Suresh B", "suresh@gmail.com", "STAFF", true, "9876543212", "REG10003", Color(0xFFF59E0B))
        )
    }

    LaunchedEffect(students) {
        students.forEach { s ->
            val alreadyExists = initialUsers.any {
                it.email.equals(s.Email, ignoreCase = true) || 
                (s.StudentID.isNotEmpty() && it.regNo == s.StudentID) ||
                (s.RegNo.isNotEmpty() && it.regNo == s.RegNo)
            }
            if (!alreadyExists) {
                initialUsers.add(
                    PortalUser(
                        name = s.Name,
                        email = if (s.Email.isNotEmpty()) s.Email else "${s.StudentID.lowercase()}@college.edu",
                        role = "STUDENT",
                        isActive = s.Status.equals("Active", ignoreCase = true),
                        mobile = s.Mobile,
                        regNo = s.StudentID.ifEmpty { s.RegNo },
                        avatarColor = Color(0xFF10B981)
                    )
                )
            }
        }
    }

    // Interactive states
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("All") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    
    var showAddUserDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Row styling (Custom pill style tabs for a modern premium feel)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Portal Users", "Audit Trails").forEachIndexed { index, label ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF1E293B) else Color(0xFF64748B),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        if (selectedTab == 0) {
            // TITLE AND FILTER ACTIONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Users Directory",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Manage accounts, roles, and platform permissions.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
                
                // Add User Button
                IconButton(
                    onClick = { showAddUserDialog = true },
                    modifier = Modifier
                        .background(Color(0xFF2563EB), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.Add, "Add User", tint = Color.White)
                }
            }

            // SEARCH & FILTERS
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search users by name or email...") },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Role Filter Dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            var roleExpanded by remember { mutableStateOf(false) }
                            OutlinedButton(
                                onClick = { roleExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Role: $selectedRoleFilter", fontSize = 11.sp, color = Color.DarkGray)
                                    Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(14.dp))
                                }
                            }
                            DropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                                listOf("All", "SUPER_ADMIN", "ADMIN", "STAFF", "STUDENT").forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role, fontSize = 12.sp) },
                                        onClick = {
                                            selectedRoleFilter = role
                                            roleExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Status Filter Dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            var statusExpanded by remember { mutableStateOf(false) }
                            OutlinedButton(
                                onClick = { statusExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Status: $selectedStatusFilter", fontSize = 11.sp, color = Color.DarkGray)
                                    Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(14.dp))
                                }
                            }
                            DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                                listOf("All", "Active", "Inactive").forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status, fontSize = 12.sp) },
                                        onClick = {
                                            selectedStatusFilter = status
                                            statusExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // USERS LIST
            val filteredUsers = remember(initialUsers, searchQuery, selectedRoleFilter, selectedStatusFilter) {
                initialUsers.filter { u ->
                    val matchesSearch = u.name.contains(searchQuery, ignoreCase = true) ||
                            u.email.contains(searchQuery, ignoreCase = true)
                    val matchesRole = selectedRoleFilter == "All" || u.role == selectedRoleFilter
                    val matchesStatus = selectedStatusFilter == "All" ||
                            (selectedStatusFilter == "Active" && u.isActive) ||
                            (selectedStatusFilter == "Inactive" && !u.isActive)
                    matchesSearch && matchesRole && matchesStatus
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredUsers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No users found matching search criteria.", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                } else {
                    items(filteredUsers.size) { index ->
                        val u = filteredUsers[index]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Beautiful circle avatar with dynamic gradient or initials
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(u.avatarColor.copy(alpha = 0.8f), u.avatarColor)
                                                ),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (u.name.isNotEmpty()) u.name.take(1).uppercase() else "U",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                    }

                                    // Name, Email & Role Badge Row
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = u.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF1E293B)
                                            )
                                            
                                            // Role Badge
                                            val (badgeText, badgeColor, badgeBg) = when (u.role) {
                                                "SUPER_ADMIN" -> Triple("SUPER ADMIN", Color(0xFF6366F1), Color(0xFFEEF2FF))
                                                "ADMIN" -> Triple("ADMIN", Color(0xFFEF4444), Color(0xFFFEF2F2))
                                                "STAFF" -> Triple("STAFF", Color(0xFFF59E0B), Color(0xFFFEF3C7))
                                                else -> Triple("STUDENT", Color(0xFF3B82F6), Color(0xFFEFF6FF))
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(badgeBg, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = badgeText,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = badgeColor
                                                )
                                            }
                                        }

                                        Text(
                                            text = u.email,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )

                                        // Status textual tag (Active / Inactive)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(if (u.isActive) Color(0xFF16A34A) else Color(0xFF94A3B8), CircleShape)
                                            )
                                            Text(
                                                text = if (u.isActive) "Active" else "Inactive",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (u.isActive) Color(0xFF16A34A) else Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }

                                // Interactive toggle switch (exactly like the image)
                                Switch(
                                    checked = u.isActive,
                                    onCheckedChange = { isChecked ->
                                        // Update local list state
                                        val targetIdx = initialUsers.indexOfFirst { it.email == u.email }
                                        if (targetIdx != -1) {
                                            initialUsers[targetIdx] = initialUsers[targetIdx].copy(isActive = isChecked)
                                            Toast.makeText(context, "${u.name} set to ${if (isChecked) "Active" else "Inactive"}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF16A34A),
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color(0xFFCBD5E1)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Live Audit Trail of student management systems
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(auditLogs) { log ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(log.Action, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), fontSize = 13.sp)
                            Text(log.Timestamp.take(16), fontSize = 10.sp, color = Color.Gray)
                        }
                        Text("User: ${log.User}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("Details: ${log.Details}", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
    }

    // ADD USER DIALOG (Matches the Add User visual screen layout exactly!)
    if (showAddUserDialog) {
        var fullName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var mobile by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var selectedRole by remember { mutableStateOf("STUDENT") }
        var isUserActive by remember { mutableStateOf(true) }

        Dialog(onDismissRequest = { showAddUserDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Block
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add User",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E293B)
                        )
                        IconButton(onClick = { showAddUserDialog = false }) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Profile avatar placeholder with camera icon (matching top-right mockup)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(80.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFEFF6FF), CircleShape)
                                .border(1.dp, Color(0xFFBFDBFE), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Color(0xFF3B82F6)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF2563EB), CircleShape)
                                .border(1.dp, Color.White, CircleShape)
                                .size(26.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Upload Picture",
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                        }
                    }

                    // Inputs
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            placeholder = { Text("Enter full name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            placeholder = { Text("Enter email") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it },
                            label = { Text("Mobile") },
                            placeholder = { Text("Enter mobile number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            placeholder = { Text("Enter password") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        // Role selection dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            var roleMenuExpanded by remember { mutableStateOf(false) }
                            OutlinedButton(
                                onClick = { roleMenuExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Role: $selectedRole", fontSize = 13.sp, color = Color.DarkGray)
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                            DropdownMenu(expanded = roleMenuExpanded, onDismissRequest = { roleMenuExpanded = false }) {
                                listOf("SUPER_ADMIN", "ADMIN", "STAFF", "STUDENT").forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text(r) },
                                        onClick = {
                                            selectedRole = r
                                            roleMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Status toggle switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Active Status", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Switch(
                                checked = isUserActive,
                                onCheckedChange = { isUserActive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF16A34A),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFCBD5E1)
                                )
                            )
                        }
                    }

                    // Save User Button (matching the mockup exactly)
                    Button(
                        onClick = {
                            if (fullName.trim().isEmpty() || email.trim().isEmpty()) {
                                Toast.makeText(context, "Full Name and Email are required", Toast.LENGTH_SHORT).show()
                            } else {
                                val newColor = when (selectedRole) {
                                    "SUPER_ADMIN" -> Color(0xFF6366F1)
                                    "ADMIN" -> Color(0xFFEF4444)
                                    "STAFF" -> Color(0xFFF59E0B)
                                    else -> Color(0xFF3B82F6)
                                }
                                val newUser = PortalUser(
                                    name = fullName.trim(),
                                    email = email.trim(),
                                    role = selectedRole,
                                    isActive = isUserActive,
                                    mobile = mobile.trim(),
                                    avatarColor = newColor
                                )
                                initialUsers.add(newUser)
                                Toast.makeText(context, "User saved successfully!", Toast.LENGTH_SHORT).show()
                                showAddUserDialog = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Save User", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// --- ANNOUNCEMENT BROADCST POPUP ---
@Composable
fun AnnounceDialog(
    onDismiss: () -> Unit,
    onPost: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("All") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Broadcast Campus Alert", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2563EB))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notification Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = msg,
                    onValueChange = { msg = it },
                    label = { Text("Alert message details...") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Target Group (All/CSE/MBA/Admin)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onPost(title, msg, target) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) { Text("Broadcast") }
                }
            }
        }
    }
}

// ==========================================
// FEES VIEW - REAL-TIME OUTSTANDING BALANCE MANAGEMENT
// ==========================================
@Composable
fun FeesView(
    students: List<Student>,
    feeAssignments: List<FeeAssignment>,
    payments: List<Payment>,
    courses: List<Course>,
    onCollectFee: (Student) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var selectedCourseFilter by remember { mutableStateOf("All") }
    var selectedStudentDetails by remember { mutableStateOf<Student?>(null) }

    // Metrics calculation
    val totalAssigned = remember(feeAssignments) {
        feeAssignments.sumOf { it.TotalAmount.toDoubleOrNull() ?: 0.0 }.toInt()
    }
    val totalCollected = remember(payments) {
        payments.sumOf { it.Amount.toDoubleOrNull() ?: 0.0 }.toInt()
    }
    val outstandingBalance = maxOf(0, totalAssigned - totalCollected)
    val collectionRate = if (totalAssigned > 0) (totalCollected * 100.0 / totalAssigned).toInt() else 0

    // Filter students
    val filteredStudents = remember(students, searchQuery, selectedStatusFilter, selectedCourseFilter, feeAssignments, payments) {
        students.filter { s ->
            val matchesSearch = s.Name.contains(searchQuery, ignoreCase = true) ||
                    s.StudentID.contains(searchQuery, ignoreCase = true) ||
                    s.RegNo.contains(searchQuery, ignoreCase = true)
            
            val matchesCourse = selectedCourseFilter == "All" || s.Course.equals(selectedCourseFilter, ignoreCase = true)

            // Compute fee details for student
            val studentAssignments = feeAssignments.filter { it.StudentID == s.StudentID }
            val studentPayments = payments.filter { it.StudentID == s.StudentID }
            val sAssigned = studentAssignments.sumOf { it.TotalAmount.toDoubleOrNull() ?: 0.0 }
            val sPaid = studentPayments.sumOf { it.Amount.toDoubleOrNull() ?: 0.0 }
            val sBalance = maxOf(0.0, sAssigned - sPaid)

            val sStatus = when {
                sAssigned == 0.0 -> "Unassigned"
                sBalance <= 0.0 -> "Fully Paid"
                sPaid > 0.0 -> "Partially Paid"
                else -> "Unpaid"
            }

            val matchesStatus = selectedStatusFilter == "All" || sStatus.equals(selectedStatusFilter, ignoreCase = true)

            matchesSearch && matchesCourse && matchesStatus
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Heading Block
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Fees Management Center", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1E293B))
                Text("Track student payment statuses, collections, and dues.", fontSize = 12.sp, color = Color(0xFF64748B))
            }
            IconButton(
                onClick = {
                    val report = com.example.data.models.ReportResponse(
                        totalStudents = students.size,
                        totalFeesAssigned = totalAssigned.toDouble(),
                        totalPaid = totalCollected.toDouble(),
                        totalPending = outstandingBalance.toDouble(),
                        todayCollection = 0.0,
                        payments = payments,
                        assignments = feeAssignments
                    )
                    val pdfFile = PdfGenerator.generateCollectionReport(context, report)
                    if (pdfFile != null) {
                        PdfGenerator.sharePdf(context, pdfFile)
                    } else {
                        Toast.makeText(context, "Error generating report PDF", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.background(Color.White, CircleShape).size(40.dp)
            ) {
                Icon(Icons.Default.FileDownload, "Download Ledger Report", tint = Color(0xFF16A34A))
            }
        }

        // Stats Cards Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "Total Outstanding",
                value = "₹$outstandingBalance",
                icon = Icons.Default.TrendingDown,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFFEF4444), Color(0xFFDC2626))),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Collection Rate",
                value = "$collectionRate%",
                icon = Icons.Default.TrendingUp,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFF10B981), Color(0xFF059669))),
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "Total Assigned",
                value = "₹$totalAssigned",
                icon = Icons.Default.RequestQuote,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Total Paid",
                value = "₹$totalCollected",
                icon = Icons.Default.Payments,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
                modifier = Modifier.weight(1f)
            )
        }

        // Search & Filters Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Student Name, ID, or Reg No...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Status Filters Row
                    Box(modifier = Modifier.weight(1f)) {
                        var statusMenuExpanded by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { statusMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Status: $selectedStatusFilter", fontSize = 11.sp, color = Color.DarkGray)
                                Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            }
                        }
                        DropdownMenu(expanded = statusMenuExpanded, onDismissRequest = { statusMenuExpanded = false }) {
                            listOf("All", "Fully Paid", "Partially Paid", "Unpaid", "Unassigned").forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status, fontSize = 13.sp) },
                                    onClick = {
                                        selectedStatusFilter = status
                                        statusMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Course Filters Row
                    Box(modifier = Modifier.weight(1f)) {
                        var courseMenuExpanded by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { courseMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Course: $selectedCourseFilter", fontSize = 11.sp, color = Color.DarkGray)
                                Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            }
                        }
                        DropdownMenu(expanded = courseMenuExpanded, onDismissRequest = { courseMenuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("All Courses", fontSize = 13.sp) },
                                onClick = {
                                    selectedCourseFilter = "All"
                                    courseMenuExpanded = false
                                }
                            )
                            courses.forEach { course ->
                                DropdownMenuItem(
                                    text = { Text(course.CourseCode, fontSize = 13.sp) },
                                    onClick = {
                                        selectedCourseFilter = course.CourseCode
                                        courseMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Students Fees List
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredStudents.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No student fee records found matching parameters.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                items(filteredStudents) { s ->
                    val studentAssignments = feeAssignments.filter { it.StudentID == s.StudentID }
                    val studentPayments = payments.filter { it.StudentID == s.StudentID }
                    val sAssigned = studentAssignments.sumOf { it.TotalAmount.toDoubleOrNull() ?: 0.0 }
                    val sPaid = studentPayments.sumOf { it.Amount.toDoubleOrNull() ?: 0.0 }
                    val sBalance = maxOf(0.0, sAssigned - sPaid)

                    val sStatus = when {
                        sAssigned == 0.0 -> "Unassigned"
                        sBalance <= 0.0 -> "Fully Paid"
                        sPaid > 0.0 -> "Partially Paid"
                        else -> "Unpaid"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStudentDetails = s },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Top Row: Name and Status Tag
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(s.Name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                    Text("ID: ${s.StudentID} · Sem ${s.Semester} (${s.Course})", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                                Box {
                                    val tagColor = when(sStatus) {
                                        "Fully Paid" -> Color(0xFF16A34A)
                                        "Partially Paid" -> Color(0xFFD97706)
                                        "Unassigned" -> Color(0xFF64748B)
                                        else -> Color(0xFFDC2626)
                                    }
                                    val tagBg = when(sStatus) {
                                        "Fully Paid" -> Color(0xFFDCFCE7)
                                        "Partially Paid" -> Color(0xFFFEF3C7)
                                        "Unassigned" -> Color(0xFFF1F5F9)
                                        else -> Color(0xFFFEE2E2)
                                    }
                                    Text(
                                        text = sStatus,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = tagColor,
                                        modifier = Modifier
                                            .background(tagBg, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = Color(0xFFF1F5F9))

                            // Middle Row: Values
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Total Fees", fontSize = 10.sp, color = Color.Gray)
                                    Text("₹${sAssigned.toInt()}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF1E293B))
                                }
                                Column {
                                    Text("Paid", fontSize = 10.sp, color = Color.Gray)
                                    Text("₹${sPaid.toInt()}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF16A34A))
                                }
                                Column {
                                    Text("Outstanding", fontSize = 10.sp, color = Color.Gray)
                                    Text("₹${sBalance.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (sBalance > 0) Color(0xFFDC2626) else Color(0xFF1E293B))
                                }
                            }

                            // Payment Progress Bar
                            if (sAssigned > 0.0) {
                                val pct = (sPaid / sAssigned).toFloat()
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    LinearProgressIndicator(
                                        progress = { pct },
                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                        color = Color(0xFF10B981),
                                        trackColor = Color(0xFFE2E8F0)
                                    )
                                    Text("Paid: ${(pct * 100).toInt()}%", fontSize = 9.sp, color = Color.Gray, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // STUDENT DETAILED STATEMENT DIALOG
    selectedStudentDetails?.let { s ->
        Dialog(onDismissRequest = { selectedStudentDetails = null }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFDFD1B8))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(s.Name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                            Text("Dues & Statements", fontSize = 12.sp, color = Color.Gray)
                        }
                        IconButton(onClick = { selectedStudentDetails = null }) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Bio Info Panel
                    Column(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp)).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Student ID: ${s.StudentID}", fontSize = 12.sp, color = Color.DarkGray)
                        Text("Reg Number: ${s.RegNo}", fontSize = 12.sp, color = Color.DarkGray)
                        Text("Department: ${s.Department}", fontSize = 12.sp, color = Color.DarkGray)
                        Text("Course/Branch: ${s.Course} - Sem ${s.Semester}", fontSize = 12.sp, color = Color.DarkGray)
                        Text("Batch Intake: ${s.Batch}", fontSize = 12.sp, color = Color.DarkGray)
                    }

                    // Ledger breakdown
                    Text("Assigned Fee Components", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                    val studentAssignments = feeAssignments.filter { it.StudentID == s.StudentID }
                    if (studentAssignments.isEmpty()) {
                        Text("No fees assigned to this student yet.", fontSize = 11.sp, color = Color.Gray)
                    } else {
                        studentAssignments.forEach { a ->
                            val components = listOf(
                                "Admission Fee" to a.AdmissionFee,
                                "Tuition Fee" to a.TuitionFee,
                                "Exam Fee" to a.ExamFee,
                                "Library Fee" to a.LibraryFee,
                                "Hostel Fee" to a.HostelFee,
                                "Transport Fee" to a.TransportFee,
                                "Fine" to a.Fine,
                                "Scholarship" to a.Scholarship,
                                "Discount" to a.Discount
                            )
                            components.forEach { (name, value) ->
                                val amt = value.toDoubleOrNull() ?: 0.0
                                if (amt > 0.0 || name == "Scholarship" || name == "Discount") {
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(name, fontSize = 12.sp, color = Color.DarkGray)
                                        val prefix = if (name == "Scholarship" || name == "Discount") "-" else ""
                                        Text("${prefix}₹${amt.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                    }
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Assigned Amount", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                Text("₹${(a.TotalAmount.toDoubleOrNull() ?: 0.0).toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Payments Ledger
                    Text("Receipt Collections History", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                    val studentPayments = payments.filter { it.StudentID == s.StudentID }
                    if (studentPayments.isEmpty()) {
                        Text("No payments recorded yet.", fontSize = 11.sp, color = Color.Gray)
                    } else {
                        studentPayments.forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFEFF6FF), RoundedCornerShape(6.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Recpt: ${p.ReceiptNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                    Text("Date: ${p.Date} · ${p.PaymentMode}", fontSize = 10.sp, color = Color.Gray)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("₹${(p.Amount.toDoubleOrNull() ?: 0.0).toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                    IconButton(
                                        onClick = {
                                            val file = PdfGenerator.generatePaymentReceipt(context, p)
                                            if (file != null) PdfGenerator.sharePdf(context, file)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.FileDownload, "Download Receipt", tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Dialog Actions
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val sAssigned = studentAssignments.sumOf { it.TotalAmount.toDoubleOrNull() ?: 0.0 }
                        val sPaid = studentPayments.sumOf { it.Amount.toDoubleOrNull() ?: 0.0 }
                        val sBalance = maxOf(0.0, sAssigned - sPaid)

                        OutlinedButton(
                            onClick = {
                                if (studentPayments.isNotEmpty()) {
                                    val file = PdfGenerator.generatePaymentReceipt(context, studentPayments.last())
                                    if (file != null) PdfGenerator.sharePdf(context, file)
                                } else {
                                    Toast.makeText(context, "No payments to generate receipt", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Print Receipt", fontSize = 12.sp)
                        }

                        if (sBalance > 0.0) {
                            Button(
                                onClick = {
                                    selectedStudentDetails = null
                                    onCollectFee(s)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                            ) {
                                Text("Receive Fee", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// RESULTS VIEW - ACADEMIC GRADE & PERFORMANCE
// ==========================================
@Composable
fun ResultsView(
    students: List<Student>,
    academicPerformance: List<AcademicPerformance>,
    courses: List<Course>,
    semesters: List<Semester>,
    onSavePerformance: (AcademicPerformance) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedSemesterFilter by remember { mutableStateOf("All") }
    var selectedStudentForEntry by remember { mutableStateOf<Student?>(null) }

    val filteredList = remember(academicPerformance, students, searchQuery, selectedSemesterFilter) {
        academicPerformance.filter { ap ->
            val studentName = students.find { it.StudentID == ap.StudentID }?.Name ?: ""
            val matchesSearch = ap.StudentID.contains(searchQuery, ignoreCase = true) ||
                    studentName.contains(searchQuery, ignoreCase = true) ||
                    ap.Subject.contains(searchQuery, ignoreCase = true)
            
            val matchesSem = selectedSemesterFilter == "All" || ap.Semester.equals(selectedSemesterFilter, ignoreCase = true)
            
            matchesSearch && matchesSem
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Academic Results Panel", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1E293B))
                Text("Record and audit exam grades and scoreboards.", fontSize = 12.sp, color = Color(0xFF64748B))
            }
            Button(
                onClick = { selectedStudentForEntry = students.firstOrNull() },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Grade Entry", fontSize = 12.sp, color = Color.White)
            }
        }

        // Search & Filter Panel
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Student or Subject...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                modifier = Modifier.weight(1.3f),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2563EB))
            )

            Box(modifier = Modifier.weight(0.7f)) {
                var semMenuExpanded by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { semMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Sem: $selectedSemesterFilter", fontSize = 12.sp, color = Color.DarkGray)
                        Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
                    }
                }
                DropdownMenu(expanded = semMenuExpanded, onDismissRequest = { semMenuExpanded = false }) {
                    DropdownMenuItem(text = { Text("All") }, onClick = { selectedSemesterFilter = "All"; semMenuExpanded = false })
                    listOf("Sem/Year 1", "Sem/Year 2", "Sem/Year 3", "Sem/Year 4").forEach { s ->
                        DropdownMenuItem(text = { Text(s) }, onClick = { selectedSemesterFilter = s; semMenuExpanded = false })
                    }
                }
            }
        }

        // Results log List
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredList.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No academic performances found.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                items(filteredList) { ap ->
                    val sName = students.find { it.StudentID == ap.StudentID }?.Name ?: "Unknown Student"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                Text("ID: ${ap.StudentID} · ${ap.Semester}", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Subject: ${ap.Subject}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF2563EB))
                                if (ap.Remarks.isNotEmpty()) {
                                    Text("Remarks: ${ap.Remarks}", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFEFF6FF), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Grade: ${ap.Grade}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2563EB))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Marks: ${ap.Marks} / ${ap.TotalMarks}", fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }

    // GRADE ENTRY MODAL DIALOG
    selectedStudentForEntry?.let { initialStudent ->
        var studentSelected by remember { mutableStateOf(initialStudent) }
        var subjectQuery by remember { mutableStateOf("") }
        var semSelected by remember { mutableStateOf(studentSelected.Semester) }
        var grade by remember { mutableStateOf("A") }
        var marksText by remember { mutableStateOf("") }
        var totalMarksText by remember { mutableStateOf("100") }
        var remarksText by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { selectedStudentForEntry = null }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFDFD1B8))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Record Academic Marks", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                        IconButton(onClick = { selectedStudentForEntry = null }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Select Student Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        var studentMenuExpanded by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { studentMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Student: ${studentSelected.Name} (${studentSelected.StudentID})", fontSize = 13.sp, color = Color.DarkGray)
                        }
                        DropdownMenu(expanded = studentMenuExpanded, onDismissRequest = { studentMenuExpanded = false }) {
                            students.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("${s.Name} (${s.StudentID})", fontSize = 12.sp) },
                                    onClick = {
                                        studentSelected = s
                                        semSelected = s.Semester
                                        studentMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Select Semester
                    Box(modifier = Modifier.fillMaxWidth()) {
                        var semMenuExpanded by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { semMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Semester: $semSelected", fontSize = 13.sp, color = Color.DarkGray)
                        }
                        DropdownMenu(expanded = semMenuExpanded, onDismissRequest = { semMenuExpanded = false }) {
                            listOf("Sem/Year 1", "Sem/Year 2", "Sem/Year 3", "Sem/Year 4").forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        semSelected = s
                                        semMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Subject text field
                    OutlinedTextField(
                        value = subjectQuery,
                        onValueChange = { subjectQuery = it },
                        label = { Text("Subject / Course Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Marks and Total Marks Row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = marksText,
                            onValueChange = { marksText = it },
                            label = { Text("Marks Secured") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = totalMarksText,
                            onValueChange = { totalMarksText = it },
                            label = { Text("Max Marks") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    // Select Grade Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        var gradeMenuExpanded by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { gradeMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Award Grade: $grade", fontSize = 13.sp, color = Color.DarkGray)
                        }
                        DropdownMenu(expanded = gradeMenuExpanded, onDismissRequest = { gradeMenuExpanded = false }) {
                            listOf("O", "A+", "A", "B+", "B", "C", "F").forEach { g ->
                                DropdownMenuItem(
                                    text = { Text(g) },
                                    onClick = {
                                        grade = g
                                        gradeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Remarks text field
                    OutlinedTextField(
                        value = remarksText,
                        onValueChange = { remarksText = it },
                        label = { Text("Remarks (e.g. Pass, Fail, Outstanding)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Save Entry CTA
                    Button(
                        onClick = {
                            if (subjectQuery.trim().isEmpty() || marksText.trim().isEmpty()) {
                                Toast.makeText(context, "Please input subject and markssecured", Toast.LENGTH_SHORT).show()
                            } else {
                                val ap = AcademicPerformance(
                                    StudentID = studentSelected.StudentID,
                                    Semester = semSelected,
                                    Subject = subjectQuery.trim(),
                                    Grade = grade,
                                    Marks = marksText.trim(),
                                    TotalMarks = totalMarksText.trim(),
                                    Remarks = remarksText.trim()
                                )
                                onSavePerformance(ap)
                                selectedStudentForEntry = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Record Academic Grade", color = Color.White)
                    }
                }
            }
        }
    }
}


// ==========================================
// ATTENDANCE VIEW - ROLL CALL & RECORD SHEET
// ==========================================
@Composable
fun AttendanceView(
    students: List<Student>,
    attendanceList: List<Attendance>,
    courses: List<Course>,
    semesters: List<Semester>,
    onSaveAttendance: (Attendance) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedSemesterFilter by remember { mutableStateOf("All") }
    var selectedStudentForEntry by remember { mutableStateOf<Student?>(null) }

    val filteredList = remember(attendanceList, students, searchQuery, selectedSemesterFilter) {
        attendanceList.filter { att ->
            val studentName = students.find { it.StudentID == att.StudentID }?.Name ?: ""
            val matchesSearch = att.StudentID.contains(searchQuery, ignoreCase = true) ||
                    studentName.contains(searchQuery, ignoreCase = true) ||
                    att.Subject.contains(searchQuery, ignoreCase = true)
            
            val matchesSem = selectedSemesterFilter == "All" || att.Semester.equals(selectedSemesterFilter, ignoreCase = true)
            
            matchesSearch && matchesSem
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Attendance Record Center", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1E293B))
                Text("Maintain student physical classes attendance sheets.", fontSize = 12.sp, color = Color(0xFF64748B))
            }
            Button(
                onClick = { selectedStudentForEntry = students.firstOrNull() },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Roll Call", fontSize = 12.sp, color = Color.White)
            }
        }

        // Search & Filter Panel
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Student or Subject...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                modifier = Modifier.weight(1.3f),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2563EB))
            )

            Box(modifier = Modifier.weight(0.7f)) {
                var semMenuExpanded by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { semMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Sem: $selectedSemesterFilter", fontSize = 12.sp, color = Color.DarkGray)
                        Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
                    }
                }
                DropdownMenu(expanded = semMenuExpanded, onDismissRequest = { semMenuExpanded = false }) {
                    DropdownMenuItem(text = { Text("All") }, onClick = { selectedSemesterFilter = "All"; semMenuExpanded = false })
                    listOf("Sem/Year 1", "Sem/Year 2", "Sem/Year 3", "Sem/Year 4").forEach { s ->
                        DropdownMenuItem(text = { Text(s) }, onClick = { selectedSemesterFilter = s; semMenuExpanded = false })
                    }
                }
            }
        }

        // Attendance Table log List
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredList.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No attendance sheets found.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                items(filteredList) { att ->
                    val sName = students.find { it.StudentID == att.StudentID }?.Name ?: "Unknown Student"
                    val percentage = (att.Percentage.toDoubleOrNull() ?: 0.0).toFloat() / 100f

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(sName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                    Text("ID: ${att.StudentID} · ${att.Semester}", fontSize = 11.sp, color = Color.Gray)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (percentage >= 0.75f) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${att.Percentage}%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (percentage >= 0.75f) Color(0xFF15803D) else Color(0xFFB91C1C)
                                    )
                                }
                            }

                            Text("Subject: ${att.Subject}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF2563EB))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Classes Attended: ${att.ClassesAttended}", fontSize = 11.sp, color = Color.DarkGray)
                                Text("Total Sessions: ${att.TotalClasses}", fontSize = 11.sp, color = Color.DarkGray)
                            }

                            LinearProgressIndicator(
                                progress = { percentage },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = if (percentage >= 0.75f) Color(0xFF10B981) else Color(0xFFEF4444),
                                trackColor = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            }
        }
    }

    // ATTENDANCE ENTRY MODAL DIALOG
    selectedStudentForEntry?.let { initialStudent ->
        var studentSelected by remember { mutableStateOf(initialStudent) }
        var subjectQuery by remember { mutableStateOf("") }
        var semSelected by remember { mutableStateOf(studentSelected.Semester) }
        var totalClassesText by remember { mutableStateOf("40") }
        var classesAttendedText by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { selectedStudentForEntry = null }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFDFD1B8))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Record Classes Attendance", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                        IconButton(onClick = { selectedStudentForEntry = null }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Select Student Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        var studentMenuExpanded by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { studentMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Student: ${studentSelected.Name} (${studentSelected.StudentID})", fontSize = 13.sp, color = Color.DarkGray)
                        }
                        DropdownMenu(expanded = studentMenuExpanded, onDismissRequest = { studentMenuExpanded = false }) {
                            students.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("${s.Name} (${s.StudentID})", fontSize = 12.sp) },
                                    onClick = {
                                        studentSelected = s
                                        semSelected = s.Semester
                                        studentMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Select Semester
                    Box(modifier = Modifier.fillMaxWidth()) {
                        var semMenuExpanded by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { semMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Semester: $semSelected", fontSize = 13.sp, color = Color.DarkGray)
                        }
                        DropdownMenu(expanded = semMenuExpanded, onDismissRequest = { semMenuExpanded = false }) {
                            listOf("Sem/Year 1", "Sem/Year 2", "Sem/Year 3", "Sem/Year 4").forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        semSelected = s
                                        semMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Subject text field
                    OutlinedTextField(
                        value = subjectQuery,
                        onValueChange = { subjectQuery = it },
                        label = { Text("Subject / Course Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Total Classes & Attended Rows
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = totalClassesText,
                            onValueChange = { totalClassesText = it },
                            label = { Text("Total Sessions") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = classesAttendedText,
                            onValueChange = { classesAttendedText = it },
                            label = { Text("Sessions Attended") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    // Save Entry CTA
                    Button(
                        onClick = {
                            val tot = totalClassesText.toDoubleOrNull() ?: 0.0
                            val att = classesAttendedText.toDoubleOrNull() ?: 0.0
                            if (subjectQuery.trim().isEmpty() || classesAttendedText.trim().isEmpty() || tot == 0.0) {
                                Toast.makeText(context, "Please input subject and valid sessions", Toast.LENGTH_SHORT).show()
                            } else if (att > tot) {
                                Toast.makeText(context, "Attended sessions cannot exceed total sessions", Toast.LENGTH_SHORT).show()
                            } else {
                                val percentage = String.format(Locale.US, "%.2f", (att * 100.0 / tot))
                                val attRecord = Attendance(
                                    StudentID = studentSelected.StudentID,
                                    Semester = semSelected,
                                    Subject = subjectQuery.trim(),
                                    TotalClasses = totalClassesText.trim(),
                                    ClassesAttended = classesAttendedText.trim(),
                                    Percentage = percentage,
                                    LastUpdated = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                                )
                                onSaveAttendance(attRecord)
                                selectedStudentForEntry = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Record Attendance Sheet", color = Color.White)
                    }
                }
            }
        }
    }
}
