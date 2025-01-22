package com.capstone.bookshelf.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "image_path",
    indices = [Index(
        value = ["bookId"],
        unique = false
    )],
)
data class ImagePathEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: String,
    val imagePath: String
)