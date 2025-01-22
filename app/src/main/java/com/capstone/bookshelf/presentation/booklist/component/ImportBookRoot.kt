package com.capstone.bookshelf.presentation.booklist.component

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import nl.siegmann.epublib.epub.EpubReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun ImportBookRoot(
    importBookViewModel: AsyncImportBookViewModel,
) {
    val context = LocalContext.current
    val importBookLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val fileName = getFileName(context = context, uri = uri)
            val cacheFilePath = saveFileToCache(context = context, uri = uri, fileName = fileName)
            cacheFilePath?.let{
                try {
                    val book = EpubReader().readEpub(File(it).inputStream())
                    importBookViewModel.processAndSaveBook(book = book, context = context, cacheFilePath = it)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Can't open book file", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
    IconButton(
        onClick = {
            importBookLauncher.launch(arrayOf("application/epub+zip"))
        }
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = "Add Icon",
        )
    }
}
private fun getFileName(context: Context, uri: Uri): String {
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

private fun saveFileToCache(context: Context, uri: Uri, fileName: String): String? {
    return try {
        val cacheFile = File(context.cacheDir, fileName)
        cacheFile.parentFile?.mkdirs()
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.let { input ->
            val outputStream = FileOutputStream(cacheFile)
            outputStream.use { output ->
                input.copyTo(output)
            }
            return cacheFile.absolutePath
        }
        null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}