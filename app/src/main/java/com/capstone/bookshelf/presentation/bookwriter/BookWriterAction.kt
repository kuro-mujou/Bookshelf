package com.capstone.bookshelf.presentation.bookwriter

import android.content.Context

sealed interface BookWriterAction {
    data class AddBookInfo(
        val context: Context,
        val bookTitle: String,
        val authorName: String,
        val coverImagePath: String
    ): BookWriterAction
    data class AddChapter(val chapterTitle: String): BookWriterAction
    data class AddImage(val context: Context,val imageUri: String): BookWriterAction
}