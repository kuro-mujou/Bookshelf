package com.capstone.bookshelf.presentation.main.homepage.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier,
){
    Column (
        modifier = modifier.fillMaxSize().background(Color.Red).border(
            width = 2.dp,
            color = Color.Yellow
        )
    ){

    }
}