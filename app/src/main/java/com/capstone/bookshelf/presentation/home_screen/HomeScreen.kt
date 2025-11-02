package com.capstone.bookshelf.presentation.home_screen

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.capstone.bookshelf.app.Route
import com.capstone.bookshelf.presentation.home_screen.booklist.BookList
import com.capstone.bookshelf.presentation.home_screen.booklist.BookListAction
import com.capstone.bookshelf.presentation.home_screen.booklist.BookListViewModel
import com.capstone.bookshelf.presentation.home_screen.booklist.component.AsyncImportBookViewModel
import com.capstone.bookshelf.presentation.home_screen.main_screen.MainScreen
import com.capstone.bookshelf.presentation.home_screen.main_screen.MainViewModel
import com.capstone.bookshelf.presentation.home_screen.setting_screen.SettingScreen
import com.capstone.bookshelf.presentation.home_screen.setting_screen.SettingViewModel
import com.capstone.bookshelf.util.DataStoreManager
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    mainNavController: NavHostController,
) {
    val homeNavController = rememberNavController()
    CustomNavigationSuiteScaffold(
        navController = homeNavController,
    ){
        NavHost(
            navController = homeNavController,
            startDestination = Route.MainScreen,
        ) {
            composable<Route.MainScreen> {
                val mainViewModel = koinViewModel<MainViewModel>()
                MainScreen(
                    mainViewModel = mainViewModel,
                    onClick = { bookId ->
                        mainNavController.navigate(Route.BookContent(bookId))
                    },
                    onDoubleClick = { bookId ->
                        mainNavController.navigate(Route.BookDetail(bookId))
                    },
                    navigateToBookList = {
                        homeNavController.navigate(Route.BookList) {
                            popUpTo(Route.MainScreen) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable<Route.BookList> {
                val bookListViewModel = koinViewModel<BookListViewModel>()
                val importBookViewModel = koinViewModel<AsyncImportBookViewModel>()
                val dataStoreManager = DataStoreManager(LocalContext.current)
                BookList(
                    bookListViewModel = bookListViewModel,
                    importBookViewModel = importBookViewModel,
                    dataStoreManager = dataStoreManager,
                    navigateTo = { route ->
                        mainNavController.navigate(route)
                    },
                    onAction = { action ->
                        when (action) {
                            is BookListAction.OnBookClick -> {
                                mainNavController.navigate(Route.BookContent(action.book.id))
                            }

                            is BookListAction.OnViewBookDetailClick -> {
                                mainNavController.navigate(Route.BookDetail(action.book.id))
                            }

                            else -> {
                                bookListViewModel.onAction(action)
                            }
                        }
                    }
                )
            }
            composable<Route.SettingScreen> {
                val settingViewModel = koinViewModel<SettingViewModel>()
                val settingState by settingViewModel.state.collectAsStateWithLifecycle()
                val dataStoreManager = DataStoreManager(LocalContext.current)
                SettingScreen(
                    settingState = settingState,
                    dataStoreManager = dataStoreManager,
                    onAction = { action ->
                        settingViewModel.onAction(action)
                    }
                )
            }
        }
    }
}