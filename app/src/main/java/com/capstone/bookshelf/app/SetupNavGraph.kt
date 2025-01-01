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
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.capstone.bookshelf.core.presentation.LoadingAnimation
import com.capstone.bookshelf.presentation.SelectedBookViewModel
import com.capstone.bookshelf.presentation.bookcontent.BookContentRootAction
import com.capstone.bookshelf.presentation.bookcontent.BookContentRootViewModel
import com.capstone.bookshelf.presentation.bookcontent.BookContentScreenRoot
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPaletteViewModel
import com.capstone.bookshelf.presentation.bookdetail.BookDetailAction
import com.capstone.bookshelf.presentation.bookdetail.BookDetailScreenRoot
import com.capstone.bookshelf.presentation.bookdetail.BookDetailViewModel
import com.capstone.bookshelf.presentation.main.Root
import com.capstone.bookshelf.presentation.main.RootAction
import com.capstone.bookshelf.presentation.main.RootViewModel
import com.capstone.bookshelf.util.DataStoreManger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.yield
import org.koin.androidx.compose.koinViewModel

@Composable
fun SetupNavGraph(navController: NavHostController) {
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
                val rootViewModel = koinViewModel<RootViewModel>()
                val selectedBookViewModel =
                    it.sharedKoinViewModel<SelectedBookViewModel>(navController)
                val rootState by rootViewModel.state.collectAsStateWithLifecycle()

                LaunchedEffect(true) {
                    selectedBookViewModel.onSelectBook(null)
                }

                Root(
                    rootState = rootState,
                    onRootAction = { action ->
                        when (action) {
                            is RootAction.OnTabSelected -> {
                                rootViewModel.onAction(action)
                            }
                            is RootAction.OnBookClick -> {
                                selectedBookViewModel.onSelectBook(action.book)
                                navController.navigate(
                                    Route.BookContent(action.book.id)
                                )
                            }
                            is RootAction.OnViewBookDetailClick -> {
                                selectedBookViewModel.onSelectBook(action.book)
                                navController.navigate(
                                    Route.BookDetail(action.book.id)
                                )
                            }
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
            composable<Route.BookContent>(
//                enterTransition = {
//                    slideInHorizontally { initialOffset ->
//                        initialOffset
//                    }
//                },
//                exitTransition = {
//                    slideOutHorizontally { initialOffset ->
//                        initialOffset
//                    }
//                }
            ) { nav ->
                var isLoading by remember { mutableStateOf(true) }
                val selectedBookViewModel =
                    nav.sharedKoinViewModel<SelectedBookViewModel>(navController)
                val viewModel = koinViewModel<BookContentRootViewModel>()
                val colorPaletteViewModel = koinViewModel<ColorPaletteViewModel>()
                val dataStore = DataStoreManger(LocalContext.current)
                val selectedBook by selectedBookViewModel.selectedBook.collectAsStateWithLifecycle()
                if(isLoading){
                    LoadingAnimation()
                }
                BackHandler {

                }
                LaunchedEffect(selectedBook) {
                    selectedBook?.let {
                        viewModel.onAction(BookContentRootAction.SelectedBook(it))
                    }
                    colorPaletteViewModel.updateBackgroundColor(Color(dataStore.backgroundColor.first()))
                    colorPaletteViewModel.updateTextColor(Color(dataStore.textColor.first()))
                    yield()
                    isLoading = false
                }

                BookContentScreenRoot(
                    viewModel = viewModel,
                    colorPaletteViewModel = colorPaletteViewModel,
                    dataStore = dataStore,
                    onBackClick = {
                        navController.navigateUp()
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