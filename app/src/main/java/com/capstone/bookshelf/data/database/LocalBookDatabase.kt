package com.capstone.bookshelf.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.capstone.bookshelf.data.database.dao.BookDao
import com.capstone.bookshelf.data.database.dao.ChapterDao
import com.capstone.bookshelf.data.database.dao.ImagePathDao
import com.capstone.bookshelf.data.database.dao.MusicPathDao
import com.capstone.bookshelf.data.database.dao.NoteDao
import com.capstone.bookshelf.data.database.dao.TableOfContentDao
import com.capstone.bookshelf.data.database.entity.BookEntity
import com.capstone.bookshelf.data.database.entity.ChapterContentEntity
import com.capstone.bookshelf.data.database.entity.ImagePathEntity
import com.capstone.bookshelf.data.database.entity.MusicPathEntity
import com.capstone.bookshelf.data.database.entity.NoteEntity
import com.capstone.bookshelf.data.database.entity.TableOfContentEntity

@Database(
    entities = [
        BookEntity::class,
        TableOfContentEntity::class,
        ChapterContentEntity::class,
        ImagePathEntity::class,
        MusicPathEntity::class,
        NoteEntity::class
    ],
    exportSchema = false,
    version = 2
)
@TypeConverters(StringListTypeConverter::class)
abstract class LocalBookDatabase : RoomDatabase() {
    abstract val bookDao: BookDao
    abstract val chapterDao: ChapterDao
    abstract val tableOfContentDao: TableOfContentDao
    abstract val imagePathDao: ImagePathDao
    abstract val musicPathDao: MusicPathDao
    abstract val noteDao: NoteDao

    companion object {
        const val DATABASE_NAME = "local_book_database"
    }
}

