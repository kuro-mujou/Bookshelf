package com.capstone.bookshelf.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.capstone.bookshelf.presentation.SelectedBookViewModel
import com.capstone.bookshelf.presentation.bookcontent.BookContentRootAction
import com.capstone.bookshelf.presentation.bookcontent.BookContentRootViewModel
import com.capstone.bookshelf.presentation.bookcontent.BookContentScreenRoot
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPaletteViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.font.FontAction
import com.capstone.bookshelf.presentation.bookcontent.component.font.FontViewModel
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.presentation.bookdetail.BookDetailAction
import com.capstone.bookshelf.presentation.bookdetail.BookDetailScreenRoot
import com.capstone.bookshelf.presentation.bookdetail.BookDetailViewModel
import com.capstone.bookshelf.presentation.booklist.BookList
import com.capstone.bookshelf.presentation.booklist.BookListAction
import com.capstone.bookshelf.presentation.booklist.BookListViewModel
import com.capstone.bookshelf.presentation.booklist.component.AsyncImportBookViewModel
import com.capstone.bookshelf.presentation.component.LoadingAnimation
import com.capstone.bookshelf.util.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.yield
import org.koin.androidx.compose.koinViewModel

@Composable
fun SetupNavGraph(
    navController: NavHostController,
) {
    val importBookViewModel = koinViewModel<AsyncImportBookViewModel>()
    var launchAlertDialog by remember{ mutableStateOf(false)}
    if(launchAlertDialog){
        Dialog(
            onDismissRequest = {
                launchAlertDialog = false
            }
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary)
            ) {
                Column {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "Please wait about few second to load remaining chapters"
                    )
                }
            }
        }
    }
    NavHost(
        navController = navController,
        startDestination = Route.BookGraph
    ) {
        navigation<Route.BookGraph>(
            startDestination = Route.Home,
        ){
            composable<Route.Home>(
                exitTransition = { slideOutHorizontally() },
                popEnterTransition = { slideInHorizontally() }
            ){
                val bookListViewModel = koinViewModel<BookListViewModel>()
                val selectedBookViewModel =
                    it.sharedKoinViewModel<SelectedBookViewModel>(navController)
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
                            else -> Unit
                        }
                    }
                )
            }
            composable<Route.BookDetail>(
                enterTransition = {
                    slideInHorizontally { initialOffset ->
                        initialOffset
                    }
                },
                exitTransition = {
                    slideOutHorizontally { initialOffset ->
                        initialOffset
                    }
                }
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
            composable<Route.BookContent>{ nav ->
                var isContentLoading by remember { mutableStateOf(true) }
                val selectedBookViewModel =
                    nav.sharedKoinViewModel<SelectedBookViewModel>(navController)
                val viewModel = koinViewModel<BookContentRootViewModel>()
                val colorPaletteViewModel = koinViewModel<ColorPaletteViewModel>()
                val contentViewModel = koinViewModel<ContentViewModel>()
                val fontViewModel = koinViewModel<FontViewModel>()
                val dataStoreManager = DataStoreManager(LocalContext.current)
                val selectedBook by selectedBookViewModel.selectedBook.collectAsStateWithLifecycle()
                var currentChapterIndex by remember { mutableIntStateOf(0) }
                if(isContentLoading){
                    LoadingAnimation()
                }
                BackHandler {

                }
                LaunchedEffect(selectedBook) {
                    selectedBook?.let { sbook->
                        viewModel.onAction(BookContentRootAction.SelectedBook(sbook))
//                        val book = sbook.storagePath?.let { loadEpubFile(it) }
//                        if (book != null) {
//                            importBookViewModel.processAndSaveBook(book, context, sbook.storagePath)
//                        }
                    }
                    currentChapterIndex = contentViewModel.getChapterIndex()
                    colorPaletteViewModel.updateBackgroundColor(Color(dataStoreManager.backgroundColor.first()))
                    colorPaletteViewModel.updateTextColor(Color(dataStoreManager.textColor.first()))
                    colorPaletteViewModel.updateSelectedColorSet(dataStoreManager.selectedColorSet.first())
                    fontViewModel.onAction(FontAction.UpdateFontSize(dataStoreManager.fontSize.first()))
                    fontViewModel.onAction(FontAction.UpdateTextAlign(dataStoreManager.textAlign.first()))
                    fontViewModel.onAction(FontAction.UpdateTextIndent(dataStoreManager.textIndent.first()))
                    fontViewModel.onAction(FontAction.UpdateLineSpacing(dataStoreManager.lineSpacing.first()))
                    fontViewModel.onAction(FontAction.UpdateSelectedFontFamilyIndex(dataStoreManager.fontFamily.first()))
                    yield()
                    isContentLoading = false
                }
                BookContentScreenRoot(
                    viewModel = viewModel,
                    currentChapterIndex = currentChapterIndex,
                    contentViewModel = contentViewModel,
                    colorPaletteViewModel = colorPaletteViewModel,
                    fontViewModel = fontViewModel,
                    dataStoreManager = dataStoreManager,
                    onBackClick = {
                        navController.navigateUp()
                    },
                    launchAlertDialog = { state ->
                        navController.navigateUp()
                        launchAlertDialog = state
//                        val book = selectedBook?.storagePath?.let { loadEpubFile(it) }
//                        if (book != null) {
//                            importBookViewModel.processAndSaveBook(book, context, selectedBook?.storagePath!!)
//                        }
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