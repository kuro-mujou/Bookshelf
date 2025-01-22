package com.capstone.bookshelf.domain.book

import com.capstone.bookshelf.data.database.entity.ChapterContentEntity
import com.capstone.bookshelf.domain.wrapper.Chapter

interface ChapterRepository {
    suspend fun getChapterContent(bookId: String, tocId: Int): Chapter?
    suspend fun saveChapterContent(chapterContentEntity: ChapterContentEntity)
}