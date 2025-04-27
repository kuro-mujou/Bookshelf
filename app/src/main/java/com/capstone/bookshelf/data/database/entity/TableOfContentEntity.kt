package com.capstone.bookshelf.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "table_of_contents",
    indices = [
        Index(
            value = ["bookId"],
            unique = false
        ),
    ],
    foreignKeys = [ForeignKey(
        entity = BookEntity::class,
        parentColumns = ["bookId"],
        childColumns = ["bookId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TableOfContentEntity(
    @PrimaryKey(autoGenerate = true) val tocId: Int = 0,
    val bookId: String,
    val title: String,
    val index: Int,
    val isFavorite: Boolean = false
)

