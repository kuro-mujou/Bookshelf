package com.capstone.bookshelf.presentation.booklist.component

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import com.capstone.bookshelf.R
import kotlinx.coroutines.launch

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
                    when {
                        fileName.endsWith(".epub", ignoreCase = true) -> {
                            importBookViewModel.processAndSaveBook(context, it.toString())
                        }
                        fileName.endsWith(".pdf", ignoreCase = true) -> {
                            importBookViewModel.processAndSavePdf(context, it.toString(),fileName)
                        }
                        fileName.endsWith(".cbz", ignoreCase = true) -> {
                            importBookViewModel.processAndSaveCBZ(context, it.toString(),fileName)
                        }
                        else -> {
                            Toast.makeText(context, "Unsupported file format", Toast.LENGTH_SHORT).show()
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
            importBookLauncher.launch(arrayOf(
                "application/*"
            ))
        }
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_add_epub),
            contentDescription = "Add Icon",
            tint = if(isSystemInDarkTheme()) Color(255, 250, 160) else Color(131,105,83)
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