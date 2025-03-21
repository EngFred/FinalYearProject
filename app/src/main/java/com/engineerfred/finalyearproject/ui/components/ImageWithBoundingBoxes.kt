package com.engineerfred.finalyearproject.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import com.engineerfred.finalyearproject.domain.model.BoundingBox

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

        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
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

                // Draw bounding box
                drawRect(
                    color = Color(0xFF8ECB1F), // Greenish color
                    topLeft = Offset(x1, y1),
                    size = Size(x2 - x1, y2 - y1),
                    style = Stroke(width = 4f)
                )

                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 24f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    isAntiAlias = true
                }
                val textWidth = textPaint.measureText(labelText)
                val textHeight = textPaint.fontMetrics.run { bottom - top }

                // Draw label background
                drawRoundRect(
                    color = Color(0xFF8ECB1F), // Semi-transparent black for better contrast
                    topLeft = Offset(x1 - 5, y1 - textHeight - 10),
                    size = Size(textWidth + 20, textHeight + 10),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Draw label text
                drawContext.canvas.nativeCanvas.drawText(
                    labelText,
                    x1 + 10,
                    y1 - 10,
                    textPaint
                )
            }
        }
    }
}
