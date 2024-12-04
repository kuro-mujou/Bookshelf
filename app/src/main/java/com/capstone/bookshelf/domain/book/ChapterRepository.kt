package com.capstone.bookshelf.domain.book

import com.capstone.bookshelf.data.book.database.entity.ChapterContentEntity

interface ChapterRepository {
    suspend fun getChapterContent(bookId: Int, tocId: Int): ChapterContentEntity?
    suspend fun saveChapterContent(chapterContentEntity: ChapterContentEntity)
}