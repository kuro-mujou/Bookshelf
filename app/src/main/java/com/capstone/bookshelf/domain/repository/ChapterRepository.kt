package com.capstone.bookshelf.domain.repository

import com.capstone.bookshelf.data.database.entity.ChapterContentEntity
import com.capstone.bookshelf.domain.wrapper.Chapter

interface ChapterRepository {
    suspend fun getChapterContent(bookId: String, tocId: Int): Chapter?
    suspend fun saveChapterContent(chapterContentEntity: ChapterContentEntity)
    suspend fun updateChapterContent(bookId: String, tocId: Int, content: List<String>)
    suspend fun deleteChapter(bookId: String, tocId: Int)
    suspend fun updateChapterIndexOnDelete(bookId: String, tocId: Int)
    suspend fun updateChapterIndexOnInsert(bookId: String, tocId: Int)
}