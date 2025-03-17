package com.capstone.bookshelf.presentation.bookcontent.drawer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.capstone.bookshelf.app.Route
import com.capstone.bookshelf.domain.repository.TableOfContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DrawerContainerViewModel(
    private val tableOfContentRepository: TableOfContentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookId = savedStateHandle.toRoute<Route.BookContent>().bookId
    private val _state = MutableStateFlow(DrawerContainerState())
    val state = _state
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )
    fun onAction(action: DrawerContainerAction) {
        when (action) {
            is DrawerContainerAction.UpdateDrawerState -> {
                _state.value = _state.value.copy(
                    drawerState = action.drawerState
                )
            }

            is DrawerContainerAction.UpdateCurrentTOC -> {
                try {
                    val currentTOC = _state.value.tableOfContents[action.toc]
                    _state.value = _state.value.copy(
                        currentTOC = currentTOC
                    )
                }catch (e : Exception){
                    viewModelScope.launch {
                        val currentTOC = tableOfContentRepository.getTableOfContent(bookId,action.toc)
                        _state.value = _state.value.copy(
                            currentTOC = currentTOC
                        )
                    }
                }
            }
        }
    }
    init {
        viewModelScope.launch {
            tableOfContentRepository
                .getTableOfContents(bookId)
                .collectLatest{ tableOfContents ->
                    _state.update { it.copy(
                        tableOfContents = tableOfContents
                    ) }
                }
        }
    }
}