package com.engineerfred.finalyearproject.tutor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import java.io.InputStream

@Composable
fun DetectorScreen(
    modifier: Modifier = Modifier,
    context: Context
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var boundingBoxes by remember { mutableStateOf<List<BoundingBox>>(emptyList()) }

    val detector = remember { Detector(context, object : Detector.DetectorListener {
        override fun onEmptyDetect() {
            boundingBoxes = emptyList()
            Toast.makeText(context, "No fractures detected", Toast.LENGTH_SHORT).show()
        }
        override fun onDetect(boxes: List<BoundingBox>) {
            boundingBoxes = boxes
        }
    }) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        if (uri != null) {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            imageBitmap = BitmapFactory.decodeStream(inputStream)
            imageBitmap?.let {
                detector.detect(it)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { launcher.launch("image/*") }) {
            Text("Select Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        imageBitmap?.let { bitmap ->
            ImageWithBoundingBoxes(bitmap = bitmap, boundingBoxes = boundingBoxes)
        }
    }
}

@Composable
fun ImageWithBoundingBoxes(bitmap: Bitmap, boundingBoxes: List<BoundingBox>) {
    val imageWidth = bitmap.width.toFloat()
    val imageHeight = bitmap.height.toFloat()
    Box(
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Selected Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.background(androidx.compose.ui.graphics.Color.Cyan)
        )

        // Draw bounding boxes
        boundingBoxes.forEach { box ->
            DrawBoundingBox(box, imageWidth, imageHeight)
        }
    }
}

@Composable
fun DrawBoundingBox(box: BoundingBox, imageWidth: Float, imageHeight: Float) {

    val x1 = box.x1 * imageWidth
    val y1 = box.y1 * imageHeight
    val x2 = box.x2 * imageWidth
    val y2 = box.y2 * imageHeight

    // Draw the rectangle
    Canvas(modifier = Modifier.fillMaxSize()) {

        val textPadding = 8f // Padding around text

        val textPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
        }

        val bgPaint = Paint().apply {
            color = android.graphics.Color.CYAN
            alpha = 180  // Adjust transparency (0-255)
        }

        val textBounds = Rect()
        textPaint.getTextBounds(box.clsName, 0, box.clsName.length, textBounds)

        val bgTop = y1 - textBounds.height() - textPadding
        val bgRight = x1 + textBounds.width() + textPadding * 2

        // Draw class name above the bounding box
        drawContext.canvas.nativeCanvas.apply {

            // Draw background rectangle for text
            drawRect(x1, bgTop, bgRight, y1, bgPaint)

            // Draw class name
            drawText(
                box.clsName,
                x1 + textPadding, y1 - textPadding,
                textPaint
            )
        }

        drawRect(
            color = androidx.compose.ui.graphics.Color.Cyan,
            topLeft = Offset(x1, y1),
            size = Size(x2 - x1, y2 - y1),
            style = Stroke(width = 3f)
        )
    }
}
