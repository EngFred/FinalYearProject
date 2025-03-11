package com.engineerfred.finalyearproject.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST(ApiConstants.PREDICT_ENDPOINT)
    suspend fun predict(@Part file: MultipartBody.Part): ApiResponse
}