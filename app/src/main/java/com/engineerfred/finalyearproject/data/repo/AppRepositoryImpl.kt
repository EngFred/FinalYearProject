package com.engineerfred.finalyearproject.data.repo

import android.graphics.Bitmap
import android.util.Log
import com.engineerfred.finalyearproject.data.local.OfflineDetector
import com.engineerfred.finalyearproject.data.remote.ApiService
import com.engineerfred.finalyearproject.domain.repo.AppRepository
import com.engineerfred.finalyearproject.domain.model.BoundingBox
import com.engineerfred.finalyearproject.core.resource.Resource
import okhttp3.MultipartBody
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val offlineDetector: OfflineDetector
) : AppRepository {

    override suspend fun detectOnline(file: MultipartBody.Part): Resource<String?> {
        return try {
            Log.d("MyRepo", "Predicting...")
            val imageUrl = apiService.predict(file).imageUrl
            Log.d("MyRepo", "Predicted Image URL: $imageUrl")
            Resource.Success(imageUrl)
        } catch (ex: Exception) {
            if (ex.message?.contains("502") == true) {
                Resource.Error("The Server is down, Please try again!")
            } else if (ex.message?.contains("500") == true) {
                Resource.Error("Something went wrong, Please try again!")
            } else {
                Log.e("MyRepo", "Error making predictions: ${ex.message}")
                Resource.Error(ex.message.toString())
            }
        }
    }

    override fun detectOffline(bitmap: Bitmap): List<BoundingBox> {
        return offlineDetector.detect(bitmap)
    }
}
