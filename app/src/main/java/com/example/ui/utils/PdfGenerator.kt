package com.example.ui.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.models.Payment
import com.example.data.models.ReportResponse
import java.io.File
import java.io.FileOutputStream
import android.print.PrintManager
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

object PdfGenerator {

    /**
     * Generates a beautiful PDF Receipt for a fee payment, saves it, and returns the File.
     */
    fun generatePaymentReceipt(context: Context, payment: Payment): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size: 595 x 842 pt
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        // 1. Draw Elegant Header Banner
        paint.color = Color.parseColor("#2563EB") // Blue primary
        canvas.drawRect(0f, 0f, 595f, 150f, paint)

        // Header Title
        textPaint.color = Color.WHITE
        textPaint.textSize = 24f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ABC COLLEGE", 40f, 40f, textPaint)

        textPaint.textSize = 16f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Center for Distance Education", 40f, 65f, textPaint)

        textPaint.textSize = 14f
        canvas.drawText("Bharathidasan University Trichy", 40f, 85f, textPaint)
        
        textPaint.textSize = 12f
        canvas.drawText("#3 New Street, Sethiyathoppu", 40f, 105f, textPaint)
        canvas.drawText("Bhuvanagiri TK Cuddalore dist 608702", 40f, 125f, textPaint)

        // 2. Receipt Details
        textPaint.color = Color.BLACK
        textPaint.textSize = 14f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("FEE PAYMENT RECEIPT", 40f, 190f, textPaint)

        textPaint.textSize = 11f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Receipt No: ${payment.ReceiptNumber}", 40f, 220f, textPaint)
        canvas.drawText("Payment ID: ${payment.PaymentID}", 40f, 240f, textPaint)
        canvas.drawText("Date: ${payment.Date}", 40f, 260f, textPaint)
        canvas.drawText("Payment Mode: ${payment.PaymentMode}", 40f, 280f, textPaint)

        // Student Column
        canvas.drawText("Student ID: ${payment.StudentID}", 320f, 220f, textPaint)
        canvas.drawText("Course: ${payment.Course}", 320f, 240f, textPaint)
        canvas.drawText("Semester: Semester ${payment.Semester}", 320f, 260f, textPaint)
        if (payment.TransactionNumber.isNotEmpty()) {
            canvas.drawText("Txn Ref: ${payment.TransactionNumber}", 320f, 280f, textPaint)
        }

