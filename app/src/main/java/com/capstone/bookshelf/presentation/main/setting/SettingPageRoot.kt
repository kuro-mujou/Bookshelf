package com.capstone.bookshelf.presentation.main.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingPageRoot(
    settingViewModel: SettingViewModel = koinViewModel(),
    hazeState: HazeState,
){
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .haze(state = hazeState),
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(text = "Setting")
        }
    }
}