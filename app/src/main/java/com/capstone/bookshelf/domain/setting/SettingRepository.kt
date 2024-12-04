package com.capstone.bookshelf.domain.setting

import com.capstone.bookshelf.data.setting.database.MainSettingEntity

interface SettingRepository {
    suspend fun createSetting(setting: MainSettingEntity): Long
    suspend fun getSetting(settingId: Int): MainSettingEntity?
    suspend fun updateLocalBookListFavourite(settingId: Int, toggleFavourite: Boolean)
    suspend fun updateRemoteBookListFavourite(settingId: Int, toggleFavourite: Boolean)
}