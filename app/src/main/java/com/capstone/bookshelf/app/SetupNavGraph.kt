package com.capstone.bookshelf.app

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.capstone.bookshelf.presentation.SelectedBookViewModel
import com.capstone.bookshelf.presentation.main.Root
import com.capstone.bookshelf.presentation.main.RootViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        modifier = Modifier.navigationBarsPadding().statusBarsPadding(),
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
                        rootViewModel.onAction(action)
                    },
                    onBookSelectedAction = { book ->
                        selectedBookViewModel.onSelectBook(book)
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
            ) {

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
            ) {

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