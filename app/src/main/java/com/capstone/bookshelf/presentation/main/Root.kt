package com.capstone.bookshelf.presentation.main

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.capstone.bookshelf.presentation.main.booklist.LibraryAction
import com.capstone.bookshelf.presentation.main.booklist.LibraryPageRoot
import com.capstone.bookshelf.presentation.main.booklist.LibraryViewModel
import com.capstone.bookshelf.presentation.main.booklist.local.LocalBookListAction
import com.capstone.bookshelf.presentation.main.booklist.local.LocalBookListViewModel
import com.capstone.bookshelf.presentation.main.booklist.remote.RemoteBookListViewModel
import com.capstone.bookshelf.presentation.main.component.BottomNavigation
import com.capstone.bookshelf.presentation.main.component.GlassmorphicBottomNavigation
import com.capstone.bookshelf.presentation.main.homepage.HomePageRoot
import com.capstone.bookshelf.presentation.main.homepage.HomePageViewModel
import com.capstone.bookshelf.presentation.main.search.SearchPageRoot
import com.capstone.bookshelf.presentation.main.search.SearchPageViewModel
import com.capstone.bookshelf.presentation.main.setting.SettingPageRoot
import com.capstone.bookshelf.presentation.main.setting.SettingViewModel
import dev.chrisbanes.haze.HazeState
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Root(
    rootState: RootState,
    onRootAction: (RootAction) -> Unit,
) {
    val hazeState = remember { HazeState() }

    val homePageViewModel = koinViewModel<HomePageViewModel>()
    val searchPageViewModel = koinViewModel<SearchPageViewModel>()
    val libraryViewModel = koinViewModel<LibraryViewModel>()
    val settingViewModel = koinViewModel<SettingViewModel>()
    val localBookListViewModel = koinViewModel<LocalBookListViewModel>()
    val remoteBookListViewModel = koinViewModel<RemoteBookListViewModel>()
    val modifier = Modifier.navigationBarsPadding().statusBarsPadding()

    Scaffold(
        bottomBar = {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                GlassmorphicBottomNavigation(
                    modifier = modifier,
                    hazeState = hazeState,
                    rootState = rootState,
                    onTabSelected = { action ->
                        onRootAction(action)
                        localBookListViewModel.onAction(
                            LocalBookListAction.OnDeletingBooks(false)
                        )
                    }
                )
            } else {
                BottomNavigation(
                    modifier = modifier,
                    rootState = rootState,
                    onTabSelected = { action ->
                        onRootAction(action)
                        localBookListViewModel.onAction(
                            LocalBookListAction.OnDeletingBooks(false)
                        )
                    }
                )
            }
        },
        content = {
            when(rootState.selectedTabIndex) {
                0 -> {
                    HomePageRoot(
                        homePageViewModel = homePageViewModel,
                        hazeState = hazeState,
                        modifier = modifier
                    )
                }
                1 -> {
                    SearchPageRoot(
                        searchPageViewModel = searchPageViewModel,
                        hazeState = hazeState,
                        modifier = modifier
                    )
                }
                2 -> {
                    LibraryPageRoot(
                        modifier = modifier,
                        libraryViewModel = libraryViewModel,
                        remoteBookListViewModel = remoteBookListViewModel,
                        localBookListViewModel = localBookListViewModel,
                        hazeState = hazeState,
                        onAction = {action->
                            when(action) {
                                is LibraryAction.OnBookClick -> onRootAction(RootAction.OnBookClick(action.book))
                                is LibraryAction.OnTabSelected -> libraryViewModel.onAction(action)
                                is LibraryAction.OnViewBookDetailClick -> onRootAction(RootAction.OnViewBookDetailClick(action.book))
                                else -> Unit
                            }
                        },
                        currentTab = {

                        }
                    )
                }
                3 -> {
                    SettingPageRoot(
                        settingViewModel = settingViewModel,
                        hazeState = hazeState,
                        modifier = modifier
                    )
                }
            }
        }
    )
}

