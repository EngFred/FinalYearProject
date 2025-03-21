package com.engineerfred.finalyearproject.ui.screen.home

import android.content.Context
import android.net.Uri
import com.engineerfred.finalyearproject.domain.model.Detector

sealed class AppUiEvents {
    data class DetectClicked(val detector: Detector, val context: Context) : AppUiEvents()
    data class SelectedImage(val context: Context, val imageUri: Uri) : AppUiEvents()
}