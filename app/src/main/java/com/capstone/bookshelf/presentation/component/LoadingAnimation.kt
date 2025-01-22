package com.capstone.bookshelf.presentation.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun LoadingAnimation(
    progress: Float,
    message: String,
    dismissDialog: (Boolean) -> Unit
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("loading_animation.json")
    )
    Dialog(
        onDismissRequest = { dismissDialog(false) },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        BackHandler(true) {}
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
}
@Composable
fun LoadingAnimation(
    dismissDialog: (Boolean) -> Unit
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("loading_animation.json")
    )
    Dialog(
        onDismissRequest = { dismissDialog(false) },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        BackHandler(true) {}
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
            }
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
        }
    }
}