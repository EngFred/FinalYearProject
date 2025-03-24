package com.engineerfred.finalyearproject.ui.screen.home

import android.graphics.Bitmap
import android.net.Uri
import com.engineerfred.finalyearproject.domain.model.BoundingBox
import com.engineerfred.finalyearproject.domain.model.LiteModel

data class HomeUiState(
    val imageUri: Uri? = null,
    val feedbackMessage: String? = null,
    val isDetecting: Boolean = false,
    val imageBitmap: Bitmap? = null,
    val boundingBoxes: List<BoundingBox> = emptyList(),
    val savingDocument: Boolean = false,
    val username: String? = null,
    val usedModel: LiteModel? = null
)



