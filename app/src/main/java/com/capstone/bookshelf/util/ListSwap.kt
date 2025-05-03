package com.capstone.bookshelf.util

fun <T> List<T>.move(from: Int, to: Int): List<T> {
    val size = this.size
    if (from !in 0..<size) {
        return this
    }
    if (to < 0 || to > size) {
        return this
    }
    if (from == to) {
        return this
    }
    val mutableList = this.toMutableList()
    val item = mutableList.removeAt(from)
    val adjustedTo = if (to > from) to - 1 else to

    if (adjustedTo <= mutableList.size) {
        mutableList.add(adjustedTo, item)
    } else {
        return mutableList.toList()
    }
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