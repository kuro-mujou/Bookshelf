package com.capstone.bookshelf.data.mapper

import com.capstone.bookshelf.data.database.entity.TableOfContentEntity
import com.capstone.bookshelf.domain.wrapper.TableOfContent

fun TableOfContentEntity.toDataClass(): TableOfContent {
    return TableOfContent(
        tocId = tocId,
        bookId = bookId,
        title = title,
        index = index,
        isFavorite = isFavorite
    )
}

fun TableOfContent.toEntity(): TableOfContentEntity {
    return TableOfContentEntity(
        bookId = bookId,
        title = title,
        index = index
    )
}