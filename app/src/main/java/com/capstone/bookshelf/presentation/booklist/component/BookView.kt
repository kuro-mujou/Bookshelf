package com.capstone.bookshelf.presentation.booklist.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.capstone.bookshelf.domain.wrapper.Book
import com.capstone.bookshelf.presentation.booklist.BookListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookView(
    book: Book,
    bookListState: BookListState,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
    onItemStarClick: () -> Unit,
    onItemCheckBoxClick: (Boolean, Book) -> Unit
){
    var checkBoxState by rememberSaveable { mutableStateOf(false) }
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
            Box(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
                contentAlignment = Alignment.BottomEnd
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
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 5.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier.padding(top = 2.dp, start = 4.dp),
                        text = "${book.currentChapter+1} / ${book.totalChapter}",
                        style = TextStyle(
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            background = MaterialTheme.colorScheme.surfaceContainer,
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                progress = {
                    (book.currentChapter+1).toFloat() / book.totalChapter.toFloat()
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
        if(!bookListState.isOnDeleteBooks) {
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
                        if(isSystemInDarkTheme())
                            Color(155, 212, 161)
                        else
                            Color(52, 105, 63)
                    else
                        Color.Gray,
                )
            }
        }
        LaunchedEffect(bookListState.isOnDeleteBooks) {
            if(!bookListState.isOnDeleteBooks){
                checkBoxState = false
            }
        }
        if(bookListState.isOnDeleteBooks){
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
