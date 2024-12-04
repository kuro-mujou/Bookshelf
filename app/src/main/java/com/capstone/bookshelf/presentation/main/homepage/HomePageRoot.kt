package com.capstone.bookshelf.presentation.main.homepage

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
fun HomePageRoot(
    homePageViewModel: HomePageViewModel = koinViewModel(),
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
            Text(text = "HomePage")
        }
    }
}