package com.capstone.bookshelf.core.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.capstone.bookshelf.core.presentation.Root
import com.capstone.bookshelf.feature.readbook.presentation.BookContent
import com.capstone.bookshelf.feature.readbook.presentation.BookContentViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Root.route
    ) {
        composable(
            route = Screen.Root.route
        ){
            Root(
                navController = navController
            )
        }
        composable(
            route = Screen.BookContent.route,
            arguments = listOf(
                navArgument(
                    name = BOOK_ID_ARG
                ) {
                    type = NavType.IntType
                    defaultValue = 0
                },
                navArgument(
                    name = CURRENT_CHAPTER
                ) {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) {
            BackHandler(true) {}
            val bookId = it.arguments?.getInt(BOOK_ID_ARG) ?: 0
            val currentChapter = it.arguments?.getInt(CURRENT_CHAPTER) ?: 0
            val bookContentViewModel = koinViewModel<BookContentViewModel>(
                parameters = {
                    parametersOf(
                        bookId,
                        currentChapter
                    )
                }
            )
            bookContentViewModel.updateCurrentBookIndex(bookId)
            bookContentViewModel.updateCurrentChapterIndex(currentChapter)
            bookContentViewModel.getBookInfo()
            BookContent(
                bookContentViewModel = bookContentViewModel,
                onBackIconClick = { currentChapterIndex->
                    navController.navigateUp()
                    bookContentViewModel.saveBookInfo(bookId,currentChapterIndex)
                }
            )
        }
    }
}