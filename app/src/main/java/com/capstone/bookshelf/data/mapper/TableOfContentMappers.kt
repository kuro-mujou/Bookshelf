package com.capstone.bookshelf.data.mapper

import com.capstone.bookshelf.data.database.entity.TableOfContentEntity
import com.capstone.bookshelf.domain.wrapper.TableOfContent

fun TableOfContent.toEntity(): TableOfContentEntity {
    return TableOfContentEntity(
        tocId = tocId,
        bookId = bookId,
        title = title,
        index = index
    )
}

fun TableOfContentEntity.toDataClass(): TableOfContent {
    return TableOfContent(
        tocId = tocId,
        bookId = bookId,
        title = title,
        index = index
    )
}