package com.capstone.bookshelf.presentation.bookcontent.component.colorpicker

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.bookshelf.util.toHsv
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlin.math.abs

class ColorPaletteViewModel(
) : ViewModel() {

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

    private fun generateTextSelectionColor(backgroundColor: Color, textColor: Color): Color {
        val bgH: Float
        val bgS: Float
        val bgV: Float
        backgroundColor.toHsv().let {
            bgH = it.first
            bgS = it.second
            bgV = it.third
        }
        val txtH: Float
        val txtS: Float
        val txtV: Float
        textColor.toHsv().let {
            txtH = it.first
            txtS = it.second
            txtV = it.third
        }
        val selectionColor = Color.hsv(abs(bgH + txtH)/2, abs(bgS + txtS)/2, abs(bgV + txtV)/2)
        return selectionColor.copy(0.8f)
    }

    private fun generateTOCTextColor(backgroundColor: Color): Color {
        val tocTextColor: Color
        val bgV = backgroundColor.toHsv().third
        tocTextColor = if (bgV <= 0.5) {
            Color.White
        } else {
            Color.Black
        }
        return tocTextColor
    }

    private fun generateDarkenColor(backgroundColor: Color, ratio: Float): Color {
        val r = backgroundColor.red * (1 - ratio)
        val g = backgroundColor.green * (1 - ratio)
        val b = backgroundColor.blue * (1 - ratio)
        return Color(r, g, b)
    }

    private fun generateLightenColor(backgroundColor: Color, ratio: Float): Color {
        val r = backgroundColor.red
        val g = backgroundColor.green
        val b = backgroundColor.blue
        val r1 = ((100 - ratio) * r + ratio * 255) / 100
        val g1 = ((100 - ratio) * g + ratio * 255) / 100
        val b1 = ((100 - ratio) * b + ratio * 255) / 100
        return Color(r1, g1, b1)
    }
}