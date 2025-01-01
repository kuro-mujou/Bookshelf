package com.capstone.bookshelf.presentation.bookcontent.component.colorpicker

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.bookshelf.data.book.database.entity.BookSettingEntity
import com.capstone.bookshelf.domain.book.BookSettingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs

class ColorPaletteViewModel(
    private val bookSettingRepository: BookSettingRepository
) : ViewModel() {

    private val _colorPalette = MutableStateFlow(ColorPalette())
    val colorPalette = _colorPalette
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _colorPalette.value
        )
    init {
        viewModelScope.launch {
            val setting = bookSettingRepository.getBookSetting(0)
            if (setting != null) {
                if(setting.textColor != null && setting.backgroundColor != null){
                    _colorPalette.value = _colorPalette.value.copy(
                        backgroundColor = Color(setting.backgroundColor),
                        textColor = Color(setting.textColor)
                    )
                } else {

                }
            } else {
                val newSetting = BookSettingEntity(settingId = 0)
                bookSettingRepository.saveBookSetting(newSetting)
            }
        }
    }
    fun updateBackgroundColor(it: Color) {
        _colorPalette.value = _colorPalette.value.copy(
            backgroundColor = it,
            tocTextColor = generateTOCTextColor(it, _colorPalette.value.textColor),
            textBackgroundColor = generateTextSelectionColor(it, _colorPalette.value.textColor),
        )
        Log.d("test color", _colorPalette.value.tocTextColor.toHsl().toString())
        Log.d("test color", _colorPalette.value.textBackgroundColor.toHsl().toString())
    }
    fun updateTextColor(it: Color) {
        _colorPalette.value = _colorPalette.value.copy(
            textColor = it,
            tocTextColor = generateTOCTextColor(_colorPalette.value.backgroundColor, it),
            textBackgroundColor = generateTextSelectionColor(_colorPalette.value.backgroundColor, it),
        )
    }
    // Function to calculate contrast ratio
    private fun calculateContrast(color1: Color, color2: Color): Double {
        val lum1 = color1.luminance() + 0.05
        val lum2 = color2.luminance() + 0.05
        return maxOf(lum1, lum2) / minOf(lum1, lum2)
    }

    // Function to blend two colors
    private fun blendColors(color1: Color, color2: Color, ratio: Float): Color {
        val inverseRatio = 1 - ratio
        val r = (color1.red * ratio + color2.red * inverseRatio)
        val g = (color1.green * ratio + color2.green * inverseRatio)
        val b = (color1.blue * ratio + color2.blue * inverseRatio)
        val a = (color1.alpha * ratio + color2.alpha * inverseRatio)
        return Color(r, g, b, a)
    }

    private fun generateTextSelectionColor(backgroundColor: Color, textColor: Color): Color {
        var selectionColor = blendColors(backgroundColor, textColor, 0.7f) // Adjusted blend ratio

        // Ensure sufficient contrast with text color (contrast ratio > 4.5 for readability)
        if (calculateContrast(selectionColor, textColor) < 4.5) {
            selectionColor = blendColors(selectionColor, Color.White, 0.5f)
        }

        // Ensure sufficient contrast with background color (contrast ratio > 2.5 for visibility)
        if (calculateContrast(selectionColor, backgroundColor) < 2.5) {
            selectionColor = blendColors(selectionColor, Color.Black, 0.5f)
        }

        // Final adjustment to ensure the selection color is vivid and not too dull
        return selectionColor.copy(alpha = 0.8f)
    }

    private fun generateContrastingColor(baseColor: Color, avoidColor: Color): Color {
        val baseHsv = FloatArray(3).apply { android.graphics.Color.colorToHSV(baseColor.toArgb(), this) }
        val avoidHsv = FloatArray(3).apply { android.graphics.Color.colorToHSV(avoidColor.toArgb(), this) }

        val newHue = (baseHsv[0] + 180) % 360 // Complementary hue
        val hueDifference = abs(newHue - avoidHsv[0])
        val finalHue = if (hueDifference < 60) (newHue + 120) % 360 else newHue
        return Color.hsv(finalHue, 0.7f, 0.9f)
    }

    private fun generateTOCTextColor(backgroundColor: Color, textColor: Color): Color {
        var currentChapterColor = generateContrastingColor(backgroundColor, textColor)
        if (calculateContrast(currentChapterColor, backgroundColor) < 3.0) {
            currentChapterColor = blendColors(currentChapterColor, Color.White, 0.3f)
        }
        if (calculateContrast(currentChapterColor, textColor) < 3.0) {
            currentChapterColor = blendColors(currentChapterColor, Color.Black, 0.3f)
        }
        return currentChapterColor
    }
    private fun generateDarkenColor(backgroundColor: Color, ratio: Float): Color{
        val r = backgroundColor.red * (1 - ratio)
        val g = backgroundColor.green * (1 - ratio)
        val b = backgroundColor.blue * (1 - ratio)
        return Color(r,g,b)
    }
    private fun generateLightenColor(backgroundColor: Color, ratio: Float): Color{
        val r = backgroundColor.red
        val g = backgroundColor.green
        val b = backgroundColor.blue
        val r1 = ((100 - ratio) * r + ratio * 255) / 100
        val g1 = ((100 - ratio) * g + ratio * 255) / 100
        val b1 = ((100 - ratio) * b + ratio * 255) / 100
        return Color(r1,g1,b1)
    }
}