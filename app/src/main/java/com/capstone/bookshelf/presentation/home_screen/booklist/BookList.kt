package com.capstone.bookshelf.presentation.home_screen.booklist

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.home_screen.booklist.component.AsyncImportBookViewModel
import com.capstone.bookshelf.presentation.home_screen.booklist.component.BookMenuBottomSheet
import com.capstone.bookshelf.presentation.home_screen.booklist.component.GridBookView
import com.capstone.bookshelf.presentation.home_screen.booklist.component.ListBookView
import com.capstone.bookshelf.util.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookList(
    bookListViewModel: BookListViewModel,
    importBookViewModel: AsyncImportBookViewModel,
    dataStoreManager: DataStoreManager,
    onAction: (BookListAction) -> Unit,
) {
    val localBookListState by bookListViewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val gridState = rememberLazyStaggeredGridState()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val isKeyboardVisible = WindowInsets.isImeVisible
    var searchText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        bookListViewModel.onAction(BookListAction.UpdateBookListType(dataStoreManager.bookListView.first()))
    }
    LaunchedEffect(isKeyboardVisible) {
        if (!isKeyboardVisible) {
            focusManager.clearFocus()
        }
    }
    Column(
        modifier = Modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { focusManager.clearFocus() }
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!localBookListState.isOnDeleteBooks) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(
                    onClick = {
                        Log.d("BookListViewModel", "onClick: ${localBookListState.listViewType}")
                        onAction(BookListAction.UpdateBookListType())
                    }
                ) {
                    if (localBookListState.listViewType == 1) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_grid_view),
                            contentDescription = "Grid",
                            tint = if (isSystemInDarkTheme())
                                Color(154, 204, 250)
                            else
                                Color(45, 98, 139)
                        )
                    } else if (localBookListState.listViewType == 0) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_list_view),
                            contentDescription = "List",
                            tint = if (isSystemInDarkTheme())
                                Color(154, 204, 250)
                            else
                                Color(45, 98, 139)
                        )
                    }
                }
                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                    },
                    modifier = Modifier
                        .weight(1f),
                    placeholder = {
                        Text(text = "Search")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(25.dp)
                )
                Box {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_delete),
                                    contentDescription = "Delete Icon",
                                    tint = if (isSystemInDarkTheme())
                                        Color(250, 160, 160)
                                    else
                                        Color(194, 59, 34)
                                )
                            },
                            onClick = {
                                bookListViewModel.onAction(BookListAction.OnDeletingBooks(true))
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Bookmark") },
                            leadingIcon = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_bookmark_star),
                                    contentDescription = "Sorting Icon",
                                    tint = if (localBookListState.isSortedByFavorite)
                                        if (isSystemInDarkTheme())
                                            Color(155, 212, 161)
                                        else
                                            Color(52, 105, 63)
                                    else Color.Gray
                                )
                            },
                            onClick = {
                                bookListViewModel.onAction(
                                    BookListAction.OnBookListBookmarkClick(!localBookListState.isSortedByFavorite)
                                )
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        if (localBookListState.isOnDeleteBooks) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(
                    onClick = {
                        bookListViewModel.onAction(
                            BookListAction.OnConfirmDeleteBooks
                        )
                    }
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_confirm),
                        contentDescription = "Confirm Delete Icon",
                    )
                }
                IconButton(
                    onClick = {
                        bookListViewModel.onAction(
                            BookListAction.OnDeletingBooks(false)
                        )
                    }
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_cancel),
                        contentDescription = "Cancel Delete Icon",
                    )
                }
            }
        }
        Crossfade(targetState = localBookListState.listViewType) { option ->
            when (option) {
                -1 -> {
                    Box(modifier = Modifier.fillMaxSize())
                }

                0 -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        items(
                            items = localBookListState.bookList.filter {
                                it.title.contains(searchText, ignoreCase = true) ||
                                        it.authors.joinToString(",")
                                            .contains(searchText, ignoreCase = true)
                            },
                            key = { it.id }
                        ) {
                            ListBookView(
                                book = it,
                                bookListState = localBookListState,
                                onItemClick = {
                                    if (!localBookListState.isOnDeleteBooks) {
                                        onAction(BookListAction.OnBookClick(it))
                                    }
                                },
                                onItemLongClick = {
                                    if (!localBookListState.isOnDeleteBooks) {
                                        bookListViewModel.onAction(
                                            BookListAction.OnBookLongClick(it, true)
                                        )
                                        scope.launch {
                                            sheetState.show()
                                        }
                                    }
                                },
                                onItemDoubleClick = {
                                    if (!localBookListState.isOnDeleteBooks) {
                                        onAction(BookListAction.OnViewBookDetailClick(it))
                                    }
                                },
                                onItemStarClick = {
                                    bookListViewModel.onAction(
                                        BookListAction.OnBookBookmarkClick(it)
                                    )
                                },
                                onItemCheckBoxClick = { isChecked, book ->
                                    bookListViewModel.onAction(
                                        BookListAction.OnBookCheckBoxClick(isChecked, book)
                                    )
                                }
                            )
                        }
                    }
                }

                1 -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        verticalItemSpacing = 8.dp,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(all = 8.dp),
                        state = gridState,
                        content = {
                            items(
                                items = localBookListState.bookList.filter {
                                    it.title.contains(searchText, ignoreCase = true) ||
                                            it.authors.joinToString(",")
                                                .contains(searchText, ignoreCase = true)
                                },
                                key = { it.id }
                            ) {
                                GridBookView(
                                    book = it,
                                    bookListState = localBookListState,
                                    onItemClick = {
                                        if (!localBookListState.isOnDeleteBooks) {
                                            onAction(BookListAction.OnBookClick(it))
                                        }
                                    },
                                    onItemLongClick = {
                                        if (!localBookListState.isOnDeleteBooks) {
                                            bookListViewModel.onAction(
                                                BookListAction.OnBookLongClick(it, true)
                                            )
                                            scope.launch {
                                                sheetState.show()
                                            }
                                        }
                                    },
                                    onItemDoubleClick = {
                                        if (!localBookListState.isOnDeleteBooks) {
                                            onAction(BookListAction.OnViewBookDetailClick(it))
                                        }
                                    },
                                    onItemStarClick = {
                                        bookListViewModel.onAction(
                                            BookListAction.OnBookBookmarkClick(it)
                                        )
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
            }
        }
    }
    if (localBookListState.isOpenBottomSheet) {
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