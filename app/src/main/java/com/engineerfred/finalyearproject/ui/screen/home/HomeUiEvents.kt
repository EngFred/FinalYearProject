package com.engineerfred.finalyearproject.ui.screen.home

import android.content.Context
import android.net.Uri
import com.engineerfred.finalyearproject.domain.model.LiteModel

sealed class HomeUiEvents {
    data class ModelUpdated(val model: LiteModel, val context: Context) : HomeUiEvents()
    data class SelectedImage(val context: Context, val imageUri: Uri) : HomeUiEvents()
    data class GeneratedMedicalReport(val context: Context) : HomeUiEvents()
}