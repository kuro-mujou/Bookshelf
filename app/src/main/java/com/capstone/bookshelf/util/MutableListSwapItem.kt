package com.capstone.bookshelf.util

fun <T> MutableList<T>.move(fromIndex: Int, toIndex: Int) {
    if (fromIndex in indices && toIndex in indices) {
        val item = removeAt(fromIndex)
        add(toIndex, item)
    }
}