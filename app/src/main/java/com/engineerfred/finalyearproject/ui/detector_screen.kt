package com.engineerfred.finalyearproject.ui

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.engineerfred.finalyearproject.R
import com.engineerfred.finalyearproject.ui.components.CameraPreview
import com.engineerfred.finalyearproject.ui.components.DetectionModeSelector
import com.engineerfred.finalyearproject.ui.components.ImageWithBoundingBoxes
import com.engineerfred.finalyearproject.ui.components.ScanningEffect
import com.engineerfred.finalyearproject.ui.vm.MyViewModel
import com.engineerfred.finalyearproject.utils.DetectionMode
import com.engineerfred.finalyearproject.utils.isValidIpAddress

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DetectorScreen(
    modifier: Modifier = Modifier,
    context: Context,
) {

    val viewModel = remember {
        MyViewModel(context)
    }

    val errMessage = viewModel.errMsg.collectAsState().value
    val detecting = viewModel.isDetecting.collectAsState().value
    val showCamera = viewModel.showCamera.collectAsState().value
    val boundingBoxes = viewModel.boundingBoxes.collectAsState().value
    val imageBitmap = viewModel.imageBitmap.collectAsState().value
    val imageUri = viewModel.imageUri.collectAsState().value
    val detectionMode = viewModel.detectionMode.collectAsState().value
    val ipAddress = viewModel.ipAddress.collectAsState().value

    var showIpAddressDialog by remember { mutableStateOf(false) }

    // Handle back press
    BackHandler(enabled = showCamera) {
        viewModel.updateCameraVisibility(false)
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.updateImageUriAndBitmap(it)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            viewModel.updateCameraVisibility(true)
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    if (showCamera) {
        CameraPreview(
            modifier = modifier.fillMaxSize(),
            onImageCaptured = {
                viewModel.updateImageUriAndBitmap(it)
                viewModel.updateCameraVisibility(false)
            },
            onClose = {
                viewModel.updateCameraVisibility(false)
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
                            viewModel.updateCameraVisibility(true)
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
                    detectionMode == DetectionMode.Local && imageUri != null -> {
                        if ( imageBitmap != null) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                ImageWithBoundingBoxes(imageBitmap, boundingBoxes)
                            }
                        }
                    }
                    detectionMode == DetectionMode.Remote && imageUri != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.LightGray.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            GlideImage(
                                model = viewModel.imageUri.collectAsState().value,
                                contentDescription = "Predicted Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                            if (detecting) {
                                ScanningEffect()
                            }
                        }
                    }

                    detectionMode == null -> {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if ( imageUri == null ) {
                                Text(
                                    "Detection Results will be shown here!",
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.LightGray
                                )
                            } else {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = "Selected Image",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
            errMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            if ( imageUri != null ) {
                //the image uri is not empty! but how are we predicting????
                DetectionModeSelector(
                    onLocalDetect = {
                        Toast.makeText(context, "Detecting...", Toast.LENGTH_SHORT).show()
                        imageBitmap?.let {
                            viewModel.predictLocally(it)
                        }
                    },
                    onRemoteDetect = {
                       showIpAddressDialog = true
                    }
                )
            }

            if (showIpAddressDialog) {
                val keyboardController = LocalSoftwareKeyboardController.current
                AlertDialog(
                    onDismissRequest = { showIpAddressDialog = false },
                    title = { Text("Enter Device IP Address") },
                    text = {
                        OutlinedTextField(
                            value = ipAddress ?: "",
                            onValueChange = { viewModel.setIpAddress(it) },
                            label = { Text("IP Address (e.g., 192.168.x.x)") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if(isValidIpAddress(ipAddress ?: "")) {
                                    imageUri?.let {
                                        Toast.makeText(context, "Detecting...", Toast.LENGTH_SHORT).show()
                                        viewModel.predictOnline(context)
                                        keyboardController?.hide()
                                        showIpAddressDialog = false
                                    }
                                } else {
                                    Toast.makeText(context, "Invalid IP Address", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showIpAddressDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
