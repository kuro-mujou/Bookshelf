package com.capstone.bookshelf.presentation.home_screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.capstone.bookshelf.R
import com.capstone.bookshelf.app.Route
import com.capstone.bookshelf.presentation.home_screen.booklist.BookList
import com.capstone.bookshelf.presentation.home_screen.booklist.BookListAction
import com.capstone.bookshelf.presentation.home_screen.booklist.BookListViewModel
import com.capstone.bookshelf.presentation.home_screen.booklist.component.AsyncImportBookViewModel
import com.capstone.bookshelf.presentation.home_screen.booklist.component.ExpandableFab
import com.capstone.bookshelf.presentation.home_screen.booklist.component.MiniFabItems
import com.capstone.bookshelf.presentation.home_screen.component.AppBottomNavigation
import com.capstone.bookshelf.presentation.home_screen.component.DriveInputLinkDialog
import com.capstone.bookshelf.presentation.home_screen.main_screen.MainScreen
import com.capstone.bookshelf.presentation.home_screen.main_screen.MainViewModel
import com.capstone.bookshelf.presentation.home_screen.setting_screen.SettingScreen
import com.capstone.bookshelf.presentation.home_screen.setting_screen.SettingViewModel
import com.capstone.bookshelf.util.DataStoreManager
import com.capstone.bookshelf.util.ImportBook
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    mainNavController: NavHostController,
) {
    val bottomNavController = rememberNavController()
    var showDriveInputLinkDialog by remember { mutableStateOf(false) }
    var fabExpanded by remember { mutableStateOf(false) }
    var showFab by remember { mutableStateOf(true) }
    var paddingForFab by remember { mutableStateOf(PaddingValues()) }
    var specialIntent by remember { mutableStateOf("null") }
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val importBookLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        ImportBook(
            context = context,
            scope = scope,
            specialIntent = specialIntent
        ).processIntentUri(uri)
    }
    val fabItems = listOf(
        MiniFabItems(
            icon = R.drawable.ic_add_epub,
            title = "Import EPUB/CBZ",
            tint = if (isSystemInDarkTheme())
                Color(255, 250, 160)
            else
                Color(131, 105, 83),
            onClick = {
                specialIntent = "null"
                fabExpanded = false
                importBookLauncher.launch(
                    arrayOf(
                        "application/epub+zip", "application/vnd.comicbook+zip"
                    )
                )
            }
        ),
        MiniFabItems(
            icon = R.drawable.ic_add_epub,
            title = "Import PDF with page render",
            tint = if (isSystemInDarkTheme())
                Color(255, 250, 160)
            else
                Color(131, 105, 83),
            onClick = {
                specialIntent = "PAGE"
                fabExpanded = false
                importBookLauncher.launch(
                    arrayOf("application/pdf")
                )
            }
        ),
        MiniFabItems(
            icon = R.drawable.ic_add_epub,
            title = "Import PDF with text/image extraction",
            tint = if (isSystemInDarkTheme())
                Color(255, 250, 160)
            else
                Color(131, 105, 83),
            onClick = {
                specialIntent = "TEXT"
                fabExpanded = false
                importBookLauncher.launch(
                    arrayOf("application/pdf")
                )
            }
        ),
        MiniFabItems(
            icon = R.drawable.ic_add_epub,
            title = "Import EPUB via Google Drive",
            tint = if (isSystemInDarkTheme())
                Color(255, 250, 160)
            else
                Color(131, 105, 83),
            onClick = {
                specialIntent = "null"
                fabExpanded = false
                showDriveInputLinkDialog = true
            }
        ),
        MiniFabItems(
            icon = R.drawable.ic_write_ebook,
            title = "Write new Book",
            tint = if (isSystemInDarkTheme())
                Color(155, 212, 161)
            else
                Color(52, 105, 63),
            onClick = {
                fabExpanded = false
                mainNavController.navigate(Route.WriteBook(""))
            }
        )
    )

    Scaffold(
        bottomBar = {
            AppBottomNavigation(navController = bottomNavController)
        },
    ) { innerPadding ->
        paddingForFab = innerPadding
        NavHost(
            navController = bottomNavController,
            startDestination = Route.MainScreen,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Route.MainScreen> {
                val mainViewModel = koinViewModel<MainViewModel>()
                MainScreen(
                    mainViewModel = mainViewModel,
                    onClick = { bookId ->
                        mainNavController.navigate(Route.BookContent(bookId))
                    },
                    onDoubleClick = { bookId ->
                        mainNavController.navigate(Route.BookDetail(bookId))
                    },
                    navigateToBookList = {
                        bottomNavController.navigate(Route.BookList) {
                            popUpTo(Route.MainScreen) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable<Route.BookList> {
                val bookListViewModel = koinViewModel<BookListViewModel>()
                val importBookViewModel = koinViewModel<AsyncImportBookViewModel>()
                val dataStoreManager = DataStoreManager(LocalContext.current)
                BookList(
                    bookListViewModel = bookListViewModel,
                    importBookViewModel = importBookViewModel,
                    dataStoreManager = dataStoreManager,
                    controlFabVisible = {
                        showFab = it
                    },
                    onAction = { action ->
                        when (action) {
                            is BookListAction.OnBookClick -> {
                                mainNavController.navigate(Route.BookContent(action.book.id))
                            }

                            is BookListAction.OnViewBookDetailClick -> {
                                mainNavController.navigate(Route.BookDetail(action.book.id))
                            }

                            else -> {
                                bookListViewModel.onAction(action)
                            }
                        }
                    }
                )
            }
            composable<Route.SettingScreen> {
                val settingViewModel = koinViewModel<SettingViewModel>()
                val settingState by settingViewModel.state.collectAsStateWithLifecycle()
                val dataStoreManager = DataStoreManager(LocalContext.current)
                SettingScreen(
                    settingState = settingState,
                    dataStoreManager = dataStoreManager,
                    onAction = { action ->
                        settingViewModel.onAction(action)
                    }
                )
            }
        }
    }
    if (showDriveInputLinkDialog) {
        DriveInputLinkDialog(
            onDismiss = { showDriveInputLinkDialog = false },
            onConfirm = { link ->
                ImportBook(
                    context = context,
                    scope = scope,
                    specialIntent = "null"
                ).importBookViaGoogleDrive(link)
            }
        )
    }
    AnimatedVisibility(
        visible = currentDestination?.route == Route.BookList::class.qualifiedName && showFab,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ExpandableFab(
            paddingForFab = paddingForFab,
            items = fabItems,
            expanded = fabExpanded,
            onToggle = { fabExpanded = !fabExpanded },
            onDismiss = { fabExpanded = false }
        )
    }
}