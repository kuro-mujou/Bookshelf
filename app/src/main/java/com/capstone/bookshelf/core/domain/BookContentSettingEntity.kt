package com.capstone.bookshelf.core.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookContentSetting",
    foreignKeys = [androidx.room.ForeignKey(
        entity = BookEntity::class,
        parentColumns = ["bookId"],
        childColumns = ["bookId"],
        onDelete = androidx.room.ForeignKey.CASCADE
    )]
)
data class BookContentSettingEntity(
    @PrimaryKey(autoGenerate = true) val settingId: Int = 0,
    val bookId: Int,
)