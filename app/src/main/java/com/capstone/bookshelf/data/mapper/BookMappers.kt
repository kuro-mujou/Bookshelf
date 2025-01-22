package com.capstone.bookshelf.data.mapper

import com.capstone.bookshelf.data.database.entity.BookEntity
import com.capstone.bookshelf.domain.wrapper.Book

fun Book.toEntity(): BookEntity {
    return BookEntity(
        bookId = id,
        title = title,
        coverImagePath = coverImagePath,
        authors = authors,
        categories = categories,
        description = description,
        totalChapter = totalChapter,
        currentChapter = currentChapter,
        isFavorite = isFavorite,
        storagePath = storagePath,
        ratingsAverage = ratingsAverage,
        ratingsCount = ratingsCount,
    )
}

fun BookEntity.toDataClass(): Book {
    return Book(
        id = bookId,
        title = title,
        coverImagePath = coverImagePath,
        authors = authors,
        categories = categories,
        description = description,
        totalChapter = totalChapter,
        currentChapter = currentChapter,
        isFavorite = isFavorite,
        storagePath = storagePath,
        ratingsAverage = ratingsAverage,
        ratingsCount = ratingsCount,
    )
}