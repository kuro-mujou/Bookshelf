package com.capstone.bookshelf.data.mapper

import com.capstone.bookshelf.data.database.entity.ChapterContentEntity
import com.capstone.bookshelf.domain.wrapper.Chapter

fun ChapterContentEntity.toDataClass(): Chapter {
    return Chapter(
        chapterContentId = chapterContentId,
        tocId = tocId,
        bookId = bookId,
        chapterTitle = chapterTitle,
        content = content,
    )
}