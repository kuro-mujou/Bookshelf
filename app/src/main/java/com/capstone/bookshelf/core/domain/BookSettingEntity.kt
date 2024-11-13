package com.capstone.bookshelf.core.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookContentSetting",)
data class BookSettingEntity(
    @PrimaryKey(autoGenerate = false) val settingId: Int = 0,
    val ttsLocale: String? = null,
    val ttsVoice: String? = null,
)