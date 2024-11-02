package com.capstone.bookshelf.feature.importbook.presentation.component

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun ImportBookFloatingButton(
    onSavingBook: (Float, String) -> Unit,
    onBookSaved: @Composable () -> Unit,
) {
    val importBookViewModel = koinViewModel<ImportBookViewModel>()
    var cacheFilePath by remember { mutableStateOf<String?>(null) }
    var book by remember { mutableStateOf<Book?>(null) }
    val context = LocalContext.current
    val isLoading by importBookViewModel.isLoading.collectAsState()
    val progress by importBookViewModel.progress.collectAsState()
    val message by importBookViewModel.message.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                val fileName = getFileName(context, uri)

                cacheFilePath = saveFileToCache(context, uri, fileName)

                cacheFilePath?.let {
                    book = loadEpubFile(it)
                }

                book?.let {
                    cacheFilePath?.let { filePath ->
                        importBookViewModel.processAndSaveBook(it, context, filePath)
                    }
                }
            }
        }
    )
    if (isLoading) {
        onSavingBook(progress, message)
    } else{
        onBookSaved()
    }
    ExtendedFloatingActionButton(
        onClick = {
            launcher.launch(arrayOf("application/epub+zip"))
        }
    ){
        Icon(
            Icons.Rounded.Create,
            contentDescription = "import book"
        )
        Text(text = "Import Book")
    }

}
private fun loadEpubFile(filePath:String): Book {
    val inputStream = FileInputStream(File(filePath))
    return EpubReader().readEpub(inputStream)
}
private fun getFileName(context: Context, uri: Uri): String {
    var fileName = "unknown"
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                fileName = it.getString(nameIndex)
            }
        }
    }
    return fileName
}
private fun saveFileToCache(context: Context, uri: Uri, fileName: String): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val cacheFile = File(context.cacheDir, fileName)
        val outputStream = FileOutputStream(cacheFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        cacheFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}