package com.capstone.bookshelf.presentation.main.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.main.RootAction
import com.capstone.bookshelf.presentation.main.RootState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassmorphicBottomNavigation(
    modifier: Modifier,
    hazeState: HazeState,
    rootState: RootState,
    onTabSelected: (RootAction) -> Unit
) {
    val tabs = listOf(
        BottomBarTab.HomePage,
        BottomBarTab.Search,
        BottomBarTab.Library,
        BottomBarTab.Setting,
    )
    val style = HazeMaterials.ultraThin(MaterialTheme.colorScheme.background)
    Box(
        modifier = modifier
            .padding(vertical = 24.dp, horizontal = 24.dp)
            .fillMaxWidth()
            .height(70.dp)
            .clip(CircleShape)
            .hazeChild(
                state = hazeState,
                style = style
            ),
        contentAlignment = Alignment.Center
    ) {
        BottomBarTabs(
            tabs,
            selectedTab = rootState.selectedTabIndex,
            onTabSelected = {
                onTabSelected(RootAction.OnTabSelected(tabs.indexOf(it)))
            }
        )
        val animatedSelectedTabIndex by animateFloatAsState(
            targetValue = rootState.selectedTabIndex.toFloat(),
            label = "animatedSelectedTabIndex",
            animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioLowBouncy,
            )
        )

        val animatedColor by animateColorAsState(
            targetValue = MaterialTheme.colorScheme.primary,
            label = "animatedColor",
            animationSpec = spring(
                stiffness = Spring.StiffnessLow,
            )
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .blur(70.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        ) {
            val tabWidth = size.width / tabs.size
            drawCircle(
                color = animatedColor,
                radius = size.height / 2,
                center = Offset(
                    (tabWidth * animatedSelectedTabIndex) + tabWidth / 2,
                    size.height / 2
                )
            )
        }
    }
}
@Composable
private fun BottomBarTabs(
    tabs: List<BottomBarTab>,
    selectedTab: Int,
    onTabSelected: (BottomBarTab) -> Unit,
) {
    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        ),
        LocalContentColor provides MaterialTheme.colorScheme.onBackground
    ) {
        Row(
            modifier = Modifier.wrapContentHeight().fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            for (tab in tabs) {
                val alpha by animateFloatAsState(
                    targetValue = if (selectedTab == tabs.indexOf(tab)) 1f else .35f,
                    label = "alpha"
                )
                val scale by animateFloatAsState(
                    targetValue = if (selectedTab == tabs.indexOf(tab)) 1f else .90f,
                    visibilityThreshold = .000001f,
                    animationSpec = spring(
                        stiffness = Spring.StiffnessLow,
                        dampingRatio = Spring.DampingRatioNoBouncy,
                    ),
                    label = "scale"
                )
                Column(
                    modifier = Modifier
                        .scale(scale)
                        .alpha(alpha)
                        .wrapContentHeight()
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                onTabSelected(tab)
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(tab.icon),
                        modifier = Modifier.size(24.dp),
                        contentDescription = null
                    )
                    Text(text = tab.title)
                }
            }
        }
    }
}

sealed class BottomBarTab(val title: String, val icon: Int) {
    data object HomePage : BottomBarTab(
        title = "Home",
        icon = R.drawable.ic_home
    )
    data object Search : BottomBarTab(
        title = "Search",
        icon = R.drawable.ic_search
    )
    data object Library : BottomBarTab(
        title = "Library",
        icon = R.drawable.ic_book_list
    )
    data object Setting : BottomBarTab(
        title = "Setting",
        icon = R.drawable.ic_settings
    )
}