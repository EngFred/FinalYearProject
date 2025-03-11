package com.engineerfred.finalyearproject.data.remote

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    val message: String,
    @SerializedName("image_url")
    val imageUrl: String,
)
