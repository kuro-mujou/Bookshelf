package com.capstone.bookshelf.presentation.bookcontent.component.font

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.capstone.bookshelf.R

data class FontState(
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
)
