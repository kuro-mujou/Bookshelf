package com.capstone.bookshelf.presentation.home_screen.component

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.capstone.bookshelf.R
import com.capstone.bookshelf.app.Route

val bottomNavItems = listOf(
    BottomNavItem.Main,
    BottomNavItem.BookList,
    BottomNavItem.Settings
)

sealed class BottomNavItem(val route: Route, val icon: Int, val label: String) {
    data object Main : BottomNavItem(
        route = Route.MainScreen,
        icon = R.drawable.ic_home,
        label = "Home"
    )

    data object BookList : BottomNavItem(
        route = Route.BookList,
        icon = R.drawable.ic_book_list,
        label = "Books"
    )

    data object Settings : BottomNavItem(
        route = Route.SettingScreen,
        icon = R.drawable.ic_setting,
        label = "Settings"
    )
}

@Composable
fun AppBottomNavigation(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(item.icon),
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(text = item.label)
                },
                selected = currentDestination?.route == item.route::class.qualifiedName,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}