package com.capstone.bookshelf.domain.book

import com.capstone.bookshelf.data.book.database.entity.TableOfContentEntity

interface TableOfContentRepository {
    suspend fun saveTableOfContent(tocEntity: TableOfContentEntity): Long
    suspend fun getTableOfContents(bookId: Int): List<TableOfContentEntity>
}