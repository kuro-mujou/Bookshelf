package com.capstone.bookshelf.presentation.bookwriter.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun BottomBar(

) {
    var expanded by remember { mutableStateOf(false) }
    BottomAppBar(
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxWidth()
    ) {
        IconButton(onClick = { /* Handle Home click */ }) {
            Icon(Icons.Default.Home, contentDescription = "Home")
        }
        IconButton(onClick = { /* Handle Search click */ }) {
            Icon(Icons.Default.Search, contentDescription = "Search")
        }
        Box {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                offset = DpOffset(0.dp, (-40).dp) // Position above the button
            ) {
                DropdownMenuItem(
                    text = { Icon(Icons.Default.Settings, contentDescription = "Search") },
                    onClick = { /* Handle Settings click */ }
                )
                DropdownMenuItem(
                    text = { Icon(Icons.Default.AccountCircle, contentDescription = "Search") },
                    onClick = { /* Handle Profile click */ }
                )
            }
        }
    }
}