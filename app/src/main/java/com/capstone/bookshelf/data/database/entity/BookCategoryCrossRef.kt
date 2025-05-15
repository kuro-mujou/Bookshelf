package com.capstone.bookshelf.data.database.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "book_category_cross_ref",
    primaryKeys = ["bookId", "categoryId"],
    indices = [Index("bookId"), Index("categoryId")]
)
data class BookCategoryCrossRef(
    val bookId: String,
    val categoryId: Int
)