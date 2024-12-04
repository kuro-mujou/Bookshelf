package com.capstone.bookshelf.data.setting.repository

import com.capstone.bookshelf.data.setting.database.MainSettingEntity
import com.capstone.bookshelf.data.setting.database.SettingDao
import com.capstone.bookshelf.domain.setting.SettingRepository

class SettingRepositoryImpl(
    private val settingDao: SettingDao,
): SettingRepository {
    override suspend fun createSetting(setting: MainSettingEntity): Long{
        return settingDao.saveSetting(setting)
    }
    override suspend fun getSetting(settingId : Int): MainSettingEntity?{
        return settingDao.getSetting(settingId)
    }
    override suspend fun updateLocalBookListFavourite(settingId: Int, toggleFavourite: Boolean){
        settingDao.updateLocalBookListFavourite(settingId, toggleFavourite)
    }
    override suspend fun updateRemoteBookListFavourite(settingId: Int, toggleFavourite: Boolean){
        settingDao.updateRemoteBookListFavourite(settingId, toggleFavourite)
    }
}