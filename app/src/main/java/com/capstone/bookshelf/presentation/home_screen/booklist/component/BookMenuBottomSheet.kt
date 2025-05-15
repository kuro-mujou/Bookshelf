package com.capstone.bookshelf.presentation.home_screen.booklist.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.capstone.bookshelf.presentation.home_screen.booklist.BookListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookMenuBottomSheet(
    sheetState: SheetState,
    state: BookListState,
    onDismiss: () -> Unit,
    onViewBookDetails: () -> Unit,
    onDeleteBook: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = state.selectedBook!!.title,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                    fontWeight = FontWeight.Medium
                ),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = state.selectedBook.authors.joinToString(","),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Normal
                )
            )
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onDismiss()
                    onViewBookDetails()
                }
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "•View Book Details",
                        textAlign = TextAlign.Start
                    )
                }
            }
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onDismiss()
                    onDeleteBook()
                }
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "•Delete",
                        style = TextStyle(
                            color = Color.Red
                        ),
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}