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

//enum class AppDestinations(
//    @StringRes val label: Int,
//    val icon: ImageVector,
//    @StringRes val contentDescription: Int
//) {
//    HOME(R.string.home, Icons.Default.Home, R.string.home),
//    FAVORITES(R.string.favorites, Icons.Default.Favorite, R.string.favorites),
//    SHOPPING(R.string.shopping, Icons.Default.ShoppingCart, R.string.shopping),
//    PROFILE(R.string.profile, Icons.Default.AccountBox, R.string.profile),
//}
