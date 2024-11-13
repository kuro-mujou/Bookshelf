package com.capstone.bookshelf.core.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.bookshelf.core.data.BookRepository
import com.capstone.bookshelf.core.domain.MainSettingEntity
import kotlinx.coroutines.launch

class SettingViewModel(
    private val repository: BookRepository
) : ViewModel() {

    private var _sortedByFavorite: MutableState<Boolean> = mutableStateOf(false)
    var sortedByFavorite: State<Boolean> = _sortedByFavorite

    init{
        viewModelScope.launch {
            val setting = repository.getSetting(0)
            if(setting != null){
                _sortedByFavorite.value = setting.toggleFavourite
            }
            else{
                val newSetting = MainSettingEntity(settingId = 0, toggleFavourite = false)
                repository.saveSetting(newSetting)
                _sortedByFavorite.value = false
            }
        }
    }

    fun updateSortedByFavorite(favourite: Boolean) {
        viewModelScope.launch {
            repository.updateSetting(settingId = 0, toggleFavourite = favourite)
            _sortedByFavorite.value = favourite
        }
    }
}