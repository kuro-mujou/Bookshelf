package com.capstone.bookshelf.app

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object BookGraph: Route

    @Serializable
    data object Home: Route

    @Serializable
    data class BookDetail(val bookId: Int): Route

    @Serializable
    data class BookContent(val bookId: Int): Route
}