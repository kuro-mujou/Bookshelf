package com.capstone.bookshelf.core.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.airbnb.lottie.compose.*

@Composable
fun LoadingAnimation(
    progress: Float,
    message: String
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("loading_animation.json")
    )
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                    }
                }
            },
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
            )
            Text(text = "Loading...")
            Spacer(modifier = Modifier.height(15.dp))
            Text(text = message)

            LinearProgressIndicator(
                progress = { progress },
            )
        }
    }
}

@Composable
fun LoadingAnimation(
    message: String
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("loading_animation.json")
    )
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
            )
            Text(text = "Loading...")
            Spacer(modifier = Modifier.height(15.dp))
            Text(text = message)
        }
    }
}

@Composable
fun LoadingAnimation() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("loading_animation.json")
    )
    Column(
        modifier = Modifier.fillMaxSize()
            .zIndex(1f)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                    }
                }
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
        )
        Text(text = "Loading...")
    }
}