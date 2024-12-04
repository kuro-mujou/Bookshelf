package com.capstone.bookshelf.data.setting.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        MainSettingEntity::class,
    ],
    exportSchema = false,
    version = 1
)

abstract class SettingDatabase : RoomDatabase() {
    abstract val settingDao: SettingDao

    companion object {
        const val DATABASE_NAME = "setting_database"
    }
}

