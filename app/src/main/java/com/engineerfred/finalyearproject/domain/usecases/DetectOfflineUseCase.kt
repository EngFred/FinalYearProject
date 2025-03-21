package com.engineerfred.finalyearproject.domain.usecases

import android.graphics.Bitmap
import com.engineerfred.finalyearproject.domain.model.Detector
import com.engineerfred.finalyearproject.domain.repo.AppRepository
import javax.inject.Inject

class DetectOfflineUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(detector: Detector, imageBitmap: Bitmap) = repository.detectOffline(detector, imageBitmap)
}