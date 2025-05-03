package com.capstone.bookshelf.app

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {

    @Serializable
    data object HomeGraph : Route
        @Serializable
        data object MainScreen : Route
        @Serializable
        data object BookList : Route
        @Serializable
        data object SettingScreen : Route

    @Serializable
    data class BookDetail(val bookId: String) : Route
    @Serializable
    data class BookContent(val bookId: String) : Route
    @Serializable
    data class WriteBook(val bookId: String) : Route
}