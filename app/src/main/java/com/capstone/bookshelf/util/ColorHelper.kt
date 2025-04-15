package com.capstone.bookshelf.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

fun Color.toHsl(): FloatArray {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(this.toArgb(), hsl)
    return hsl
}

fun Color.toHsv(): FloatArray {
    val argb = this.toArgb()
    val r = (argb shr 16) and 0xFF
    val g = (argb shr 8) and 0xFF
    val b = argb and 0xFF
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(r, g, b, hsv)
    return hsv
}

fun Color.isDark(): Boolean {
    val colorV = this.toHsv()
    return if (colorV[2] <= 0.5) {
        true
    } else {
        false
    }
}

fun Color.lighten(percentage: Float): Color {
    val clamped = percentage.coerceIn(0f, 1f)
    val hsl = this.toHsl()

    val originalLightness = hsl[2]
    val newLightness = originalLightness + (1f - originalLightness) * clamped

    hsl[2] = newLightness.coerceIn(0f, 1f)
    return Color(ColorUtils.HSLToColor(hsl))
}

fun Color.darken(percentage: Float): Color {
    val clamped = percentage.coerceIn(0f, 1f)
    var hsl = this.toHsl()

    val originalLightness = hsl[2]
    val newLightness = originalLightness * (1f - clamped)

    hsl[2] = newLightness.coerceIn(0f, 1f)
    return Color(ColorUtils.HSLToColor(hsl))
}