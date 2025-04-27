package com.capstone.bookshelf.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

fun saveImageToPrivateStorage(
    context: Context,
    bitmap: Bitmap?,
    filename: String
): String {
    return try {
        val file = File(context.filesDir, "$filename.webp")
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            FileOutputStream(file).use { outputStream ->
                @Suppress("DEPRECATION")
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

/**
 * Saves a Bitmap to the app's private files directory as a WEBP image.
 * Returns the absolute path if successful, or an error string otherwise.
 */
fun saveBitmapToPrivateStorage(
    context: Context,
    bitmap: Bitmap?,
    filenameWithoutExtension: String
): String {
    if (bitmap == null) {
        return "error_null_bitmap"
    }
    val safeFilename = filenameWithoutExtension.replace(Regex("[^A-Za-z0-9._-]"), "_")
    val file = File(context.filesDir, "$safeFilename.webp")
    return try {
        FileOutputStream(file).use { outputStream ->
            val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }
            bitmap.compress(format, 80, outputStream)
            outputStream.flush()
        }
        file.absolutePath
    } catch (e: IOException) {
        file.delete()
        "error_saving_image"
    } catch (e: Exception) {
        file.delete()
        "error_saving_image_unexpected"
    }
}

/**
 * Calculates `inSampleSize` to decode bitmaps efficiently.
 * */
fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.outHeight to options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

/** Decodes bitmap with sampling to prevent OOM errors. */
fun decodeSampledBitmapFromStream(
    stream: InputStream,
    reqWidth: Int,
    reqHeight: Int
): Bitmap? {
    return try {
        if (!stream.markSupported()) {
            BitmapFactory.decodeStream(stream)
        } else {
            stream.mark(10 * 1024 * 1024)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(stream, null, options)

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                stream.reset()
                return null
            }

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            stream.reset()
            BitmapFactory.decodeStream(stream, null, options)
        }
    } catch (e: IOException) {
        null
    } catch (oom: OutOfMemoryError) {
        null
    } catch (e: Exception) {
        null
    }
}

suspend fun deleteImageFromPrivateStorage(
    path: String
): Boolean = withContext(Dispatchers.IO) {
    try {
        val file = File(path)
        if (file.exists()) {
            val deleted = file.delete()
            return@withContext deleted
        } else {
            return@withContext false
        }
    } catch (e: SecurityException) {
        return@withContext false
    } catch (e: Exception) {
        return@withContext false
    }
}