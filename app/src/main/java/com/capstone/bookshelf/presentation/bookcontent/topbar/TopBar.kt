package com.capstone.bookshelf.presentation.bookcontent.topbar

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun TopBar(
    hazeState: HazeState,
    topBarState: Boolean,
    colorPaletteState: ColorPalette,
    onMenuIconClick: () -> Unit,
    onBackIconClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = topBarState,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it }),
    ) {
        val style = HazeMaterials.ultraThin(colorPaletteState.containerColor)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .then(
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                        Modifier.hazeChild(
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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = colorPaletteState.textColor
                )
            }
        }
    }
}