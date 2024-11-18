package com.capstone.bookshelf.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.capstone.bookshelf.core.data.dao.BookDao
import com.capstone.bookshelf.core.domain.BookEntity
import com.capstone.bookshelf.core.domain.BookSettingEntity
import com.capstone.bookshelf.core.domain.ChapterContentEntity
import com.capstone.bookshelf.core.domain.MainSettingEntity
import com.capstone.bookshelf.core.domain.TableOfContentEntity
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

@Database(
    entities = [
        BookEntity::class,
        TableOfContentEntity::class,
        ChapterContentEntity::class,
        MainSettingEntity::class,
        BookSettingEntity::class
    ],
    version = 4
)
@TypeConverters(StringListTypeConverter::class)
abstract class BookDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao

    companion object {
        const val DATABASE_NAME = "book_database"
    }
}

class StringListTypeConverter {
    @TypeConverter
    fun fromString(value: String): List<String> {
        return Json.decodeFromString(
            ListSerializer(String.serializer()), value
        )
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return Json.encodeToString(
            ListSerializer(String.serializer()), list
        )
    }
}