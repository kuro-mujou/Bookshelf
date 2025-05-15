package com.capstone.bookshelf.presentation.bookdetail

import com.capstone.bookshelf.data.database.entity.BookWithCategories
import com.capstone.bookshelf.domain.wrapper.Category
import com.capstone.bookshelf.domain.wrapper.TableOfContent

data class BookDetailState(
    val isLoading: Boolean = true,
    val isFavorite: Boolean = false,
    val bookWithCategories: BookWithCategories? = null,
    val tableOfContents: List<TableOfContent> = emptyList(),
    val isSortedByFavorite: Boolean = false,
    val categories: List<Category> = emptyList()
)
