package com.capstone.bookshelf.presentation.booklist.component

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import com.capstone.bookshelf.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.siegmann.epublib.epub.EpubReader

@Composable
fun ImportBookRoot(
    importBookViewModel: AsyncImportBookViewModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val importBookLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val fileName = getFileName(context, it)
            try {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(it)?.use { inputStream ->
                            when {
                                fileName.endsWith(".epub", ignoreCase = true) -> {
                                    val book = EpubReader().readEpub(inputStream)
                                    importBookViewModel.processAndSaveBook(book, context, it.toString())
                                }
                                fileName.endsWith(".pdf", ignoreCase = true) -> {
                                    importBookViewModel.processAndSavePdf(context, it.toString())
                                    Log.d("ImportBookRoot", "PDF file selected")
                                }
                                else -> {
                                    Toast.makeText(context, "Unsupported file format", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } ?: run {
                            Toast.makeText(context, "Failed to open file", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Can't open book file", Toast.LENGTH_SHORT).show()
            }
        }
    }
    IconButton(
        onClick = {
            importBookLauncher.launch(arrayOf("application/epub+zip","application/pdf"))
        }
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_add_epub),
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