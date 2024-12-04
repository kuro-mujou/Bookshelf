package com.capstone.bookshelf.data.setting.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SettingDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun saveSetting(setting: MainSettingEntity) : Long

    @Query("SELECT * FROM setting WHERE settingId = :settingId")
    suspend fun getSetting(settingId : Int) : MainSettingEntity?

    @Query("UPDATE setting SET localBookListFavourite = :toggleFavourite WHERE settingId = :settingId")
    suspend fun updateLocalBookListFavourite(settingId: Int, toggleFavourite: Boolean)

    @Query("UPDATE setting SET localBookListFavourite = :toggleFavourite WHERE settingId = :settingId")
    suspend fun updateRemoteBookListFavourite(settingId: Int, toggleFavourite: Boolean)
}