package com.capstone.bookshelf.domain.repository

import com.capstone.bookshelf.presentation.bookcontent.component.music.MusicItem
import kotlinx.coroutines.flow.Flow

interface MusicPathRepository {
    suspend fun getMusicPaths(): Flow<List<MusicItem>>
    suspend fun getSelectedMusicPaths(): List<MusicItem>
    suspend fun deleteByName(names: List<String>)
    suspend fun saveMusicPaths(musicPathEntity: List<MusicItem>)
    suspend fun setMusicAsFavorite(id: Int, isFavorite: Boolean)
    suspend fun setMusicAsSelected(id: Int, isSelected: Boolean)
    suspend fun deleteMusicPath(id: Int)
}