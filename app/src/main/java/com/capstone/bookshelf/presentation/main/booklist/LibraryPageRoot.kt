package com.capstone.bookshelf.presentation.main.booklist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.bookshelf.presentation.main.booklist.local.LocalBookList
import com.capstone.bookshelf.presentation.main.booklist.local.LocalBookListAction
import com.capstone.bookshelf.presentation.main.booklist.local.LocalBookListViewModel
import com.capstone.bookshelf.presentation.main.booklist.remote.RemoteBookList
import com.capstone.bookshelf.presentation.main.booklist.remote.RemoteBookListViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import org.koin.androidx.compose.koinViewModel

@Composable
fun LibraryPageRoot(
    modifier: Modifier,
    libraryViewModel: LibraryViewModel = koinViewModel(),
    remoteBookListViewModel: RemoteBookListViewModel,
    localBookListViewModel: LocalBookListViewModel,
    hazeState: HazeState,
    onAction: (LibraryAction) -> Unit,
    currentTab: (Int) -> Unit
){
    val libraryState by libraryViewModel.state.collectAsStateWithLifecycle()
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .haze(state = hazeState),
    ){
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            TabRow(
                selectedTabIndex = libraryState.selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth(),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[libraryState.selectedTabIndex])
                    )
                }
            ) {
                Tab(
                    selected = libraryState.selectedTabIndex == 0,
                    onClick = {
                        onAction(LibraryAction.OnTabSelected(0))
                        localBookListViewModel.onAction(
                            LocalBookListAction.OnDeletingBooks(false)
                        )
                        currentTab(0)
                    },
                    modifier = Modifier.weight(1f),
                    selectedContentColor = MaterialTheme.colorScheme.secondary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Online Library",
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                    )
                }
                Tab(
                    selected = libraryState.selectedTabIndex == 1,
                    onClick = {
                        onAction(LibraryAction.OnTabSelected(1))
                        localBookListViewModel.onAction(
                            LocalBookListAction.OnDeletingBooks(false)
                        )
                        currentTab(1)
                    },
                    modifier = Modifier.weight(1f),
                    selectedContentColor = MaterialTheme.colorScheme.secondary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Offline Library",
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            when(libraryState.selectedTabIndex) {
                0 -> {
                    Box{
                        RemoteBookList(
                            remoteBookListViewModel = remoteBookListViewModel,
                            onBookClick = {
                                onAction(LibraryAction.OnBookClick(it))
                            },
                        )
                    }
                }
                1 -> {
                    Box{
                        LocalBookList(
                            localBookListViewModel = localBookListViewModel,
                            onAction={ action->
                                when(action){
                                    is LocalBookListAction.OnBookClick -> onAction(LibraryAction.OnBookClick(action.book))
                                    is LocalBookListAction.OnViewBookDetailClick -> onAction(LibraryAction.OnViewBookDetailClick(action.book))
                                    else -> Unit
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}