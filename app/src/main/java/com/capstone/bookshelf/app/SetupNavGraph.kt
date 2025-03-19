package com.capstone.bookshelf.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
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
import com.capstone.bookshelf.presentation.bookcontent.content.ContentAction
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
import com.capstone.bookshelf.presentation.component.LoadingAnimation
import com.capstone.bookshelf.util.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.yield
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
                var isContentLoading by remember { mutableStateOf(true) }
                val selectedBookViewModel =
                    nav.sharedKoinViewModel<SelectedBookViewModel>(navController)
                val selectedBook by selectedBookViewModel.selectedBook.collectAsStateWithLifecycle()
                val colorPaletteViewModel = koinViewModel<ColorPaletteViewModel>()
                val viewModel = koinViewModel<ContentViewModel>()
                val dataStoreManager = DataStoreManager(LocalContext.current)
                if(isContentLoading){
                    LoadingAnimation()
                }
                BackHandler(
                    onBack = {
                        if(selectedBook?.isEditable == true){
                            navController.navigate(Route.Home){
                                popUpTo(Route.Home){
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    }
                )
                LaunchedEffect(selectedBook) {
                    selectedBook?.let {
                        viewModel.onContentAction(dataStoreManager,ContentAction.SelectedBook(it))
                    }
                    colorPaletteViewModel.updateBackgroundColor(Color(dataStoreManager.backgroundColor.first()))
                    colorPaletteViewModel.updateTextColor(Color(dataStoreManager.textColor.first()))
                    colorPaletteViewModel.updateSelectedColorSet(dataStoreManager.selectedColorSet.first())
                    viewModel.onContentAction(dataStoreManager,ContentAction.UpdateFontSize(dataStoreManager.fontSize.first()))
                    viewModel.onContentAction(dataStoreManager,ContentAction.UpdateTextAlign(dataStoreManager.textAlign.first()))
                    viewModel.onContentAction(dataStoreManager,ContentAction.UpdateTextIndent(dataStoreManager.textIndent.first()))
                    viewModel.onContentAction(dataStoreManager,ContentAction.UpdateLineSpacing(dataStoreManager.lineSpacing.first()))
                    viewModel.onContentAction(dataStoreManager,ContentAction.UpdateSelectedFontFamilyIndex(dataStoreManager.fontFamily.first()))
                    yield()
                    isContentLoading = false
                }
                BookContentScreenRoot(
                    viewModel = viewModel,
                    colorPaletteViewModel = colorPaletteViewModel,
                    dataStoreManager = dataStoreManager,
                    onBackClick = {
                        selectedBookViewModel.onSelectBook(null)
                        navController.navigateUp()
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
                            Route.BookContent(bookId)
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