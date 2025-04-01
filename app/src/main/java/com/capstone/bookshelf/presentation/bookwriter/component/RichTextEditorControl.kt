package com.capstone.bookshelf.presentation.bookwriter.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookwriter.BookWriterState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorControls(
    bookWriterState: BookWriterState,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    onStrikethroughClick: () -> Unit,
    onAlignClick: () -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ToggleControlWrapper(
            selected = bookWriterState.toggleBold,
            onClick = onBoldClick
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_text_bold),
                contentDescription = "Bold Control",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        ToggleControlWrapper(
            selected = bookWriterState.toggleItalic,
            onClick = onItalicClick
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_text_italic),
                contentDescription = "Italic Control",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        ToggleControlWrapper(
            selected = bookWriterState.toggleUnderline,
            onClick = onUnderlineClick
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_text_underline),
                contentDescription = "Underline Control",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        ToggleControlWrapper(
            selected = bookWriterState.toggleStrikethrough,
            onClick = onStrikethroughClick
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_text_strikethrough),
                contentDescription = "Underline Control",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        SwitchControlWrapper(
            selected = bookWriterState.toggleAlign,
            onClick = onAlignClick
        ) {
            when(bookWriterState.toggleAlign) {
                1 -> Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_align_justify),
                    contentDescription = "Start Align Control",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                2 -> Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_align_left),
                    contentDescription = "Start Align Control",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                3 -> Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_align_center),
                    contentDescription = "Start Align Control",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
