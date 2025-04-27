package com.capstone.bookshelf.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

fun <T> Modifier.dragContainer(
    state: DragAndDropListState<T>,
    getLazyListItemInfo: (offset: Offset) -> LazyListItemInfo?,
): Modifier {
    var currentPosition by mutableStateOf(Offset.Zero)
    return pointerInput(Unit) {
        detectDragGesturesAfterLongPress(
            onDragStart = { offset ->
                currentPosition = offset
                val itemInfo = getLazyListItemInfo(offset)
                state.onDragStart(offset, itemInfo)
            },
            onDrag = { change, dragAmount ->
                change.consume()
                currentPosition += dragAmount
                state.onDrag(dragAmount, currentPosition)
            },
            onDragEnd = { state.onDragEnd() },
            onDragCancel = { state.onDragInterrupted() }
        )
    }
}

@Composable
fun <T> LazyItemScope.DraggableItem(
    dragAndDropListState: DragAndDropListState<T>,
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable LazyItemScope.(isDragging: Boolean, modifier: Modifier) -> Unit
) {
    val initialDraggingIndex = dragAndDropListState.initialIndexOfDraggedItem
    val currentHoverIndex = dragAndDropListState.currentHoveredItemIndex
    val draggedItemHeight = dragAndDropListState.draggedItemHeight
    val isCurrentlyDraggedItem = index == initialDraggingIndex

    val targetOffsetY = remember(initialDraggingIndex, currentHoverIndex, draggedItemHeight, index, isCurrentlyDraggedItem) {
        val calculatedOffset = when {
            isCurrentlyDraggedItem -> 0f
            initialDraggingIndex == null || currentHoverIndex == null || draggedItemHeight == null -> 0f
            initialDraggingIndex < currentHoverIndex && index in (initialDraggingIndex + 1)..currentHoverIndex -> {
                -(draggedItemHeight.toFloat())
            }
            initialDraggingIndex > currentHoverIndex && index in currentHoverIndex..<initialDraggingIndex -> {
                draggedItemHeight.toFloat()
            }
            else -> 0f
        }
        calculatedOffset
    }

    val animatedOffsetY by animateFloatAsState(
        targetValue = targetOffsetY,
        label = "DraggableItemOffsetY"
    )

    val itemModifier = modifier
        .graphicsLayer {
            translationY = animatedOffsetY
            alpha = if (isCurrentlyDraggedItem) 0f else 1f
        }

    content(false, itemModifier)
}

@Composable
fun <T> rememberDragAndDropListState(
    lazyListState: LazyListState,
    stableIndex: Int? = null,
    onMove: (Int, Int) -> Unit,
    getCurrentList: () -> List<T>
): DragAndDropListState<T> {
    val scope = rememberCoroutineScope()
    val state = remember(lazyListState, onMove, scope) {
        DragAndDropListState<T>(
            lazyListState = lazyListState,
            stableIndex = stableIndex,
            onMove = onMove,
            scope = scope,
            getCurrentList = getCurrentList
        )
    }

    LaunchedEffect(state) {
        while (true) {
            val diff = state.scrollChannel.receive()
            if (abs(diff) > 0.01f) {
                launch {
                    state.lazyListState.scrollBy(diff)
                }
            }
        }
    }
    return state
}

