package com.capstone.bookshelf.util

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Suppress("DEPRECATION")
fun saveImageToPrivateStorage(
    context: Context,
    bitmap: Bitmap?,
    filename: String
): String {
    return try {
        val file = File(context.filesDir, "$filename.webp")
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
            FileOutputStream(file).use { outputStream ->
                bitmap?.compress(Bitmap.CompressFormat.WEBP, 80, outputStream)
            }
        } else {
            FileOutputStream(file).use { outputStream ->
                bitmap?.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, outputStream)
            }
        }
        file.absolutePath
    } catch (e: IOException) {
        e.printStackTrace()
        "error when loading image"
    }
}