package com.capstone.bookshelf.feature.booklist.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.capstone.bookshelf.core.navigation.Screen
import com.capstone.bookshelf.core.presentation.component.ErrorView
import com.capstone.bookshelf.core.presentation.component.LoadingAnimation
import com.capstone.bookshelf.core.util.DisplayResult
import com.capstone.bookshelf.feature.booklist.presentation.component.BookView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookList(
    navController: NavController,
    modifier: Modifier,
    toggleSort: Boolean,
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyStaggeredGridState()
    val viewModel = koinViewModel<BookListViewModel>()
    val books by viewModel.books
    LaunchedEffect(toggleSort) {
        viewModel.updateSortedByFavorite(toggleSort)
        scope.launch {
            delay(100)
            listState.animateScrollToItem(0)
        }
    }
    books.DisplayResult(
        onLoading = { LoadingAnimation() },
        onError = { ErrorView(it) },
        onSuccess = { data ->
            if (data.isNotEmpty()) {
                var contextBookId by rememberSaveable { mutableStateOf<Int?>(null) }
                val sheetState = rememberModalBottomSheetState()
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    verticalItemSpacing = 8.dp,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = modifier
                        .fillMaxSize()
                        .padding(all = 8.dp),
                    state = listState,
                    content = {
                        items(
                            data,
                            key = { it.bookId }
                        ) {
                            BookView(
                                book = it,
                                onItemClick = {
                                    navController.navigate(Screen.BookContent.passVal(it.bookId, it.currentChapter-1))
                                },
                                onItemLongClick = { contextBookId = it.bookId },
                                onItemStarClick = { viewModel.toggleFavorite(it.bookId, !it.isFavorite) },
                            )
                        }
                    }
                )
                if (contextBookId != null) {
                    ModalBottomSheet(
                        onDismissRequest = { contextBookId = null },
                        sheetState = sheetState,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "Hello")
                        }
                    }
                }
            } else {
                ErrorView()
            }
        }
    )
}