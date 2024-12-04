package com.capstone.bookshelf.data.book.mapper

import com.capstone.bookshelf.data.book.database.entity.BookEntity
import com.capstone.bookshelf.domain.book.wrapper.Book

//fun SearchedBookDto.toBook(): Book {
//    return Book(
//        id = id.substringAfterLast("/"),
//        title = title,
//        imageUrl = if(coverKey != null) {
//            "https://covers.openlibrary.org/b/olid/${coverKey}-L.jpg"
//        } else {
//            "https://covers.openlibrary.org/b/id/${coverAlternativeKey}-L.jpg"
//        },
//        authors = authorNames ?: emptyList(),
//        description = null,
//        languages = languages ?: emptyList(),
//        firstPublishYear = firstPublishYear.toString(),
//        averageRating = ratingsAverage,
//        ratingCount = ratingsCount,
//        numPages = numPagesMedian,
//        numEditions = numEditions ?: 0
//    )
//}

fun Book.toBookEntity(): BookEntity {
    return BookEntity(
        bookId = id,
        title = title,
        coverImagePath = coverImagePath,
        authors = authors,
        categories = categories,
        description = description,
        totalChapter = totalChapter,
        isFavorite = isFavorite,
        ratingsAverage = ratingsAverage,
        ratingsCount = ratingsCount,
    )
}

fun BookEntity.toBook(): Book {
    return Book(
        id = bookId,
        title = title,
        coverImagePath = coverImagePath,
        authors = authors,
        categories = categories,
        description = description,
        totalChapter = totalChapter,
        isFavorite = isFavorite,
        ratingsAverage = ratingsAverage,
        ratingsCount = ratingsCount,
    )
}