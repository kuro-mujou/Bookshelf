package com.capstone.bookshelf.presentation.bookcontent.component.colorpicker

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            containerColor = generateContainerColor(it)
        )
    }

    private fun generateContainerColor(backgroundColor: Color): Color {
        val bgV = backgroundColor.toHsv().third
        return if (bgV <= 0.5) {
            generateLightenColor(backgroundColor)
        }else{
            generateDarkenColor(backgroundColor)
        }
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

    private fun generateDarkenColor(backgroundColor: Color): Color {
        val r = backgroundColor.red * (1 - 0.1f)
        val g = backgroundColor.green * (1 - 0.1f)
        val b = backgroundColor.blue * (1 - 0.1f)
        return Color(r, g, b)
    }

    private fun generateLightenColor(backgroundColor: Color): Color {
        val r = backgroundColor.red * 255
        val g = backgroundColor.green * 255
        val b = backgroundColor.blue * 255
        val r1 = ((100 - 5) * r + 5 * 255) / 100
        val g1 = ((100 - 5) * g + 5 * 255) / 100
        val b1 = ((100 - 5) * b + 5 * 255) / 100
        return Color(r1/255, g1/255, b1/255)
    }
}