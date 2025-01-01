package com.capstone.bookshelf.presentation.bookcontent.component.colorpicker

import androidx.compose.foundation.gestures.*
import androidx.compose.ui.input.pointer.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Reads [awaitFirstDown], and [awaitPointerEvent] to
 * get [PointerInputChange] and motion event states
 * [onDown], [onMove], and [onUp].
 *
 * To prevent other pointer functions that call [awaitFirstDown] or [awaitPointerEvent]
 * (scroll, swipe, detect functions)
 * receiving changes call [PointerInputChange.consume] in [onDown],
 * and call [PointerInputChange.consumePositionChange]
 * in [onMove] block.
 *
 * @param onDown is invoked when first pointer is down initially.
 * @param onMove one or multiple pointers are being moved on screen.
 * @param onUp last pointer is up
 * @param delayAfterDownInMillis is optional delay after [onDown] This delay might be
 * required Composables like **Canvas** to process [onDown] before [onMove]
 *

 */
suspend fun PointerInputScope.detectMotionEvents(
    onDown: (PointerInputChange) -> Unit = {},
    onMove: (PointerInputChange) -> Unit = {},
    onUp: (PointerInputChange) -> Unit = {},
    delayAfterDownInMillis: Long = 0L
) {
    coroutineScope {
        awaitEachGesture {
            val down: PointerInputChange = awaitFirstDown()
            onDown(down)

            var pointer = down
            var pointerId = down.id

            var waitedAfterDown = false

            launch {
                delay(delayAfterDownInMillis)
                waitedAfterDown = true
            }

            while (true) {

                val event: PointerEvent = awaitPointerEvent()

                val anyPressed = event.changes.any { it.pressed }

                if (anyPressed) {
                    val pointerInputChange =
                        event.changes.firstOrNull { it.id == pointerId }
                            ?: event.changes.first()

                    pointerId = pointerInputChange.id
                    pointer = pointerInputChange

                    if (waitedAfterDown) {
                        onMove(pointer)
                    }
                } else {
                    onUp(pointer)
                    break
                }
            }
        }
    }
}

/**
 * Reads [awaitFirstDown], and [awaitPointerEvent] to
 * get [PointerInputChange] and motion event states
 * [onDown], [onMove], and [onUp]. Unlike overload of this function [onMove] returns
 * list of [PointerInputChange] to get data about all pointers that are on the screen.
 *
 * To prevent other pointer functions that call [awaitFirstDown] or [awaitPointerEvent]
 * (scroll, swipe, detect functions)
 * receiving changes call [PointerInputChange.consume] in [onDown],
 * and call [PointerInputChange.consumePositionChange]
 * in [onMove] block.
 *
 * @param onDown is invoked when first pointer is down initially.
 * @param onMove one or multiple pointers are being moved on screen.
 * @param onUp last pointer is up
 * @param delayAfterDownInMillis is optional delay after [onDown] This delay might be
 * required Composables like **Canvas** to process [onDown] before [onMove]
 *
 */
suspend fun PointerInputScope.detectMotionEventsAsList(
    onDown: (PointerInputChange) -> Unit = {},
    onMove: (List<PointerInputChange>) -> Unit = {},
    onUp: (PointerInputChange) -> Unit = {},
    delayAfterDownInMillis: Long = 0L
) {

    coroutineScope {
        awaitEachGesture {
            val down: PointerInputChange = awaitFirstDown()
            onDown(down)

            var pointer = down
            var pointerId = down.id

            var waitedAfterDown = false

            launch {
                delay(delayAfterDownInMillis)
                waitedAfterDown = true
            }

            while (true) {

                val event: PointerEvent = awaitPointerEvent()

                val anyPressed = event.changes.any { it.pressed }

                if (anyPressed) {
                    val pointerInputChange =
                        event.changes.firstOrNull { it.id == pointerId }
                            ?: event.changes.first()

                    pointerId = pointerInputChange.id
                    pointer = pointerInputChange

                    if (waitedAfterDown) {
                        onMove(event.changes)
                    }

                } else {
                    onUp(pointer)
                    break
                }
            }
        }
    }
}
