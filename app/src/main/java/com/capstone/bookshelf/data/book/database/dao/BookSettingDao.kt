package com.capstone.bookshelf.data.book.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.capstone.bookshelf.data.book.database.entity.BookSettingEntity

@Dao
interface BookSettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createBookSetting(setting: BookSettingEntity) : Long

    @Query("SELECT * FROM bookContentSetting WHERE settingId = :settingId")
    suspend fun getBookSetting(settingId : Int) : BookSettingEntity?

    @Query("UPDATE bookContentSetting SET ttsLocale = :ttsLocale WHERE settingId = :settingId")
    suspend fun updateBookSettingLocale(settingId: Int, ttsLocale: String)

    @Query("UPDATE bookContentSetting SET ttsVoice = :ttsVoice WHERE settingId = :settingId")
    suspend fun updateBookSettingVoice(settingId: Int, ttsVoice: String)

    @Query("UPDATE bookContentSetting SET speed = :speed WHERE settingId = :settingId")
    suspend fun updateBookSettingSpeed(settingId: Int, speed: Float)

    @Query("UPDATE bookContentSetting SET pitch = :pitch WHERE settingId = :settingId")
    suspend fun updateBookSettingPitch(settingId: Int, pitch: Float)

    @Query("UPDATE bookContentSetting SET screenShallBeKeptOn = :screenShallBeKeptOn WHERE settingId = :settingId")
    suspend fun updateBookSettingScreenShallBeKeptOn(settingId: Int, screenShallBeKeptOn: Boolean)

    @Query("UPDATE bookContentSetting SET autoScrollSpeed = :autoScrollSpeed WHERE settingId = :settingId")
    suspend fun updateBookSettingAutoScrollSpeed(settingId: Int, autoScrollSpeed: Float)
}