package com.capstone.bookshelf.data.book.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookContentSetting",)
data class BookSettingEntity(
    @PrimaryKey(autoGenerate = false) val settingId: Int = 0,
    val screenShallBeKeptOn: Boolean = false,
    val speed: Float? = null,
    val pitch: Float? = null,
    val ttsLocale: String? = null,
    val ttsVoice: String? = null,
    val autoScrollSpeed: Float? = null,
    val backgroundColor: Int? = null,
    val textColor: Int? = null,
)