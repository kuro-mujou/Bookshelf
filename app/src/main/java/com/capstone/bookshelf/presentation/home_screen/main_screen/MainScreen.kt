package com.capstone.bookshelf.presentation.home_screen.main_screen

import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.home_screen.component.PagerIndicator
import com.capstone.bookshelf.presentation.home_screen.component.RecentBookCard
import com.capstone.bookshelf.util.DeviceConfiguration
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onClick: (String) -> Unit,
    onDoubleClick: (String) -> Unit,
    navigateToBookList: () -> Unit
) {
    val state by mainViewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val realItemCount = state.recentBooks.size
    val pagerStateHorizontal = rememberPagerState(
        initialPage = 1,
        pageCount = { if (realItemCount > 0) realItemCount + 2 else 0 }
    )
    val pagerStateVertical = rememberPagerState(
        initialPage = 0,
        pageCount = { realItemCount }
    )
    val customPageSize =
        object : PageSize {
            override fun Density.calculateMainAxisPageSize(
                availableSpace: Int,
                pageSpacing: Int,
            ): Int {
                return (availableSpace - 2 * pageSpacing) / 3
            }
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
                modifier = Modifier.padding(
                    top = WindowInsets.safeContent
                        .only(WindowInsetsSides.Top)
                        .asPaddingValues()
                        .calculateTopPadding() + 12.dp,
                    bottom = 8.dp
                ),
                style = TextStyle(
                    fontSize = MaterialTheme.typography.displaySmall.fontSize,
                    fontWeight = FontWeight.Bold
                ),
            )
            val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
            val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

            when (deviceConfiguration) {
                DeviceConfiguration.PHONE_PORTRAIT,
                DeviceConfiguration.TABLET_PORTRAIT -> {
                    LaunchedEffect(Unit) {
                        pagerStateVertical.animateScrollToPage(0)
                    }
                    HorizontalPager(
                        state = pagerStateVertical,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 48.dp),
                    ) { pageIndex ->
                        RecentBookCard(
                            book = state.recentBooks[pageIndex],
                            pagerState = pagerStateVertical,
                            pageIndex = pageIndex,
                            onClick = {
                                scope.launch {
                                    if (pageIndex == pagerStateVertical.currentPage)
                                        onClick(state.recentBooks[pageIndex].id)
                                    else
                                        pagerStateVertical.animateScrollToPage(pageIndex)
                                }
                            },
                            onDoubleClick = {
                                scope.launch {
                                    if (pageIndex == pagerStateVertical.currentPage)
                                        onDoubleClick(state.recentBooks[pageIndex].id)
                                    else
                                        pagerStateVertical.animateScrollToPage(pageIndex)
                                }
                            }
                        )
                    }
                    PagerIndicator(
                        modifier = Modifier.padding(bottom = 24.dp, top = 8.dp),
                        pagerState = pagerStateVertical,
                    )
                }

                DeviceConfiguration.PHONE_LANDSCAPE,
                DeviceConfiguration.TABLET_LANDSCAPE -> {
                    LaunchedEffect(Unit) {
                        pagerStateHorizontal.animateScrollToPage(1)
                    }
                    HorizontalPager(
                        state = pagerStateHorizontal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        pageSize = customPageSize,
                        snapPosition = SnapPosition.Center,
                    ) { pageIndex ->
                        if (pageIndex == 0 || pageIndex == realItemCount + 1) {
                            Box(modifier = Modifier.fillMaxWidth().aspectRatio(0.65f))
                        } else {
                            val realIndex = pageIndex - 1
                            val book = state.recentBooks[realIndex]

                            RecentBookCard(
                                book = book,
                                pagerState = pagerStateHorizontal,
                                pageIndex = pageIndex,
                                onClick = {
                                    scope.launch {
                                        if (pageIndex == pagerStateHorizontal.currentPage)
                                            onClick(book.id)
                                        else
                                            pagerStateHorizontal.animateScrollToPage(pageIndex)
                                    }
                                },
                                onDoubleClick = {
                                    scope.launch {
                                        if (pageIndex == pagerStateHorizontal.currentPage)
                                            onDoubleClick(book.id)
                                        else
                                            pagerStateHorizontal.animateScrollToPage(pageIndex)
                                    }
                                }
                            )
                        }
                    }
                    val indicatorPagerState = remember(pagerStateHorizontal.currentPage, realItemCount) {
                        object : PagerState(
                            currentPage = (pagerStateHorizontal.currentPage - 1).coerceIn(0, realItemCount - 1),
                            currentPageOffsetFraction = pagerStateHorizontal.currentPageOffsetFraction
                        ) {
                            override val pageCount: Int
                                get() = realItemCount
                        }
                    }

                    PagerIndicator(
                        modifier = Modifier.padding(bottom = 24.dp, top = 8.dp),
                        pagerState = indicatorPagerState,
                    )
                }
            }


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