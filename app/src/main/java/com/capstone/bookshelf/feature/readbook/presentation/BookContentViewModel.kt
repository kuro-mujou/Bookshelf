package com.capstone.bookshelf.feature.readbook.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.bookshelf.core.data.BookRepository
import com.capstone.bookshelf.core.domain.BookEntity
import com.capstone.bookshelf.core.domain.ChapterContentEntity
import com.capstone.bookshelf.core.domain.TableOfContentEntity
import com.capstone.bookshelf.feature.readbook.presentation.state.ContentUIState
import com.capstone.bookshelf.feature.readbook.presentation.state.TTSState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookContentViewModel(
    private val repository: BookRepository,
    private val bookId: Int,
    private val tocId: Int
) : ViewModel() {

    private val _tableOfContents : MutableState<List<TableOfContentEntity>> = mutableStateOf(emptyList())
    val tableOfContents: State<List<TableOfContentEntity>> = _tableOfContents

    private val _book: MutableState<BookEntity> = mutableStateOf(BookEntity(
        title = "",
        coverImagePath = "",
        totalChapter = 0
    ))
    val book: State<BookEntity> = _book

    private val _chapterContent: MutableState<ChapterContentEntity?> = mutableStateOf(null)
    val chapterContent: State<ChapterContentEntity?> = _chapterContent

    private val _contentUIState = MutableStateFlow(ContentUIState())
    val contentUIState : StateFlow<ContentUIState> = _contentUIState.asStateFlow()

    private val _ttsUiState = MutableStateFlow(TTSState())
    val ttsUiState : StateFlow<TTSState> = _ttsUiState.asStateFlow()

    fun getBookInfo(){
        viewModelScope.launch {
            _book.value = repository.getBookById(bookId)
        }
        updateTotalChapter(_book.value.totalChapter)
    }

    fun getTableOfContents(bookId: Int) {
        viewModelScope.launch {
            _tableOfContents.value = repository.getTableOfContents(bookId)
        }
    }

    suspend fun getChapterContent(tocId: Int){
        _chapterContent.value = repository.getChapterContent(bookId,tocId)
    }

    fun saveBookInfo(bookId: Int,chapterId: Int){
        viewModelScope.launch {
            repository.saveBookInfo(bookId,chapterId)
        }
    }

    fun updateCurrentBookIndex(currentBookIndex: Int) {
        _contentUIState.update {currentState->
            currentState.copy(
                currentBookIndex = currentBookIndex
            )
        }
    }

    fun updateCurrentChapterIndex(currentChapterIndex: Int) {
        _contentUIState.update {currentState->
            currentState.copy(
                currentChapterIndex = currentChapterIndex
            )
        }
    }

    fun updateCurrentParagraphIndex(currentParagraphIndex: Int) {
        _contentUIState.update {currentState->
            currentState.copy(
                currentParagraphIndex = currentParagraphIndex
            )
        }
    }
    fun updateCurrentChapterHeader(currentChapterHeader: String?) {
        _contentUIState.update {currentState->
            currentState.copy(
                currentChapterHeader = currentChapterHeader
            )
        }
    }
    private fun updateTotalChapter(totalChapter: Int) {
        _contentUIState.update {currentState->
            currentState.copy(
                totalChapter = totalChapter
            )
        }
    }

    fun updateTopBarState(topBarState: Boolean) {
        _contentUIState.update { currentState ->
            currentState.copy(
                topBarState = topBarState
            )
        }
    }
    fun updateBottomBarState(bottomBarState: Boolean) {
        _contentUIState.update { currentState ->
            currentState.copy(
                bottomBarState = bottomBarState
            )
        }
    }
    fun updateDrawerState(drawerState: Boolean) {
        _contentUIState.update { currentState ->
            currentState.copy(
                drawerState = drawerState
            )
        }
    }
    fun updateEnablePagerScroll(enablePagerScroll: Boolean) {
        _contentUIState.update { currentState ->
            currentState.copy(
                enablePagerScroll = enablePagerScroll
            )
        }
    }

    fun updateEnableScaffoldBar(enableScaffoldBar: Boolean) {
        _contentUIState.update { currentState ->
            currentState.copy(
                enableScaffoldBar = enableScaffoldBar
            )
        }
    }
    fun updateScreenHeight(screenHeight: Int) {
        _contentUIState.update { currentState ->
            currentState.copy(
                screenHeight = screenHeight
            )
        }
    }
    fun updateScreenWidth(screenWidth: Int) {
        _contentUIState.update { currentState ->
            currentState.copy(
                screenWidth = screenWidth
            )
        }
    }
    fun updateIsSpeaking(isSpeaking: Boolean) {
        _ttsUiState.update { currentState ->
            currentState.copy(
                isSpeaking = isSpeaking
            )
        }
    }
    fun updateIsPaused(isPaused: Boolean) {
        _ttsUiState.update { currentState ->
            currentState.copy(
                isPaused = isPaused
            )
        }
    }
    fun updateIsFocused(isFocused: Boolean) {
        _ttsUiState.update { currentState ->
            currentState.copy(
                isFocused = isFocused
            )
        }
    }
    fun updateIsStop(isStop: Boolean) {
        _ttsUiState.update { currentState ->
            currentState.copy(
                isStop = isStop
            )
        }
    }
    fun updateScrollTime(scrollTime: Int) {
        _ttsUiState.update { currentState ->
            currentState.copy(
                scrollTime = scrollTime
            )
        }
    }
    fun updateCurrentReadingParagraph(currentReadingParagraph: Int) {
        _ttsUiState.update { currentState ->
            currentState.copy(
                currentReadingParagraph = currentReadingParagraph
            )
        }
    }
    fun updateFirstVisibleItemIndex(firstVisibleItemIndex: Int){
        _ttsUiState.update { currentState ->
            currentState.copy(
                firstVisibleItemIndex = firstVisibleItemIndex
            )
        }
    }
    fun updateFlagTriggerScrolling(flagTriggerScrolling : Boolean) {
        _ttsUiState.update { currentState ->
            currentState.copy(
                flagTriggerScrolling = flagTriggerScrolling
            )
        }
    }
    fun updateLastVisibleItemIndex(lastVisibleItemIndex: Int) {
        _ttsUiState.update { currentState ->
            currentState.copy(
                lastVisibleItemIndex = lastVisibleItemIndex
            )
        }
    }
    fun updateFlagStartScrolling(flagStartScrolling: Boolean) {
        _ttsUiState.update { currentState ->
            currentState.copy(
                flagStartScrolling = flagStartScrolling
            )
        }
    }
    fun updateFlagScrollAdjusted(flagScrollAdjusted: Boolean) {
        _ttsUiState.update { currentState ->
            currentState.copy(
                flagScrollAdjusted = flagScrollAdjusted
            )
        }
    }
    fun updateFlagTriggerAdjustScroll(flagTriggerAdjustScroll: Boolean) {
        _ttsUiState.update { currentState ->
            currentState.copy(
                flagTriggerAdjustScroll = flagTriggerAdjustScroll
            )
        }
    }
    fun updateFlagStartAdjustScroll(flagStartAdjustScroll: Boolean) {
        _ttsUiState.update { currentState ->
            currentState.copy(
                flagStartAdjustScroll = flagStartAdjustScroll
            )
        }
    }
}