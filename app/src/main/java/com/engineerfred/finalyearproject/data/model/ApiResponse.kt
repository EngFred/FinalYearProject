package com.engineerfred.finalyearproject.data.model

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    val message: String,
    @SerializedName("image_url")
    val imageUrl: String,
)
