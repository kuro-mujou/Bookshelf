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
    // --- Use full state object again if simpler ---
    // It seems passing individual params didn't fix the core issue,
    // and using the state object is cleaner if @Stable works.
    // Let's assume DragAndDropListState IS marked @Stable.
    dragAndDropListState: DragAndDropListState<T>,
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable LazyItemScope.(isDragging: Boolean, modifier: Modifier) -> Unit
) {
    val initialDraggingIndex = dragAndDropListState.initialIndexOfDraggedItem
    val currentHoverIndex = dragAndDropListState.currentHoveredItemIndex
    val draggedItemHeight = dragAndDropListState.draggedItemHeight
    // Check if THIS item is the one originally dragged
    val isCurrentlyDraggedItem = index == initialDraggingIndex

    // Calculate the TARGET offset based on current state
    val targetOffsetY = remember(initialDraggingIndex, currentHoverIndex, draggedItemHeight, index, isCurrentlyDraggedItem) {
        // Calculation logic remains the same...
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
        // Log.d("DraggableItem", "Index $index: Calculated TARGET Offset = $calculatedOffset")
        calculatedOffset
    }

    // --- Animate the translationY ---
    val animatedOffsetY by animateFloatAsState(
        targetValue = targetOffsetY,
        label = "DraggableItemOffsetY"
        // animationSpec = tween(durationMillis = 150) // Optional: Adjust speed
    )
    // -------------------------------

    // Log the value being applied (will now be the animated value)
    // Log.d("DraggableItemApply", "Index $index: Applying animatedOffsetY = $animatedOffsetY, alpha = ${if (isCurrentlyDraggedItem) 0f else 1f}")

    val itemModifier = modifier
        .graphicsLayer {
            // Apply the ANIMATED offset
            translationY = animatedOffsetY
            alpha = if (isCurrentlyDraggedItem) 0f else 1f
        }

    content(false, itemModifier)
}

