package com.capstone.bookshelf.presentation.bookwriter.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.capstone.bookshelf.R

@Composable
fun TopIndicator(
    onAddParagraph: () -> Unit,
    onAddImage: () -> Unit,
){
    Row(
        modifier = Modifier.fillMaxWidth()
    ){
        IconButton(onClick = { onAddParagraph() }) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_add_paragraph),
                contentDescription = null,
            )
        }
        IconButton(onClick = { onAddImage() }) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_add_image),
                contentDescription = null,
            )
        }
    }
}

@Composable
fun BottomIndicator(
    onAddParagraph: () -> Unit,
    onAddImage: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
){
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = { onAddParagraph() }) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_add_paragraph),
                contentDescription = null,
            )
        }
        IconButton(onClick = { onAddImage() }) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_add_image),
                contentDescription = null,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { onDelete() }) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_delete),
                contentDescription = null,
            )
        }
        IconButton(onClick = { onMoveUp() }) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_up),
                contentDescription = null,
            )
        }
        IconButton( onClick = { onMoveDown() }) {
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
){
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = {
                onAddParagraph()
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_add_paragraph),
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