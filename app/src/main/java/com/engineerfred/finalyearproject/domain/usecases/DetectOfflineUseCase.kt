package com.engineerfred.finalyearproject.domain.usecases

import android.graphics.Bitmap
import com.engineerfred.finalyearproject.domain.repo.AppRepository
import javax.inject.Inject

class DetectOfflineUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(imageBitmap: Bitmap) = repository.detectOffline(imageBitmap)
}