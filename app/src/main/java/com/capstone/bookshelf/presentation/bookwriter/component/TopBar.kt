package com.capstone.bookshelf.presentation.bookwriter.component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookwriter.BookWriterAction
import com.capstone.bookshelf.presentation.bookwriter.BookWriterState
import com.capstone.bookshelf.presentation.bookwriter.BookWriterViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

@Composable
fun TopBar(
    bookWriterViewModel: BookWriterViewModel,
    bookWriterState: BookWriterState,
    onNavigateBack: () -> Unit,
    onDrawerClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    val context = LocalContext.current
    val currentFontSize = MaterialTheme.typography.bodyMedium.fontSize
    val scope = rememberCoroutineScope()
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/epub+zip")
    ) { uri: Uri? ->
        uri?.let {
            bookWriterViewModel.onAction(
                BookWriterAction.ExportEpub(
                    context = context,
                    uri = it,
                    fontSize = currentFontSize.value
                )
            )
        }
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        IconButton(
            modifier = Modifier.statusBarsPadding(),
            onClick = {
                onNavigateBack()
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_back),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            modifier = Modifier.statusBarsPadding(),
            onClick = {
                onDrawerClick()
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_menu),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            modifier = Modifier.statusBarsPadding(),
            onClick = {
                scope.launch {
                    onSaveClick()
                    yield()
                    bookWriterViewModel.getBookTitle{ bookTitle ->
                        val fixedBookTitle = bookTitle.replace(Regex("\\s*\\(Draft\\)$"),"")
                        createDocumentLauncher.launch("$fixedBookTitle.epub")
                    }
                }
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_export),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (bookWriterState.contentList.isNotEmpty()) {
            IconButton(
                modifier = Modifier.statusBarsPadding(),
                onClick = {
                    onSaveClick()
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_save),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}