package com.engineerfred.finalyearproject.ui.screen.home

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.engineerfred.finalyearproject.R
import com.engineerfred.finalyearproject.ui.components.DetectionModeSelector
import com.engineerfred.finalyearproject.ui.components.ImageWithBoundingBoxes
import com.engineerfred.finalyearproject.ui.theme.DarkGrayBlue
import com.engineerfred.finalyearproject.ui.theme.White10

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onCaptureImage: () -> Unit,
    capturedImageUrl:  String?
) {

    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsState().value

    LaunchedEffect(capturedImageUrl) {
        capturedImageUrl?.let {
            viewModel.onEvent(HomeUiEvents.SelectedImage(context, it.toUri()))
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.onEvent(HomeUiEvents.SelectedImage(context, it))
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            onCaptureImage()
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                uiState.imageBitmap?.let {
                    viewModel.onEvent(HomeUiEvents.GeneratedMedicalReport(context))
                } ?: Toast.makeText(context, "No detection results found!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(DarkGrayBlue).height(60.dp).padding(start = 20.dp, end = 10.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FracDetect",
                modifier = Modifier.weight(1f).padding(end = 4.dp),
                style = TextStyle(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    ),
                )
            )

            androidx.compose.animation.AnimatedVisibility(visible = uiState.imageUri != null && uiState.isDetecting.not() && uiState.boundingBoxes.isNotEmpty()) {
                IconButton(onClick = {
                    when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                            // Android 10+ (Scoped Storage) - No permission required
                            if (uiState.boundingBoxes.isNotEmpty()) {
                                uiState.imageBitmap?.let {
                                    viewModel.onEvent(HomeUiEvents.GeneratedMedicalReport(context))
                                } ?: Toast.makeText(context, "No detection results found!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                            // Permission is already granted
                            if (uiState.boundingBoxes.isNotEmpty()) {
                                uiState.imageBitmap?.let {
                                    viewModel.onEvent(HomeUiEvents.GeneratedMedicalReport(context))
                                } ?: Toast.makeText(context, "No detection results found!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        else -> {
                            // Request permission
                            storagePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    }
                }, enabled = uiState.savingDocument.not()) {
                    if( uiState.savingDocument.not() ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_download),
                            contentDescription = "download button"
                        )
                    } else {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
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
                    modifier = Modifier
                        .weight(1f)
                        .height(57.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(painterResource(R.drawable.ic_gallery), contentDescription = "Gallery", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Select from Gallery",
                        color = Color.White,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            ),
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            onCaptureImage()
                        } else {
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(57.dp),
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
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = White10
                ),
            ) {
                when {
                    uiState.imageUri != null -> {
                        if ( uiState.imageBitmap != null) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                ImageWithBoundingBoxes(uiState.imageBitmap, uiState.boundingBoxes)
                            }
                        }
                    }else -> {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Detection Results will be shown here!",
                            style = TextStyle(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.LightGray,
                                shadow = Shadow(
                                    color = Color.Black,
                                    offset = Offset(2f, 2f),
                                    blurRadius = 2f
                                )
                            )
                        )
                    }
                }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedVisibility(visible = uiState.feedbackMessage != null) {
                uiState.feedbackMessage?.let { message ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        modifier = Modifier.fillMaxWidth(),
                        style = TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            ),
                        )
                    )
                }
            }

            AnimatedVisibility(visible = uiState.feedbackMessage == null && uiState.boundingBoxes.isNotEmpty() && uiState.isDetecting.not()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Detection complete! If your not contented with the results, try again with another model.",
                    modifier = Modifier.fillMaxWidth(),
                    style = TextStyle(
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        ),
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(visible = uiState.imageUri != null ) {
                DetectionModeSelector(
                    isDetecting = uiState.isDetecting,
                    onDetect = { model ->
                        uiState.imageBitmap?.let {
                            viewModel.onEvent(HomeUiEvents.ModelUpdated(model, context))
                        }
                    },
                    detectionModel = uiState.usedModel,
                    enabled = uiState.isDetecting.not()
                )
            }
        }
    }
}
