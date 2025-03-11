package com.engineerfred.finalyearproject.ui.screen

import android.content.Context
import android.net.Uri

sealed class AppUiEvents {
    data object DetectedLocally : AppUiEvents()
    data class DetectedRemotely(val context: Context) : AppUiEvents()
    data class SelectedImage(val context: Context, val imageUri: Uri) : AppUiEvents()
    data class ToggledCameraVisibility(val visible: Boolean) : AppUiEvents()
}