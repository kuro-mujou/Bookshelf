package com.capstone.bookshelf.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.capstone.bookshelf.presentation.bookdetail.BookDetailAction
import com.capstone.bookshelf.presentation.bookdetail.BookDetailScreenRoot
import com.capstone.bookshelf.presentation.bookdetail.BookDetailViewModel
import com.capstone.bookshelf.presentation.main.Root
import com.capstone.bookshelf.presentation.main.RootAction
import com.capstone.bookshelf.presentation.main.RootViewModel
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
                val viewModel = koinViewModel<BookContentRootViewModel>()
                val selectedBook by selectedBookViewModel.selectedBook.collectAsStateWithLifecycle()
                BackHandler {

                }
                LaunchedEffect(selectedBook) {
                    selectedBook?.let {
                        viewModel.onAction(BookContentRootAction.SelectedBookRoot(it))
                    }
                }

                BookContentScreenRoot(
                    viewModel = viewModel,
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