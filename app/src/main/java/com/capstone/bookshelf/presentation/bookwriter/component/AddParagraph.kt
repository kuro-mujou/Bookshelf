package com.capstone.bookshelf.presentation.bookwriter.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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

@Composable
fun AddParagraph(
    paragraph : String,
    index : Int
){
    var selected by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    LaunchedEffect(paragraph){
        text = paragraph
    }
    Column(
        modifier = Modifier.wrapContentSize()
    ){
        AnimatedVisibility(
            visible = selected
        ) {
            ComponentIndicator(
                onAddSubTitle = {

                },
                onAddParagraph = {

                },
                onEditChapterTitle = {

                }
            )
        }
        Row {
            OutlinedTextField(
                modifier = Modifier
                    .weight(1f),
                value = text,
                onValueChange = {newText ->
                    text = newText
                },
                label = {
                    Text(
                        text = "Paragraph ${index + 1}"
                    )
                }
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
            ComponentIndicator(
                onAddSubTitle = {

                },
                onAddParagraph = {

                },
                onEditChapterTitle = {

                }
            )
        }
    }
}