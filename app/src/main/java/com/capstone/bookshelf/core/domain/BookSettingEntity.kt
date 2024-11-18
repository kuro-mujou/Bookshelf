package com.capstone.bookshelf.core.domain

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
)