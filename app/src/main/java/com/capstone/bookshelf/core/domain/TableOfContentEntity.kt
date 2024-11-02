package com.capstone.bookshelf.core.domain

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "table_of_contents",
    foreignKeys = [ForeignKey(
        entity = BookEntity::class,
        parentColumns = ["bookId"],
        childColumns = ["bookId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TableOfContentEntity(
    @PrimaryKey(autoGenerate = true) val tocId: Int = 0,
    val bookId: Int, // Foreign Key from BookEntity
    val title: String,
    val index: Int
)