@Stable
class DragAndDropListState<T>(
    val lazyListState: LazyListState,
    val stableIndex : Int? = null,
    private val onMove: (Int, Int) -> Unit,
    private val scope: CoroutineScope,
    private val getCurrentList: () -> List<T>
) {
    val scrollChannel = Channel<Float>(Channel.UNLIMITED)
    var initialIndexOfDraggedItem by mutableStateOf<Int?>(null)
        private set
    var currentHoveredItemIndex by mutableStateOf<Int?>(null)
        private set
    private var initialDraggingElement by mutableStateOf<LazyListItemInfo?>(null)
    var draggedItemHeight by mutableStateOf<Int?>(null)
        private set
    private var draggingDistance by mutableFloatStateOf(0f)

    var draggedItemData by mutableStateOf<T?>(null)
        private set
    var currentDragPosition by mutableStateOf<Offset?>(null)
        private set
    var initialDragAbsolutePosition by mutableStateOf<Offset?>(null)
        private set

    private var continuousScrollJob by mutableStateOf<Job?>(null)
    private var currentIndexOfDraggedItem by mutableStateOf<Int?>(null)

    fun onDragStart(
        startOffset: Offset,
        itemInfo: LazyListItemInfo?
    ) {
        stableIndex?.let{
            if (itemInfo?.index == it) {
                return
            }
        }
        cancelContinuousScroll()
        if (itemInfo != null) {
            val itemIndex = itemInfo.index
            val currentList: List<T> = getCurrentList()
            val itemData = currentList.getOrNull(itemIndex)
            if (itemData != null) {
                initialIndexOfDraggedItem = itemIndex
                currentHoveredItemIndex = itemIndex
                initialDraggingElement = itemInfo
                draggedItemHeight = itemInfo.size
                draggingDistance = 0f
                currentIndexOfDraggedItem = itemIndex
                draggedItemData = itemData
                initialDragAbsolutePosition = startOffset
                currentDragPosition = startOffset
                checkAndManageAutoScroll()
            } else {
                resetInternalState()
            }
        } else {
            resetInternalState()
        }
    }

    fun onDrag(dragAmount: Offset, absolutePosition: Offset) {
        draggingDistance += dragAmount.y
        currentDragPosition = absolutePosition
        val currentDraggingIdx = currentIndexOfDraggedItem ?: return
        val itemHeight = draggedItemHeight?.toFloat() ?: 0f
        val visualCenterY = absolutePosition.y + itemHeight/2
        val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
        var newHoverIndex: Int? = null
        val directHoverItem = visibleItems.find { item ->
            visualCenterY >= item.offset && visualCenterY <= (item.offset + item.size) &&
                    item.index != currentDraggingIdx
        }
        if (directHoverItem != null) {
            newHoverIndex = directHoverItem.index
        } else {
            val itemAbove = visibleItems
                .filter { it.index != currentDraggingIdx }
                .lastOrNull { item -> visualCenterY > item.offset + item.size / 2 }
            newHoverIndex = if (itemAbove != null) {
                itemAbove.index + 1
            } else {
                val firstVisibleItem = visibleItems.firstOrNull { it.index != currentDraggingIdx }
                if (firstVisibleItem != null) {
                    if (visualCenterY < firstVisibleItem.offset + firstVisibleItem.size / 2f) {
                        firstVisibleItem.index
                    } else {
                        firstVisibleItem.index + 1
                    }
                } else {
                    initialIndexOfDraggedItem
                }
            }
        }
        stableIndex?.let {
            if (newHoverIndex == it) {
                val listSize = getCurrentList().size
                newHoverIndex = if (listSize > 1) {
                    1
                } else {
                    initialIndexOfDraggedItem
                }
            }
        }
        if (newHoverIndex != null && currentHoveredItemIndex != newHoverIndex) {
            currentHoveredItemIndex = newHoverIndex
        }
        checkAndManageAutoScroll()
    }

    fun onDragEnd() {
        cancelContinuousScroll()
        val initialIndex = initialIndexOfDraggedItem
        val targetIndex = currentHoveredItemIndex
        if (initialIndex != null && targetIndex != null && initialIndex != targetIndex) {
            stableIndex?.let{
                if (targetIndex != stableIndex) {
                    onMove(initialIndex, targetIndex)
                }
            }?: onMove(initialIndex, targetIndex)
        }
        resetInternalState()
    }

    fun onDragInterrupted() {
        cancelContinuousScroll()
        resetInternalState()
    }

    private fun checkAndManageAutoScroll() {
        val initialInfo = initialDraggingElement ?: return
        val height = draggedItemHeight ?: return
        val scrollAmount = calculateScrollAmount(initialInfo, height, draggingDistance)
        if (abs(scrollAmount) > 0.5f) {
            if (continuousScrollJob?.isActive != true) {
                startContinuousScroll(scrollAmount)
            } else {
                continuousScrollJob?.cancel()
                startContinuousScroll(scrollAmount)
            }
        } else {
            cancelContinuousScroll()
        }
    }

    private fun calculateScrollAmount(
        initialInfo: LazyListItemInfo, draggedItemHeight: Int, currentDragDistance: Float
    ): Float {
        val startOffset = initialInfo.offset + currentDragDistance
        val endOffset = startOffset + draggedItemHeight
        val viewportStart = lazyListState.layoutInfo.viewportStartOffset
        val viewportEnd = lazyListState.layoutInfo.viewportEndOffset
        val scrollThreshold = lazyListState.layoutInfo.viewportSize.height * 0.15f
        val speedFactor = 0.05f
        return when {
            currentDragDistance > 0 && endOffset > viewportEnd - scrollThreshold ->
                (endOffset - (viewportEnd - scrollThreshold)).coerceAtLeast(0f) * speedFactor

            currentDragDistance < 0 && startOffset < viewportStart + scrollThreshold ->
                (startOffset - (viewportStart + scrollThreshold)).coerceAtMost(0f) * speedFactor

            else -> 0f
        }
    }

    private fun startContinuousScroll(scrollAmount: Float) {
        continuousScrollJob = scope.launch {
            while (isActive) {
                try {
                    scrollChannel.send(scrollAmount)
                    delay(10L)
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) break
                    break
                }
            }
        }
    }

    private fun cancelContinuousScroll() {
        continuousScrollJob?.cancel()
        continuousScrollJob = null
    }

    private fun resetInternalState() {
        initialDraggingElement = null
        initialIndexOfDraggedItem = null
        currentHoveredItemIndex = null
        draggingDistance = 0f
        draggedItemHeight = null
        currentIndexOfDraggedItem = null
        draggedItemData = null
        currentDragPosition = null
        initialDragAbsolutePosition = null
        cancelContinuousScroll()
    }
}