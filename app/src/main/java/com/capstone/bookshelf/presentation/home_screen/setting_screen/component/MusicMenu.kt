package com.capstone.bookshelf.presentation.home_screen.setting_screen.component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.component.music.MusicItem
import com.capstone.bookshelf.presentation.bookcontent.component.music.MusicListAction
import com.capstone.bookshelf.presentation.bookcontent.component.music.MusicViewModel
import com.capstone.bookshelf.presentation.home_screen.setting_screen.SettingAction
import com.capstone.bookshelf.presentation.home_screen.setting_screen.SettingState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicMenu(
    settingState: SettingState,
    onAction: (SettingAction) -> Unit
) {
    val musicViewModel = koinViewModel<MusicViewModel>()
    val state by musicViewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            musicViewModel.onEvent(MusicListAction.OnAddPerform(it, context))
        }
    }
    var volumeSliderValue by remember { mutableFloatStateOf(state.playerVolume) }
    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    WindowInsets.safeContent
                        .only(WindowInsetsSides.Horizontal)
                        .asPaddingValues()
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.Center),
                    text = "MUSIC MENU",
                    style = TextStyle(
                        fontSize = 20.sp,
                    )
                )
                IconButton(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = {
                        launcher.launch(arrayOf("audio/mpeg", "audio/wav"))
                    }
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_add_music),
                        contentDescription = null,
                    )
                }
            }
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = "Enable background music",
                    style = TextStyle(
                        fontSize = 16.sp,
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = settingState.enableBackgroundMusic,
                    onCheckedChange = {
                        onAction(SettingAction.OnEnableBackgroundMusicChange(it))
                    },
                )
            }
            Row(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Volume",
                    style = TextStyle(
                        fontSize = 16.sp,
                    )
                )
                Text(
                    text = "%.2fx".format(volumeSliderValue),
                    style = TextStyle(
                        fontSize = 16.sp,
                    )
                )
            }
            Slider(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                value = volumeSliderValue,
                onValueChange = { value ->
                    volumeSliderValue = (value * 100).roundToInt() / 100f
                },
                onValueChangeFinished = {
                    onAction(SettingAction.OnPlayerVolumeChange(volumeSliderValue))
                },
                valueRange = 0f..1f,
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    )
                }
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 8.dp),
                state = listState,
                content = {
                    items(
                        items = state.musicList,
                        key = { it.id!! }
                    ) { listItem ->
                        MusicItemView(
                            music = listItem,
                            onFavoriteClick = { musicItem ->
                                musicViewModel.onEvent(MusicListAction.OnFavoriteClick(musicItem))
                            },
                            onItemClick = { musicItem ->
                                musicViewModel.onEvent(MusicListAction.OnItemClick(musicItem))
                            },
                            onDelete = { musicItem ->
                                musicViewModel.onEvent(MusicListAction.OnDelete(musicItem))
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun MusicItemView(
    music: MusicItem,
    onFavoriteClick: (MusicItem) -> Unit,
    onItemClick: (MusicItem) -> Unit,
    onDelete: (MusicItem) -> Unit
) {
    val isSelected by rememberUpdatedState(music.isSelected)
    val dismissState = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()
    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp)),
        backgroundContent = {
            if (dismissState.dismissDirection.name == SwipeToDismissBoxValue.StartToEnd.name) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Red),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.padding(start = 12.dp),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_delete),
                        contentDescription = "delete"
                    )
                }
            }
        },
        enableDismissFromEndToStart = false,
        onDismiss = {
            if (!isSelected) {
                onDelete(music)
            } else {
                scope.launch {
                    dismissState.reset()
                }
            }
        },
        content = {
            val infiniteTransition = rememberInfiniteTransition()
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
            Box(
                modifier = Modifier
                    .clickable(
                        onClick = {
                            onItemClick(music)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(12.dp)
                            .size(40.dp)
                            .then(
                                if (music.isSelected)
                                    Modifier.graphicsLayer(rotationZ = rotation)
                                else
                                    Modifier
                            ),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_music_disk),
                        contentDescription = null,
                        tint = if (music.isSelected) Color.Red else Color.Gray
                    )
                    music.name?.let {
                        Text(
                            text = it,
                            modifier = Modifier
                                .weight(1f)
                                .basicMarquee(
                                    animationMode = MarqueeAnimationMode.Immediately,
                                    initialDelayMillis = 0,
                                    repeatDelayMillis = 0
                                )
                        )
                    }
                    IconButton(
                        modifier = Modifier.padding(top = 4.dp, end = 4.dp),
                        onClick = {
                            onFavoriteClick(music)
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_favourite_music),
                            contentDescription = null,
                            tint = if (music.isFavorite) Color.Red else
                                Color.Gray
                        )
                    }
                }
            }
        }
    )
}