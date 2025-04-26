package com.capstone.bookshelf.util

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/** Standard Composable for displaying a permission rationale dialog. */
@Composable
fun CustomAlertDialog(
    title: String = "",
    text: String = "",
    confirmButtonText: String = "",
    dismissButtonText: String = "",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmButtonText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissButtonText) }
        }
    )
}