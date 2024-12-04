package com.capstone.bookshelf.data.setting.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "setting")
data class MainSettingEntity(
    @PrimaryKey(autoGenerate = false) val settingId: Int = 0,
    val localBookListFavourite: Boolean = false,
    val remoteBookListFavourite: Boolean = false,
)