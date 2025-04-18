package com.capstone.bookshelf.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["bookId"], unique = false)
    ],
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["bookId"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val noteId: Int = 0,
    val bookId: String,
    val tocId: Int,
    val contentId: Int,
    val noteBody: String,
    val noteInput: String,
    val timestamp: String
)