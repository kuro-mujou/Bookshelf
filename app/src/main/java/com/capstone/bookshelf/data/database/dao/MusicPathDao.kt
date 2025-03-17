package com.capstone.bookshelf.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.capstone.bookshelf.data.database.entity.MusicPathEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicPathDao {

    @Transaction
    @Query("SELECT * FROM music_path ORDER BY isFavorite DESC")
    fun getMusicPaths(): Flow<List<MusicPathEntity>>

    @Transaction
    @Query("SELECT * FROM music_path WHERE isSelected = 1")
    suspend fun getSelectedMusicPaths(): List<MusicPathEntity>

    @Transaction
    @Query("DELETE FROM music_path WHERE name IN (:names)")
    suspend fun deleteByName(names: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMusicPaths(musicPathEntity: List<MusicPathEntity>)

    @Query("UPDATE music_path SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setMusicAsFavorite(id: Int, isFavorite: Boolean)

    @Query("UPDATE music_path SET isSelected = :isSelected WHERE id = :id")
    suspend fun setMusicAsSelected(id: Int, isSelected: Boolean)

    @Query("DELETE FROM music_path WHERE id = :id")
    suspend fun deleteMusicPath(id: Int)
}