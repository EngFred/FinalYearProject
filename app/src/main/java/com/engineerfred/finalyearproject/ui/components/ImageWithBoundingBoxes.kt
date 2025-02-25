package com.engineerfred.finalyearproject.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import com.engineerfred.finalyearproject.model.BoundingBox

@Composable
fun ImageWithBoundingBoxes(bitmap: Bitmap, boundingBoxes: List<BoundingBox>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(bitmap.width.toFloat() / bitmap.height.toFloat()) // Maintain aspect ratio
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Selected Image",
            contentScale = ContentScale.Fit,
            modifier = Modifier.matchParentSize()
        )

        boundingBoxes.forEach { box ->
            DrawBoundingBox(box, bitmap)
        }
    }
}

@Composable
fun DrawBoundingBox(box: BoundingBox, bitmap: Bitmap) {
    val imageWidth = bitmap.width.toFloat()
    val imageHeight = bitmap.height.toFloat()

    val x1 = box.x1 * imageWidth
    val y1 = box.y1 * imageHeight
    val x2 = box.x2 * imageWidth
    val y2 = box.y2 * imageHeight

    val labelText = "${box.clsName}: ${(box.cnf * 100).toInt()}%" // Confidence percentage

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Scale the bounding box coordinates to the actual displayed image size
        val scaleX = canvasWidth / imageWidth
        val scaleY = canvasHeight / imageHeight

        val adjX1 = x1 * scaleX
        val adjY1 = y1 * scaleY
        val adjX2 = x2 * scaleX
        val adjY2 = y2 * scaleY

        // Draw bounding box (Cyan)
        drawRect(
            color = Color(0xFF8ECB1F),
            topLeft = Offset(adjX1, adjY1),
            size = Size(adjX2 - adjX1, adjY2 - adjY1),
            style = Stroke(width = 4f)
        )

        // Measure text width
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 24f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val textWidth = textPaint.measureText(labelText)
        val textHeight = textPaint.fontMetrics.run { bottom - top }

        // Draw label background (Cyan) - starts exactly at the bounding box
        drawRoundRect(
            color = Color(0xFF8ECB1F),
            topLeft = Offset(adjX1 - 3, adjY1 - textHeight - 10),
            size = Size(textWidth + 20, textHeight + 10),
        )

        // Draw label text
        drawContext.canvas.nativeCanvas.drawText(
            labelText,
            adjX1 + 10,
            adjY1 - 10,
            textPaint
        )
    }
}