package com.capstone.bookshelf.domain.repository

import com.capstone.bookshelf.data.database.entity.TableOfContentEntity
import com.capstone.bookshelf.domain.wrapper.TableOfContent
import kotlinx.coroutines.flow.Flow

interface TableOfContentRepository {
    suspend fun saveTableOfContent(tocEntity: TableOfContentEntity): Long
    suspend fun getFlowTableOfContents(bookId: String): Flow<List<TableOfContent>>
    suspend fun getTableOfContents(bookId: String): List<TableOfContent>
    suspend fun getTableOfContent(bookId: String, tocId: Int): TableOfContent?
    suspend fun addChapter(bookId: String, chapter: TableOfContent)
    suspend fun updateTableOfContentFavoriteStatus(bookId: String, index: Int, isFavorite: Boolean)
    suspend fun updateTableOfContentTitle(bookId: String, index: Int, header: String)
    suspend fun deleteTableOfContent(bookId: String, tocId: Int)
    suspend fun updateTableOfContentIndexOnDelete(bookId: String, index: Int)
    suspend fun updateTableOfContentIndexOnInsert(bookId: String, index: Int)
    suspend fun swapTableOfContent(currentBookId: String, draggedItemTocId: Int, fromIndex: Int, toIndex: Int)
}