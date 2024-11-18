package com.capstone.bookshelf.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.capstone.bookshelf.core.domain.BookEntity
import com.capstone.bookshelf.core.domain.BookSettingEntity
import com.capstone.bookshelf.core.domain.ChapterContentEntity
import com.capstone.bookshelf.core.domain.MainSettingEntity
import com.capstone.bookshelf.core.domain.TableOfContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBook(book: BookEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTableOfContent(tableOfContent: TableOfContentEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertChapterContent(chapterContent: ChapterContentEntity)

    @Transaction
    @Query("SELECT * FROM books WHERE bookId = :id")
    suspend fun getBookById(id: Int): BookEntity

    @Transaction
    @Query("SELECT * FROM table_of_contents WHERE bookId = :bookId")
    suspend fun getTableOfContents(bookId: Int): List<TableOfContentEntity>

    @Transaction
    @Query("SELECT * FROM chapter_content WHERE bookId = :bookId AND tocId = :tocId")
    suspend fun getChapterContent(bookId: Int,tocId: Int): ChapterContentEntity?

    @Query("SELECT COUNT(*) FROM chapter_content WHERE bookId = :bookId")
    fun getPageSize(bookId: Int): Flow<Int>

    @Transaction
    @Query("SELECT * FROM books WHERE title = :title LIMIT 1")
    suspend fun getBookByTitleAndAuthor(title: String): BookEntity?

    @Transaction
    @Query("SELECT * FROM books ORDER BY isFavorite DESC")
    fun readAllBooksSortByFavorite(): Flow<List<BookEntity>>

    @Transaction
    @Query("SELECT * FROM books")
    fun readAllBooks(): Flow<List<BookEntity>>

    @Query("UPDATE books SET isFavorite = :isFavorite WHERE bookId = :bookId")
    suspend fun setFavoriteBook(isFavorite: Boolean, bookId: Int)

    @Query("UPDATE books SET currentChapter = :chapterIndex WHERE bookId = :bookId")
    suspend fun saveBookInfo(bookId: Int, chapterIndex: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: MainSettingEntity) : Long

    @Query("UPDATE setting SET toggleFavourite = :toggleFavourite WHERE settingId = :settingId")
    suspend fun updateSetting(settingId: Int, toggleFavourite: Boolean)

    @Query("SELECT * FROM setting WHERE settingId = :settingId")
    suspend fun getSetting(settingId : Int) : MainSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBookSetting(setting: BookSettingEntity) : Long

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
}
