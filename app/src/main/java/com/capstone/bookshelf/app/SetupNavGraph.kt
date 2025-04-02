package com.capstone.bookshelf.app

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.capstone.bookshelf.presentation.SelectedBookViewModel
import com.capstone.bookshelf.presentation.bookcontent.BookContentScreenRoot
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPaletteViewModel
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.presentation.bookdetail.BookDetailAction
import com.capstone.bookshelf.presentation.bookdetail.BookDetailScreenRoot
import com.capstone.bookshelf.presentation.bookdetail.BookDetailViewModel
import com.capstone.bookshelf.presentation.booklist.BookList
import com.capstone.bookshelf.presentation.booklist.BookListAction
import com.capstone.bookshelf.presentation.booklist.BookListViewModel
import com.capstone.bookshelf.presentation.booklist.component.AsyncImportBookViewModel
import com.capstone.bookshelf.presentation.bookwriter.BookWriterCreate
import com.capstone.bookshelf.presentation.bookwriter.BookWriterViewModel
import com.capstone.bookshelf.util.DataStoreManager
import org.koin.androidx.compose.koinViewModel

@UnstableApi
@Composable
fun SetupNavGraph(
    navController: NavHostController,
) {
    val importBookViewModel = koinViewModel<AsyncImportBookViewModel>()
    NavHost(
        navController = navController,
        startDestination = Route.BookGraph
    ) {
        navigation<Route.BookGraph>(
            startDestination = Route.Home,
        ){
            composable<Route.Home>(
                exitTransition = { slideOutHorizontally() },
                popEnterTransition = { slideInHorizontally() },
            ){nav ->
                val bookListViewModel = koinViewModel<BookListViewModel>()
                val selectedBookViewModel =
                    nav.sharedKoinViewModel<SelectedBookViewModel>(navController)
                LaunchedEffect(true) {
                    selectedBookViewModel.onSelectBook(null)
                }
                BookList(
                    bookListViewModel = bookListViewModel,
                    importBookViewModel = importBookViewModel,
                    onAction = {action->
                        when(action){
                            is BookListAction.OnBookClick -> {
                                selectedBookViewModel.onSelectBook(action.book)
                                navController.navigate(
                                    Route.BookContent(action.book.id)
                                )
                            }
                            is BookListAction.OnViewBookDetailClick -> {
                                selectedBookViewModel.onSelectBook(action.book)
                                navController.navigate(
                                    Route.BookDetail(action.book.id)
                                )
                            }
                            is BookListAction.OnWritingNewBook -> {
                                navController.navigate(
                                    Route.WriteBook
                                )
                            }
                            else -> Unit
                        }
                    }
                )
            }
            composable<Route.BookDetail>(
                exitTransition = { slideOutHorizontally() },
                popEnterTransition = { slideInHorizontally() },
            ) { nav ->
                val selectedBookViewModel =
                    nav.sharedKoinViewModel<SelectedBookViewModel>(navController)
                val viewModel = koinViewModel<BookDetailViewModel>()
                val selectedBook by selectedBookViewModel.selectedBook.collectAsStateWithLifecycle()

                LaunchedEffect(selectedBook) {
                    selectedBook?.let {
                        viewModel.onAction(BookDetailAction.OnSelectedBookChange(it))
                    }
                }
                BookDetailScreenRoot(
                    viewModel = viewModel,
                    onBackClick = {
                        navController.navigateUp()
                    }
                )
            }
            composable<Route.BookContent>(
                exitTransition = { slideOutHorizontally() },
                popEnterTransition = { slideInHorizontally() },
            ){ nav ->
                val colorPaletteViewModel = koinViewModel<ColorPaletteViewModel>()
                val viewModel = koinViewModel<ContentViewModel>()
                val dataStoreManager = DataStoreManager(LocalContext.current)
                BookContentScreenRoot(
                    viewModel = viewModel,
                    colorPaletteViewModel = colorPaletteViewModel,
                    dataStoreManager = dataStoreManager,
                    onBackClick = { back->
                        if(back){
                            navController.navigate(Route.Home){
                                popUpTo(Route.Home){
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigateUp()
                        }
                    },
                )
            }
            composable<Route.WriteBook>(
                exitTransition = { slideOutHorizontally() },
                popEnterTransition = { slideInHorizontally() },
            ){nav ->
                val bookWriterViewModel = koinViewModel<BookWriterViewModel>()
                val selectedBookViewModel =
                    nav.sharedKoinViewModel<SelectedBookViewModel>(navController)
                BookWriterCreate(
                    viewModel = bookWriterViewModel,
                    onNavigateToBookContent = { bookId,book ->
                        selectedBookViewModel.onSelectBook(book)
                        navController.navigate(
                            Route.Home
                        )
                    }
                )
            }
        }
    }
}

@Composable
private inline fun <reified T: ViewModel> NavBackStackEntry.sharedKoinViewModel(
    navController: NavController
): T {
    val navGraphRoute = destination.parent?.route ?: return koinViewModel<T>()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return koinViewModel(
        viewModelStoreOwner = parentEntry
    )
}