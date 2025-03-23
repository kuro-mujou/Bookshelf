package com.capstone.bookshelf.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

fun calculateHeaderSize(level : Int, fontSize: Int): Float {
    return when(level){
        1 -> fontSize*2f
        2 -> fontSize*1.414f
        3 -> fontSize*1.155f
        4 -> fontSize*1f
        5 -> fontSize*0.894f
        6 -> fontSize*0.816f
        else -> fontSize.toFloat()
    }
}
@Composable
fun calculateHeaderSize(level : Int): Float {
    val fontSize = MaterialTheme.typography.bodyMedium.fontSize.value
    return when(level){
        1 -> fontSize*2f
        2 -> fontSize*1.414f
        3 -> fontSize*1.155f
        4 -> fontSize*1f
        5 -> fontSize*0.894f
        6 -> fontSize*0.816f
        else -> fontSize.toFloat()
    }
}