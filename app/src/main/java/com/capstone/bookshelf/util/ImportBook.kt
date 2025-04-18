package com.capstone.bookshelf.util

import android.content.Context
import android.widget.Toast
import androidx.core.uri.Uri
import com.capstone.bookshelf.presentation.booklist.component.AsyncImportBookViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class ImportBook(
    private val context: Context,
    private val scope: CoroutineScope
) {
    val importBookViewModel: AsyncImportBookViewModel by inject(AsyncImportBookViewModel::class.java)
    fun importBook(uri: Uri?) {
        uri?.let {
            val fileName = getFileName(context, it)
            try {
                scope.launch {
                    when {
                        fileName.endsWith(".epub", ignoreCase = true) -> {
                            importBookViewModel.processAndSaveBook(context, it.toString(), fileName)
                        }

                        fileName.endsWith(".pdf", ignoreCase = true) -> {
                            importBookViewModel.processAndSavePdf(context, it.toString(), fileName)
                        }

                        fileName.endsWith(".cbz", ignoreCase = true) -> {
                            importBookViewModel.processAndSaveCBZ(context, it.toString(), fileName)
                        }

                        else -> {
                            Toast.makeText(context, "Unsupported file format", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Can't open book file", Toast.LENGTH_SHORT).show()
            }
        }
    }
}