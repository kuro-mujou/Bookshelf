package com.capstone.bookshelf.presentation.home_screen.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.capstone.bookshelf.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DriveInputLinkDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        var link by remember { mutableStateOf("") }
        val focusManager = LocalFocusManager.current
        val isImeVisible = WindowInsets.isImeVisible
        LaunchedEffect(isImeVisible) {
            if (!isImeVisible) {
                focusManager.clearFocus()
            }
        }
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { focusManager.clearFocus() }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 8.dp),
                    text = "Import from Google Drive",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    modifier = Modifier
                        .padding(horizontal = 8.dp),
                    text = "To import a file from Google Drive, please enter a shareable link that anyone can access.\n" +
                            "\n" +
                            "Make sure the file is set to:\n" +
                            "\"Anyone with the link can view\"\n" +
                            "\n" +
                            "Links that require sign-in or restricted access will not work.\n" +
                            "\n" +
                            "Supported formats: EPUB",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Justify
                    )
                )
                Spacer(modifier = Modifier.size(8.dp))
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    placeholder = { Text("Enter your link") },
                    value = link,
                    onValueChange = {
                        link = it
                    }
                )
                Spacer(modifier = Modifier.size(8.dp))
                Button(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .align(Alignment.End),
                    onClick = {
                        if (link.isBlank())
                            onDismiss()
                        else {
                            onDismiss()
                            onConfirm(link)
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Confirm")
                        Spacer(modifier = Modifier.size(8.dp))
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_send),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}