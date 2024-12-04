package com.capstone.bookshelf.presentation.main.booklist.remote

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.capstone.bookshelf.R
import com.capstone.bookshelf.domain.book.wrapper.Book
import org.koin.androidx.compose.koinViewModel

@Composable
fun RemoteBookList(
    remoteBookListViewModel: RemoteBookListViewModel = koinViewModel(),
    onBookClick: (Book) -> Unit,
){
    val state by remoteBookListViewModel.state.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ){
        items(
            items = state.imageUrl,
        ){
            AsyncImage(
                model =
                if(it=="error")
                    R.mipmap.book_cover_not_available
                else
                    it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(15.dp))
            )
        }
    }
}