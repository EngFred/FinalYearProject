package com.engineerfred.finalyearproject.ui.screen

import android.graphics.Bitmap
import android.net.Uri
import com.engineerfred.finalyearproject.domain.model.BoundingBox
import com.engineerfred.finalyearproject.domain.model.DetectionMode

data class AppUiState(
    val imageUri: Uri? = null,
    val feedbackMessage: String? = null,
    val isDetecting: Boolean = false,
    val detectionMode: DetectionMode? = null,
    val imageBitmap: Bitmap? = null,
    val boundingBoxes: List<BoundingBox> = emptyList(),
    val showCamera: Boolean = false
)