        // 3. Draw Table Headers
        paint.color = Color.parseColor("#F1F5F9") // Slate Background
        canvas.drawRect(40f, 320f, 555f, 350f, paint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Fee Component", 50f, 340f, textPaint)
        canvas.drawText("Details", 200f, 340f, textPaint)
        canvas.drawText("Amount ($)", 450f, 340f, textPaint)

        // Table Data Row 1
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(payment.FeeType, 50f, 380f, textPaint)
        canvas.drawText(payment.Remarks.ifEmpty { "Academic Dues" }, 200f, 380f, textPaint)
        canvas.drawText("$${payment.Amount}", 450f, 380f, textPaint)

        // Fine Row if any
        var currentY = 410f
        if (payment.Fine.toDoubleOrNull() ?: 0.0 > 0.0) {
            canvas.drawText("Fine/Late Fee", 50f, currentY, textPaint)
            canvas.drawText("Administrative Fine", 200f, currentY, textPaint)
            canvas.drawText("+$${payment.Fine}", 450f, currentY, textPaint)
            currentY += 30f
        }

        // Discount Row if any
        if (payment.Discount.toDoubleOrNull() ?: 0.0 > 0.0) {
            canvas.drawText("Scholarship/Discount", 50f, currentY, textPaint)
            canvas.drawText("Fee concession", 200f, currentY, textPaint)
            canvas.drawText("-$${payment.Discount}", 450f, currentY, textPaint)
            currentY += 30f
        }

        // Separator line
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawRect(40f, currentY, 555f, currentY + 1f, paint)
        currentY += 25f

        // Balance Due Row
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Remaining Balance:", 50f, currentY, textPaint)
        canvas.drawText("$${payment.Balance}", 450f, currentY, textPaint)
        currentY += 30f

        // Total Paid Box
        paint.color = Color.parseColor("#DCFCE7") // Light green
        canvas.drawRect(40f, currentY, 555f, currentY + 45f, paint)

        textPaint.color = Color.parseColor("#15803D") // Dark green
        canvas.drawText("TOTAL PAID AMOUNT", 50f, currentY + 28f, textPaint)
        textPaint.textSize = 16f
        canvas.drawText("$${payment.Amount}", 440f, currentY + 28f, textPaint)

        // 4. QR Stamp representation (drawing a mini outline block)
        currentY += 90f
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawRect(40f, currentY, 120f, currentY + 80f, paint)
        paint.color = Color.BLACK
        canvas.drawRect(45f, currentY + 5, 115f, currentY + 75, paint)
        paint.color = Color.WHITE
        canvas.drawRect(55f, currentY + 15, 105f, currentY + 65, paint)
        paint.color = Color.BLACK
        canvas.drawRect(65f, currentY + 25, 95f, currentY + 55, paint)

        textPaint.color = Color.GRAY
        textPaint.textSize = 10f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("This is an electronically generated receipt, verified securely.", 140f, currentY + 30f, textPaint)
        canvas.drawText("Scan the QR block to verify on official portal.", 140f, currentY + 50f, textPaint)

        // Footer signatures
        textPaint.color = Color.BLACK
        textPaint.textSize = 11f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Finance Officer", 420f, currentY + 40f, textPaint)
        paint.color = Color.BLACK
        canvas.drawRect(400f, currentY + 15f, 520f, currentY + 16f, paint)

        pdfDocument.finishPage(page)

        // Save PDF to Cache
        val file = File(context.cacheDir, "Receipt_${payment.ReceiptNumber}.pdf")
        return try {
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Generates a beautiful PDF Collection Report.
     */
    fun generateCollectionReport(context: Context, report: ReportResponse): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        // Header
        paint.color = Color.parseColor("#16A34A") // Green Success Theme for Finance Report
        canvas.drawRect(0f, 0f, 595f, 100f, paint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 20f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("COLLEGE FINANCIAL COLLECTION REPORT", 40f, 50f, textPaint)
        textPaint.textSize = 11f
        canvas.drawText("Generated on: " + java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()), 40f, 75f, textPaint)

        // Financial Overview Widgets
        textPaint.color = Color.BLACK
        textPaint.textSize = 14f
        canvas.drawText("Financial Summary Dashboard", 40f, 140f, textPaint)

        textPaint.textSize = 11f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Total Students Enrolled: ${report.totalStudents}", 40f, 175f, textPaint)
        canvas.drawText("Total Fees Assigned: $${report.totalFeesAssigned}", 40f, 195f, textPaint)
        canvas.drawText("Total Fees Collected: $${report.totalPaid}", 40f, 215f, textPaint)
        canvas.drawText("Outstanding Pending: $${report.totalPending}", 40f, 235f, textPaint)
        canvas.drawText("Today's Fresh Collection: $${report.todayCollection}", 40f, 255f, textPaint)

        // Draw payments history table
        textPaint.textSize = 13f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Recent Fee Collections Log", 40f, 300f, textPaint)

        // Header Row
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawRect(40f, 320f, 555f, 345f, paint)

        textPaint.textSize = 10f
        canvas.drawText("Receipt", 45f, 337f, textPaint)
        canvas.drawText("StudentID", 110f, 337f, textPaint)
        canvas.drawText("Semester", 200f, 337f, textPaint)
        canvas.drawText("Mode", 280f, 337f, textPaint)
        canvas.drawText("Date", 350f, 337f, textPaint)
        canvas.drawText("Amount", 480f, 337f, textPaint)

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        var y = 365f
        report.payments.take(12).forEach { p ->
            if (y < 800) {
                canvas.drawText(p.ReceiptNumber, 45f, y, textPaint)
                canvas.drawText(p.StudentID, 110f, y, textPaint)
                canvas.drawText("Sem ${p.Semester}", 200f, y, textPaint)
                canvas.drawText(p.PaymentMode, 280f, y, textPaint)
                canvas.drawText(p.Date, 350f, y, textPaint)
                canvas.drawText("$${p.Amount}", 480f, y, textPaint)
                
                // line divider
                paint.color = Color.parseColor("#F1F5F9")
                canvas.drawRect(40f, y + 5f, 555f, y + 6f, paint)
                y += 25f
            }
        }

        // Footer
        textPaint.color = Color.GRAY
        textPaint.textSize = 9f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Page 1 of 1 · Generated by College ERP Smart Reporting Utility", 40f, 810f, textPaint)

        pdfDocument.finishPage(page)
        val file = File(context.cacheDir, "Collection_Report_${System.currentTimeMillis()}.pdf")
        return try {
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Share PDF file securely to other apps.
     */
    fun sharePdf(context: Context, file: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share ERP PDF Document"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Prints a PDF document using the standard Android PrintManager.
     */
    fun printPdf(context: Context, file: File) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "Receipt Document ${file.name}"
            val printAdapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val info = android.print.PrintDocumentInfo.Builder(file.name)
                        .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .build()
                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out android.print.PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    var input: FileInputStream? = null
                    var output: FileOutputStream? = null
                    try {
                        input = FileInputStream(file)
                        output = FileOutputStream(destination?.fileDescriptor)
                        val buf = ByteArray(1024)
                        var bytesRead: Int
                        while (input.read(buf).also { bytesRead = it } > 0) {
                            output.write(buf, 0, bytesRead)
                        }
                        callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    } finally {
                        try { input?.close() } catch (e: IOException) {}
                        try { output?.close() } catch (e: IOException) {}
                    }
                }
            }
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        } catch (e: Exception) {
            Toast.makeText(context, "Printing failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Share payment receipt details directly to WhatsApp.
     */
    fun sharePaymentToWhatsApp(context: Context, payment: Payment) {
        val text = """
            *ABC COLLEGE*
            *Center for Distance Education*
            *Fee Payment Receipt Confirmation*
            ------------------------------------
            *Receipt Number:* ${payment.ReceiptNumber}
            *Student ID:* ${payment.StudentID}
            *Course & Sem:* ${payment.Course} - Sem ${payment.Semester}
            *Paid Amount:* $${payment.Amount}
            *Fine Paid:* $${payment.Fine}
            *Discount/Waiver:* $${payment.Discount}
            *Remaining Balance:* $${payment.Balance}
            *Payment Mode:* ${payment.PaymentMode}
            *Date:* ${payment.Date}
            ------------------------------------
            Thank you for your payment. Scan receipt QR in the student portal to verify.
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("com.whatsapp")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback generic share
            val genericIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(genericIntent, "Share Receipt Confirmation"))
        }
    }
}
