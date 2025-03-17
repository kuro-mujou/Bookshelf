package com.capstone.bookshelf.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "music_path",
    indices = [Index(
        value = ["name"],
        unique = false
    )],
)
data class MusicPathEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val uri: String,
    val isFavorite: Boolean = false,
    val isSelected: Boolean = false
)