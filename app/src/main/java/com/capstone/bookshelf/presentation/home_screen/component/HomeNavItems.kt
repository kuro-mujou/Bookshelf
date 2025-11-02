package com.capstone.bookshelf.presentation.home_screen.component

import com.capstone.bookshelf.R
import com.capstone.bookshelf.app.Route

val homeNavItems = listOf(
    HomeNavItem.Main,
    HomeNavItem.BookList,
    HomeNavItem.Settings,
)

sealed class HomeNavItem(val route: Route, val icon: Int, val label: String) {
    data object Main : HomeNavItem(
        route = Route.MainScreen,
        icon = R.drawable.ic_home,
        label = "Home"
    )

    data object BookList : HomeNavItem(
        route = Route.BookList,
        icon = R.drawable.ic_book_list,
        label = "Books"
    )

    data object Settings : HomeNavItem(
        route = Route.SettingScreen,
        icon = R.drawable.ic_setting,
        label = "Settings"
    )
}