package com.capstone.bookshelf.data.mapper

import com.capstone.bookshelf.data.database.entity.CategoryEntity
import com.capstone.bookshelf.domain.wrapper.Category

fun Category.toEntity(): CategoryEntity{
    id?.let {
        return CategoryEntity(
            categoryId = it,
            name = name,
            color = color
        )
    } ?: return CategoryEntity(
        name = name,
        color = color
    )
}

fun CategoryEntity.toDataClass(): Category{
    return Category(
        id = categoryId,
        name = name,
        color = color,
    )
}