package com.capstone.bookshelf.presentation.bookcontent.bottomBar.component

import android.os.Build
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect


@Composable
@UnstableApi
fun BottomBarDefault(
    hazeState: HazeState,
    style: HazeStyle,
    contentState: ContentState,
    drawerContainerState: DrawerContainerState,
    colorPaletteState: ColorPalette,
    onThemeIconClick: () -> Unit,
    onTTSIconClick: () -> Unit,
    onAutoScrollIconClick: () -> Unit,
    onSettingIconClick: () -> Unit
){
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
        Spacer(modifier = Modifier.height(10.dp))
        drawerContainerState.currentTOC?.let {
            Text(
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp)
                    .basicMarquee(
                        animationMode = MarqueeAnimationMode.Immediately,
                        initialDelayMillis = 0,
                        repeatDelayMillis = 0
                    ),
                text = it.title,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    color = colorPaletteState.textColor,
                    textAlign = TextAlign.Center,
                    fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex],
                ),
                maxLines = 1,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onThemeIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    imageVector = ImageVector.vectorResource(R.drawable.ic_theme),
                    tint = colorPaletteState.textColor,
                    contentDescription = "theme"
                )
            }
            if(contentState.book?.fileType != "cbz") {
                IconButton(
                    modifier = Modifier.size(50.dp),
                    onClick = {
                        onTTSIconClick()
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(30.dp),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_headphones),
                        tint = colorPaletteState.textColor,
                        contentDescription = "start tts"
                    )
                }
            }
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onAutoScrollIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    imageVector = ImageVector.vectorResource(R.drawable.ic_scroll),
                    tint = colorPaletteState.textColor,
                    contentDescription = "auto scroll"
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
                    imageVector = ImageVector.vectorResource(R.drawable.ic_setting),
                    tint = colorPaletteState.textColor,
                    contentDescription = "setting"
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}