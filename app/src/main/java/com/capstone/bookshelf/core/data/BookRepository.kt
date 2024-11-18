package com.capstone.bookshelf.core.data

import com.capstone.bookshelf.core.data.dao.BookDao
import com.capstone.bookshelf.core.domain.BookEntity
import com.capstone.bookshelf.core.domain.BookSettingEntity
import com.capstone.bookshelf.core.domain.ChapterContentEntity
import com.capstone.bookshelf.core.domain.MainSettingEntity
import com.capstone.bookshelf.core.domain.TableOfContentEntity
import kotlinx.coroutines.flow.Flow

class BookRepository(
    private val bookDao: BookDao,
) {
    suspend fun saveBook(book: BookEntity): Long {
        return bookDao.insertBook(book)
    }

    suspend fun saveTableOfContent(tocEntity: TableOfContentEntity): Long {
        return bookDao.insertTableOfContent(tocEntity)
    }

    suspend fun saveChapterContent(chapterContentEntity: ChapterContentEntity) {
        bookDao.insertChapterContent(chapterContentEntity)
    }
    suspend fun isBookAlreadyImported(title: String): Boolean {
        return bookDao.getBookByTitleAndAuthor(title) != null
    }

    suspend fun getBookById(id: Int): BookEntity {
        return bookDao.getBookById(id)
    }

    suspend fun getChapterContent(bookId: Int, tocId: Int): ChapterContentEntity? {
        return bookDao.getChapterContent(bookId, tocId)
    }

    fun readAllBooksSortByFavorite(): Flow<List<BookEntity>> {
        return bookDao.readAllBooksSortByFavorite()
    }
    fun readAllBooks(): Flow<List<BookEntity>> {
        return bookDao.readAllBooks()
    }
    suspend fun setBookAsFavorite(bookId: Int, isFavorite: Boolean) {
        bookDao.setFavoriteBook( isFavorite, bookId)
    }

    suspend fun getTableOfContents(bookId: Int): List<TableOfContentEntity> {
        return bookDao.getTableOfContents(bookId)
    }

    suspend fun saveBookInfo(bookId: Int, chapterIndex: Int) {
        bookDao.saveBookInfo(bookId,chapterIndex)
    }

    suspend fun saveSetting(setting: MainSettingEntity): Long{
        return bookDao.saveSetting(setting)
    }

    suspend fun getSetting(settingId : Int): MainSettingEntity?{
        return bookDao.getSetting(settingId)
    }

    suspend fun updateSetting(settingId: Int, toggleFavourite: Boolean){
        bookDao.updateSetting(settingId, toggleFavourite)
    }

    suspend fun saveBookSetting(setting: BookSettingEntity): Long{
        return bookDao.saveBookSetting(setting)
    }

    suspend fun getBookSetting(settingId : Int): BookSettingEntity?{
        return bookDao.getBookSetting(settingId)
    }

    suspend fun updateBookSettingVoice(settingId: Int, voice: String){
        bookDao.updateBookSettingVoice(settingId, voice)
    }

    suspend fun updateBookSettingLocale(settingId: Int, locale: String){
        bookDao.updateBookSettingLocale(settingId, locale)
    }
    suspend fun updateBookSettingSpeed(settingId: Int, speed: Float){
        bookDao.updateBookSettingSpeed(settingId, speed)
    }
    suspend fun updateBookSettingPitch(settingId: Int, pitch: Float){
        bookDao.updateBookSettingPitch(settingId, pitch)
    }
    suspend fun updateBookSettingScreenShallBeKeptOn(settingId: Int, screenShallBeKeptOn: Boolean){
        bookDao.updateBookSettingScreenShallBeKeptOn(settingId, screenShallBeKeptOn)
    }
}
