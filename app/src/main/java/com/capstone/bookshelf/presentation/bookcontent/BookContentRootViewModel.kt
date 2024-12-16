package com.capstone.bookshelf.presentation.bookcontent
//
//import android.speech.tts.TextToSpeech
//import android.speech.tts.Voice
//import androidx.compose.runtime.MutableState
//import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.capstone.bookshelf.app.Route
import com.capstone.bookshelf.domain.book.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

//import androidx.lifecycle.viewModelScope
//import com.capstone.bookshelf.data.book.database.entity.BookEntity
//import com.capstone.bookshelf.data.book.database.entity.BookSettingEntity
//import com.capstone.bookshelf.data.book.database.entity.ChapterContentEntity
//import com.capstone.bookshelf.data.book.database.entity.TableOfContentEntity
//import com.capstone.bookshelf.data.book.repository.BookRepositoryImpl
//import com.capstone.bookshelf.presentation.bookcontent.state.ContentUIState
//import com.capstone.bookshelf.presentation.bookcontent.state.TTSState
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//import java.util.Locale
//
class BookContentRootViewModel(
    private val repository: BookRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val bookId = savedStateHandle.toRoute<Route.BookDetail>().bookId
    private val _state = MutableStateFlow(BookContentRootState())
    val state = _state
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    fun onAction(action: BookContentRootAction) {
        when(action) {
            is BookContentRootAction.SelectedBookRoot -> {
                _state.update { it.copy(
                    book = action.book
                ) }
            }
        }
    }
}
//

//
//    private val _book: MutableState<BookEntity> = mutableStateOf(
//        BookEntity(
//        title = "",
//        coverImagePath = "",
//        totalChapter = 0
//    )
//    )
//    val book: State<BookEntity> = _book
//

//
//    private val _contentUIState = MutableStateFlow(ContentUIState())
//    val contentUIState : StateFlow<ContentUIState> = _contentUIState.asStateFlow()
//
//    private val _ttsUiState = MutableStateFlow(TTSState())
//    val ttsUiState : StateFlow<TTSState> = _ttsUiState.asStateFlow()
//
//

