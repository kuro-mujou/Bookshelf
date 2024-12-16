package com.capstone.bookshelf.presentation.main.booklist.remote

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.bookshelf.domain.wrapper.Book

@Composable
fun RemoteBookList(
    remoteBookListViewModel: RemoteBookListViewModel,
    onBookClick: (Book) -> Unit,
){
    val state by remoteBookListViewModel.state.collectAsStateWithLifecycle()

}