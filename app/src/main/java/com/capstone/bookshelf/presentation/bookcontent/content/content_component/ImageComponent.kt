package com.capstone.bookshelf.presentation.bookcontent.content.content_component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Dialog
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage

@Composable
@UnstableApi
fun ImageComponent(
    content: ImageContent
) {
    val popup = remember { mutableStateOf(false) }
    if(popup.value){
        Dialog(
            onDismissRequest = { popup.value = false }
        ) {
            var size by remember { mutableStateOf(IntSize.Zero) }
            Surface(
                modifier = Modifier
                    .wrapContentSize(),
                color = Color.White
            ) {
                AsyncImage(
                    model = content.content,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            val zoom = content.zoom.floatValue
                            val offset = content.offset.value
                            translationX = offset.x
                            translationY = offset.y
                            scaleX = zoom
                            scaleY = zoom
                        }
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown()
                                do {
                                    val event = awaitPointerEvent()
                                    var zoom = content.zoom.floatValue
                                    zoom *= event.calculateZoom()
                                    zoom = zoom.coerceIn(1f, 3f)
                                    content.zoom.floatValue = zoom
                                    val pan = event.calculatePan()
                                    val currentOffset = if (zoom == 1f) {
                                        Offset.Zero
                                    } else {
                                        val temp = content.offset.value + pan.times(zoom)
                                        val maxX = (size.width * (zoom - 1) / 2f)
                                        val maxY = (size.height * (zoom - 1) / 2f)
                                        Offset(
                                            temp.x.coerceIn(-maxX, maxX),
                                            temp.y.coerceIn(-maxY, maxY)
                                        )
                                    }
                                    content.offset.value = currentOffset
                                    if (zoom > 1f) {
                                        event.changes.forEach { pointerInputChange: PointerInputChange ->
                                            pointerInputChange.consume()
                                        }

                                    }
                                } while (event.changes.any { it.pressed })
                            }

                        }
                        .onSizeChanged {
                            size = it
                        }
                )
            }
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) {
                popup.value = true
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RectangleShape
    ) {
        AsyncImage(
            model = content.content,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}