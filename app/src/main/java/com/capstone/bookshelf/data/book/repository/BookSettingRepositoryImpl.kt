package com.capstone.bookshelf.data.book.repository

import com.capstone.bookshelf.data.book.database.dao.BookSettingDao
import com.capstone.bookshelf.data.book.database.entity.BookSettingEntity
import com.capstone.bookshelf.domain.book.BookSettingRepository

class BookSettingRepositoryImpl(
    private val bookSettingDao: BookSettingDao
) : BookSettingRepository {
    override suspend fun saveBookSetting(setting: BookSettingEntity): Long{
        return bookSettingDao.createBookSetting(setting)
    }
    override suspend fun getBookSetting(settingId : Int): BookSettingEntity?{
        return bookSettingDao.getBookSetting(settingId)
    }
    override suspend fun updateBookSettingVoice(settingId: Int, voice: String){
        bookSettingDao.updateBookSettingVoice(settingId, voice)
    }
    override suspend fun updateBookSettingLocale(settingId: Int, locale: String){
        bookSettingDao.updateBookSettingLocale(settingId, locale)
    }
    override suspend fun updateBookSettingSpeed(settingId: Int, speed: Float){
        bookSettingDao.updateBookSettingSpeed(settingId, speed)
    }
    override suspend fun updateBookSettingPitch(settingId: Int, pitch: Float){
        bookSettingDao.updateBookSettingPitch(settingId, pitch)
    }
    override suspend fun updateBookSettingScreenShallBeKeptOn(settingId: Int, screenShallBeKeptOn: Boolean){
        bookSettingDao.updateBookSettingScreenShallBeKeptOn(settingId, screenShallBeKeptOn)
    }
    override suspend fun updateBookSettingAutoScrollSpeed(settingId: Int, autoScrollSpeed: Float){
        bookSettingDao.updateBookSettingAutoScrollSpeed(settingId, autoScrollSpeed)
    }
    override suspend fun updateBookSettingBackgroundColor(settingId: Int, backgroundColor: Int){
        bookSettingDao.updateBookSettingBackgroundColor(settingId, backgroundColor)
    }
    override suspend fun updateBookSettingTextColor(settingId: Int, textColor: Int){
        bookSettingDao.updateBookSettingTextColor(settingId, textColor)
    }
}