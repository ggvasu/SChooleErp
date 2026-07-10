/**
 * Google Apps Script Backend for College ERP System
 * This script connects your Android Application to Google Sheets.
 * Deploy this script as a "Web App" in Google Apps Script editor.
 * Set access to "Anyone, even anonymous" and copy the Web App URL into the App's settings!
 */

const SPREADSHEET_ID = "YOUR_SPREADSHEET_ID_HERE"; // Or leaves empty to use active spreadsheet

function getSpreadsheet() {
  if (SPREADSHEET_ID && SPREADSHEET_ID !== "YOUR_SPREADSHEET_ID_HERE") {
    return SpreadsheetApp.openById(SPREADSHEET_ID);
  }
  return SpreadsheetApp.getActiveSpreadsheet();
}

/**
 * Initialize Sheets if they don't exist
 */
function setupDatabase() {
  const ss = getSpreadsheet();
  const sheets = [
    { name: "Students", headers: ["StudentID", "RegNo", "Name", "Gender", "DOB", "Mobile", "Email", "Address", "Course", "Department", "Semester", "Batch", "JoiningDate", "Status", "Password"] },
    { name: "Courses", headers: ["CourseCode", "CourseName", "Duration", "TotalSemesters", "CourseFees", "Description", "Status"] },
    { name: "Departments", headers: ["DepartmentCode", "DepartmentName", "HOD", "Status"] },
    { name: "Batches", headers: ["BatchName", "AcademicYear", "StartDate", "EndDate", "Status"] },
    { name: "Semesters", headers: ["SemesterNo", "Course", "SemesterFees", "Subjects", "Status"] },
    { name: "FeeAssignments", headers: ["StudentID", "Course", "Semester", "AdmissionFee", "TuitionFee", "ExamFee", "LibraryFee", "HostelFee", "TransportFee", "Fine", "Scholarship", "Discount", "TotalAmount", "DueDate", "Remarks"] },
    { name: "Payments", headers: ["PaymentID", "ReceiptNumber", "StudentID", "Course", "Semester", "FeeType", "Amount", "Fine", "Discount", "Balance", "PaymentMode", "TransactionNumber", "Date", "Remarks"] },
    { name: "Users", headers: ["Username", "Password", "Role", "Permissions"] },
    { name: "Settings", headers: ["Key", "Value"] },
    { name: "AuditLogs", headers: ["Timestamp", "User", "Action", "Details"] },
    { name: "Notifications", headers: ["ID", "Title", "Message", "Date", "TargetGroup"] },
    { name: "Attendance", headers: ["StudentID", "Semester", "Subject", "TotalClasses", "ClassesAttended", "Percentage", "LastUpdated"] },
    { name: "AcademicPerformance", headers: ["StudentID", "Semester", "Subject", "Grade", "Marks", "TotalMarks", "Remarks"] }
  ];

  sheets.forEach(s => {
    let sheet = ss.getSheetByName(s.name);
    if (!sheet) {
      sheet = ss.insertSheet(s.name);
      sheet.appendRow(s.headers);
    }
  });

  // Seed default Admin if empty
  const userSheet = ss.getSheetByName("Users");
  if (userSheet.getLastRow() <= 1) {
    userSheet.appendRow(["admin", "admin123", "Admin", "Add,Edit,Delete,View"]);
  }

  return "Setup completed successfully!";
}

