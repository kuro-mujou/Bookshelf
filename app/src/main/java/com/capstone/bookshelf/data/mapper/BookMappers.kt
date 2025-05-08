package com.capstone.bookshelf.data.mapper

import com.capstone.bookshelf.data.database.entity.BookEntity
import com.capstone.bookshelf.domain.wrapper.Book
import com.capstone.bookshelf.domain.wrapper.EmptyBook

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
        currentParagraph = currentParagraph,
        isRecentRead = isRecentRead,
        isFavorite = isFavorite,
        storagePath = storagePath,
        isEditable = isEditable,
        fileType = fileType
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
        currentParagraph = currentParagraph,
        isRecentRead = isRecentRead,
        isFavorite = isFavorite,
        storagePath = storagePath,
        isEditable = isEditable,
        fileType = fileType
    )
}

fun EmptyBook.toDataClass(): Book {
    return Book(
        id = id ?: "",
        title = title ?: "",
        coverImagePath = coverImagePath ?: "",
        authors = authors ?: emptyList(),
        categories = categories ?: emptyList(),
        description = description,
        totalChapter = totalChapter ?: 0,
        currentChapter = currentChapter,
        currentParagraph = currentParagraph,
        isRecentRead = isRecentRead,
        isFavorite = isFavorite,
        storagePath = storagePath,
        isEditable = isEditable == true,
        fileType = fileType ?: ""
    )
}