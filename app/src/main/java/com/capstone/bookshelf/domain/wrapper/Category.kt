package com.capstone.bookshelf.domain.wrapper

data class Category(
    val id: Int? = null,
    val name: String,
    val color: Int,
    val isSelected: Boolean = false,
)