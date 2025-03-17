package com.capstone.bookshelf.data.mapper

import com.capstone.bookshelf.data.database.entity.MusicPathEntity
import com.capstone.bookshelf.presentation.bookcontent.component.music.MusicItem

fun MusicItem.toEntity(): MusicPathEntity {
    return MusicPathEntity(
        name = name!!,
        uri = uri!!,
    )
}

fun MusicPathEntity.toDataClass(): MusicItem {
    return MusicItem(
        id = id,
        name = name,
        uri = uri,
        isFavorite = isFavorite,
        isSelected = isSelected
    )
}