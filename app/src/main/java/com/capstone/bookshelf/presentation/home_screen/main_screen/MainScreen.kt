package com.capstone.bookshelf.presentation.home_screen.main_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.home_screen.component.PagerIndicator
import com.capstone.bookshelf.presentation.home_screen.component.RecentBookCard

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onClick: (String) -> Unit,
    onDoubleClick: (String) -> Unit,
    navigateToBookList: () -> Unit
) {
    val state by mainViewModel.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { state.recentBooks.size })
    LaunchedEffect(Unit) {
        pagerState.animateScrollToPage(0)
    }
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.recentBooks.isNotEmpty()) {
            Text(
                text = "Recent Books",
                style = TextStyle(
                    fontSize = MaterialTheme.typography.displaySmall.fontSize,
                    fontWeight = FontWeight.Bold
                ),
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(48.dp),
            ) { pageIndex ->
                RecentBookCard(
                    book = state.recentBooks[pageIndex],
                    pagerState = pagerState,
                    pageIndex = pageIndex,
                    onClick = {
                        onClick(state.recentBooks[pageIndex].id)
                    },
                    onDoubleClick = {
                        onDoubleClick(state.recentBooks[pageIndex].id)
                    }
                )
            }
            PagerIndicator(
                pagerState = pagerState,
            )
        } else {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = "No Recent Books Found",
                style = TextStyle(
                    fontSize = MaterialTheme.typography.displaySmall.fontSize,
                    fontWeight = FontWeight.Bold
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    modifier = Modifier.weight(1f),
                    text = "Add more books to your library",
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                        fontWeight = FontWeight.Bold
                    ),
                )
                OutlinedIconButton(
                    onClick = {
                        navigateToBookList()
                    },
                    colors = IconButtonDefaults.outlinedIconButtonColors(),
                ) {
                    Icon(
                        modifier = Modifier.rotate(180f),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_back),
                        contentDescription = "Add Books"
                    )
                }
            }
        }
    }
}