function doPost(e) {
  try {
    const params = JSON.parse(e.postData.contents);
    const action = params.action;
    
    // Log active actions
    logAudit(params.username || "System", action, JSON.stringify(params));

    switch (action) {
      case "login":
        return jsonResponse(handleLogin(params));
      case "getStudents":
        return jsonResponse(getData("Students"));
      case "addStudent":
        return jsonResponse(addStudent(params));
      case "updateStudent":
        return jsonResponse(updateStudent(params));
      case "deleteStudent":
        return jsonResponse(deleteData("Students", "StudentID", params.studentId));
      case "getCourses":
        return jsonResponse(getData("Courses"));
      case "addCourse":
        return jsonResponse(addRecord("Courses", params));
      case "updateCourse":
        return jsonResponse(updateRecord("Courses", "CourseCode", params.courseCode, params));
      case "deleteCourse":
        return jsonResponse(deleteData("Courses", "CourseCode", params.courseCode));
      case "getDepartments":
        return jsonResponse(getData("Departments"));
      case "addDepartment":
        return jsonResponse(addRecord("Departments", params));
      case "updateDepartment":
        return jsonResponse(updateRecord("Departments", "DepartmentCode", params.departmentCode, params));
      case "deleteDepartment":
        return jsonResponse(deleteData("Departments", "DepartmentCode", params.departmentCode));
      case "getBatches":
        return jsonResponse(getData("Batches"));
      case "addBatch":
        return jsonResponse(addRecord("Batches", params));
      case "updateBatch":
        return jsonResponse(updateRecord("Batches", "BatchName", params.batchName, params));
      case "deleteBatch":
        return jsonResponse(deleteData("Batches", "BatchName", params.batchName));
      case "getSemesters":
        return jsonResponse(getData("Semesters"));
      case "addSemester":
        return jsonResponse(addRecord("Semesters", params));
      case "updateSemester":
        return jsonResponse(updateRecord("Semesters", "SemesterNo", params.semesterNo, params)); // Complicated primary keys could be composite but simple works
      case "deleteSemester":
        return jsonResponse(deleteData("Semesters", "SemesterNo", params.semesterNo));
      case "getAssignedFees":
        return jsonResponse(getData("FeeAssignments"));
      case "assignFees":
        return jsonResponse(assignFees(params));
      case "getPayments":
        return jsonResponse(getData("Payments"));
      case "addPayment":
        return jsonResponse(addPayment(params));
      case "changePassword":
        return jsonResponse(changePassword(params));
      case "getReports":
        return jsonResponse(generateReports());
      case "getNotifications":
        return jsonResponse(getData("Notifications"));
      case "addNotification":
        return jsonResponse(addRecord("Notifications", params));
      case "getAttendance":
        return jsonResponse(getData("Attendance"));
      case "saveAttendance":
        return jsonResponse(saveAttendance(params));
      case "getAcademicPerformance":
        return jsonResponse(getData("AcademicPerformance"));
      case "saveAcademicPerformance":
        return jsonResponse(saveAcademicPerformance(params));
      case "logAudit":
        logAudit(params.username || "System", params.auditAction || "Custom", params.details || "");
        return jsonResponse({ success: true, message: "Audit logged successfully" });
      default:
        return jsonResponse({ success: false, message: "Invalid action: " + action });
    }
  } catch (error) {
    return jsonResponse({ success: false, message: "Server Error: " + error.toString() });
  }
}

function handleLogin(p) {
  const ss = getSpreadsheet();
  
  if (p.role === "Admin") {
    const sheet = ss.getSheetByName("Users");
    const data = sheet.getDataRange().getValues();
    for (let i = 1; i < data.length; i++) {
      if (data[i][0] === p.username && data[i][1] === p.password) {
        return { 
          success: true, 
          role: "Admin", 
          username: data[i][0], 
          permissions: data[i][3] || "View"
        };
      }
    }
  } else if (p.role === "Student") {
    const sheet = ss.getSheetByName("Students");
    const data = sheet.getDataRange().getValues();
    for (let i = 1; i < data.length; i++) {
      if (data[i][0] === p.username && data[i][14] === p.password) {
        return { 
          success: true, 
          role: "Student", 
          username: data[i][0],
          studentName: data[i][2],
          permissions: "View"
        };
      }
    }
  }
  return { success: false, message: "Invalid credentials" };
}

