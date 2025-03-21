package com.engineerfred.finalyearproject.data.repo

import android.graphics.Bitmap
import android.util.Log
import com.engineerfred.finalyearproject.data.local.OfflineDetector1
import com.engineerfred.finalyearproject.data.remote.ApiService
import com.engineerfred.finalyearproject.domain.repo.AppRepository
import com.engineerfred.finalyearproject.domain.model.BoundingBox
import com.engineerfred.finalyearproject.core.resource.Resource
import com.engineerfred.finalyearproject.data.local.OfflineDetector2
import com.engineerfred.finalyearproject.data.local.OfflineDetector3
import com.engineerfred.finalyearproject.domain.model.Detector
import okhttp3.MultipartBody
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val offlineDetector1: OfflineDetector1,
    private val offlineDetector2: OfflineDetector2,
    private val offlineDetector3: OfflineDetector3
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

    override suspend fun detectOffline(detector:Detector, bitmap: Bitmap): List<BoundingBox> {
        return when(detector){
            Detector.DETECTOR_1 -> offlineDetector1.detect(bitmap)
            Detector.DETECTOR_2 -> offlineDetector2.detect(bitmap)
            else -> offlineDetector3.detect(bitmap)
        }
    }
}
