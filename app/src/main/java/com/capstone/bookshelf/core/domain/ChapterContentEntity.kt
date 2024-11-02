package com.capstone.bookshelf.core.domain

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.capstone.bookshelf.core.data.StringListTypeConverter

@Entity(
    tableName = "chapter_content",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["bookId"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
)

data class ChapterContentEntity(
    @PrimaryKey(autoGenerate = true) val chapterContentId: Int = 0,
    val tocId: Int,
    val bookId: Int,
    val chapterTitle: String,
    @TypeConverters(StringListTypeConverter::class)
    val content: List<String>,
)