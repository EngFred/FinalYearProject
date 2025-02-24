package com.engineerfred.finalyearproject

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream

fun Uri.toBitmap(contentResolver: ContentResolver): Bitmap? {
    return try {
        // Open input stream from the URI
        val inputStream = contentResolver.openInputStream(this)
        // Decode the input stream to a Bitmap
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null // Return null if there's an error
    }
}

fun getBitmapFromUri(context: Context, uri: Uri): Bitmap {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    return Bitmap.createBitmap(
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    )
}


fun getImageRotation(uri: Uri, contentResolver: ContentResolver): Int {
    var rotation = 0
    try {
        // Open input stream from the URI
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        // Create an ExifInterface object using the input stream
        inputStream?.let {
            val exif = ExifInterface(it)
            // Get the orientation of the image
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            rotation = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0 // Default to no rotation
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return rotation
}


