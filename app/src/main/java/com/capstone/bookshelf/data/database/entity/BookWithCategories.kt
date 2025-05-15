package com.capstone.bookshelf.data.database.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class BookWithCategories(
    @Embedded val book: BookEntity,
    @Relation(
        parentColumn = "bookId", // Primary key of BookEntity
        entityColumn = "categoryId", // Primary key of CategoryEntity
        associateBy = Junction(
            value = BookCategoryCrossRef::class,
        )
    )
    val categories: List<CategoryEntity>
)