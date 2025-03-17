package com.capstone.bookshelf.presentation.bookcontent.component.music

import android.content.Context
import android.net.Uri

sealed interface MusicListAction {
    data class OnAddPerform(val uri: Uri, val context: Context) : MusicListAction
    data class OnFavoriteClick(val musicItem: MusicItem) : MusicListAction
    data class OnItemClick(val musicItem: MusicItem) : MusicListAction
    data class OnDelete(val musicItem: MusicItem) : MusicListAction
    data class OnVolumeChange(val volume: Float) : MusicListAction
}