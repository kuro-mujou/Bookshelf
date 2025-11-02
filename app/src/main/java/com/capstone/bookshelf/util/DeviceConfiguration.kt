package com.capstone.bookshelf.util

import androidx.window.core.layout.WindowSizeClass

enum class DeviceConfiguration {
    PHONE_PORTRAIT,
    PHONE_LANDSCAPE,
    TABLET_PORTRAIT,
    TABLET_LANDSCAPE;

    companion object {
        fun fromWindowSizeClass(windowSizeClass: WindowSizeClass): DeviceConfiguration {
            val width = windowSizeClass.minWidthDp
            val height = windowSizeClass.minHeightDp

            return when {
                width >= WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND -> {
                    when {
                        height < WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND ->
                            PHONE_LANDSCAPE
                        else ->
                            TABLET_LANDSCAPE
                    }
                }
                width >= WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND -> {
                    when {
                        height < WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND ->
                            PHONE_LANDSCAPE
                        else ->
                            TABLET_PORTRAIT
                    }
                }
                else ->
                    PHONE_PORTRAIT
            }
        }
    }
}