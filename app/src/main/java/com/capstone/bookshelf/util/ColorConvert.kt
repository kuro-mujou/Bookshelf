package com.capstone.bookshelf.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb


fun Color.toHsl(): Triple<Float,Float,Float> {
    val r = this.red
    val g = this.green
    val b = this.blue
    val max = if ((r > g && r > b)) r else if ((g > b)) g else b
    val min = if ((r < g && r < b)) r else if ((g < b)) g else b

    var h: Float
    val s: Float
    val l = (max + min) / 2.0f

    if (max == min) {
        s = 0.0f
        h = s
    } else {
        val d = max - min
        s = if ((l > 0.5f)) d / (2.0f - max - min) else d / (max + min)

        h = if (r > g && r > b) (g - b) / d + (if (g < b) 6.0f else 0.0f)
        else if (g > b) (b - r) / d + 2.0f
        else (r - g) / d + 4.0f

        h /= 6.0f
    }
    return Triple(h,s,l)
}
fun Color.toHsv(): Triple<Float,Float,Float> {
    val bgArgb = this.toArgb()
    val bgRed = (bgArgb shr 16 and 0xFF)
    val bgGreen = (bgArgb shr 8 and 0xFF)
    val bgBlue = (bgArgb and 0xFF)
    val bgHSV = FloatArray(3)
    android.graphics.Color.RGBToHSV(bgRed, bgGreen, bgBlue, bgHSV)
    return Triple(bgHSV[0],bgHSV[1],bgHSV[2])
}