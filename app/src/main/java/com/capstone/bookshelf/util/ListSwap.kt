package com.capstone.bookshelf.util

fun <T> List<T>.move(from: Int, to: Int): List<T> {
    if (from !in indices || to !in indices) return this
    if (from == to) return this

    val mutableList = this.toMutableList()
    val item = mutableList.removeAt(from)
    mutableList.add(to, item)
    return mutableList.toList()
}

fun updateFocusIndex(focusedIndex: Int, startIndex: Int, endIndex: Int): Int {
    return when {
        focusedIndex == startIndex -> endIndex
        startIndex > endIndex && focusedIndex in endIndex until startIndex -> focusedIndex + 1
        startIndex < endIndex && focusedIndex in (startIndex + 1)..endIndex -> focusedIndex - 1
        else -> focusedIndex
    }
}