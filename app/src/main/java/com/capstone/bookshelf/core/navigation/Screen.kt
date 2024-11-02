package com.capstone.bookshelf.core.navigation

const val BOOK_ID_ARG = "bookId"
const val CURRENT_CHAPTER = "currentChapter"

sealed class Screen(val route: String) {
    data object Root : Screen(route = Route.Root.name)

    data object BookContent : Screen(route = "${Route.BookContentsScreen.name}/{$BOOK_ID_ARG}/{$CURRENT_CHAPTER}") {
        fun passVal(id: Int, chapter: Int) = "${Route.BookContentsScreen.name}/$id/$chapter"
    }
}
enum class Route{
    BookContentsScreen,
    Root
}