//
//    fun fixNullVoice(textToSpeech: TextToSpeech){
//        viewModelScope.launch {
//            var selectedVoice = textToSpeech.voices.find {
//                it.locale == _ttsUiState.value.currentLanguage
//            }
//            if (selectedVoice == null) {
//                selectedVoice = textToSpeech.voices.firstOrNull {
//                    it.locale == _ttsUiState.value.currentLanguage
//                } ?: textToSpeech.defaultVoice
//            }
//            updateTTSVoice(selectedVoice)
//        }
//    }
//    fun updateIsAutoScrollPaused(isAutoScrollPaused : Boolean){
//        _ttsUiState.update {currentState->
//            currentState.copy(
//                isAutoScrollPaused = isAutoScrollPaused
//            )
//        }
//    }
//    fun updateIsAutoScroll(isAutoScroll : Boolean){
//        _ttsUiState.update {currentState->
//            currentState.copy(
//                isAutoScroll = isAutoScroll
//            )
//        }
//    }
//    fun updateBookSettingKeepScreenOn(screenShallBeKeptOn : Boolean){
//        viewModelScope.launch {
//            repository.updateBookSettingScreenShallBeKeptOn(0,screenShallBeKeptOn)
//        }
//    }
//    fun updateKeepScreenOn(screenShallBeKeptOn : Boolean){
//        _contentUIState.update {currentState->
//            currentState.copy(
//                screenShallBeKeptOn = screenShallBeKeptOn
//            )
//        }
//    }
//    fun changeMenuTriggerSetting(openSetting : Boolean){
//        _contentUIState.update {currentState->
//            currentState.copy(
//                openSetting = openSetting
//            )
//        }
//    }
//    fun changeMenuTriggerVoice(openTTSVoiceMenu : Boolean){
//        _contentUIState.update {currentState->
//            currentState.copy(
//                openTTSVoiceMenu = openTTSVoiceMenu
//            )
//        }
//    }
//    fun changeMenuTriggerAutoScroll(openAutoScrollMenu : Boolean){
//        _contentUIState.update {currentState->
//            currentState.copy(
//                openAutoScrollMenu = openAutoScrollMenu
//            )
//        }
//    }
//    fun updateBookSettingAutoScrollSpeed(autoScrollSpeed: Float){
//        viewModelScope.launch {
//            repository.updateBookSettingAutoScrollSpeed(0,autoScrollSpeed)
//        }
//    }
//    fun updateBookSettingVoice(voice: String){
//        viewModelScope.launch {
//            repository.updateBookSettingVoice(0,voice)
//        }
//    }
//    fun updateBookSettingLocale(locale: String){
//        viewModelScope.launch {
//            repository.updateBookSettingLocale(0,locale)
//        }
//    }
//    fun updateBookSettingSpeed(speed: Float){
//        viewModelScope.launch {
//            repository.updateBookSettingSpeed(0,speed)
//        }
//    }
//    fun updateBookSettingPitch(pitch: Float){
//        viewModelScope.launch {
//            repository.updateBookSettingPitch(0,pitch)
//        }
//    }
//    fun getBookInfo(){
//        viewModelScope.launch {
//            _book.value = repository.getBookById(bookId)
//        }
//        updateTotalChapter(_book.value.totalChapter)
//    }
//
//    fun getTableOfContents(bookId: Int) {
//        viewModelScope.launch {
//            _tableOfContents.value = repository.getTableOfContents(bookId)
//        }
//    }
//
//    suspend fun getChapterContent(tocId: Int){
//        _chapterContent.value = repository.getChapterContent(bookId,tocId)
//    }
//
//    fun saveBookInfo(bookId: Int,chapterId: Int){
//        viewModelScope.launch {
//            repository.saveBookInfo(bookId,chapterId)
//        }
//    }
//
//    fun updateCurrentBookIndex(currentBookIndex: Int) {
//        _contentUIState.update {currentState->
//            currentState.copy(
//                currentBookIndex = currentBookIndex
//            )
//        }
//    }
//
//    fun updateCurrentChapterIndex(currentChapterIndex: Int) {
//        _contentUIState.update {currentState->
//            currentState.copy(
//                currentChapterIndex = currentChapterIndex
//            )
//        }
//    }
//
//    fun updateCurrentParagraphIndex(currentParagraphIndex: Int) {
//        _contentUIState.update {currentState->
//            currentState.copy(
//                currentParagraphIndex = currentParagraphIndex
//            )
//        }
//    }
//    fun updateCurrentChapterHeader(currentChapterHeader: String?) {
//        _contentUIState.update {currentState->
//            currentState.copy(
//                currentChapterHeader = currentChapterHeader
//            )
//        }
//    }
//    private fun updateTotalChapter(totalChapter: Int) {
//        _contentUIState.update {currentState->
//            currentState.copy(
//                totalChapter = totalChapter
//            )
//        }
//    }
//
//    fun updateTopBarState(topBarState: Boolean) {
//        _contentUIState.update { currentState ->
//            currentState.copy(
//                topBarState = topBarState
//            )
//        }
//    }
//    fun updateBottomBarState(bottomBarState: Boolean) {
//        _contentUIState.update { currentState ->
//            currentState.copy(
//                bottomBarState = bottomBarState
//            )
//        }
//    }
//    fun updateBottomBarIndex(bottomBarIndex: Int) {
//        _contentUIState.update { currentState ->
//            currentState.copy(
//                bottomBarIndex = bottomBarIndex
//            )
//        }
//    }
//    fun updateDrawerState(drawerState: Boolean) {
//        _contentUIState.update { currentState ->
//            currentState.copy(
//                drawerState = drawerState
//            )
//        }
//    }
//    fun updateEnablePagerScroll(enablePagerScroll: Boolean) {
//        _contentUIState.update { currentState ->
//            currentState.copy(
//                enablePagerScroll = enablePagerScroll
//            )
//        }
//    }
//
//    fun updateEnableScaffoldBar(enableScaffoldBar: Boolean) {
//        _contentUIState.update { currentState ->
//            currentState.copy(
//                enableScaffoldBar = enableScaffoldBar
//            )
//        }
//    }
//    fun updateScreenHeight(screenHeight: Int) {
//        _contentUIState.update { currentState ->
//            currentState.copy(
//                screenHeight = screenHeight
//            )
//        }
//    }
//    fun updateScreenWidth(screenWidth: Int) {
//        _contentUIState.update { currentState ->
//            currentState.copy(
//                screenWidth = screenWidth
//            )
//        }
//    }
//    fun updateReadingContent(readingContent: List<String>) {
//        _ttsUiState.update { currentState ->
//            currentState.copy(
//                readingContent = readingContent
//            )
//        }
//    }
//    fun updateIsSpeaking(isSpeaking: Boolean) {
//        _ttsUiState.update { currentState ->
//            currentState.copy(
//                isSpeaking = isSpeaking
//            )
//        }
//    }
//    fun updateIsPaused(isPaused: Boolean) {
//        _ttsUiState.update { currentState ->
//            currentState.copy(
//                isPaused = isPaused
//            )
//        }
//    }
//    fun updateIsFocused(isFocused: Boolean) {
//        _ttsUiState.update { currentState ->
//            currentState.copy(
//                isFocused = isFocused
//            )
//        }
//    }
//    fun updateScrollTime(scrollTime: Int) {
//        _ttsUiState.update { currentState ->
//            currentState.copy(
//                scrollTime = scrollTime
//            )
//        }
//    }
//    fun updateCurrentReadingParagraph(currentReadingParagraph: Int) {
//        _ttsUiState.update { currentState ->
//            currentState.copy(
//                currentReadingParagraph = currentReadingParagraph
//            )
//        }
//    }
//    fun updateFirstVisibleItemIndex(firstVisibleItemIndex: Int){
//        _ttsUiState.update { currentState ->
//            currentState.copy(
//                firstVisibleItemIndex = firstVisibleItemIndex
//            )
//        }
//    }
//    fun updateFlagTriggerScrolling(flagTriggerScrolling : Boolean) {
//        _ttsUiState.update { currentState ->
//            currentState.copy(
//                flagTriggerScrolling = flagTriggerScrolling
//            )
//        }
//    }
//    fun updateLastVisibleItemIndex(lastVisibleItemIndex: Int) {
//        _ttsUiState.update { currentState ->
//            currentState.copy(
//                lastVisibleItemIndex = lastVisibleItemIndex
//            )
//        }
//    }
//    fun updateFlagStartScrolling(flagStartScrolling: Boolean) {
//        _ttsUiState.update { currentState ->
//            currentState.copy(
//                flagStartScrolling = flagStartScrolling
//            )
//        }
//    }
//    fun updateFlagScrollAdjusted(flagScrollAdjusted: Boolean) {
//        _ttsUiState.update { currentState ->
//            currentState.copy(
//                flagScrollAdjusted = flagScrollAdjusted
//            )
//        }
//    }
//    fun updateFlagTriggerAdjustScroll(flagTriggerAdjustScroll: Boolean) {
//        _ttsUiState.update { currentState ->
//            currentState.copy(
//                flagTriggerAdjustScroll = flagTriggerAdjustScroll
//            )
//        }
//    }
//    fun updateFlagStartAdjustScroll(flagStartAdjustScroll: Boolean) {
//        _ttsUiState.update { currentState ->
//            currentState.copy(
//                flagStartAdjustScroll = flagStartAdjustScroll
//            )
//        }
//    }
//    fun updateCurrentSpeed(currentSpeed : Float){
//        _ttsUiState.update { currentState ->
//            currentState.copy(
//                currentSpeed = currentSpeed
//            )
//        }
//    }
//    fun updateCurrentAutoScrollSpeed(autoScrollSpeed : Float) {
//        _ttsUiState.update { currentState ->
//            currentState.copy(
//                autoScrollSpeed = autoScrollSpeed
//            )
//        }
//    }
//    fun updateCurrentPitch(currentPitch : Float){
//        _ttsUiState.update { currentState ->
//            currentState.copy(
//                currentPitch = currentPitch
//            )
//        }
//    }
//    fun updateTTSLocale(currentLanguage : Locale?){
//        _ttsUiState.update { currentState ->
//            currentState.copy(
//                currentLanguage = currentLanguage
//            )
//        }
//    }
//    fun updateTTSVoice(currentVoice : Voice?){
//        _ttsUiState.update { currentState ->
//            currentState.copy(
//                currentVoice = currentVoice
//            )
//        }
//    }
//
//    fun updateCurrentChapterContent(chapterContent: List<String>?) {
//        _contentUIState.update { currentState ->
//            currentState.copy(
//                currentChapterContent = chapterContent
//            )
//        }
//    }
//
//    fun updateCurrentReadingPosition(position: Int) {
//        _contentUIState.update { currentState ->
//            currentState.copy(
//                currentReadingPosition = position
//            )
//        }
//    }
//
//    fun updateIsSelectedParagraph(isSelected: Boolean) {
//        _contentUIState.update { currentState ->
//            currentState.copy(
//                isSelectedParagraph = isSelected
//            )
//        }
//    }
//
//    fun updateCommentButtonClicked(buttonClicked: Boolean) {
//        _contentUIState.update { currentState ->
//            currentState.copy(
//                commentButtonClicked = buttonClicked
//            )
//        }
//    }
//}