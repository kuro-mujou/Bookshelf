package com.capstone.bookshelf.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.zIndex
import androidx.media3.common.util.UnstableApi
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState

@UnstableApi
@Composable
fun LoadingAnimation(
    contentState : ContentState,
    colorPaletteState : ColorPalette
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("loading_animation.json")
    )
    Surface(
        modifier = Modifier.fillMaxSize()
            .zIndex(1f)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                    }
                }
            },
        color = Color.Transparent
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
            )
            Text(
                text = "Loading...",
                style = TextStyle(
                    color = colorPaletteState.textColor,
                    fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                )
            )
        }
    }
}