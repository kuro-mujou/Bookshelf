package com.capstone.bookshelf.presentation.main.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.bookshelf.data.setting.database.MainSettingEntity
import com.capstone.bookshelf.domain.setting.SettingRepository
import kotlinx.coroutines.launch

class SettingViewModel(
    private val repository: SettingRepository
) : ViewModel() {
    init{
        viewModelScope.launch {
            val setting = repository.getSetting(0)
            if(setting == null){
                val newSetting = MainSettingEntity()
                repository.createSetting(newSetting)
            }
        }
    }
}