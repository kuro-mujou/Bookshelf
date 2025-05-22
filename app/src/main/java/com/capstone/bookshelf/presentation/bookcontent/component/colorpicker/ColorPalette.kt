package com.capstone.bookshelf.presentation.bookcontent.component.colorpicker

import androidx.compose.ui.graphics.Color
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.model.ColorSample

data class ColorPalette(
    val backgroundColor: Color = Color(0xFF000000),
    val textBackgroundColor: Color = Color(0xFF3A342B),
    val tocTextColor: Color = Color(0xFF001F2B),
    val textColor: Color = Color(0xFF181C20),
    val containerColor: Color = Color(0xFFC8C8C8),
    val specialArtColor: Color = Color(0xFFC8C8C8),
    val selectedColorSet: Int = 5,

    val colorSamples: List<ColorSample> = listOf(
        ColorSample(
            colorBg = Color(0xFFD3C3A3),
            colorTxt = Color(0xFF3A3129)
        ),
        ColorSample(
            colorBg = Color(0xFFA2C0E5),
            colorTxt = Color(0xFF1B310E)
        ),
        ColorSample(
            colorBg = Color(0xFFF3C9D7),
            colorTxt = Color(0xFF1B310E)
        ),
        ColorSample(
            colorBg = Color(0xFFF1F7ED),
            colorTxt = Color(0xFF1B310E)
        ),
        ColorSample(
            colorBg = Color(0xFFF7F4DC),
            colorTxt = Color(0xFF46422E)
        ),
        ColorSample(
            colorBg = Color(0xFFFFFFFF),
            colorTxt = Color(0xFF3A342B)
        ),
        ColorSample(
            colorBg = Color(0xFFC2E0CD),
            colorTxt = Color(0xFF334B39)
        ),
        ColorSample(
            colorBg = Color(0xFF393030),
            colorTxt = Color(0xFF95938F)
        ),
        ColorSample(
            colorBg = Color(0xFF051C2C),
            colorTxt = Color(0xFF637079)
        ),
        ColorSample(
            colorBg = Color(0xFF333333),
            colorTxt = Color(0xFFCCE8CF)
        ),
        ColorSample(
            colorBg = Color(0xFF152B06),
            colorTxt = Color(0xFF607057)
        ),
        ColorSample(
            colorBg = Color(0xFF151C1F),
            colorTxt = Color(0xFF4D5052)
        ),
        ColorSample(
            colorBg = Color(0xFF000000),
            colorTxt = Color(0xFF5F5F5F)
        ),
        ColorSample(
            colorBg = Color(0xFF000000),
            colorTxt = Color(0xFF494949)
        ),
        ColorSample(
            colorBg = Color(0xFF001622),
            colorTxt = Color(0xFF204353)
        ),
        ColorSample(
            colorBg = Color(0xFF171F27),
            colorTxt = Color(0xFF445053)
        ),
        ColorSample(
            colorBg = Color(0xFF251C05),
            colorTxt = Color(0xFF574F3C)
        ),
    )
)