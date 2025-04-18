package com.capstone.bookshelf.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

fun getFileName(context: Context, uri: Uri): String {
    var fileName = "unknown"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) {
                val name = cursor.getString(nameIndex)
                if (!name.isNullOrBlank()) {
                    fileName = name
                }
            }
        }
    }
    return fileName
}