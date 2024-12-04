package com.capstone.bookshelf.data.book.mapper

import com.capstone.bookshelf.data.book.database.entity.ChapterContentEntity
import com.capstone.bookshelf.domain.book.wrapper.Chapter

fun Chapter.toChapterEntity(): ChapterContentEntity {
    return ChapterContentEntity(
        chapterContentId = chapterContentId,
        tocId = tocId,
        bookId = bookId,
        chapterTitle = chapterTitle,
        content = content,
    )
}

fun ChapterContentEntity.toChapter(): Chapter {
    return Chapter(
        chapterContentId = chapterContentId,
        tocId = tocId,
        bookId = bookId,
        chapterTitle = chapterTitle,
        content = content,
    )
}