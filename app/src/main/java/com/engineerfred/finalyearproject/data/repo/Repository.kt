package com.engineerfred.finalyearproject.data.repo

import android.util.Log
import com.engineerfred.finalyearproject.data.api.RetrofitClient
import com.engineerfred.finalyearproject.resource.Resource
import okhttp3.MultipartBody

class Repository(baseUrl: String) {
    private val apiService = RetrofitClient.getInstance(baseUrl)
    suspend fun predict(file: MultipartBody.Part): Resource<String?> {
        return try {
            Log.d("MyRepo", "Predicting...")
            val imageUrl = apiService.predict(file).imageUrl
            Log.d("MyRepo", "Predicted Image URL: $imageUrl")
            Resource.Success(imageUrl)
        } catch (ex: Exception) {
            Log.e("MyRepo", "Error making predictions: ${ex.message}")
            Resource.Error(ex.message.toString())
        }
    }
}
