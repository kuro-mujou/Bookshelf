package com.capstone.bookshelf.presentation.booklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.booklist.component.AsyncImportBookViewModel
import com.capstone.bookshelf.presentation.booklist.component.BookMenuBottomSheet
import com.capstone.bookshelf.presentation.booklist.component.BookView
import com.capstone.bookshelf.presentation.booklist.component.ImportBookRoot
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookList(
    bookListViewModel: BookListViewModel,
    importBookViewModel: AsyncImportBookViewModel,
    onAction: (BookListAction) -> Unit,
) {
    val localBookListState by bookListViewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val listState = rememberLazyStaggeredGridState()
    Column(
        modifier = Modifier.statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(!localBookListState.isOnDeleteBooks){
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ){
                ImportBookRoot(
                    importBookViewModel = importBookViewModel,
                )
                IconButton(
                    onClick = {
                        bookListViewModel.onAction(
                            BookListAction.OnBookListBookmarkClick(!localBookListState.isSortedByFavorite)
                        )
                        scope.launch {
                            listState.animateScrollToItem(0)
                        }
                    }
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_rounded_bookmark_star),
                        contentDescription = "Sorting Icon",
                        tint = if (localBookListState.isSortedByFavorite) Color.Green else Color.Gray
                    )
                }
                IconButton(
                    onClick = {
                        bookListViewModel.onAction(
                            BookListAction.OnDeletingBooks(true)
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Icon",
                    )
                }
            }
        }
        if(localBookListState.isOnDeleteBooks){
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ){
                IconButton(
                    onClick = {
                        bookListViewModel.onAction(
                            BookListAction.OnConfirmDeleteBooks
                        )
                    }
                ){
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = "Confirm Delete Icon",
                    )
                }
                IconButton(
                    onClick = {
                        bookListViewModel.onAction(
                            BookListAction.OnDeletingBooks(false)
                        )
                    }
                ){
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cancel Delete Icon",
                    )
                }
            }
        }
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 8.dp),
            state = listState,
            content = {
                items(
                    items = localBookListState.bookList,
                    key = { it.id }
                ) {
                    BookView(
                        book = it,
                        bookListState = localBookListState,
                        onItemClick = {
                            if(!localBookListState.isOnDeleteBooks){
                                onAction(BookListAction.OnBookClick(it))
                            }
                        },
                        onItemLongClick = {
                            if(!localBookListState.isOnDeleteBooks){
                                bookListViewModel.onAction(
                                    BookListAction.OnBookLongClick(it, true)
                                )
                                scope.launch {
                                    sheetState.show()
                                }
                            }
                        },
                        onItemStarClick = {
                            bookListViewModel.onAction(
                                BookListAction.OnBookBookmarkClick(it)
                            )
                            scope.launch {
                                listState.animateScrollToItem(0)
                            }
                        },
                        onItemCheckBoxClick = { isChecked, book ->
                            bookListViewModel.onAction(
                                BookListAction.OnBookCheckBoxClick(isChecked, book)
                            )
                        }
                    )
                }
            }
        )
    }
    if(localBookListState.isOpenBottomSheet){
        BookMenuBottomSheet(
            sheetState = sheetState,
            state = localBookListState,
            onDismiss = {
                bookListViewModel.onAction(BookListAction.OnBookLongClick(null, false))
            },
            onViewBookDetails = {
                bookListViewModel.onAction(BookListAction.OnViewBookDetailClick(book = localBookListState.selectedBook!!))
                onAction(BookListAction.OnViewBookDetailClick(localBookListState.selectedBook!!))
            },
            onDeleteBook = {
                bookListViewModel.onAction(BookListAction.OnBookDeleteClick(book = localBookListState.selectedBook!!))
            }
        )
    }
}