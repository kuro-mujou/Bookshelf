package com.capstone.bookshelf.presentation.bookcontent.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTOCDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    Dialog(
        onDismissRequest = {
            onDismissRequest()
        }
    ) {
        var text by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }
        val options = listOf("H1", "H2", "H3", "H4", "H5", "H6")
        var selectedHeaderSize by remember { mutableStateOf(options[0]) }
        val focusManager = LocalFocusManager.current
        val isImeVisible = WindowInsets.isImeVisible
        LaunchedEffect(expanded) {
            if (!expanded) {
                focusManager.clearFocus()
            }
        }
        LaunchedEffect(isImeVisible) {
            if (!isImeVisible) {
                focusManager.clearFocus()
            }
        }
        Surface(
            modifier = Modifier.clip(RoundedCornerShape(15.dp))
        ) {
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
                    placeholder = {
                        Text(text = "Add new Chapter")
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {
                        expanded = it
                    }
                ) {
                    OutlinedTextField(
                        value = selectedHeaderSize,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text(text = "Header Size")
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {
                        options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedHeaderSize = option
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
                Button(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 15.dp),
                    onClick = {
                        onDismissRequest()
                        onConfirm(text, selectedHeaderSize)
                    }
                ) {
                    Text(text = "Add")
                }
            }
        }
    }
}