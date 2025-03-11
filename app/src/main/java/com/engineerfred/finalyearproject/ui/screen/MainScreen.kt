package com.engineerfred.finalyearproject.ui.screen

import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.engineerfred.finalyearproject.R
import com.engineerfred.finalyearproject.ui.components.CameraPreview
import com.engineerfred.finalyearproject.ui.components.DetectionModeSelector
import com.engineerfred.finalyearproject.ui.components.ImageWithBoundingBoxes
import com.engineerfred.finalyearproject.ui.components.ScanningEffect
import com.engineerfred.finalyearproject.domain.model.DetectionMode

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsState().value

    // Handle back press
    BackHandler(enabled = uiState.showCamera) {
        viewModel.onEvent(AppUiEvents.ToggledCameraVisibility(false))
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.onEvent(AppUiEvents.SelectedImage(context, it))
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            viewModel.onEvent(AppUiEvents.ToggledCameraVisibility(true))
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    if (uiState.showCamera) {
        CameraPreview(
            modifier = modifier.fillMaxSize(),
            onImageCaptured = {
                viewModel.onEvent(AppUiEvents.SelectedImage(context, it))
            },
            onClose = {
                viewModel.onEvent(AppUiEvents.ToggledCameraVisibility(false))
            }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            //title
            Text(
                text = "Bone Fracture Detection",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            //button rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        if( uiState.isDetecting ) {
                            Toast.makeText(context, "Please wait!", Toast.LENGTH_SHORT).show()
                        } else {
                            galleryLauncher.launch("image/*")
                        }
                    },
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
                        if ( uiState.isDetecting ) {
                            Toast.makeText(context, "Please wait!", Toast.LENGTH_SHORT).show()
                        } else {
                            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                viewModel.onEvent(AppUiEvents.ToggledCameraVisibility(true))
                            } else {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
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

            //image to detect
            Card(
                modifier = Modifier
                    .fillMaxWidth().weight(1f)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF232323)
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                when {
                    uiState.detectionMode == DetectionMode.Local && uiState.imageUri != null -> {
                        if ( uiState.imageBitmap != null) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                ImageWithBoundingBoxes(uiState.imageBitmap, uiState.boundingBoxes)
                            }
                        }
                    }
                    uiState.detectionMode == DetectionMode.Remote && uiState.imageUri != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.LightGray.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            GlideImage(
                                model = uiState.imageUri,
                                contentDescription = "Predicted Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                            if (uiState.isDetecting) {
                                ScanningEffect()
                            }
                        }
                    }

                    uiState.detectionMode == null -> {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if ( uiState.imageUri == null ) {
                                Text(
                                    "Detection Results will be shown here!",
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.LightGray
                                )
                            } else {
                                AsyncImage(
                                    model = uiState.imageUri,
                                    contentDescription = "Selected Image",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedVisibility(visible = uiState.feedbackMessage != null) {
                uiState.feedbackMessage?.let { message ->
                    Spacer(modifier = Modifier.height(16.dp))
                    val color = if( message.contains("Detection Completed!") || message.contains("No fracture detected!") ) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    val size = if( message.contains("Detection Completed!") || message.contains("No fracture detected!") ) 21.sp else 17.sp
                    Text(
                        text = message,
                        modifier = Modifier.fillMaxWidth(),
                        color = color,
                        textAlign = TextAlign.Center,
                        fontSize = size,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(visible = uiState.imageUri != null ) {
                DetectionModeSelector(
                    onLocalDetect = {
                        Toast.makeText(context, "Detecting...", Toast.LENGTH_SHORT).show()
                        uiState.imageBitmap?.let {
                            viewModel.onEvent(AppUiEvents.DetectedLocally)
                        }
                    },
                    onRemoteDetect = {
                        Toast.makeText(context, "Detecting...", Toast.LENGTH_SHORT).show()
                        viewModel.onEvent(AppUiEvents.DetectedRemotely(context))
                    },
                    enabled = uiState.isDetecting.not()
                )
            }
        }
    }
}
