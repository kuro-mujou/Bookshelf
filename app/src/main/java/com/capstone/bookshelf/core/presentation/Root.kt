package com.capstone.bookshelf.core.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavController
import com.capstone.bookshelf.R
import com.capstone.bookshelf.core.navigation.BottomNavigationItem
import com.capstone.bookshelf.core.presentation.component.LoadingAnimation
import com.capstone.bookshelf.feature.booklist.presentation.BookList
import com.capstone.bookshelf.feature.importbook.presentation.component.ImportBookFloatingButton
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Root(
    navController : NavController,
) {
    var navigationSelectedItem by remember {
        mutableIntStateOf(0)
    }
    var message by remember { mutableStateOf("") }
    var progress by remember { mutableFloatStateOf(0f) }
    var isSavingBook by remember { mutableStateOf(false) }
    val viewModel = koinViewModel<SettingViewModel>()
    val sortedByFavorite by viewModel.sortedByFavorite
//    var sortedByFavorite by rememberSaveable { mutableStateOf(false) }
    val currentTitle = when (navigationSelectedItem) {
        0 -> "Home"
        1 -> "Book Library"
        else -> ""
    }
//    LaunchedEffect(Unit) {
//        sortedByFavorite = viewModel.sortedByFavorite
//    }
    Scaffold(
        modifier = Modifier.fillMaxSize().background(Color.Blue),
        topBar = {
            TopAppBar(
                title = { Text(text = currentTitle) },
                actions = {
                    if (navigationSelectedItem == 1){
                        IconButton(
                            onClick = {
                                viewModel.updateSortedByFavorite(!sortedByFavorite)
                            }
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_rounded_bookmark_star),
                                contentDescription = "Sorting Icon",
                                tint = if (sortedByFavorite) Color.Green else Color.Gray
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar{
                BottomNavigationItem().bottomNavigationItems().forEachIndexed { index, navigationItem ->
                    NavigationBarItem(
                        selected = index == navigationSelectedItem,
                        label = {
                            Text(navigationItem.label)
                        },
                        icon = {
                            Icon(
                                navigationItem.icon,
                                contentDescription = navigationItem.label
                            )
                        },
                        onClick = {
                            navigationSelectedItem = index
                        }
                    )
                }
            }
        },
        floatingActionButton =  {
            if (navigationSelectedItem == 1) {
                ImportBookFloatingButton(
                    onSavingBook = { sProgress, sMessage ->
                        progress = sProgress
                        message = sMessage
                        isSavingBook = true
                    },
                    onBookSaved = {
                        progress = 0f
                        message = ""
                        isSavingBook = false
                    },
                )
            }
        }
    ){innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (navigationSelectedItem) {
            0 -> HomeScreen(navController,modifier)
            1 -> BookList(navController,modifier,sortedByFavorite)
        }
    }

    if (isSavingBook) {
        LoadingAnimation(
            message = message,
            progress = progress
        )
    }
}