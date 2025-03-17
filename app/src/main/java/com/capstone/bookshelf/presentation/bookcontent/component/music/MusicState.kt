package com.capstone.bookshelf.presentation.bookcontent.component.music

data class MusicState(
    val musicList: List<MusicItem> = emptyList(),
    val playerVolume: Float = 1f,
)
