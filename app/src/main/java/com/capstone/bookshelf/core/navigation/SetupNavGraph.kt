package com.capstone.bookshelf.core.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.capstone.bookshelf.feature.readbook.presentation.BookContent

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
            val id = it.arguments?.getInt(BOOK_ID_ARG) ?: 0
            val currentChapter = it.arguments?.getInt(CURRENT_CHAPTER) ?: 0
            BookContent(
                navController = navController,
                bookId = id,
                previousChapterIndex = currentChapter
            )
        }
    }
}