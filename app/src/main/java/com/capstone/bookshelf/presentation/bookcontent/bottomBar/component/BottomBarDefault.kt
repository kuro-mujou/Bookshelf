package com.capstone.bookshelf.presentation.bookcontent.bottomBar.component

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.drawer.DrawerContainerState


@Composable
fun BottomBarDefault(
    drawerContainerState: DrawerContainerState,
    onThemeIconClick: () -> Unit,
    onTTSIconClick: () -> Unit,
    onAutoScrollIconClick: () -> Unit,
    onSettingIconClick: () -> Unit,
){
    val iconList = listOf(
        R.drawable.ic_theme,
        R.drawable.ic_headphone,
        R.drawable.ic_play,
        R.drawable.ic_setting
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.surfaceContainer),
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        Spacer(modifier = Modifier.height(10.dp))
        drawerContainerState.currentTOC?.let {
            Text(
                modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                text = it.title,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .wrapContentHeight()
                .background(MaterialTheme.colorScheme.surfaceContainer),
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
                    painter = painterResource(id = iconList[0]),
                    contentDescription = "theme"
                )
            }
            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onTTSIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[1]),
                    contentDescription = "start tts"
                )
            }

            IconButton(
                modifier = Modifier.size(50.dp),
                onClick = {
                    onAutoScrollIconClick()
                }
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = iconList[2]),
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
                    painter = painterResource(id = iconList[3]),
                    contentDescription = "setting"
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}