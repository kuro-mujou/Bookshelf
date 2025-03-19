package com.capstone.bookshelf.presentation.bookwriter.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.capstone.bookshelf.R
import com.capstone.bookshelf.domain.wrapper.TableOfContent

@Composable
fun AddChapterTitle(
    currentTOC: TableOfContent?,
    onAddParagraph: () -> Unit
) {
    var selected by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    LaunchedEffect(currentTOC){
        currentTOC?.title?.let {
            text = it
        }
    }
    Row {
        OutlinedTextField(
            modifier = Modifier
                .weight(1f),
            value = text,
            onValueChange = {newText ->
                text = newText
            },
        )
        IconButton(
            onClick = {
                selected = !selected
            }
        ){
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_right),
                contentDescription = null
            )
        }
    }
    AnimatedVisibility(
        visible = selected
    ) {
        ChapterTitleIndicator(
            onAddSubTitle = {

            },
            onAddParagraph = {
                onAddParagraph()
            },
            onEditChapterTitle = {

            }
        )
    }
}