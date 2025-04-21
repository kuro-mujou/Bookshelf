package com.capstone.bookshelf.presentation.bookwriter.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookwriter.BookWriterState
import com.mohamedrejeb.richeditor.model.RichTextState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorControls(
    bookWriterState: BookWriterState,
    selectedState: RichTextState?,
    onAction: (FormatAction) -> Unit,
) {
    val underlineActive =
        selectedState?.currentSpanStyle?.textDecoration?.contains(TextDecoration.Underline) == true
    val strikethroughActive =
        selectedState?.currentSpanStyle?.textDecoration?.contains(TextDecoration.LineThrough) == true
    val boldActive = selectedState?.currentSpanStyle?.fontWeight == FontWeight.Bold
    val italicActive = selectedState?.currentSpanStyle?.fontStyle == FontStyle.Italic

    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier
                .background(
                    color = if (boldActive)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 2.dp,
                    color = if (boldActive)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .size(36.dp),
            onClick = {
                onAction(FormatAction.BOLD)
            }
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_text_bold),
                contentDescription = "Bold",
                tint = if (boldActive)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            modifier = Modifier
                .background(
                    color = if (italicActive)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 2.dp,
                    color = if (italicActive)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .size(36.dp),
            onClick = {
                onAction(FormatAction.ITALIC)
            }
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_text_italic),
                contentDescription = "Italic",
                tint = if (italicActive)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            modifier = Modifier
                .background(
                    color = if (underlineActive)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 2.dp,
                    color = if (underlineActive)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .size(36.dp),
            onClick = {
                onAction(FormatAction.UNDERLINE)
            }
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_text_underline),
                contentDescription = "Underline",
                tint = if (underlineActive)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            modifier = Modifier
                .background(
                    color = if (strikethroughActive)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 2.dp,
                    color = if (strikethroughActive)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .size(36.dp),
            onClick = {
                onAction(FormatAction.STRIKETHROUGH)
            }
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_text_strikethrough),
                contentDescription = "Strikethrough",
                tint = if (strikethroughActive)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            modifier = Modifier
                .background(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.error,
                    shape = RoundedCornerShape(8.dp)
                )
                .size(36.dp),
            onClick = {
                onAction(FormatAction.CLEAR)
            }
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_text_clear_format),
                contentDescription = "Clear format",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

enum class FormatAction {
    BOLD,
    ITALIC,
    UNDERLINE,
    STRIKETHROUGH,
    CLEAR
}