@Composable
fun <T> rememberDragAndDropListState(
    lazyListState: LazyListState,
    onMove: (Int, Int) -> Unit,
    getCurrentList: () -> List<T>
): DragAndDropListState<T> {
    val scope = rememberCoroutineScope()
    val state = remember(lazyListState, onMove, scope) {
        DragAndDropListState<T>(
            lazyListState = lazyListState,
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
    private val onMove: (Int, Int) -> Unit,
    private val scope: CoroutineScope,
    private val getCurrentList: () -> List<T>
) {
    //should pass in the null object so we can check if the place holder is null
    companion object{
        const val UNDRAGGABLE_INDEX = 0
    }

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
        //in normal condition this if will not be called
        if (itemInfo?.index == UNDRAGGABLE_INDEX) {
            return
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
        //in normal condition this if will not be called
        if (newHoverIndex == UNDRAGGABLE_INDEX) {
            val listSize = getCurrentList().size
            newHoverIndex = if (listSize > 1) {
                1
            } else {
                initialIndexOfDraggedItem
            }
        }
        if (newHoverIndex != null && currentHoveredItemIndex != newHoverIndex) {
            currentHoveredItemIndex = newHoverIndex
        }
        checkAndManageAutoScroll()
    }

//    fun onDrag(dragAmount: Offset, absolutePosition: Offset) {
//        draggingDistance += dragAmount.y // Keep tracking for direction and scroll
//        currentDragPosition = absolutePosition
//
//        val currentDraggingIdx = currentIndexOfDraggedItem ?: return
//        val initialIndex = initialIndexOfDraggedItem ?: return
//        val itemHeight = draggedItemHeight?.toFloat() ?: 0f
//        //should not use this cus we calculate middle point of the preview item
////        val fixOffsetPx = this.fixOffsetPx
////        val previewTopY = absolutePosition.y
////        val previewBottomY = previewTopY + itemHeight
//        // Calculate the effective center of the preview for gap checking
//        val previewCenterY = absolutePosition.y + itemHeight / 2f
//
//        val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
//        var newHoverIndex: Int? = null
//
//        // --- Hover Index Calculation v3 ---
//
//        // 1. Prioritize finding the item whose midpoint is closest to the preview's CENTER Y
//        //    This is generally the most stable way to determine the target slot.
//        val closestItem = visibleItems
//            .filter { it.index != currentDraggingIdx } // Exclude self
//            .minByOrNull { abs((it.offset + it.size / 2f) - previewCenterY) } // Find item with min distance from center
//
//        if (closestItem != null) {
//            // Check if the preview center is actually within the bounds where it should target this item.
//            // Generally, target the index of the closest item if the preview center is
//            // below the midpoint of the item above it (if any) and
//            // above the midpoint of the item below it (if any).
//
//            // Simpler approach: If the preview center is closer to this item's midpoint
//            // than half the item's height, consider it a likely target.
//            // A more robust check involves comparing to neighbours.
//
//            // Let's refine: Determine if the preview center is before or after the closest item's center
//            val closestItemMidY = closestItem.offset + closestItem.size / 2f
//
//            if (previewCenterY <= closestItemMidY) {
//                // If preview center is at or above the closest item's center, target THIS item's index
//                newHoverIndex = closestItem.index
//            } else {
//                // If preview center is below the closest item's center, target the NEXT index
//                newHoverIndex = closestItem.index + 1
//            }
//            Log.d("DragDropDebug", "onDrag Hover: Closest Item Idx: ${closestItem.index}, Preview Center: $previewCenterY, Closest MidY: $closestItemMidY -> Target Index $newHoverIndex")
//
//        } else if (visibleItems.any { it.index != currentDraggingIdx }) {
//            // If no "closest" found but other items exist, maybe we are way above or below?
//            // Fallback: Check if above all visible items or below all visible items
//            val firstOtherVisible = visibleItems.firstOrNull { it.index != currentDraggingIdx }
//            val lastOtherVisible = visibleItems.lastOrNull { it.index != currentDraggingIdx }
//
//            newHoverIndex = if (firstOtherVisible != null && previewCenterY < (firstOtherVisible.offset + firstOtherVisible.size / 2f)) {
//                // Above the first item's center
//                firstOtherVisible.index
//            } else if (lastOtherVisible != null && previewCenterY > (lastOtherVisible.offset + lastOtherVisible.size / 2f)) {
//                // Below the last item's center
//                lastOtherVisible.index + 1
//            } else {
//                // Should not happen if items exist, fallback to initial
//                initialIndex
//            }
//            Log.d("DragDropDebug", "onDrag Hover: Edge Case Fallback -> Target Index $newHoverIndex")
//        } else {
//            // Only the dragged item is visible
//            newHoverIndex = initialIndex
//            Log.d("DragDropDebug", "onDrag Hover: Only dragged item visible -> Target Index $newHoverIndex")
//        }
//
//
//        // Prevent hovering/dropping onto UNDRAGGABLE_INDEX
//        if (newHoverIndex != null && newHoverIndex <= UNDRAGGABLE_INDEX) {
//            val listSize = getCurrentList().size
//            newHoverIndex = if (listSize > 1 && initialIndex > UNDRAGGABLE_INDEX) {
//                UNDRAGGABLE_INDEX + 1
//            } else {
//                initialIndex
//            }
//            Log.d("DragDropDebug", "onDrag Hover: Corrected hover index from <=$UNDRAGGABLE_INDEX to $newHoverIndex")
//        }
//
//        // Clamp to valid range (1 to list size)
//        val totalCount = getCurrentList().size
//        // Allow hovering index `totalCount` which means dropping at the very end
//        newHoverIndex = newHoverIndex?.coerceIn(UNDRAGGABLE_INDEX + 1, totalCount)
//
//
//        // Update Hover State only if it changed
//        if (newHoverIndex != null && currentHoveredItemIndex != newHoverIndex) {
//            Log.d("DragDropDebug", "onDrag: Updating hover index from $currentHoveredItemIndex to $newHoverIndex")
//            currentHoveredItemIndex = newHoverIndex
//        }
//
//        checkAndManageAutoScroll()
//    }
    fun onDragEnd() {
        cancelContinuousScroll()
        val initialIndex = initialIndexOfDraggedItem
        val targetIndex = currentHoveredItemIndex
        if (initialIndex != null && targetIndex != null && initialIndex != targetIndex) {
//            in normal condition this will be called
//            onMove(initialIndex, targetIndex)
            if (targetIndex != UNDRAGGABLE_INDEX) {
                onMove(initialIndex, targetIndex)
            }
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