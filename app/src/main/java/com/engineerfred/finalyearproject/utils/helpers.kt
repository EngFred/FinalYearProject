package com.engineerfred.finalyearproject.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

fun Uri.toBitmap(contentResolver: ContentResolver): Bitmap? {
    return try {
        val inputStream = contentResolver.openInputStream(this)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun prepareFilePart(context: Context, uri: Uri): MultipartBody.Part {
    val file = File(getRealPathFromUri(context, uri))
    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("file", file.name, requestFile)
}

// Helper function to get real file path from URI
fun getRealPathFromUri(context: Context, uri: Uri): String {
    var filePath = ""
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
        if (columnIndex != -1) {
            cursor.moveToFirst()
            filePath = cursor.getString(columnIndex)
        }
    }
    return filePath
}


fun isValidIpAddress(ip: String): Boolean {
    val parts = ip.split(".")
    if (parts.size != 4) return false
    return try {
        parts.all { it.toInt() in 0..255 }
    } catch (e: NumberFormatException) {
        false
    }
}


