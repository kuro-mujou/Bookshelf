package com.capstone.bookshelf.presentation.main.booklist.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.capstone.bookshelf.R
import com.capstone.bookshelf.domain.book.wrapper.Book
import com.capstone.bookshelf.presentation.main.booklist.local.LocalBookListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookView(
    book: Book,
    localBookListState: LocalBookListState,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
    onItemStarClick: () -> Unit,
    onItemCheckBoxClick: (Boolean, Book) -> Unit
){
    var checkBoxState by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .combinedClickable(
                onClick = { onItemClick() },
                onLongClick = { onItemLongClick() },
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ){
            AsyncImage(
                model =
                    if(book.coverImagePath=="error")
                        R.mipmap.book_cover_not_available
                    else
                        book.coverImagePath,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(15.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                progress = {
                    book.currentChapter.toFloat() / book.totalChapter.toFloat()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = book.title,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    fontWeight = FontWeight.Medium
                ),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = book.authors.joinToString(","),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )
            )
        }
        if(!localBookListState.isOnDeleteBooks) {
            IconButton(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(15.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                onClick = {
                    onItemStarClick()
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_bookmark),
                    contentDescription = null,
                    tint = if (book.isFavorite)
                        Color.Green
                    else
                        Color.Gray,
                )
            }
        }
        if(localBookListState.isOnDeleteBooks){
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(15.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Checkbox(
                    checked = checkBoxState,
                    onCheckedChange = {
                        checkBoxState = it
                        onItemCheckBoxClick(it, book)
                    },
                )
            }
        }
    }
}
