package com.example.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Student(
    val StudentID: String = "",
    val RegNo: String = "",
    val Name: String = "",
    val Gender: String = "",
    val DOB: String = "",
    val Mobile: String = "",
    val Email: String = "",
    val Address: String = "",
    val Course: String = "",
    val Department: String = "",
    val Semester: String = "",
    val Batch: String = "",
    val JoiningDate: String = "",
    val Status: String = "Active",
    val Password: String = "stu123"
)

@JsonClass(generateAdapter = true)
data class Course(
    val CourseCode: String = "",
    val CourseName: String = "",
    val Duration: String = "",
    val TotalSemesters: String = "",
    val CourseFees: String = "",
    val Description: String = "",
    val Status: String = "Active"
)

@JsonClass(generateAdapter = true)
data class Department(
    val DepartmentCode: String = "",
    val DepartmentName: String = "",
    val HOD: String = "",
    val Status: String = "Active"
)

@JsonClass(generateAdapter = true)
data class Batch(
    val BatchName: String = "",
    val AcademicYear: String = "",
    val StartDate: String = "",
    val EndDate: String = "",
    val Status: String = "Active"
)

@JsonClass(generateAdapter = true)
data class Semester(
    val SemesterNo: String = "",
    val Course: String = "",
    val SemesterFees: String = "",
    val Subjects: String = "",
    val Status: String = "Active"
)

@JsonClass(generateAdapter = true)
data class FeeAssignment(
    val StudentID: String = "",
    val Course: String = "",
    val Semester: String = "",
    val AdmissionFee: String = "0",
    val TuitionFee: String = "0",
    val ExamFee: String = "0",
    val LibraryFee: String = "0",
    val HostelFee: String = "0",
    val TransportFee: String = "0",
    val Fine: String = "0",
    val Scholarship: String = "0",
    val Discount: String = "0",
    val TotalAmount: String = "0",
    val DueDate: String = "",
    val Remarks: String = ""
)

@JsonClass(generateAdapter = true)
data class Payment(
    val PaymentID: String = "",
    val ReceiptNumber: String = "",
    val StudentID: String = "",
    val Course: String = "",
    val Semester: String = "",
    val FeeType: String = "",
    val Amount: String = "0",
    val Fine: String = "0",
    val Discount: String = "0",
    val Balance: String = "0",
    val PaymentMode: String = "Cash",
    val TransactionNumber: String = "",
    val Date: String = "",
    val Remarks: String = ""
)

@JsonClass(generateAdapter = true)
data class User(
    val Username: String = "",
    val Password: String = "",
    val Role: String = "Student",
    val Permissions: String = "View"
)

@JsonClass(generateAdapter = true)
data class AuditLog(
    val Timestamp: String = "",
    val User: String = "",
    val Action: String = "",
    val Details: String = ""
)

@JsonClass(generateAdapter = true)
data class NotificationItem(
    val ID: String = "",
    val Title: String = "",
    val Message: String = "",
    val Date: String = "",
    val TargetGroup: String = "All"
)

@JsonClass(generateAdapter = true)
data class Attendance(
    val StudentID: String = "",
    val Semester: String = "",
    val Subject: String = "",
    val TotalClasses: String = "0",
    val ClassesAttended: String = "0",
    val Percentage: String = "0",
    val LastUpdated: String = ""
)

@JsonClass(generateAdapter = true)
data class AcademicPerformance(
    val StudentID: String = "",
    val Semester: String = "",
    val Subject: String = "",
    val Grade: String = "",
    val Marks: String = "0",
    val TotalMarks: String = "100",
    val Remarks: String = ""
)

// API Interaction Requests and Responses
@JsonClass(generateAdapter = true)
data class GenericRequest(
    val action: String,
    val username: String? = null,
    val role: String? = null,
    val password: String? = null,
    val newPassword: String? = null,
    val studentId: String? = null,
    val name: String? = null,
    val gender: String? = null,
    val dob: String? = null,
    val mobile: String? = null,
    val email: String? = null,
    val address: String? = null,
    val course: String? = null,
    val department: String? = null,
    val semester: String? = null,
    val batch: String? = null,
    val joiningDate: String? = null,
    val status: String? = null,
    
    // Course Code
    val courseCode: String? = null,
    val courseName: String? = null,
    val duration: String? = null,
    val totalSemesters: String? = null,
    val courseFees: String? = null,
    val description: String? = null,
    
    // Department
    val departmentCode: String? = null,
    val departmentName: String? = null,
    val hod: String? = null,
    
    // Batch
    val batchName: String? = null,
    val academicYear: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    
    // Semester
    val semesterNo: String? = null,
    val semesterFees: String? = null,
    val subjects: String? = null,
    
    // FeeAssignment
    val admissionFee: String? = null,
    val tuitionFee: String? = null,
    val examFee: String? = null,
    val libraryFee: String? = null,
    val hostelFee: String? = null,
    val transportFee: String? = null,
    val fine: String? = null,
    val scholarship: String? = null,
    val discount: String? = null,
    val totalAmount: String? = null,
    val dueDate: String? = null,
    val remarks: String? = null,
    val bulk: Boolean? = null,
    
    // Payment
    val paymentId: String? = null,
    val receiptNo: String? = null,
    val feeType: String? = null,
    val amount: String? = null,
    val balance: String? = null,
    val paymentMode: String? = null,
    val transactionNumber: String? = null,
    val date: String? = null,
    
    // Notification
    val title: String? = null,
    val message: String? = null,
    val targetGroup: String? = null,

    // Audit Logging
    val auditAction: String? = null,
    val details: String? = null,

    // Attendance & Academic Performance
    val subject: String? = null,
    val totalClasses: String? = null,
    val classesAttended: String? = null,
    val percentage: String? = null,
    val lastUpdated: String? = null,
    val grade: String? = null,
    val marks: String? = null,
    val totalMarks: String? = null
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val success: Boolean = false,
    val message: String? = null,
    val role: String? = null,
    val username: String? = null,
    val studentName: String? = null,
    val permissions: String? = null
)

@JsonClass(generateAdapter = true)
data class CommonResponse(
    val success: Boolean = false,
    val message: String? = null,
    val studentId: String? = null,
    val regNo: String? = null,
    val paymentId: String? = null,
    val receiptNumber: String? = null
)

@JsonClass(generateAdapter = true)
data class ReportResponse(
    val totalStudents: Int = 0,
    val totalFeesAssigned: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalPending: Double = 0.0,
    val todayCollection: Double = 0.0,
    val payments: List<Payment> = emptyList(),
    val assignments: List<FeeAssignment> = emptyList()
)
