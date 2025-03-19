package com.capstone.bookshelf.app

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {

    @Serializable
    data object BookGraph: Route

    @Serializable
    data object Home: Route

    @Serializable
    data class BookDetail(val bookId: String): Route

    @Serializable
    data class BookContent(val bookId: String): Route

    @Serializable
    data object WriteBook : Route
}