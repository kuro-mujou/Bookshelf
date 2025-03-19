package com.capstone.bookshelf.presentation.bookcontent.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AddTOCDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit
){
    Dialog(
        onDismissRequest = {
            onDismissRequest()
        }
    ) {
        var text by remember { mutableStateOf("") }
        Surface(
            modifier = Modifier.clip(RoundedCornerShape(15.dp))
        ){
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    label = {
                        Text(text = "Add new Chapter")
                    }
                )
                Button(
                    modifier = Modifier.align(Alignment.End).padding(top = 15.dp),
                    onClick = {
                        onDismissRequest()
                        onConfirm(text)
                    }
                ) {
                    Text(text = "Add")
                }
            }
        }
    }
}