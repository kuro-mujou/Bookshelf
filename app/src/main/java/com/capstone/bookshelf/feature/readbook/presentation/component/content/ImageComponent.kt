package com.capstone.bookshelf.feature.readbook.presentation.component.content

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import coil.compose.AsyncImage

class ZoomableImage(
    val path: String
) {
    var zoom = mutableFloatStateOf(1f)
    var offset = mutableStateOf(Offset.Zero)
}

@Composable
fun ImageComponent(
    zoomableSnack: ZoomableImage
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    DisableSelection {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RectangleShape
        ) {
            AsyncImage(
                model = zoomableSnack.path,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        val zoom = zoomableSnack.zoom.value
                        val offset = zoomableSnack.offset.value
                        translationX = offset.x
                        translationY = offset.y
                        scaleX = zoom
                        scaleY = zoom
                    }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            // Wait for at least one pointer to press down
                            awaitFirstDown()
                            do {
                                val event = awaitPointerEvent()
                                // Calculate gestures and consume pointerInputChange
                                var zoom = zoomableSnack.zoom.value
                                zoom *= event.calculateZoom()
                                // Limit zoom between 100% and 300%
                                zoom = zoom.coerceIn(1f, 3f)

                                zoomableSnack.zoom.value = zoom

                                val pan = event.calculatePan()

                                val currentOffset = if (zoom == 1f) {
                                    Offset.Zero
                                } else {

                                    // This is for limiting pan inside Image bounds
                                    val temp = zoomableSnack.offset.value + pan.times(zoom)
                                    val maxX = (size.width * (zoom - 1) / 2f)
                                    val maxY = (size.height * (zoom - 1) / 2f)

                                    Offset(
                                        temp.x.coerceIn(-maxX, maxX),
                                        temp.y.coerceIn(-maxY, maxY)
                                    )
                                }
                                zoomableSnack.offset.value = currentOffset

                                // When image is zoomed consume event and prevent scrolling
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