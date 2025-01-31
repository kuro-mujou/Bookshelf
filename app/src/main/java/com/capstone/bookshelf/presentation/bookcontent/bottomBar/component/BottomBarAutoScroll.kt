package com.capstone.bookshelf.presentation.bookcontent.bottomBar.component

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarState
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollState
import com.capstone.bookshelf.presentation.bookcontent.component.autoscroll.AutoScrollViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.component.dialog.AutoScrollMenuDialog
import com.capstone.bookshelf.util.DataStoreManager
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect

@Composable
fun BottomBarAutoScroll(
    dataStoreManager: DataStoreManager,
    hazeState: HazeState,
    style: HazeStyle,
    autoScrollViewModel : AutoScrollViewModel,
    bottomBarState: BottomBarState,
    autoScrollState: AutoScrollState,
    colorPaletteState: ColorPalette,
    onPlayPauseIconClick: () -> Unit,
    onStopIconClick: () -> Unit,
    onSettingIconClick: () -> Unit,
    onDismissDialogRequest: () -> Unit,
){
    val iconList = listOf(
        R.drawable.ic_previous_chapter,
        R.drawable.ic_play,
        R.drawable.ic_pause,
        R.drawable.ic_next_chapter,
        R.drawable.ic_stop,
        R.drawable.ic_settings
    )
    Column(
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
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ){
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onPlayPauseIconClick()
                }
            ) {
                if(autoScrollState.isPaused) {
                    Icon(
                        modifier = Modifier.size(30.dp),
                        painter = painterResource(id = iconList[1]),
                        tint = colorPaletteState.textColor,
                        contentDescription = "play/pause"
                    )
                }else{
                    Icon(
                        modifier = Modifier.size(30.dp),
                        painter = painterResource(id = iconList[2]),
                        tint = colorPaletteState.textColor,
                        contentDescription = "play/pause"
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth()
                .navigationBarsPadding()
                .wrapContentHeight()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ){
            IconButton(
                modifier = Modifier.size(50.dp),
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
            IconButton(
                modifier = Modifier.size(50.dp),
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

        if(bottomBarState.openAutoScrollMenu){
            AutoScrollMenuDialog(
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
