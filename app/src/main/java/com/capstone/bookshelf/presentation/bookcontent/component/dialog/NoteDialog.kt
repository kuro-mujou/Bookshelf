package com.capstone.bookshelf.presentation.bookcontent.component.dialog

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette

@Composable
fun NoteDialog(
    note: String,
    colorPaletteState: ColorPalette,
    onDismiss: () -> Unit
){
    Dialog(
        onDismissRequest = { onDismiss()},
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        BackHandler(true) {}
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = note,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        color = colorPaletteState.textColor
                    )
                )
                Button(
                    onClick = {
                        onDismiss()
                    }
                ) {
                    Text(text = "Close")
                }
            }
        }
    }
}
@Composable
fun NoteDialog(
    note: AnnotatedString,
    colorPaletteState: ColorPalette,
    onDismiss: () -> Unit
){
    var noteContent by remember { mutableStateOf("") }
    Dialog(
        onDismissRequest = { onDismiss()},
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        BackHandler(true) {}
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = note,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        color = colorPaletteState.textColor
                    )
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = noteContent,
                    onValueChange = {
                        noteContent = it
                    },
                    maxLines = 10
                )
                Button(
                    onClick = {
                        onDismiss()
                    }
                ) {
                    Text(
                        text = "Close",
                        style = TextStyle(
                            color = colorPaletteState.textColor
                        )
                    )
                }
            }
        }
    }
}