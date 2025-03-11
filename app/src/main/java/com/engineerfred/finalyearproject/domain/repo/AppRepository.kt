package com.engineerfred.finalyearproject.domain.repo

import android.graphics.Bitmap
import com.engineerfred.finalyearproject.domain.model.BoundingBox
import com.engineerfred.finalyearproject.core.resource.Resource
import okhttp3.MultipartBody

interface AppRepository {
    suspend fun detectOnline(file: MultipartBody.Part): Resource<String?>
    fun detectOffline(bitmap: Bitmap): List<BoundingBox>
}