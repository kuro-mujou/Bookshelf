package com.capstone.bookshelf.presentation.bookcontent.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette

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
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorPaletteState.backgroundColor
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Min).fillMaxWidth()
                ) {
                    VerticalDivider(
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                        thickness = 2.dp,
                        color = colorPaletteState.textColor
                    )
                    Text(
                        text = note,
                        modifier = Modifier.padding(all = 8.dp),
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            textAlign = TextAlign.Justify
                        )
                    )
                }
                OutlinedTextField(
                    value = noteContent,
                    onValueChange = {
                        noteContent = it
                    },
                    textStyle = TextStyle(
                        color = colorPaletteState.textColor
                    ),
                    maxLines = 20,
                    label = { Text("Enter your note") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = colorPaletteState.textColor,
                        unfocusedLabelColor = colorPaletteState.textColor,
                        focusedBorderColor = colorPaletteState.textColor,
                        focusedLabelColor = colorPaletteState.textColor,
                        cursorColor = colorPaletteState.textColor,
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(
                        onClick = {
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorPaletteState.textColor,
                        )
                    ) {
                        Text(
                            text = "Close",
                            style = TextStyle(
                                color = colorPaletteState.backgroundColor
                            )
                        )
                    }
                    Button(
                        onClick = {
                            //submit and create note
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorPaletteState.textColor,
                        )
                    ) {
                        Text(
                            text = "Submit",
                            style = TextStyle(
                                color = colorPaletteState.backgroundColor
                            )
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun NoteDialog(
    note: String,
    colorPaletteState: ColorPalette,
    onDismiss: () -> Unit
){
    var noteContent by remember { mutableStateOf("") }
    Dialog(
        onDismissRequest = { onDismiss()},
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorPaletteState.backgroundColor
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Min).fillMaxWidth()
                ) {
                    VerticalDivider(
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                        thickness = 2.dp,
                        color = colorPaletteState.textColor
                    )
                    Text(
                        text = note,
                        modifier = Modifier.padding(all = 8.dp),
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            textAlign = TextAlign.Justify
                        )
                    )
                }
                OutlinedTextField(
                    value = noteContent,
                    onValueChange = {
                        noteContent = it
                    },
                    textStyle = TextStyle(
                        color = colorPaletteState.textColor
                    ),
                    maxLines = 20,
                    label = { Text("Enter your note") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = colorPaletteState.textColor,
                        unfocusedLabelColor = colorPaletteState.textColor,
                        focusedBorderColor = colorPaletteState.textColor,
                        focusedLabelColor = colorPaletteState.textColor,
                        cursorColor = colorPaletteState.textColor,
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(
                        onClick = {
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorPaletteState.textColor,
                        )
                    ) {
                        Text(
                            text = "Close",
                            style = TextStyle(
                                color = colorPaletteState.backgroundColor
                            )
                        )
                    }
                    Button(
                        onClick = {
                            //submit and create note
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorPaletteState.textColor,
                        )
                    ) {
                        Text(
                            text = "Submit",
                            style = TextStyle(
                                color = colorPaletteState.backgroundColor
                            )
                        )
                    }
                }
            }
        }
    }
}