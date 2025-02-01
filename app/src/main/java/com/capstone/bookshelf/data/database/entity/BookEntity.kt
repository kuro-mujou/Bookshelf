package com.capstone.bookshelf.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.capstone.bookshelf.data.database.StringListTypeConverter

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = false) val bookId: String,
    val title: String,
    val coverImagePath: String,
    @TypeConverters(StringListTypeConverter::class)
    val authors: List<String>,
    @TypeConverters(StringListTypeConverter::class)
    val categories: List<String>,
    val description: String?,
    val totalChapter: Int,
    val currentChapter: Int = 0,
    val currentParagraph: Int = 0,
    val isFavorite: Boolean = false,
    val storagePath: String?,
    val ratingsAverage: Double?,
    val ratingsCount: Int?,
)