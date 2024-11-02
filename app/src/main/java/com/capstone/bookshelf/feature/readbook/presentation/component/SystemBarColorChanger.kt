package com.capstone.bookshelf.feature.readbook.presentation.component

import android.app.Activity
import android.view.View
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun SystemBarColorChanger(
    statusColor: Color?,
    navigationColor: Color?,
    isLightIcons: Boolean
){
    val window = (LocalContext.current as Activity).window
    val view = LocalView.current

    SideEffect {
        changeSystemBarColor(
            window = window,
            view = view,
            statusColor = statusColor,
            navigationColor = navigationColor,
            isLightIcons = isLightIcons
        )
    }
}

fun changeSystemBarColor(
    window: Window,
    view: View,
    statusColor: Color?,
    navigationColor: Color?,
    isLightIcons: Boolean
){
    statusColor?.let {
        window.statusBarColor = it.toArgb()
    }
    navigationColor?.let {
        window.navigationBarColor = it.toArgb()
    }
    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isLightIcons
}