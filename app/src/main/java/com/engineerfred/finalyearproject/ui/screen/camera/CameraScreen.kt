package com.engineerfred.finalyearproject.ui.screen.camera

import android.content.Context
import android.net.Uri
import android.util.Rational
import android.widget.Toast
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.engineerfred.finalyearproject.R
import java.io.File

@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    onCaptureComplete: (String) -> Unit,
    onBack: () -> Unit
) {

    val context = LocalContext.current

    val previewUseCase = remember { Preview.Builder().build() }
//    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    val imageCaptureUseCase = remember { ImageCapture.Builder().build() }

    var flashEnabled by remember { mutableStateOf(false) }
    var zoomState by remember { mutableFloatStateOf(1f) }
    var isCapturing by remember { mutableStateOf(false) }

    // Get the device's screen dimensions
    val displayMetrics = LocalContext.current.resources.displayMetrics
    val screenWidth = displayMetrics.widthPixels
    val screenHeight = displayMetrics.heightPixels

    // Define aspect ratios
    val aspectRatioOptions = listOf(
        "5:4" to  Rational(4, 5),
        "4:3" to Rational(3, 4),
        "3:2" to Rational(2, 3),
        "16:9" to Rational(9, 16),
        "Square" to Rational(1, 1),
        "Full" to Rational(screenWidth, screenHeight) // Full screen aspect ratio
    )

    var selectedAspectRatio by remember { mutableStateOf("Full") }

    val selectedRatio = when (selectedAspectRatio) {
        "5:4" -> 4f / 5f
        "4:3" -> 3f / 4f
        "3:2" -> 2f / 3f
        "16:9" -> 9f / 16f
        "Square" -> 1f
        "Full" -> screenWidth.toFloat() / screenHeight.toFloat() // Full screen
        else -> 4f / 3f // Default
    }

    LaunchedEffect(Unit) {
        cameraProvider = ProcessCameraProvider.awaitInstance(context)
        rebindCameraProvider(cameraProvider, previewUseCase, imageCaptureUseCase, context) { control ->
            cameraControl = control
        }
    }

    LaunchedEffect(selectedAspectRatio) {
        updateAspectRatio(imageCaptureUseCase, selectedAspectRatio, aspectRatioOptions)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(selectedRatio)
                .padding(horizontal = 8.dp)
                .background(Color.Black, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp)),
            factory = { ctx ->
                PreviewView(ctx).also {
                    previewUseCase.surfaceProvider = it.surfaceProvider
                    rebindCameraProvider(cameraProvider, previewUseCase, imageCaptureUseCase, context) { control ->
                        cameraControl = control
                    }
                }
            },
        )

        Row(
            modifier = Modifier.align(Alignment.TopStart).fillMaxWidth().padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { onBack() }) {
                Icon(Icons.Default.Close, contentDescription = "Close Camera", tint = Color.White)
            }

            Spacer(Modifier.width(8.dp))

            // Aspect Ratio Selector
            Row(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                aspectRatioOptions.forEach { (label,ratio) ->
                    val isSelected = label == selectedAspectRatio
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else Color.Gray,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                selectedAspectRatio = label
                                imageCaptureUseCase.setCropAspectRatio(ratio) // Set the selected aspect ratio
                            }
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Flash Toggle Button
            IconButton(
                onClick = {
                    flashEnabled = !flashEnabled
                    imageCaptureUseCase.flashMode = if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
                }
            ) {
                Icon(
                    painter = painterResource(if (flashEnabled) R.drawable.ic_flash_on else R.drawable.flash_off),
                    contentDescription = "Toggle Flash",
                    tint = Color.White
                )
            }
        }

        // Zoom Control Slider
        Slider(
            value = zoomState,
            onValueChange = {
                zoomState = it
                cameraControl?.setZoomRatio(it)
            },
            valueRange = 1f..4f,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .padding(horizontal = 16.dp)
                .height(10.dp),
            colors = SliderDefaults.colors(activeTrackColor = MaterialTheme.colorScheme.primary, thumbColor = Color.White)
        )

        // Capture Image Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
            ) {
                IconButton(
                    onClick = {
                        startCamera(
                            onCaptureComplete = {
                                onCaptureComplete(it)
                            },
                            context = context,
                            imageCaptureUseCase = imageCaptureUseCase,
                            onUpdateCaptureStatus = { isCapturing = it }
                        )
                    },
                    modifier = Modifier.size(64.dp),
                    enabled = !isCapturing
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_camera),
                            contentDescription = "Capture Image",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    }
}

fun rebindCameraProvider(
    cameraProvider: ProcessCameraProvider?,
    previewUseCase: Preview,
    imageCaptureUseCase: ImageCapture,
    context: Context,
    onCameraControlReady: (CameraControl?) -> Unit
) {
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()

    cameraProvider?.unbindAll()

    val camera = cameraProvider?.bindToLifecycle(
        context as LifecycleOwner,
        cameraSelector,
        previewUseCase,
        imageCaptureUseCase
    )

    onCameraControlReady(camera?.cameraControl)
}


fun updateAspectRatio(imageCaptureUseCase: ImageCapture, selectedAspectRatio: String, aspectRatioOptions: List<Pair<String, Rational>>) {
    val ratio = aspectRatioOptions.find { it.first == selectedAspectRatio }?.second
    if (ratio != null) {
        imageCaptureUseCase.setCropAspectRatio(ratio)
    }
}

fun startCamera(
    onUpdateCaptureStatus: (Boolean) -> Unit,
    context: Context,
    onCaptureComplete: (String) -> Unit,
    imageCaptureUseCase: ImageCapture
) {

    onUpdateCaptureStatus(true)

    val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
        File(context.externalCacheDir, "image.jpg")
    ).build()
    val callback = object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            outputFileResults.savedUri?.let {
                val encodedImageUrl = Uri.encode(it.toString())
                onCaptureComplete(encodedImageUrl)
            } ?: Toast.makeText(context, "Unable to save photo!", Toast.LENGTH_SHORT).show()
            onUpdateCaptureStatus(false)
        }

        override fun onError(exception: ImageCaptureException) {
            onUpdateCaptureStatus(false)
            Toast.makeText(context, "Unable to take photo!", Toast.LENGTH_SHORT).show()
        }
    }

    imageCaptureUseCase.takePicture(
        outputFileOptions,
        ContextCompat.getMainExecutor(context),
        callback
    )
}
