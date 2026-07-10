package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.models.*
import com.example.data.session.SessionManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

class CollegeRepository(
    context: Context,
    sessionManager: SessionManager
) : BaseRepository(context, sessionManager) {
    private val tag = "CollegeRepository"

    // LOCAL MOCK STATE (Used if scriptUrl is empty or network fails)
    private val mockStudents = mutableListOf<Student>()
    private val mockCourses = mutableListOf<Course>()
    private val mockDepartments = mutableListOf<Department>()
    private val mockBatches = mutableListOf<Batch>()
    private val mockSemesters = mutableListOf<Semester>()
    private val mockFeeAssignments = mutableListOf<FeeAssignment>()
    private val mockPayments = mutableListOf<Payment>()
    private val mockUsers = mutableListOf<User>()
    private val mockNotifications = mutableListOf<NotificationItem>()
    private val mockAuditLogs = mutableListOf<AuditLog>()
    private val mockAttendance = mutableListOf<Attendance>()
    private val mockAcademicPerformance = mutableListOf<AcademicPerformance>()

    init {
        seedMockData()
    }

    private fun seedMockData() {
        // Seed Courses
        mockCourses.addAll(listOf(
            Course("CSE101", "B.Tech Computer Science", "4 Years", "8", "120000", "Focuses on Software Engineering and AI", "Active"),
            Course("ECE102", "B.Tech Electronics", "4 Years", "8", "110000", "Semiconductors and Communication Systems", "Active"),
            Course("MBA201", "MBA Finance", "2 Years", "4", "150000", "Business administration and financial models", "Active"),
            Course("BSC301", "B.Sc Mathematics", "3 Years", "6", "60000", "Pure and Applied Mathematics", "Active")
        ))

        // Seed Departments
        mockDepartments.addAll(listOf(
            Department("CS", "Computer Science & Engineering", "Dr. Alan Turing", "Active"),
            Department("EE", "Electrical & Electronics Engineering", "Dr. Nikola Tesla", "Active"),
            Department("MGMT", "School of Management Studies", "Dr. Peter Drucker", "Active"),
            Department("SCI", "Department of Basic Sciences", "Dr. Marie Curie", "Active")
        ))

        // Seed Batches
        mockBatches.addAll(listOf(
            Batch("Batch 2026", "2026-2030", "2026-08-01", "2030-06-30", "Active"),
            Batch("Batch 2027", "2027-2031", "2027-08-01", "2031-06-30", "Active")
        ))

        // Seed Semesters
        mockSemesters.addAll(listOf(
            Semester("Sem/Year 1", "CSE101", "60000", "Programming in C, Physics, Calculus, Communication Skills", "Active"),
            Semester("Sem/Year 2", "CSE101", "60000", "Data Structures, Chemistry, Linear Algebra, Environmental Sci", "Active"),
            Semester("Sem/Year 1", "MBA201", "75000", "Microeconomics, Accounting, Stats for Managers, Org Behavior", "Active")
        ))

        // Seed Students
        mockStudents.addAll(listOf(
            Student("STU2026001", "REG2026402", "Jane Doe", "Female", "2005-04-12", "9876543210", "jane.doe@college.edu", "123 Academic Block, New York", "CSE101", "CS", "Sem/Year 1", "Batch 2026", "2026-06-20", "Active", "jane123"),
            Student("STU2026002", "REG2026589", "John Smith", "Male", "2004-11-23", "8765432109", "john.smith@college.edu", "456 University Ave, Boston", "MBA201", "MGMT", "Sem/Year 1", "Batch 2026", "2026-06-21", "Active", "john123")
        ))

        // Seed Users
        mockUsers.addAll(listOf(
            User("admin", "admin123", "Admin", "Add,Edit,Delete,View"),
            User("STU2026001", "jane123", "Student", "View"),
            User("STU2026002", "john123", "Student", "View")
        ))

        // Seed Fee Assignments
        mockFeeAssignments.addAll(listOf(
            FeeAssignment("STU2026001", "CSE101", "Sem/Year 1", "5000", "45000", "3000", "2000", "5000", "0", "0", "5000", "0", "55000", "2026-09-15", "First semester dues"),
            FeeAssignment("STU2026002", "MBA201", "Sem/Year 1", "10000", "60000", "4000", "3000", "0", "3000", "0", "0", "10000", "70000", "2026-09-20", "Scholarship applied")
        ))

        // Seed Payments
        mockPayments.addAll(listOf(
            Payment("PAY171922384", "REC0001", "STU2026001", "CSE101", "Sem/Year 1", "Tuition Fee", "35000", "0", "0", "20000", "UPI", "TXN928374928", "2026-06-25", "Partial tution payment"),
            Payment("PAY171922395", "REC0002", "STU2026002", "MBA201", "Sem/Year 1", "Full Fee", "70000", "0", "0", "0", "Bank Transfer", "TXN102938475", "2026-06-26", "Full payment done")
        ))

        // Seed Notifications
        mockNotifications.addAll(listOf(
            NotificationItem("1", "Welcome to New Semester!", "The first semester classes will commence from 1st August. Please complete fee assignments.", "2026-06-25", "All"),
            NotificationItem("2", "Independence Day Holiday", "College will remain closed on 15th August for Independence Day celebrations.", "2026-06-26", "All")
        ))

        // Seed Audit Logs
        mockAuditLogs.add(AuditLog("2026-06-27T02:00:00Z", "System", "Initialization", "Database pre-seeded with sample records."))

        // Seed Attendance
        mockAttendance.addAll(listOf(
            Attendance("STU2026001", "Sem/Year 1", "Programming in C", "45", "40", "88.89", "2026-06-27"),
            Attendance("STU2026001", "Sem/Year 1", "Physics", "40", "35", "87.50", "2026-06-27"),
            Attendance("STU2026001", "Sem/Year 1", "Calculus", "38", "30", "78.95", "2026-06-27"),
            Attendance("STU2026002", "Sem/Year 1", "Microeconomics", "30", "28", "93.33", "2026-06-27")
        ))

        // Seed Academic Performance
        mockAcademicPerformance.addAll(listOf(
            AcademicPerformance("STU2026001", "Sem/Year 1", "Programming in C", "A", "85", "100", "Excellent progress"),
            AcademicPerformance("STU2026001", "Sem/Year 1", "Physics", "B+", "78", "100", "Good"),
            AcademicPerformance("STU2026001", "Sem/Year 1", "Calculus", "B", "72", "100", "Needs improvement in integration"),
            AcademicPerformance("STU2026002", "Sem/Year 1", "Microeconomics", "O", "95", "100", "Outstanding")
        ))
    }

    // GENERIC POST API METHOD
    private suspend fun <T> makePostCall(action: String, request: GenericRequest, responseType: Class<T>): T? {
        val url = sessionManager.scriptUrl
        if (url.isEmpty()) {
            Log.d(tag, "No Apps Script URL configured, running in Offline Mock mode.")
            return null
        }
        return withContext(Dispatchers.IO) {
            try {
                // Add action to request parameter dynamically
                val fullRequest = request.copy(action = action)
                val jsonAdapter = moshi.adapter(GenericRequest::class.java)
                val jsonString = jsonAdapter.toJson(fullRequest)
                
                val body = jsonString.toRequestBody("application/json".toMediaTypeOrNull())
                val response = apiService.executeAction(url, body)
                
                if (response.isSuccessful) {
                    val responseBodyString = response.body()?.string() ?: ""
                    Log.d(tag, "Response for $action: $responseBodyString")
                    
                    // Safe Check: If we expect an Array but received a JSON Object (usually an error or error response from the backend),
                    // gracefully return null to trigger the local fallback instead of throwing JsonDataException
                    if (responseBodyString.trim().startsWith("{") && responseType.isArray) {
                        Log.w(tag, "Expected JSON Array for action $action but received JSON Object. Response: $responseBodyString")
                        return@withContext null
                    }

                    val responseAdapter = moshi.adapter(responseType)
                    responseAdapter.fromJson(responseBodyString)
                } else {
                    Log.e(tag, "HTTP error for $action: ${response.code()}")
                    null
                }
            } catch (e: Exception) {
                Log.e(tag, "Connection error for $action: ${e.message}", e)
                null
            }
        }
    }

    // LOGIN
    suspend fun login(p: GenericRequest): LoginResponse {
        val remote = makePostCall("login", p, LoginResponse::class.java)
        if (remote != null) return remote

        val url = sessionManager.scriptUrl
        if (url.isEmpty()) {
            return LoginResponse(false, "Google Sheets connection URL is not configured. Please configure it in Settings first.")
        }
        return LoginResponse(false, "Connection error: Failed to reach Google Sheets API.")
    }

    // STUDENTS CRUD
    suspend fun getStudents(): List<Student> {
        val remote = makePostCall("getStudents", GenericRequest("getStudents"), Array<Student>::class.java)
        if (remote != null) return remote.toList()
        return emptyList()
    }

    suspend fun addStudent(s: Student): CommonResponse {
        val request = GenericRequest(
            action = "addStudent",
            name = s.Name,
            gender = s.Gender,
            dob = s.DOB,
            mobile = s.Mobile,
            email = s.Email,
            address = s.Address,
            course = s.Course,
            department = s.Department,
            semester = s.Semester,
            batch = s.Batch,
            status = s.Status,
            password = s.Password
        )
        val remote = makePostCall("addStudent", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to add student to Google Sheets.")
    }

    suspend fun updateStudent(s: Student): CommonResponse {
        val request = GenericRequest(
            action = "updateStudent",
            studentId = s.StudentID,
            name = s.Name,
            gender = s.Gender,
            dob = s.DOB,
            mobile = s.Mobile,
            email = s.Email,
            address = s.Address,
            course = s.Course,
            department = s.Department,
            semester = s.Semester,
            batch = s.Batch,
            status = s.Status
        )
        val remote = makePostCall("updateStudent", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to update student in Google Sheets.")
    }

    suspend fun deleteStudent(studentId: String): CommonResponse {
        val request = GenericRequest(action = "deleteStudent", studentId = studentId)
        val remote = makePostCall("deleteStudent", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to delete student from Google Sheets.")
    }

    // COURSES CRUD
    suspend fun getCourses(): List<Course> {
        val remote = makePostCall("getCourses", GenericRequest("getCourses"), Array<Course>::class.java)
        if (remote != null) return remote.toList()
        return emptyList()
    }

    suspend fun addCourse(c: Course): CommonResponse {
        val request = GenericRequest(
            action = "addCourse",
            courseCode = c.CourseCode,
            courseName = c.CourseName,
            duration = c.Duration,
            totalSemesters = c.TotalSemesters,
            courseFees = c.CourseFees,
            description = c.Description,
            status = c.Status
        )
        val remote = makePostCall("addCourse", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to add course to Google Sheets.")
    }

    suspend fun updateCourse(c: Course): CommonResponse {
        val request = GenericRequest(
            action = "updateCourse",
            courseCode = c.CourseCode,
            courseName = c.CourseName,
            duration = c.Duration,
            totalSemesters = c.TotalSemesters,
            courseFees = c.CourseFees,
            description = c.Description,
            status = c.Status
        )
        val remote = makePostCall("updateCourse", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to update course in Google Sheets.")
    }

    suspend fun deleteCourse(courseCode: String): CommonResponse {
        val request = GenericRequest(action = "deleteCourse", courseCode = courseCode)
        val remote = makePostCall("deleteCourse", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to delete course from Google Sheets.")
    }

    // DEPARTMENTS CRUD
    suspend fun getDepartments(): List<Department> {
        val remote = makePostCall("getDepartments", GenericRequest("getDepartments"), Array<Department>::class.java)
        if (remote != null) return remote.toList()
        return emptyList()
    }

    suspend fun addDepartment(d: Department): CommonResponse {
        val request = GenericRequest(
            action = "addDepartment",
            departmentCode = d.DepartmentCode,
            departmentName = d.DepartmentName,
            hod = d.HOD,
            status = d.Status
        )
        val remote = makePostCall("addDepartment", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to add department to Google Sheets.")
    }

    suspend fun updateDepartment(d: Department): CommonResponse {
        val request = GenericRequest(
            action = "updateDepartment",
            departmentCode = d.DepartmentCode,
            departmentName = d.DepartmentName,
            hod = d.HOD,
            status = d.Status
        )
        val remote = makePostCall("updateDepartment", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to update department in Google Sheets.")
    }

    suspend fun deleteDepartment(departmentCode: String): CommonResponse {
        val request = GenericRequest(action = "deleteDepartment", departmentCode = departmentCode)
        val remote = makePostCall("deleteDepartment", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to delete department from Google Sheets.")
    }

    // BATCHES CRUD
    suspend fun getBatches(): List<Batch> {
        val remote = makePostCall("getBatches", GenericRequest("getBatches"), Array<Batch>::class.java)
        if (remote != null) return remote.toList()
        return emptyList()
    }

    suspend fun addBatch(b: Batch): CommonResponse {
        val request = GenericRequest(
            action = "addBatch",
            batchName = b.BatchName,
            academicYear = b.AcademicYear,
            startDate = b.StartDate,
            endDate = b.EndDate,
            status = b.Status
        )
        val remote = makePostCall("addBatch", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to add batch to Google Sheets.")
    }

    suspend fun updateBatch(b: Batch): CommonResponse {
        val request = GenericRequest(
            action = "updateBatch",
            batchName = b.BatchName,
            academicYear = b.AcademicYear,
            startDate = b.StartDate,
            endDate = b.EndDate,
            status = b.Status
        )
        val remote = makePostCall("updateBatch", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to update batch in Google Sheets.")
    }

    suspend fun deleteBatch(batchName: String): CommonResponse {
        val request = GenericRequest(action = "deleteBatch", batchName = batchName)
        val remote = makePostCall("deleteBatch", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to delete batch from Google Sheets.")
    }

    // SEMESTERS CRUD
    suspend fun getSemesters(): List<Semester> {
        val remote = makePostCall("getSemesters", GenericRequest("getSemesters"), Array<Semester>::class.java)
        if (remote != null) return remote.toList()
        return emptyList()
    }

    suspend fun addSemester(s: Semester): CommonResponse {
        val request = GenericRequest(
            action = "addSemester",
            semesterNo = s.SemesterNo,
            course = s.Course,
            semesterFees = s.SemesterFees,
            subjects = s.Subjects,
            status = s.Status
        )
        val remote = makePostCall("addSemester", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to add semester to Google Sheets.")
    }

    suspend fun updateSemester(s: Semester): CommonResponse {
        val request = GenericRequest(
            action = "updateSemester",
            semesterNo = s.SemesterNo,
            course = s.Course,
            semesterFees = s.SemesterFees,
            subjects = s.Subjects,
            status = s.Status
        )
        val remote = makePostCall("updateSemester", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to update semester in Google Sheets.")
    }

    suspend fun deleteSemester(semesterNo: String, course: String): CommonResponse {
        val request = GenericRequest(action = "deleteSemester", semesterNo = semesterNo, course = course)
        val remote = makePostCall("deleteSemester", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to delete semester from Google Sheets.")
    }

    // FEE ASSIGNMENT
    suspend fun getAssignedFees(): List<FeeAssignment> {
        val remote = makePostCall("getAssignedFees", GenericRequest("getAssignedFees"), Array<FeeAssignment>::class.java)
        if (remote != null) return remote.toList()
        return emptyList()
    }

    suspend fun assignFees(f: FeeAssignment, bulk: Boolean = false, bulkBatch: String = ""): CommonResponse {
        val request = GenericRequest(
            action = "assignFees",
            studentId = f.StudentID,
            course = f.Course,
            semester = f.Semester,
            admissionFee = f.AdmissionFee,
            tuitionFee = f.TuitionFee,
            examFee = f.ExamFee,
            libraryFee = f.LibraryFee,
            hostelFee = f.HostelFee,
            transportFee = f.TransportFee,
            fine = f.Fine,
            scholarship = f.Scholarship,
            discount = f.Discount,
            totalAmount = f.TotalAmount,
            dueDate = f.DueDate,
            remarks = f.Remarks,
            bulk = bulk,
            batch = bulkBatch
        )
        val remote = makePostCall("assignFees", request, CommonResponse::class.java)
        if (remote != null) return remote

        if (bulk) {
            // Apply assignments to matching mock students
            var count = 0
            mockStudents.forEach { stu ->
                val matchCourse = f.Course.isEmpty() || stu.Course == f.Course
                val matchSem = f.Semester.isEmpty() || stu.Semester == f.Semester
                val matchBat = bulkBatch.isEmpty() || stu.Batch == bulkBatch
                
                if (matchCourse && matchSem && matchBat) {
                    mockFeeAssignments.add(f.copy(StudentID = stu.StudentID))
                    count++
                }
            }
            logAuditLocal("Admin", "Bulk Fee Assignment", "Assigned fees in bulk to $count students")
            return CommonResponse(true, "Bulk assigned fees to $count students successfully")
        } else {
            mockFeeAssignments.add(f)
            logAuditLocal("Admin", "Fee Assignment", "Assigned fees to ${f.StudentID}")
            return CommonResponse(true, "Fees assigned successfully")
        }
    }

    // PAYMENTS
    suspend fun getPayments(): List<Payment> {
        val remote = makePostCall("getPayments", GenericRequest("getPayments"), Array<Payment>::class.java)
        if (remote != null) return remote.toList()
        return emptyList()
    }

    suspend fun addPayment(p: Payment): CommonResponse {
        val request = GenericRequest(
            action = "addPayment",
            studentId = p.StudentID,
            course = p.Course,
            semester = p.Semester,
            feeType = p.FeeType,
            amount = p.Amount,
            fine = p.Fine,
            discount = p.Discount,
            balance = p.Balance,
            paymentMode = p.PaymentMode,
            transactionNumber = p.TransactionNumber,
            date = p.Date,
            remarks = p.Remarks
        )
        val remote = makePostCall("addPayment", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to receive payment in Google Sheets.")
    }

    suspend fun updatePayment(p: Payment): CommonResponse {
        val request = GenericRequest(
            action = "updatePayment",
            paymentId = p.PaymentID,
            receiptNo = p.ReceiptNumber,
            studentId = p.StudentID,
            course = p.Course,
            semester = p.Semester,
            feeType = p.FeeType,
            amount = p.Amount,
            fine = p.Fine,
            discount = p.Discount,
            balance = p.Balance,
            paymentMode = p.PaymentMode,
            transactionNumber = p.TransactionNumber,
            date = p.Date,
            remarks = p.Remarks
        )
        val remote = makePostCall("updatePayment", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to update payment in Google Sheets.")
    }

    suspend fun removePayment(paymentId: String): CommonResponse {
        val request = GenericRequest(
            action = "deletePayment",
            paymentId = paymentId
        )
        val remote = makePostCall("deletePayment", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to delete payment from Google Sheets.")
    }

    // REPORTS / DASHBOARD DATA
    suspend fun getReports(): ReportResponse {
        val remote = makePostCall("getReports", GenericRequest("getReports"), ReportResponse::class.java)
        if (remote != null) return remote
        return ReportResponse(
            totalStudents = 0,
            totalFeesAssigned = 0.0,
            totalPaid = 0.0,
            totalPending = 0.0,
            todayCollection = 0.0,
            payments = emptyList(),
            assignments = emptyList()
        )
    }

    // CHANGE PASSWORD
    suspend fun changePassword(username: String, role: String, newPass: String): CommonResponse {
        val request = GenericRequest(
            action = "changePassword",
            username = username,
            role = role,
            newPassword = newPass
        )
        val remote = makePostCall("changePassword", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to change password in Google Sheets.")
    }

    // AUDIT LOGS
    fun getAuditLogs(): List<AuditLog> {
        return emptyList()
    }

    // NOTIFICATIONS
    suspend fun getNotifications(): List<NotificationItem> {
        val remote = makePostCall("getNotifications", GenericRequest("getNotifications"), Array<NotificationItem>::class.java)
        if (remote != null) return remote.toList()
        return emptyList()
    }

    suspend fun getAttendance(): List<Attendance> {
        val remote = makePostCall("getAttendance", GenericRequest("getAttendance"), Array<Attendance>::class.java)
        if (remote != null) return remote.toList()
        return emptyList()
    }

    suspend fun getAcademicPerformance(): List<AcademicPerformance> {
        val remote = makePostCall("getAcademicPerformance", GenericRequest("getAcademicPerformance"), Array<AcademicPerformance>::class.java)
        if (remote != null) return remote.toList()
        return emptyList()
    }

    suspend fun addNotification(title: String, message: String, targetGroup: String): CommonResponse {
        val request = GenericRequest(
            action = "addNotification",
            title = title,
            message = message,
            targetGroup = targetGroup
        )
        val remote = makePostCall("addNotification", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to send broadcast notification.")
    }

    suspend fun saveAttendance(a: Attendance): CommonResponse {
        val request = GenericRequest(
            action = "saveAttendance",
            studentId = a.StudentID,
            semester = a.Semester,
            subject = a.Subject,
            totalClasses = a.TotalClasses,
            classesAttended = a.ClassesAttended,
            percentage = a.Percentage,
            lastUpdated = a.LastUpdated
        )
        val remote = makePostCall("saveAttendance", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to save attendance to Google Sheets.")
    }

    suspend fun saveAcademicPerformance(ap: AcademicPerformance): CommonResponse {
        val request = GenericRequest(
            action = "saveAcademicPerformance",
            studentId = ap.StudentID,
            semester = ap.Semester,
            subject = ap.Subject,
            grade = ap.Grade,
            marks = ap.Marks,
            totalMarks = ap.TotalMarks,
            remarks = ap.Remarks
        )
        val remote = makePostCall("saveAcademicPerformance", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to save academic performance to Google Sheets.")
    }

    suspend fun logAuditRemote(user: String, action: String, details: String): CommonResponse {
        val request = GenericRequest(
            action = "logAudit",
            username = user,
            auditAction = action,
            details = details
        )
        val remote = makePostCall("logAudit", request, CommonResponse::class.java)
        if (remote != null) return remote
        return CommonResponse(false, "Connection error: Failed to log audit event to Google Sheets.")
    }

    private fun logAuditLocal(user: String, action: String, details: String) {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).format(java.util.Date())
        mockAuditLogs.add(AuditLog(todayStr, user, action, details))
    }
}
