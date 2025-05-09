package com.capstone.bookshelf.presentation.bookcontent.component.music

import android.content.Context
import android.provider.OpenableColumns
import androidx.core.uri.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.bookshelf.domain.repository.MusicPathRepository
import com.capstone.bookshelf.util.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MusicViewModel(
    private val musicRepository: MusicPathRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    private val _state = MutableStateFlow(MusicState())
    val state = _state
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    init {
        viewModelScope.launch {
            musicRepository.getMusicPaths()
                .collectLatest { sortedMusicItems ->
                    _state.update {
                        it.copy(
                            musicList = sortedMusicItems
                        )
                    }
                }
        }
        viewModelScope.launch {
            dataStoreManager.playerVolume.collectLatest { volume ->
                _state.update {
                    it.copy(
                        playerVolume = volume
                    )
                }
            }
        }
    }

    fun onEvent(event: MusicListAction) {
        when (event) {
            is MusicListAction.OnAddPerform -> {
                val fileName = getFileName(event.context, event.uri)
                val filePath = saveMusicToPrivateStorage(event.context, event.uri, fileName)
                viewModelScope.launch {
                    musicRepository.saveMusicPaths(
                        listOf(
                            MusicItem(
                                name = fileName,
                                uri = filePath
                            )
                        )
                    )
                }
            }

            is MusicListAction.OnFavoriteClick -> {
                viewModelScope.launch {
                    musicRepository.setMusicAsFavorite(
                        event.musicItem.id!!,
                        !event.musicItem.isFavorite
                    )
                }
            }

            is MusicListAction.OnItemClick -> {
                viewModelScope.launch {
                    musicRepository.setMusicAsSelected(
                        event.musicItem.id!!,
                        !event.musicItem.isSelected
                    )
                }
            }

            is MusicListAction.OnDelete -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            musicList = _state.value.musicList - event.musicItem
                        )
                    }
                    processDeleteMusic(event.musicItem)
                }
            }

            is MusicListAction.OnVolumeChange -> {
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            playerVolume = event.volume
                        )
                    }
                    dataStoreManager.setPlayerVolume(event.volume)
                }
            }
        }
    }

    private fun saveMusicToPrivateStorage(context: Context, uri: Uri, fileName: String): String {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, fileName)
            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            "error"
        }
    }

    private fun getFileName(context: Context, uri: android.net.Uri): String {
        var fileName = "unknown"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    val name = cursor.getString(nameIndex)
                    if (!name.isNullOrBlank()) {
                        fileName = name
                    }
                }
            }
        }
        return fileName
    }

    private fun processDeleteMusic(musicItem: MusicItem) {
        viewModelScope.launch {
            val file = File(musicItem.uri!!)
            if (file.exists()) {
                file.delete()
            }
            musicRepository.deleteMusicPath(musicItem.id!!)
        }
    }
}