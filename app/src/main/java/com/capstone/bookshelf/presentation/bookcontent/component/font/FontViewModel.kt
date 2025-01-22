package com.capstone.bookshelf.presentation.bookcontent.component.font

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class FontViewModel : ViewModel() {
    private val _state = MutableStateFlow(FontState())
    val state = _state
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )
    fun onAction(action: FontAction) {
        when (action) {
            is FontAction.UpdateFontSize -> {
                _state.value = _state.value.copy(
                    fontSize = action.fontSize
                )
            }
            is FontAction.UpdateLineSpacing -> {
                _state.value = _state.value.copy(
                    lineSpacing = action.lineSpacing
                )
            }
            is FontAction.UpdateSelectedFontFamilyIndex -> {
                _state.value = _state.value.copy(
                    selectedFontFamilyIndex = action.index
                )
            }
            is FontAction.UpdateTextAlign -> {
                _state.value = _state.value.copy(
                    textAlign = action.textAlign
                )
            }
            is FontAction.UpdateTextIndent -> {
                _state.value = _state.value.copy(
                    textIndent = action.textIndent
                )
            }
        }
    }
}