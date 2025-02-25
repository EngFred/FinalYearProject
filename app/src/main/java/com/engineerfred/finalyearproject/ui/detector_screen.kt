package com.engineerfred.finalyearproject.ui

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.engineerfred.finalyearproject.R
import com.engineerfred.finalyearproject.model.BoundingBox
import com.engineerfred.finalyearproject.model.Detector
import com.engineerfred.finalyearproject.ui.components.CameraPreview
import com.engineerfred.finalyearproject.ui.components.ImageWithBoundingBoxes
import java.io.InputStream

@Composable
fun DetectorScreen(
    modifier: Modifier = Modifier,
    context: Context
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var boundingBoxes by remember { mutableStateOf<List<BoundingBox>>(emptyList()) }
    var showCamera by remember { mutableStateOf(false) }

    // Handle back press
    BackHandler(enabled = showCamera) {
        showCamera = false
    }

    val detector = remember { Detector(context, object : Detector.DetectorListener {
        override fun onEmptyDetect() {
            boundingBoxes = emptyList()
            Toast.makeText(context, "No fractures detected", Toast.LENGTH_SHORT).show()
        }
        override fun onDetect(boxes: List<BoundingBox>) {
            boundingBoxes = boxes
        }
    }) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        if (uri != null) {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            imageBitmap = BitmapFactory.decodeStream(inputStream)
            imageBitmap?.let { detector.detect(it) }
            showCamera = false
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            showCamera = true
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    if (showCamera) {
        CameraPreview(
            modifier = modifier.fillMaxSize(),
            onImageCaptured = { bitmap ->
                imageBitmap = bitmap
                detector.detect(bitmap)
                showCamera = false
            },
            onClose = {
                showCamera = false
            }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Bone Fracture Detection",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(painterResource(R.drawable.ic_gallery), contentDescription = "Gallery", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select from Gallery", color = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            showCamera = true
                        } else {
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(painterResource(R.drawable.ic_camera), contentDescription = "Camera", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Capture with Camera", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if ( imageBitmap != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF232323)
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ImageWithBoundingBoxes(imageBitmap!!, boundingBoxes)
                    }
                }
            } else {
                Column(
                    Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        "Detection Results will be shown here!",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.LightGray
                    )

                }
            }
        }
    }
}
