package com.capstone.bookshelf.presentation.bookwriter.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookwriter.BookWriterState

@Composable
fun TopBar(
    bookWriterState: BookWriterState,
    onNavigateBack: () -> Unit,
    onDrawerClick: () -> Unit,
    onSaveClick: () -> Unit,
){
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