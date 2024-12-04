package com.capstone.bookshelf.data.book.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.capstone.bookshelf.data.book.database.dao.BookDao
import com.capstone.bookshelf.data.book.database.dao.BookSettingDao
import com.capstone.bookshelf.data.book.database.dao.ChapterDao
import com.capstone.bookshelf.data.book.database.dao.ImagePathDao
import com.capstone.bookshelf.data.book.database.dao.TableOfContentDao
import com.capstone.bookshelf.data.book.database.entity.BookEntity
import com.capstone.bookshelf.data.book.database.entity.BookSettingEntity
import com.capstone.bookshelf.data.book.database.entity.ChapterContentEntity
import com.capstone.bookshelf.data.book.database.entity.ImagePathEntity
import com.capstone.bookshelf.data.book.database.entity.TableOfContentEntity

@Database(
    entities = [
        BookEntity::class,
        TableOfContentEntity::class,
        ChapterContentEntity::class,
        BookSettingEntity::class,
        ImagePathEntity::class
    ],
    exportSchema = false,
    version = 2
)
@TypeConverters(StringListTypeConverter::class)
abstract class LocalBookDatabase : RoomDatabase() {
    abstract val bookDao: BookDao
    abstract val bookSettingDao: BookSettingDao
    abstract val chapterDao: ChapterDao
    abstract val tableOfContentDao: TableOfContentDao
    abstract val imagePathDao: ImagePathDao

    companion object {
        const val DATABASE_NAME = "local_book_database"
    }
}

