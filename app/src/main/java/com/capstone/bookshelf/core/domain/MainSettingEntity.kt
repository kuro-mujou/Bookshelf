package com.capstone.bookshelf.core.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "setting")
data class MainSettingEntity(
    @PrimaryKey(autoGenerate = true) val settingId: Int = 0,
    val toggleFavourite: Boolean = false
)