function getData(sheetName) {
  const ss = getSpreadsheet();
  let sheet = ss.getSheetByName(sheetName);
  if (!sheet) {
    try {
      sheet = ss.insertSheet(sheetName);
      const defaultHeaders = {
        "Students": ["StudentID", "RegNo", "Name", "Gender", "DOB", "Mobile", "Email", "Address", "Course", "Department", "Semester", "Batch", "JoiningDate", "Status", "Password"],
        "Courses": ["CourseCode", "CourseName", "Duration", "TotalSemesters", "CourseFees", "Description", "Status"],
        "Departments": ["DepartmentCode", "DepartmentName", "HOD", "Status"],
        "Batches": ["BatchName", "AcademicYear", "StartDate", "EndDate", "Status"],
        "Semesters": ["SemesterNo", "Course", "SemesterFees", "Subjects", "Status"],
        "FeeAssignments": ["StudentID", "Course", "Semester", "AdmissionFee", "TuitionFee", "ExamFee", "LibraryFee", "HostelFee", "TransportFee", "Fine", "Scholarship", "Discount", "TotalAmount", "DueDate", "Remarks"],
        "Payments": ["PaymentID", "ReceiptNumber", "StudentID", "Course", "Semester", "FeeType", "Amount", "Fine", "Discount", "Balance", "PaymentMode", "TransactionNumber", "Date", "Remarks"],
        "Users": ["Username", "Password", "Role", "Permissions"],
        "Settings": ["Key", "Value"],
        "AuditLogs": ["Timestamp", "User", "Action", "Details"],
        "Notifications": ["ID", "Title", "Message", "Date", "TargetGroup"],
        "Attendance": ["StudentID", "Semester", "Subject", "TotalClasses", "ClassesAttended", "Percentage", "LastUpdated"],
        "AcademicPerformance": ["StudentID", "Semester", "Subject", "Grade", "Marks", "TotalMarks", "Remarks"]
      };
      if (defaultHeaders[sheetName]) {
        sheet.appendRow(defaultHeaders[sheetName]);
      }
    } catch (e) {
      return [];
    }
  }
  if (sheet.getLastRow() === 0) return [];
  const data = sheet.getDataRange().getValues();
  const headers = data[0];
  const list = [];
  for (let i = 1; i < data.length; i++) {
    const obj = {};
    for (let j = 0; j < headers.length; j++) {
      obj[headers[j]] = data[i][j];
    }
    list.push(obj);
  }
  return list;
}

function addStudent(p) {
  const ss = getSpreadsheet();
  const sheet = ss.getSheetByName("Students");
  const data = sheet.getDataRange().getValues();
  
  // Auto Generate Student ID (e.g. STU2026001)
  const count = data.length;
  const year = new Date().getFullYear();
  const studentId = "STU" + year + String(count).padStart(3, "0");
  const regNo = "REG" + year + Math.floor(1000 + Math.random() * 9000);

  const newRow = [
    studentId,
    regNo,
    p.name,
    p.gender,
    p.dob,
    p.mobile,
    p.email,
    p.address,
    p.course,
    p.department,
    p.semester,
    p.batch,
    p.joiningDate || new Date().toISOString().split('T')[0],
    p.status || "Active",
    p.password || "stu123" // Default password
  ];
  
  sheet.appendRow(newRow);
  return { success: true, studentId: studentId, regNo: regNo, message: "Student added successfully" };
}

function updateStudent(p) {
  const ss = getSpreadsheet();
  const sheet = ss.getSheetByName("Students");
  const data = sheet.getDataRange().getValues();
  const headers = data[0];
  const idCol = headers.indexOf("StudentID");

  for (let i = 1; i < data.length; i++) {
    if (data[i][idCol] === p.studentId) {
      // Found, update details
      const row = i + 1;
      sheet.getRange(row, headers.indexOf("Name") + 1).setValue(p.name);
      sheet.getRange(row, headers.indexOf("Gender") + 1).setValue(p.gender);
      sheet.getRange(row, headers.indexOf("DOB") + 1).setValue(p.dob);
      sheet.getRange(row, headers.indexOf("Mobile") + 1).setValue(p.mobile);
      sheet.getRange(row, headers.indexOf("Email") + 1).setValue(p.email);
      sheet.getRange(row, headers.indexOf("Address") + 1).setValue(p.address);
      sheet.getRange(row, headers.indexOf("Course") + 1).setValue(p.course);
      sheet.getRange(row, headers.indexOf("Department") + 1).setValue(p.department);
      sheet.getRange(row, headers.indexOf("Semester") + 1).setValue(p.semester);
      sheet.getRange(row, headers.indexOf("Batch") + 1).setValue(p.batch);
      sheet.getRange(row, headers.indexOf("Status") + 1).setValue(p.status);
      return { success: true, message: "Student updated successfully" };
    }
  }
  return { success: false, message: "Student not found" };
}

