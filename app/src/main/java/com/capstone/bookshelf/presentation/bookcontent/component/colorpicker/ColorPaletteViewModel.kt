package com.capstone.bookshelf.presentation.bookcontent.component.colorpicker

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.bookshelf.util.darken
import com.capstone.bookshelf.util.isDark
import com.capstone.bookshelf.util.lighten
import com.capstone.bookshelf.util.toHsv
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlin.math.abs

class ColorPaletteViewModel : ViewModel() {

    private val _colorPalette = MutableStateFlow(ColorPalette())
    val colorPalette = _colorPalette
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _colorPalette.value
        )

    fun updateBackgroundColor(it: Color) {
        _colorPalette.value = _colorPalette.value.copy(
            backgroundColor = it,
            tocTextColor = generateTOCTextColor(it),
            textBackgroundColor = generateTextSelectionColor(it, _colorPalette.value.textColor),
            containerColor = generateContainerColor(it),
            specialArtColor = generateSpecialArtColor(it),
        )
    }

    fun updateTextColor(it: Color) {
        _colorPalette.value = _colorPalette.value.copy(
            textColor = it,
            tocTextColor = generateTOCTextColor(_colorPalette.value.backgroundColor),
            textBackgroundColor = generateTextSelectionColor(
                _colorPalette.value.backgroundColor,
                it
            ),
        )
    }

    fun updateSelectedColorSet(it: Int) {
        _colorPalette.value = _colorPalette.value.copy(
            selectedColorSet = it,
        )
    }

    private fun generateContainerColor(backgroundColor: Color): Color {
        return if (backgroundColor.isDark()) {
            backgroundColor.lighten(0.05f)
        } else {
            backgroundColor.darken(0.05f)
        }
    }

    private fun generateSpecialArtColor(backgroundColor: Color): Color {
        return if (backgroundColor.isDark()) {
            backgroundColor.lighten(0.1f)
        } else {
            backgroundColor.darken(0.1f)
        }
    }

    private fun generateTextSelectionColor(backgroundColor: Color, textColor: Color): Color {
        val (bgH, bgS, bgV) = backgroundColor.toHsv()
        val (txtH, txtS, txtV) = textColor.toHsv()
        val h = circularHueAverage(bgH, txtH)
        val s = (bgS + txtS) / 2
        val rawV = (bgV + txtV) / 2
        val v = when {
            bgV > 0.7f && txtV > 0.7f -> 0.3f
            bgV < 0.3f && txtV < 0.3f -> 0.7f
            else -> rawV
        }
        return Color.hsv(h, s, v).copy(alpha = 0.8f)
    }

    private fun circularHueAverage(h1: Float, h2: Float): Float {
        val diff = abs(h1 - h2)
        return if (diff > 180) {
            (h1 + h2 + 360) / 2 % 360
        } else {
            (h1 + h2) / 2
        }
    }

    private fun generateTOCTextColor(backgroundColor: Color): Color {
        val tocTextColor: Color = if (backgroundColor.isDark()) {
            Color.White
        } else {
            Color.Black
        }
        return tocTextColor
    }
}