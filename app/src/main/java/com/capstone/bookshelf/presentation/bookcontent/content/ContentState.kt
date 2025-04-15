package com.capstone.bookshelf.presentation.bookcontent.content

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.R
import com.capstone.bookshelf.domain.wrapper.Book
import com.capstone.bookshelf.presentation.bookcontent.drawer.component.bookmark.BookmarkStyle
import java.util.Locale

@UnstableApi
data class ContentState(
    val book: Book? = null,
    val tts : TextToSpeech? = null,
    val enableScaffoldBar : Boolean = true,
    val enablePagerScroll : Boolean = true,
    val enableUndoButton : Boolean = false,
    //content
    val currentChapterIndex: Int = 0,
    val previousChapterIndex: Int = 0,
    val flagTriggerScrolling : Boolean = false,
    val flagStartScrolling : Boolean = false,
    val flagScrollAdjusted : Boolean = false,
    val flagTriggerAdjustScroll : Boolean = false,
    val flagStartAdjustScroll : Boolean = false,
    val firstVisibleItemIndex : Int = 0,
    val lastVisibleItemIndex : Int = 0,
    val chapterHeader : String = "",
    val imagePaddingState : Boolean = false,

    val screenHeight : Int = 0,
    val screenWidth : Int = 0,
    //tts
    val currentSpeed : Float? = null,
    val currentPitch : Float? = null,
    val currentLanguage : Locale? = null,
    val currentVoice : Voice? = null,
    val scrollTime : Int = 0,
    val isSpeaking : Boolean = false,
    val isPaused : Boolean = false,
    val isFocused : Boolean = false,
    val currentReadingParagraph : Int = 0,
    //background music
    val enableBackgroundMusic : Boolean = false,
    //font
    val fontSize : Int = 20,
    val lineSpacing : Int = 15,
    val textAlign: Boolean = true,
    val textIndent : Boolean = true,
    val fontFamilies : List<FontFamily> = listOf(
        FontFamily(Font(R.font.cormorant)),//serif
        FontFamily(Font(R.font.ibm_plex_serif)),//serif
        FontFamily(Font(R.font.literata)),//serif
        FontFamily(Font(R.font.noto_serif)),//serif
        FontFamily(Font(R.font.playfair_display)),//serif
        FontFamily(Font(R.font.source_serif_4)),//serif
        FontFamily(Font(R.font.source_serif_pro)),//serif
        FontFamily(Font(R.font.noto_sans)),//san
        FontFamily(Font(R.font.open_sans)),//san
        FontFamily(Font(R.font.roboto)),//san
        FontFamily(Font(R.font.source_sans_pro)),//san
    ),
    val fontNames : List<String> = listOf(
        "Cormorant",
        "IBM Plex Serif",
        "Literata",
        "Noto Serif",
        "Playfair Display",
        "Source Serif 4",
        "Source Serif Pro",
        "Noto Sans",
        "Open Sans",
        "Roboto",
        "Source Sans Pro",
    ),
    val selectedFontFamilyIndex : Int = 0,
    val keepScreenOn : Boolean = false,
    val selectedBookmarkStyle : BookmarkStyle = BookmarkStyle.WAVE_WITH_BIRDS,
)