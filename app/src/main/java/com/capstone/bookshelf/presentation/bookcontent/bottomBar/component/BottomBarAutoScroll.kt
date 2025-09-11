package com.capstone.bookshelf.presentation.bookcontent.bottomBar.component

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarState
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollState
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.component.dialog.AutoScrollMenuDialog
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.util.DataStoreManager
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect

@Composable
@UnstableApi
fun BottomBarAutoScroll(
    contentState: ContentState,
    dataStoreManager: DataStoreManager,
    hazeState: HazeState,
    style: HazeStyle,
    autoScrollViewModel: AutoScrollViewModel,
    bottomBarState: BottomBarState,
    autoScrollState: AutoScrollState,
    colorPaletteState: ColorPalette,
    onPlayPauseIconClick: () -> Unit,
    onStopIconClick: () -> Unit,
    onSettingIconClick: () -> Unit,
    onDismissDialogRequest: () -> Unit,
) {
    val iconList = listOf(
        R.drawable.ic_skip_to_back,
        R.drawable.ic_play,
        R.drawable.ic_pause,
        R.drawable.ic_skip_to_next,
        R.drawable.ic_stop,
        R.drawable.ic_setting
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                PaddingValues(
                    start = WindowInsets.safeContent
                        .only(WindowInsetsSides.Start)
                        .asPaddingValues()
                        .calculateStartPadding(LayoutDirection.Ltr),
                    end = WindowInsets.safeContent
                        .only(WindowInsetsSides.End)
                        .asPaddingValues()
                        .calculateEndPadding(LayoutDirection.Ltr),
                    bottom = WindowInsets.navigationBars
                        .only(WindowInsetsSides.Bottom)
                        .asPaddingValues()
                        .calculateBottomPadding()
                )
            )
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .clip(CircleShape)
                .then(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.hazeEffect(
                            state = hazeState,
                            style = style
                        )
                    } else {
                        Modifier.background(
                            colorPaletteState.containerColor
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .padding(4.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(
                    modifier = Modifier
                        .size(50.dp),
                    onClick = {
                        onStopIconClick()
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(30.dp),
                        painter = painterResource(id = iconList[4]),
                        tint = colorPaletteState.textColor,
                        contentDescription = "stop"
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    modifier = Modifier
                        .size(50.dp),
                    onClick = {
                        onPlayPauseIconClick()
                    }
                ) {
                    if (autoScrollState.isPaused) {
                        Icon(
                            modifier = Modifier.size(30.dp),
                            painter = painterResource(id = iconList[1]),
                            tint = colorPaletteState.textColor,
                            contentDescription = "play/pause"
                        )
                    } else {
                        Icon(
                            modifier = Modifier.size(30.dp),
                            painter = painterResource(id = iconList[2]),
                            tint = colorPaletteState.textColor,
                            contentDescription = "play/pause"
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    modifier = Modifier
                        .size(50.dp),
                    onClick = {
                        onSettingIconClick()
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(30.dp),
                        painter = painterResource(id = iconList[5]),
                        tint = colorPaletteState.textColor,
                        contentDescription = "setting"
                    )
                }
            }
            if (bottomBarState.openAutoScrollMenu) {
                AutoScrollMenuDialog(
                    contentState = contentState,
                    autoScrollState = autoScrollState,
                    autoScrollViewModel = autoScrollViewModel,
                    colorPaletteState = colorPaletteState,
                    dataStoreManager = dataStoreManager,
                    onDismissRequest = {
                        onDismissDialogRequest()
                    }
                )
            }
        }
    }
}
