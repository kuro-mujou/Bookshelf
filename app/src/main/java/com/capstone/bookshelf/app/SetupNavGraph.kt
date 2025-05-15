package com.capstone.bookshelf.app

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.capstone.bookshelf.presentation.bookcontent.BookContentScreenRoot
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPaletteViewModel
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.presentation.bookdetail.BookDetailAction
import com.capstone.bookshelf.presentation.bookdetail.BookDetailScreenRoot
import com.capstone.bookshelf.presentation.bookdetail.BookDetailViewModel
import com.capstone.bookshelf.presentation.bookwriter.BookWriterCreate
import com.capstone.bookshelf.presentation.bookwriter.BookWriterViewModel
import com.capstone.bookshelf.presentation.home_screen.HomeScreen
import com.capstone.bookshelf.util.DataStoreManager
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@UnstableApi
@Composable
fun SetupNavGraph(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Route.HomeGraph
    ) {
        composable<Route.HomeGraph> { nav ->
            HomeScreen(
                mainNavController = navController,
            )
        }
        composable<Route.BookDetail> { nav ->
            val viewModel = koinViewModel<BookDetailViewModel>()
            val routeArgs: Route.BookDetail = nav.toRoute()
            BookDetailScreenRoot(
                viewModel = viewModel,
                onBackClick = {
                    navController.navigateUp()
                },
                onBookMarkClick = {
                    viewModel.onAction(BookDetailAction.OnBookMarkClick)
                },
                onDrawerItemClick = {
                    viewModel.onAction(BookDetailAction.OnDrawerItemClick(it))
                    navController.navigate(Route.BookContent(routeArgs.bookId)) {
                        popUpTo(Route.BookDetail(routeArgs.bookId)) {
                            inclusive = true
                        }
                    }
                },
                onReadBookClick = {
                    navController.navigate(Route.BookContent(routeArgs.bookId)) {
                        popUpTo(Route.BookDetail(routeArgs.bookId)) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable<Route.BookContent> { nav ->
            val colorPaletteViewModel = koinViewModel<ColorPaletteViewModel>()
            val viewModel = koinViewModel<ContentViewModel>()
            val dataStoreManager = DataStoreManager(LocalContext.current)
            BookContentScreenRoot(
                viewModel = viewModel,
                colorPaletteViewModel = colorPaletteViewModel,
                dataStoreManager = dataStoreManager,
                onBackClick = { back ->
                    if (back) {
                        navController.navigate(Route.HomeGraph) {
                            popUpTo(Route.HomeGraph) {
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
        composable<Route.WriteBook> { nav ->
            val bookWriterViewModel = koinViewModel<BookWriterViewModel>()
            BookWriterCreate(
                viewModel = bookWriterViewModel,
                onNavigateToBookContent = { bookId, book ->
                    navController.navigate(
                        Route.BookContent(bookId)
                    )
                }
            )
        }
    }
}