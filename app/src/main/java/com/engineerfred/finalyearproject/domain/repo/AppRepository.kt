package com.engineerfred.finalyearproject.domain.repo

import android.graphics.Bitmap
import com.engineerfred.finalyearproject.domain.model.BoundingBox
import com.engineerfred.finalyearproject.core.resource.Resource
import com.engineerfred.finalyearproject.domain.model.Detector
import okhttp3.MultipartBody

interface AppRepository {
    suspend fun detectOnline(file: MultipartBody.Part): Resource<String?>
    suspend fun detectOffline(detector: Detector, bitmap: Bitmap): List<BoundingBox>
}