function assignFees(p) {
  const ss = getSpreadsheet();
  const sheet = ss.getSheetByName("FeeAssignments");
  
  // Custom single or bulk assign based on params
  if (p.bulk === true) {
    // Find all matching students
    const studentSheet = ss.getSheetByName("Students");
    const stuData = studentSheet.getDataRange().getValues();
    let assignedCount = 0;
    
    for (let i = 1; i < stuData.length; i++) {
      const matchCourse = !p.course || stuData[i][8] === p.course;
      const matchSemester = !p.semester || stuData[i][10] === p.semester;
      const matchBatch = !p.batch || stuData[i][11] === p.batch;
      
      if (matchCourse && matchSemester && matchBatch) {
        const studentId = stuData[i][0];
        sheet.appendRow([
          studentId, p.course, p.semester,
          p.admissionFee, p.tuitionFee, p.examFee, p.libraryFee, p.hostelFee, p.transportFee,
          p.fine || 0, p.scholarship || 0, p.discount || 0, p.totalAmount, p.dueDate, p.remarks || "Bulk Assigned"
        ]);
        assignedCount++;
      }
    }
    return { success: true, message: "Bulk assigned fees to " + assignedCount + " students" };
  } else {
    sheet.appendRow([
      p.studentId, p.course, p.semester,
      p.admissionFee, p.tuitionFee, p.examFee, p.libraryFee, p.hostelFee, p.transportFee,
      p.fine || 0, p.scholarship || 0, p.discount || 0, p.totalAmount, p.dueDate, p.remarks || ""
    ]);
    return { success: true, message: "Fees assigned successfully" };
  }
}

function addPayment(p) {
  const ss = getSpreadsheet();
  const sheet = ss.getSheetByName("Payments");
  const data = sheet.getDataRange().getValues();

  // Generate Payment ID & Receipt Number
  const payId = "PAY" + Date.now();
  const receiptNo = "REC" + String(data.length).padStart(4, "0");

  sheet.appendRow([
    payId, receiptNo, p.studentId, p.course, p.semester, p.feeType,
    p.amount, p.fine || 0, p.discount || 0, p.balance || 0, p.paymentMode,
    p.transactionNumber || "CASH", p.date || new Date().toISOString().split('T')[0], p.remarks || ""
  ]);

  return { success: true, paymentId: payId, receiptNumber: receiptNo, message: "Payment added successfully" };
}

function changePassword(p) {
  const ss = getSpreadsheet();
  if (p.role === "Admin") {
    const sheet = ss.getSheetByName("Users");
    const data = sheet.getDataRange().getValues();
    for (let i = 1; i < data.length; i++) {
      if (data[i][0] === p.username) {
        sheet.getRange(i + 1, 2).setValue(p.newPassword);
        return { success: true, message: "Password updated successfully" };
      }
    }
  } else {
    const sheet = ss.getSheetByName("Students");
    const data = sheet.getDataRange().getValues();
    for (let i = 1; i < data.length; i++) {
      if (data[i][0] === p.username) {
        sheet.getRange(i + 1, 15).setValue(p.newPassword);
        return { success: true, message: "Password updated successfully" };
      }
    }
  }
  return { success: false, message: "User not found" };
}

function addRecord(sheetName, p) {
  const ss = getSpreadsheet();
  const sheet = ss.getSheetByName(sheetName);
  const headers = sheet.getDataRange().getValues()[0];
  const row = [];
  headers.forEach(h => {
    row.push(p[h.charAt(0).toLowerCase() + h.slice(1)] || "");
  });
  sheet.appendRow(row);
  return { success: true, message: "Record added to " + sheetName };
}

function updateRecord(sheetName, keyName, keyValue, p) {
  const ss = getSpreadsheet();
  const sheet = ss.getSheetByName(sheetName);
  const data = sheet.getDataRange().getValues();
  const headers = data[0];
  const keyCol = headers.indexOf(keyName);

  for (let i = 1; i < data.length; i++) {
    if (String(data[i][keyCol]) === String(keyValue)) {
      const row = i + 1;
      headers.forEach((h, colIndex) => {
        const paramName = h.charAt(0).toLowerCase() + h.slice(1);
        if (p[paramName] !== undefined) {
          sheet.getRange(row, colIndex + 1).setValue(p[paramName]);
        }
      });
      return { success: true, message: "Record updated successfully" };
    }
  }
  return { success: false, message: "Record not found" };
}

function deleteData(sheetName, keyName, keyValue) {
  const ss = getSpreadsheet();
  const sheet = ss.getSheetByName(sheetName);
  const data = sheet.getDataRange().getValues();
  const headers = data[0];
  const keyCol = headers.indexOf(keyName);

  for (let i = 1; i < data.length; i++) {
    if (String(data[i][keyCol]) === String(keyValue)) {
      sheet.deleteRow(i + 1);
      return { success: true, message: "Record deleted successfully" };
    }
  }
  return { success: false, message: "Record not found" };
}

