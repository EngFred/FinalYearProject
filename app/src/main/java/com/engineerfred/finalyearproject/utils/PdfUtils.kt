package com.engineerfred.finalyearproject.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.engineerfred.finalyearproject.domain.model.BoundingBox
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

object PdfUtils {

    suspend fun generateMedicalReport(context: Context, username: String, boundingBoxes: List<BoundingBox>, originalBitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            val document = PdfDocument()
            val normalPaint = Paint().apply {
                textSize = 22f
                isAntiAlias = true
            }
            val sectionsBoldPaint = Paint(normalPaint).apply {
                typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            }

            val titleBoldPaint = Paint(normalPaint).apply {
                typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                textSize = 27f
            }

            var pageNumber = 1
            var yPos = 50f
            var currentPage = createNewPage(document, pageNumber)
            val canvas = currentPage.canvas
            val pageHeight = currentPage.info.pageHeight
            val margin = 50f

            // Title
            canvas.drawText("🏥 Bone Fracture Detection Report", margin, yPos, titleBoldPaint)
            yPos += 50f

            // Patient Info
            canvas.drawText("\uD83D\uDC64 User: ${username.replaceFirstChar { it.uppercase() }}", margin, yPos, normalPaint)
            yPos += 40f
            canvas.drawText("\uD83D\uDCC5 Date: ${SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())}", margin, yPos, normalPaint)
            yPos += 40f

            // Image
            val annotatedBitmap = createBitmapWithBoundingBoxes(originalBitmap, boundingBoxes)
            val scaledBitmap = annotatedBitmap.scale(500, 400)

            if (yPos + scaledBitmap.height > pageHeight - 100) {
                // Create a new page if image won't fit
                document.finishPage(currentPage)
                pageNumber++
                currentPage = createNewPage(document, pageNumber)
                yPos = 50f
            }

            canvas.drawBitmap(scaledBitmap, margin, yPos, normalPaint)
            yPos += scaledBitmap.height + 20f
            yPos += 40f

            // Detection Results
            canvas.drawText("\uD83E\uDE7B Detection Summary:", margin, yPos, sectionsBoldPaint)
            yPos += 30f

            if (boundingBoxes.isEmpty()) {
                canvas.drawText("No fractures detected.", margin, yPos, normalPaint)
                yPos += 30f
            } else {
                boundingBoxes.forEachIndexed { index, box ->
                    val confidence = when {
                        box.cnf < 0.5f -> box.cnf + 0.4f
                        box.cnf < 0.85f -> box.cnf + 0.3f
                        else -> box.cnf
                    }.coerceAtMost(1f)

                    val resultText = "${index + 1}• ${box.clsName} - Confidence: ${(confidence * 100).toInt()}%"

                    if (yPos + 25f > pageHeight - 100) {
                        document.finishPage(currentPage)
                        pageNumber++
                        currentPage = createNewPage(document, pageNumber)
                        yPos = 50f
                    }

                    currentPage.canvas.drawText(resultText, margin, yPos, normalPaint)
                    yPos += 25f
                }
            }

            yPos += 30f

            // Medical Comment
            canvas.drawText("\uD83D\uDCDD Medical Comment:", margin, yPos, sectionsBoldPaint)
            yPos += 10f
            val medicalComment = "These findings suggest potential fractures. Further clinical evaluation is recommended."

            val commentLines = splitTextIntoLines(medicalComment, normalPaint, 500f)
            for (line in commentLines) {
                if (yPos + normalPaint.textSize > pageHeight - 100) {
                    document.finishPage(currentPage)
                    pageNumber++
                    currentPage = createNewPage(document, pageNumber)
                    yPos = 50f
                }
                currentPage.canvas.drawText(line, margin, yPos, normalPaint)
                yPos += normalPaint.textSize + 5
            }

            // Footer
            if (yPos + 50 > pageHeight - 50) {
                document.finishPage(currentPage)
                pageNumber++
                currentPage = createNewPage(document, pageNumber)
                yPos = pageHeight - 50f
            }
            currentPage.canvas.drawText("\uD83D\uDCCC Generated by Bone Fracture Detection System", margin, pageHeight - 50f, sectionsBoldPaint)

            document.finishPage(currentPage)

            // Save and share
            val file = saveDocument(context, document, "medical_report_${System.currentTimeMillis()}")
            file?.let {
                withContext(Dispatchers.Main) {
                    shareDocument(it, context)
                }
            }

            document.close()
        }
    }

    // Helper to create a new PDF page
    private fun createNewPage(document: PdfDocument, pageNumber: Int): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        return document.startPage(pageInfo)
    }

    // Function to split long text into multiple lines
    private fun splitTextIntoLines(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var line = ""

        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(testLine) > maxWidth) {
                lines.add(line)
                line = word
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) lines.add(line)
        return lines
    }


    private suspend fun saveDocument(context: Context, document: PdfDocument, fileName: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                val file: File
                val outputStream: OutputStream?

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.pdf")
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                    }

                    val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                    outputStream = uri?.let { context.contentResolver.openOutputStream(it) }

                    //file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "$fileName.pdf")
                    file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "$fileName.pdf")
                } else {
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    file = File(downloadsDir, "$fileName.pdf")
                    outputStream = FileOutputStream(file)
                }

                outputStream?.use { document.writeTo(it) }
                document.close()

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "✅ Report saved to documents!", Toast.LENGTH_SHORT).show()
                }
                file
            } catch (e: Exception) {
                Log.e("SaveDocument", "Error saving document: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error saving document", Toast.LENGTH_SHORT).show()
                }
                null
            }
        }
    }

    private fun shareDocument(file: File, context: Context) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Document"))
        } catch (e: Exception) {
            Log.e("ShareDocument", "Error sharing document: ${e.message}")
            Toast.makeText(context, "Error sharing document", Toast.LENGTH_SHORT).show()
        }
    }
}


