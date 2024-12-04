package com.capstone.bookshelf.data.book.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.capstone.bookshelf.data.book.database.dao.BookDao
import com.capstone.bookshelf.data.book.database.dao.BookSettingDao
import com.capstone.bookshelf.data.book.database.dao.ChapterDao
import com.capstone.bookshelf.data.book.database.dao.TableOfContentDao
import com.capstone.bookshelf.data.book.database.entity.BookEntity
import com.capstone.bookshelf.data.book.database.entity.BookSettingEntity
import com.capstone.bookshelf.data.book.database.entity.ChapterContentEntity
import com.capstone.bookshelf.data.book.database.entity.TableOfContentEntity

@Database(
    entities = [
        BookEntity::class,
        TableOfContentEntity::class,
        ChapterContentEntity::class,
        BookSettingEntity::class,
    ],
    exportSchema = false,
    version = 1
)
@TypeConverters(StringListTypeConverter::class)
abstract class RemoteBookDatabase : RoomDatabase() {
    abstract val bookDao: BookDao
    abstract val bookSettingDao: BookSettingDao
    abstract val chapterDao: ChapterDao
    abstract val tableOfContentDao: TableOfContentDao

    companion object {
        const val DATABASE_NAME = "remote_book_database"
    }
}