function generateReports() {
  const payments = getData("Payments");
  const feeAssignments = getData("FeeAssignments");
  const students = getData("Students");

  let totalCollected = 0;
  let todayCollected = 0;
  const todayStr = new Date().toISOString().split('T')[0];

  payments.forEach(p => {
    totalCollected += parseFloat(p.Amount || 0);
    if (p.Date === todayStr) {
      todayCollected += parseFloat(p.Amount || 0);
    }
  });

  let totalAssigned = 0;
  feeAssignments.forEach(f => {
    totalAssigned += parseFloat(f.TotalAmount || 0);
  });

  const pendingFees = totalAssigned - totalCollected;

  return {
    totalStudents: students.length,
    totalFeesAssigned: totalAssigned,
    totalPaid: totalCollected,
    totalPending: pendingFees > 0 ? pendingFees : 0,
    todayCollection: todayCollected,
    payments: payments,
    assignments: feeAssignments
  };
}

function saveAttendance(p) {
  const ss = getSpreadsheet();
  const sheet = ss.getSheetByName("Attendance");
  const data = sheet.getDataRange().getValues();
  const headers = data[0];
  
  const studentCol = headers.indexOf("StudentID");
  const semCol = headers.indexOf("Semester");
  const subjectCol = headers.indexOf("Subject");
  
  // Try to find existing record
  for (let i = 1; i < data.length; i++) {
    if (String(data[i][studentCol]) === String(p.studentId) &&
        String(data[i][semCol]) === String(p.semester) &&
        String(data[i][subjectCol]) === String(p.subject)) {
      const row = i + 1;
      sheet.getRange(row, headers.indexOf("TotalClasses") + 1).setValue(p.totalClasses);
      sheet.getRange(row, headers.indexOf("ClassesAttended") + 1).setValue(p.classesAttended);
      sheet.getRange(row, headers.indexOf("Percentage") + 1).setValue(p.percentage);
      sheet.getRange(row, headers.indexOf("LastUpdated") + 1).setValue(p.lastUpdated || new Date().toISOString().split('T')[0]);
      return { success: true, message: "Attendance updated successfully" };
    }
  }
  
  // Add new
  const newRow = [
    p.studentId,
    p.semester,
    p.subject,
    p.totalClasses,
    p.classesAttended,
    p.percentage,
    p.lastUpdated || new Date().toISOString().split('T')[0]
  ];
  sheet.appendRow(newRow);
  return { success: true, message: "Attendance added successfully" };
}

function saveAcademicPerformance(p) {
  const ss = getSpreadsheet();
  const sheet = ss.getSheetByName("AcademicPerformance");
  const data = sheet.getDataRange().getValues();
  const headers = data[0];
  
  const studentCol = headers.indexOf("StudentID");
  const semCol = headers.indexOf("Semester");
  const subjectCol = headers.indexOf("Subject");
  
  // Try to find existing record
  for (let i = 1; i < data.length; i++) {
    if (String(data[i][studentCol]) === String(p.studentId) &&
        String(data[i][semCol]) === String(p.semester) &&
        String(data[i][subjectCol]) === String(p.subject)) {
      const row = i + 1;
      sheet.getRange(row, headers.indexOf("Grade") + 1).setValue(p.grade);
      sheet.getRange(row, headers.indexOf("Marks") + 1).setValue(p.marks);
      sheet.getRange(row, headers.indexOf("TotalMarks") + 1).setValue(p.totalMarks);
      sheet.getRange(row, headers.indexOf("Remarks") + 1).setValue(p.remarks || "");
      return { success: true, message: "Academic performance updated successfully" };
    }
  }
  
  // Add new
  const newRow = [
    p.studentId,
    p.semester,
    p.subject,
    p.grade,
    p.marks,
    p.totalMarks,
    p.remarks || ""
  ];
  sheet.appendRow(newRow);
  return { success: true, message: "Academic performance added successfully" };
}

function logAudit(user, action, details) {
  const ss = getSpreadsheet();
  const sheet = ss.getSheetByName("AuditLogs");
  if (sheet) {
    sheet.appendRow([new Date().toISOString(), user, action, details]);
  }
}

function jsonResponse(data) {
  return ContentService.createTextOutput(JSON.stringify(data))
    .setMimeType(ContentService.MimeType.JSON);
}
