package com.capstone.bookshelf.core.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val bookId: Int = 0,
    val title: String,
    val coverImagePath: String,
    val author: String? = null,
    val totalChapter: Int,
    val currentChapter: Int = 1,
    val isFavorite: Boolean = false
)

