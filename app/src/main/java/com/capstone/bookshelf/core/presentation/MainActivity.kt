package com.capstone.bookshelf.core.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.capstone.bookshelf.core.navigation.SetupNavGraph
import com.capstone.bookshelf.core.presentation.theme.BookShelfTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookShelfTheme(
                dynamicColor = false
            ) {
                val navController = rememberNavController()
                SetupNavGraph(navController)
            }
        }
    }
}
