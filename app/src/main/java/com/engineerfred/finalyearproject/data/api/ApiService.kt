package com.engineerfred.finalyearproject.data.api

import com.engineerfred.finalyearproject.constants.Constants
import com.engineerfred.finalyearproject.data.model.ApiResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST(Constants.PREDICT_ENDPOINT)
    suspend fun predict(@Part file: MultipartBody.Part): ApiResponse
}