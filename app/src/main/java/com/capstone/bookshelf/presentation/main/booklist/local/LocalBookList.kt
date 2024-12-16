package com.capstone.bookshelf.presentation.main.booklist.local

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.bookshelf.R
import com.capstone.bookshelf.core.presentation.LoadingAnimation
import com.capstone.bookshelf.presentation.main.booklist.component.BookMenuBottomSheet
import com.capstone.bookshelf.presentation.main.booklist.component.BookView
import com.capstone.bookshelf.presentation.main.component.ImportBookRoot
import com.capstone.bookshelf.presentation.main.component.ImportBookViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalBookList(
    localBookListViewModel: LocalBookListViewModel,
    onAction: (LocalBookListAction) -> Unit,
) {
    val localBookListState by localBookListViewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val listState = rememberLazyStaggeredGridState()
    var message by remember { mutableStateOf("") }
    var progress by remember { mutableFloatStateOf(0f) }

    val importBookViewModel = koinViewModel<ImportBookViewModel>()
    Column(
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
                    onSavingBook = { sProgress, sMessage, action ->
                        progress = sProgress
                        message = sMessage
                        localBookListViewModel.onAction(action)
                    },
                    onBookSaved = {action ->
                        progress = 0f
                        message = ""
                        localBookListViewModel.onAction(action)
                    },
                )
                IconButton(
                    onClick = {
                        localBookListViewModel.onAction(
                            LocalBookListAction.OnBookListBookmarkClick(!localBookListState.isSortedByFavorite)
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
                        localBookListViewModel.onAction(
                            LocalBookListAction.OnDeletingBooks(true)
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
                        localBookListViewModel.onAction(
                            LocalBookListAction.OnConfirmDeleteBooks
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
                        localBookListViewModel.onAction(
                            LocalBookListAction.OnDeletingBooks(false)
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
                        localBookListState = localBookListState,
                        onItemClick = {
                            if(!localBookListState.isOnDeleteBooks){
                                onAction(LocalBookListAction.OnBookClick(it))
                            }
                        },
                        onItemLongClick = {
                            if(!localBookListState.isOnDeleteBooks){
                                localBookListViewModel.onAction(
                                    LocalBookListAction.OnBookLongClick(it,true)
                                )
                                scope.launch {
                                    sheetState.show()
                                }
                            }
                        },
                        onItemStarClick = {
                            localBookListViewModel.onAction(
                                LocalBookListAction.OnBookBookmarkClick(it)
                            )
                            scope.launch {
                                listState.animateScrollToItem(0)
                            }
                        },
                        onItemCheckBoxClick = { isChecked, book ->
                            localBookListViewModel.onAction(
                                LocalBookListAction.OnBookCheckBoxClick(isChecked,book)
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
                localBookListViewModel.onAction(LocalBookListAction.OnBookLongClick(null,false))
            },
            onViewBookDetails = {
                localBookListViewModel.onAction(LocalBookListAction.OnViewBookDetailClick(book = localBookListState.selectedBook!!))
                onAction(LocalBookListAction.OnViewBookDetailClick(localBookListState.selectedBook!!))
            },
            onDeleteBook = {
                localBookListViewModel.onAction(LocalBookListAction.OnBookDeleteClick(book = localBookListState.selectedBook!!))
            }
        )
    }
    if(localBookListState.isSavingBook){
        LoadingAnimation(
            progress = progress,
            message = message,
            dismissDialog = {
                localBookListViewModel.onAction(LocalBookListAction.OnSaveBook(false))
            }
        )
    }
}