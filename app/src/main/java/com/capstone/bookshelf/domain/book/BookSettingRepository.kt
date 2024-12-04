package com.capstone.bookshelf.domain.book

import com.capstone.bookshelf.data.book.database.entity.BookSettingEntity

interface BookSettingRepository {
    suspend fun saveBookSetting(setting: BookSettingEntity): Long
    suspend fun getBookSetting(settingId: Int): BookSettingEntity?
    suspend fun updateBookSettingVoice(settingId: Int, voice: String)
    suspend fun updateBookSettingLocale(settingId: Int, locale: String)
    suspend fun updateBookSettingSpeed(settingId: Int, speed: Float)
    suspend fun updateBookSettingPitch(settingId: Int, pitch: Float)
    suspend fun updateBookSettingScreenShallBeKeptOn(settingId: Int, screenShallBeKeptOn: Boolean)
    suspend fun updateBookSettingAutoScrollSpeed(settingId: Int, autoScrollSpeed: Float)
}