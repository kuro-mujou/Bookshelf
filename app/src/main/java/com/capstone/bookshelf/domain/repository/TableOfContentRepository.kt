package com.capstone.bookshelf.domain.repository

import com.capstone.bookshelf.data.database.entity.TableOfContentEntity
import com.capstone.bookshelf.domain.wrapper.TableOfContent
import kotlinx.coroutines.flow.Flow

interface TableOfContentRepository {
    suspend fun saveTableOfContent(tocEntity: TableOfContentEntity): Long
    suspend fun getTableOfContents(bookId: String): Flow<List<TableOfContent>>
    suspend fun getTableOfContent(bookId: String, tocId: Int): TableOfContent
}