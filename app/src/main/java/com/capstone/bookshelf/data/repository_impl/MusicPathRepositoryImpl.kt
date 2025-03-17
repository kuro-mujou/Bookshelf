package com.capstone.bookshelf.data.repository_impl

import com.capstone.bookshelf.data.database.dao.MusicPathDao
import com.capstone.bookshelf.data.mapper.toDataClass
import com.capstone.bookshelf.data.mapper.toEntity
import com.capstone.bookshelf.domain.repository.MusicPathRepository
import com.capstone.bookshelf.presentation.bookcontent.component.music.MusicItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicPathRepositoryImpl(
    private val musicDao: MusicPathDao,
) : MusicPathRepository {
    override suspend fun getMusicPaths(): Flow<List<MusicItem>> {
        return musicDao.getMusicPaths().map { musicEntity->
            musicEntity.map{it.toDataClass()}
        }
    }

    override suspend fun getSelectedMusicPaths(): List<MusicItem> {
        return musicDao.getSelectedMusicPaths().map { musicEntity->
            musicEntity.toDataClass()
        }
    }

    override suspend fun deleteByName(names: List<String>) {
        musicDao.deleteByName(names)
    }

    override suspend fun saveMusicPaths(musicPathEntity: List<MusicItem>) {
        musicDao.saveMusicPaths(
            musicPathEntity.map { musicItem->
                musicItem.toEntity()
            }
        )
    }

    override suspend fun setMusicAsFavorite(id: Int, isFavorite: Boolean) {
        musicDao.setMusicAsFavorite(id, isFavorite)
    }

    override suspend fun setMusicAsSelected(id: Int, isSelected: Boolean) {
        musicDao.setMusicAsSelected(id, isSelected)
    }

    override suspend fun deleteMusicPath(id: Int) {
        musicDao.deleteMusicPath(id)
    }
}