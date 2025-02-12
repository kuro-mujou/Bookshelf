package com.capstone.bookshelf.presentation.bookcontent.component.tts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi

@UnstableApi
@Composable
fun PlayerScreen(playerViewModel: PlayerViewModel = viewModel()) {
    var isPlaying by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            playerViewModel.initialize(context)
        }) {
            Text("Initialize")
        }
        Button(onClick = {
            playerViewModel.play(
                MediaItem.Builder()
//                    .setUri("")
                    .setUri("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3")
                    .build()
            )
        }) {
            Text("Load Music")
        }

        Button(onClick = {
            playerViewModel.togglePlayPause()
            isPlaying = !isPlaying
        }) {
            Text(if (isPlaying) "Pause" else "Play")
        }
        Button(onClick = {
            playerViewModel.stop()
        }) {
            Text("Stop")
        }
        Button(onClick = {
            playerViewModel.release()
        }) {
            Text("Release")
        }
        Button(onClick = {
            playerViewModel.seekToNext()
        }) {
            Text("Next")
        }
        Button(onClick = {
            playerViewModel.seekToPrevious()
        }) {
            Text("Previous")
        }
        Button(onClick = {
            playerViewModel.seekBack()
        }) {
            Text("Back")
        }
        Button(onClick = {
            playerViewModel.seekForward()
        }) {
            Text("Forward")
        }
    }
}