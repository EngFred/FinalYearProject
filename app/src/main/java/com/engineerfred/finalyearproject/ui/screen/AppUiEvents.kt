package com.engineerfred.finalyearproject.ui.screen

import android.content.Context
import android.net.Uri
import com.engineerfred.finalyearproject.domain.model.Detector

sealed class AppUiEvents {
    data class DetectedLocally(val detector: Detector ) : AppUiEvents()
    data class DetectedRemotely(val context: Context) : AppUiEvents()
    data class SelectedImage(val context: Context, val imageUri: Uri) : AppUiEvents()
    data class ToggledCameraVisibility(val visible: Boolean) : AppUiEvents()
}