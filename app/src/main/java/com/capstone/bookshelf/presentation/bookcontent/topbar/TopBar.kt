package com.capstone.bookshelf.presentation.bookcontent.topbar

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalHazeMaterialsApi::class)
@UnstableApi
@Composable
fun TopBar(
    contentState: ContentState,
    drawerContainerState: DrawerContainerState,
    hazeState: HazeState,
    topBarState: Boolean,
    colorPaletteState: ColorPalette,
    onMenuIconClick: () -> Unit,
    onBackIconClick: () -> Unit,
    onBookmarkIconClick: () -> Unit
) {
    AnimatedVisibility(
        visible = topBarState,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it }),
    ) {
        val style = HazeMaterials.thin(colorPaletteState.containerColor)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .then(
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                        Modifier.hazeEffect(
                            state = hazeState,
                            style = style
                        )
                    }else{
                        Modifier.background(colorPaletteState.containerColor)
                    }
                ),
        ) {
            IconButton(
                modifier = Modifier
                    .statusBarsPadding(),
                onClick = {
                    onBackIconClick()
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = colorPaletteState.textColor
                )
            }
            IconButton(
                modifier = Modifier
                    .statusBarsPadding(),
                onClick = {
                    onMenuIconClick()
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_menu),
                    contentDescription = "Menu",
                    tint = colorPaletteState.textColor
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                modifier = Modifier
                    .statusBarsPadding(),
                onClick = {
                    onBookmarkIconClick()
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        if(drawerContainerState.tableOfContents[contentState.currentChapterIndex].isFavorite)
                            R.drawable.ic_bookmark_filled
                        else
                            R.drawable.ic_bookmark
                    ),
                    contentDescription = "Bookmark",
                    tint = colorPaletteState.textColor
                )
            }
        }
    }
}