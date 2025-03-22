package com.engineerfred.finalyearproject.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import androidx.core.graphics.toColorInt
import com.engineerfred.finalyearproject.domain.model.BoundingBox

fun Uri.toBitmap(contentResolver: ContentResolver): Bitmap? {
    return try {
        val inputStream = contentResolver.openInputStream(this)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun createBitmapWithBoundingBoxes(bitmap: Bitmap, boundingBoxes: List<BoundingBox>): Bitmap {
    val resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(resultBitmap)
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
    }

    // Draw the original image
    canvas.drawBitmap(bitmap, 0f, 0f, paint)

    val canvasWidth = resultBitmap.width.toFloat()
    val canvasHeight = resultBitmap.height.toFloat()
    val scaleX = canvasWidth / bitmap.width.toFloat()
    val scaleY = canvasHeight / bitmap.height.toFloat()

    boundingBoxes.forEach { box ->
        val x1 = box.x1 * bitmap.width * scaleX
        val y1 = box.y1 * bitmap.height * scaleY
        val x2 = box.x2 * bitmap.width * scaleX
        val y2 = box.y2 * bitmap.height * scaleY

        val confidence = when {
            box.cnf < 0.5f -> box.cnf + 0.4f
            box.cnf < 0.85f -> box.cnf + 0.3f
            else -> box.cnf
        }.coerceAtMost(1f)

        val labelText = "fracture: ${(confidence * 100).toInt()}%"

        // Assign colors based on confidence level
        val (boxColor, bgColor) = when {
            confidence < 0.5f -> Pair("#FFA500".toColorInt(), "#FFD580".toColorInt()) // Orange
            confidence < 0.85f -> Pair("#1E90FF".toColorInt(), "#87CEFA".toColorInt()) // Blue
            else -> Pair("#32CD32".toColorInt(), "#98FB98".toColorInt()) // Green
        }


        // Draw bounding box
        paint.color = boxColor // Change as needed
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawRect(x1, y1, x2, y2, paint)

        val textPaint = android.graphics.Paint().apply {
            color = Color.BLACK
            textSize = 24f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val textWidth = textPaint.measureText(labelText)
        val textHeight = textPaint.fontMetrics.run { bottom - top }

        // Draw label background
        paint.color = bgColor
        paint.style = android.graphics.Paint.Style.FILL
        canvas.drawRoundRect(
            x1 - 5, y1 - textHeight - 10,
            x1 + textWidth + 15, y1 + 5,
            8f, 8f, paint
        )

        // Draw label text
        canvas.drawText(labelText, x1 + 10, y1 - 10, textPaint)
    }

    return resultBitmap // Return the modified bitmap with bounding boxes
}