package com.capstone.bookshelf.presentation.bookcontent.component.tts

sealed interface TtsPlayerEvent {
    data class PlayPause(val isPaused: Boolean) : TtsPlayerEvent
    data object Backward : TtsPlayerEvent
    data object Forward : TtsPlayerEvent
    data object Stop : TtsPlayerEvent
    data object SkipToBack : TtsPlayerEvent
    data object SkipToNext : TtsPlayerEvent
    data object JumpToRandomChapter : TtsPlayerEvent
}

sealed interface TtsUiEvent {
    data class PlayPause(val isPaused: Boolean) : TtsUiEvent
    data object Backward : TtsUiEvent
    data object Forward : TtsUiEvent
    data object Stop : TtsUiEvent
    data object SkipToBack : TtsUiEvent
    data object SkipToNext : TtsUiEvent
    data object JumpToRandomChapter : TtsUiEvent
}
