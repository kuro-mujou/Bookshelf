package com.capstone.bookshelf.presentation.bookwriter.component

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.capstone.bookshelf.R

@Composable
fun Indicator(
    onAdd: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
){
    Row {
        IconButton(
            onClick = {
                onAdd()
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_add_music),
                contentDescription = null,
            )
        }
        IconButton(
            onClick = {
                onDelete()
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_delete),
                contentDescription = null,
            )
        }
        IconButton(
            onClick = {
                onMoveUp()
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_up),
                contentDescription = null,
            )
        }
        IconButton(
            onClick = {
                onMoveDown()
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_down),
                contentDescription = null,
            )
        }
    }
}
@Composable
fun ChapterTitleIndicator(
    onAddParagraph: () -> Unit,
    onAddImage: () -> Unit,
    onAddSubTitle: () -> Unit,
){
    Row {
        IconButton(
            onClick = {
                onAddParagraph()
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_add_music),
                contentDescription = null,
            )
        }
        IconButton(
            onClick = {
                onAddSubTitle()
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_add_subtitile),
                contentDescription = null,
            )
        }
        IconButton(
            onClick = {
                onAddImage()
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_add_image),
                contentDescription = null,
            )
        }
    }
}