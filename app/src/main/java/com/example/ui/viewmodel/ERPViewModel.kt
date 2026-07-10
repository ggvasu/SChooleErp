package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.CollegeERPApplication
import com.example.data.models.*
import com.example.data.session.SessionManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ERPViewModel(application: Application) : BaseViewModel(application) {
    private val app = application as CollegeERPApplication
    private val repository = app.repository
    val sessionManager: SessionManager = app.sessionManager

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    private val _departments = MutableStateFlow<List<Department>>(emptyList())
    val departments: StateFlow<List<Department>> = _departments.asStateFlow()

    private val _batches = MutableStateFlow<List<Batch>>(emptyList())
    val batches: StateFlow<List<Batch>> = _batches.asStateFlow()

    private val _semesters = MutableStateFlow<List<Semester>>(emptyList())
    val semesters: StateFlow<List<Semester>> = _semesters.asStateFlow()

    private val _assignedFees = MutableStateFlow<List<FeeAssignment>>(emptyList())
    val assignedFees: StateFlow<List<FeeAssignment>> = _assignedFees.asStateFlow()

    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _attendance = MutableStateFlow<List<Attendance>>(emptyList())
    val attendance: StateFlow<List<Attendance>> = _attendance.asStateFlow()

    private val _academicPerformance = MutableStateFlow<List<AcademicPerformance>>(emptyList())
    val academicPerformance: StateFlow<List<AcademicPerformance>> = _academicPerformance.asStateFlow()

    private val _reportData = MutableStateFlow(ReportResponse())
    val reportData: StateFlow<ReportResponse> = _reportData.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    // Active Selected Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Bulk Import state flows
    private val _bulkImportProgress = MutableStateFlow<Float?>(null)
    val bulkImportProgress: StateFlow<Float?> = _bulkImportProgress.asStateFlow()

    private val _bulkImportStatus = MutableStateFlow<String?>(null)
    val bulkImportStatus: StateFlow<String?> = _bulkImportStatus.asStateFlow()

    init {
        if (sessionManager.isLoggedIn) {
            refreshAllData()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun refreshAllData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                _students.value = repository.getStudents()
                _courses.value = repository.getCourses()
                _departments.value = repository.getDepartments()
                _batches.value = repository.getBatches()
                _semesters.value = repository.getSemesters()
                _assignedFees.value = repository.getAssignedFees()
                _payments.value = repository.getPayments()
                _notifications.value = repository.getNotifications()
                _attendance.value = repository.getAttendance()
                _academicPerformance.value = repository.getAcademicPerformance()
                _reportData.value = repository.getReports()
                _auditLogs.value = repository.getAuditLogs()
            } catch (e: Exception) {
                _statusMessage.value = "Failed to load data: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // LOGIN
    fun handleLogin(username: String, role: String, pass: String, remember: Boolean, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val res = repository.login(GenericRequest("login", username, role, pass))
                if (res.success) {
                    sessionManager.isLoggedIn = true
                    sessionManager.username = res.username ?: username
                    sessionManager.role = res.role ?: role
                    sessionManager.displayName = res.studentName ?: (if (role == "Admin") "Administrator" else "Student")
                    sessionManager.permissions = res.permissions ?: "View"
                    sessionManager.rememberMe = remember
                    
                    refreshAllData()
                    onSuccess()
                } else {
                    _statusMessage.value = res.message ?: "Authentication failed"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Login error: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // STUDENTS CRUD
    fun saveStudent(student: Student, isEdit: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val res = if (isEdit) {
                    repository.updateStudent(student)
                } else {
                    repository.addStudent(student)
                }
                if (res.success) {
                    app.auditLogger.logAction(
                        sessionManager.username,
                        if (isEdit) "Edit Student" else "Add Student",
                        "Name: ${student.Name}, ID: ${student.StudentID}"
                    )
                    _statusMessage.value = res.message ?: "Student saved successfully"
                    _students.value = repository.getStudents()
                    _reportData.value = repository.getReports()
                    onComplete()
                } else {
                    _statusMessage.value = res.message ?: "Operation failed"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Save failed: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun removeStudent(id: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val res = repository.deleteStudent(id)
                if (res.success) {
                    app.auditLogger.logAction(
                        sessionManager.username,
                        "Delete Student",
                        "ID: $id"
                    )
                    _statusMessage.value = "Student removed successfully"
                    _students.value = repository.getStudents()
                    _reportData.value = repository.getReports()
                } else {
                    _statusMessage.value = res.message ?: "Failed to remove"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Delete failed: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun resetBulkImportProgress() {
        _bulkImportProgress.value = null
        _bulkImportStatus.value = null
    }

    fun bulkImportStudents(studentsList: List<Student>, onComplete: (success: Int, failed: Int) -> Unit) {
        viewModelScope.launch {
            _isRefreshing.value = true
            _bulkImportProgress.value = 0f
            _bulkImportStatus.value = "Starting import of ${studentsList.size} students..."
            var successCount = 0
            var failedCount = 0
            
            studentsList.forEachIndexed { index, student ->
                _bulkImportStatus.value = "Importing (${index + 1}/${studentsList.size}): ${student.Name}..."
                try {
                    val res = repository.addStudent(student)
                    if (res.success) {
                        successCount++
                        app.auditLogger.logAction(
                            sessionManager.username,
                            "Bulk Import Student",
                            "Name: ${student.Name}, ID: ${res.studentId ?: ""}"
                        )
                    } else {
                        failedCount++
                    }
                } catch (e: Exception) {
                    failedCount++
                }
                _bulkImportProgress.value = (index + 1).toFloat() / studentsList.size
            }
            
            try {
                // Refresh data after bulk import
                _students.value = repository.getStudents()
                _reportData.value = repository.getReports()
            } catch (e: Exception) {
                // Ignore refresh failures
            }
            
            _bulkImportStatus.value = "Import completed! $successCount succeeded, $failedCount failed."
            _isRefreshing.value = false
            onComplete(successCount, failedCount)
        }
    }

    // COURSES CRUD
    fun saveCourse(course: Course, isEdit: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val res = if (isEdit) repository.updateCourse(course) else repository.addCourse(course)
                if (res.success) {
                    app.auditLogger.logAction(
                        sessionManager.username,
                        if (isEdit) "Edit Course" else "Add Course",
                        "Code: ${course.CourseCode}, Name: ${course.CourseName}"
                    )
                    _statusMessage.value = "Course saved successfully"
                    _courses.value = repository.getCourses()
                    onComplete()
                } else {
                    _statusMessage.value = res.message ?: "Failed to save course"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Save failed: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun removeCourse(code: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val res = repository.deleteCourse(code)
                if (res.success) {
                    app.auditLogger.logAction(
                        sessionManager.username,
                        "Delete Course",
                        "Code: $code"
                    )
                    _statusMessage.value = "Course deleted successfully"
                    _courses.value = repository.getCourses()
                } else {
                    _statusMessage.value = res.message ?: "Failed to delete"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Delete failed: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // DEPARTMENTS CRUD
    fun saveDepartment(d: Department, isEdit: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val res = if (isEdit) repository.updateDepartment(d) else repository.addDepartment(d)
                if (res.success) {
                    app.auditLogger.logAction(
                        sessionManager.username,
                        if (isEdit) "Edit Department" else "Add Department",
                        "Code: ${d.DepartmentCode}, Name: ${d.DepartmentName}"
                    )
                    _statusMessage.value = "Department saved successfully"
                    _departments.value = repository.getDepartments()
                    onComplete()
                } else {
                    _statusMessage.value = res.message ?: "Failed to save department"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun removeDepartment(code: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val res = repository.deleteDepartment(code)
                if (res.success) {
                    app.auditLogger.logAction(
                        sessionManager.username,
                        "Delete Department",
                        "Code: $code"
                    )
                    _statusMessage.value = "Department removed"
                    _departments.value = repository.getDepartments()
                } else {
                    _statusMessage.value = res.message ?: "Failed to remove"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // BATCHES CRUD
    fun saveBatch(b: Batch, isEdit: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val res = if (isEdit) repository.updateBatch(b) else repository.addBatch(b)
                if (res.success) {
                    app.auditLogger.logAction(
                        sessionManager.username,
                        if (isEdit) "Edit Batch" else "Add Batch",
                        "Name: ${b.BatchName}, Year: ${b.AcademicYear}"
                    )
                    _statusMessage.value = "Batch saved successfully"
                    _batches.value = repository.getBatches()
                    onComplete()
                } else {
                    _statusMessage.value = res.message ?: "Failed to save batch"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun removeBatch(name: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val res = repository.deleteBatch(name)
                if (res.success) {
                    app.auditLogger.logAction(
                        sessionManager.username,
                        "Delete Batch",
                        "Name: $name"
                    )
                    _statusMessage.value = "Batch deleted"
                    _batches.value = repository.getBatches()
                } else {
                    _statusMessage.value = res.message ?: "Failed to delete"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // SEMESTERS CRUD
    fun saveSemester(s: Semester, isEdit: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val res = if (isEdit) repository.updateSemester(s) else repository.addSemester(s)
                if (res.success) {
                    app.auditLogger.logAction(
                        sessionManager.username,
                        if (isEdit) "Edit Semester" else "Add Semester",
                        "Semester: ${s.SemesterNo}, Course: ${s.Course}"
                    )
                    _statusMessage.value = "Semester saved successfully"
                    _semesters.value = repository.getSemesters()
                    onComplete()
                } else {
                    _statusMessage.value = res.message ?: "Failed to save semester"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun removeSemester(no: String, course: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val res = repository.deleteSemester(no, course)
                if (res.success) {
                    app.auditLogger.logAction(
                        sessionManager.username,
                        "Delete Semester",
                        "Semester: $no, Course: $course"
                    )
                    _statusMessage.value = "Semester deleted"
                    _semesters.value = repository.getSemesters()
                } else {
                    _statusMessage.value = res.message ?: "Failed to delete"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // FEE ASSIGNMENT
    fun assignFees(f: FeeAssignment, bulk: Boolean, bulkBatch: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val res = repository.assignFees(f, bulk, bulkBatch)
                if (res.success) {
                    app.auditLogger.logAction(
                        sessionManager.username,
                        "Assign Fees",
                        if (bulk) "Bulk Batch: $bulkBatch, Course: ${f.Course}" else "Single Student: ${f.StudentID}, Semester: ${f.Semester}"
                    )
                    _statusMessage.value = res.message ?: "Fees assigned successfully"
                    _assignedFees.value = repository.getAssignedFees()
                    _reportData.value = repository.getReports()
                    onComplete()
                } else {
                    _statusMessage.value = res.message ?: "Failed to assign"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // PAYMENT
    fun collectPayment(p: Payment, onComplete: (String, String) -> Unit) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val res = repository.addPayment(p)
                if (res.success) {
                    app.auditLogger.logAction(
                        sessionManager.username,
                        "Collect Payment",
                        "Student: ${p.StudentID}, FeeType: ${p.FeeType}, Amount: ${p.Amount}"
                    )
                    _statusMessage.value = "Payment collected successfully"
                    _payments.value = repository.getPayments()
                    _reportData.value = repository.getReports()
                    onComplete(res.paymentId ?: "PAY", res.receiptNumber ?: "REC")
                } else {
                    _statusMessage.value = res.message ?: "Payment failed"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun updatePayment(p: Payment, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val res = repository.updatePayment(p)
                _statusMessage.value = res.message ?: "Payment updated successfully"
                if (res.success) {
                    app.auditLogger.logAction(
                        sessionManager.username,
                        "Update Payment",
                        "Student: ${p.StudentID}, FeeType: ${p.FeeType}, Amount: ${p.Amount}"
                    )
                    _payments.value = repository.getPayments()
                    _reportData.value = repository.getReports()
                    onComplete()
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun deletePayment(paymentId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val res = repository.removePayment(paymentId)
                _statusMessage.value = res.message ?: "Payment deleted successfully"
                if (res.success) {
                    app.auditLogger.logAction(
                        sessionManager.username,
                        "Delete Payment",
                        "Payment ID: $paymentId"
                    )
                    _payments.value = repository.getPayments()
                    _reportData.value = repository.getReports()
                    onComplete()
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // CHANGE PASSWORD
    fun changePassword(newPass: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val res = repository.changePassword(sessionManager.username, sessionManager.role, newPass)
                if (res.success) {
                    app.auditLogger.logAction(
                        sessionManager.username,
                        "Change Password",
                        "Role: ${sessionManager.role}"
                    )
                    _statusMessage.value = "Password updated successfully!"
                    onComplete()
                } else {
                    _statusMessage.value = res.message ?: "Change failed"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // NOTIFICATIONS
    fun postNotification(title: String, msg: String, target: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val res = repository.addNotification(title, msg, target)
                if (res.success) {
                    app.auditLogger.logAction(
                        sessionManager.username,
                        "Broadcast Notification",
                        "Title: $title, Target: $target"
                    )
                    _statusMessage.value = "Broadcast sent successfully"
                    _notifications.value = repository.getNotifications()
                    onComplete()
                } else {
                    _statusMessage.value = res.message ?: "Broadcast failed"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // ATTENDANCE SAVE
    fun saveAttendance(a: Attendance, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val res = repository.saveAttendance(a)
                if (res.success) {
                    app.auditLogger.logAction(
                        sessionManager.username,
                        "Save Attendance",
                        "Student ID: ${a.StudentID}, Subject: ${a.Subject}, Attended: ${a.ClassesAttended}/${a.TotalClasses}"
                    )
                    _statusMessage.value = res.message ?: "Attendance saved successfully"
                    _attendance.value = repository.getAttendance()
                    onComplete()
                } else {
                    _statusMessage.value = res.message ?: "Failed to save attendance"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // ACADEMIC PERFORMANCE SAVE
    fun saveAcademicPerformance(ap: AcademicPerformance, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val res = repository.saveAcademicPerformance(ap)
                if (res.success) {
                    app.auditLogger.logAction(
                        sessionManager.username,
                        "Save Academic Performance",
                        "Student ID: ${ap.StudentID}, Subject: ${ap.Subject}, Grade: ${ap.Grade}, Marks: ${ap.Marks}"
                    )
                    _statusMessage.value = res.message ?: "Academic performance saved successfully"
                    _academicPerformance.value = repository.getAcademicPerformance()
                    onComplete()
                } else {
                    _statusMessage.value = res.message ?: "Failed to save performance"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
