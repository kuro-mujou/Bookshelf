package com.capstone.bookshelf.presentation.bookcontent.bottomBar.component

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.model.ColorSample
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPaletteViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPicker
import com.capstone.bookshelf.presentation.bookcontent.content.ContentAction
import com.capstone.bookshelf.presentation.bookcontent.content.ContentState
import com.capstone.bookshelf.presentation.bookcontent.content.ContentViewModel
import com.capstone.bookshelf.util.DataStoreManager
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@UnstableApi
fun BottomBarTheme(
    viewModel: ContentViewModel,
    colorPaletteViewModel: ColorPaletteViewModel,
    contentState: ContentState,
    colorPaletteState: ColorPalette,
    hazeState: HazeState,
    style: HazeStyle,
    dataStore: DataStoreManager,
) {
    var openColorPickerForBackground by remember { mutableStateOf(false) }
    var openColorPickerForText by remember { mutableStateOf(false) }
    var openChangeColorMenu by remember { mutableStateOf(false) }
    var textWidth by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    if (openColorPickerForBackground) {
        ColorPicker(
            onDismiss = {
                openColorPickerForBackground = false
            },
            onColorSelected = {
                colorPaletteViewModel.updateBackgroundColor(it)
                colorPaletteViewModel.updateSelectedColorSet(18)
                scope.launch {
                    dataStore.setSelectedColorSet(18)
                    dataStore.setBackgroundColor(it.toArgb())
                }
                openColorPickerForBackground = false
            }
        )
    }
    if (openColorPickerForText) {
        ColorPicker(
            onDismiss = {
                openColorPickerForText = false
            },
            onColorSelected = {
                colorPaletteViewModel.updateTextColor(it)
                colorPaletteViewModel.updateSelectedColorSet(18)
                scope.launch {
                    dataStore.setSelectedColorSet(18)
                    dataStore.setTextColor(it.toArgb())
                }
                openColorPickerForText = false
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.hazeEffect(
                        state = hazeState,
                        style = style
                    )
                } else {
                    Modifier.background(colorPaletteState.containerColor)
                }
            )
            .padding(
                PaddingValues(
                    start = WindowInsets.safeContent
                        .only(WindowInsetsSides.Start)
                        .asPaddingValues()
                        .calculateStartPadding(LayoutDirection.Ltr),
                    end = WindowInsets.safeContent
                        .only(WindowInsetsSides.End)
                        .asPaddingValues()
                        .calculateEndPadding(LayoutDirection.Ltr),
                    bottom = WindowInsets.navigationBars
                        .only(WindowInsetsSides.Bottom)
                        .asPaddingValues()
                        .calculateBottomPadding()
                )
            ),
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = openChangeColorMenu
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .weight(1f)
                            .height(40.dp)
                            .border(width = 2.dp, color = Color.Gray)
                            .background(color = colorPaletteState.containerColor)
                            .clickable(
                                onClick = {
                                    openColorPickerForBackground = true
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Background",
                            style = TextStyle(
                                color = colorPaletteState.textColor,
                                fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .weight(1f)
                            .height(40.dp)
                            .border(width = 2.dp, color = Color.Gray)
                            .background(color = colorPaletteState.textColor)
                            .clickable(
                                onClick = {
                                    openColorPickerForText = true
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Text",
                            style = TextStyle(
                                color = colorPaletteState.backgroundColor,
                                fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                            )
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .padding(top = 2.dp, bottom = 2.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LazyRow(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .weight(1f)
                        .wrapContentHeight()
                ) {
                    itemsIndexed(
                        items = colorPaletteState.colorSamples,
                    ) { index, sample ->
                        SampleColorItem(
                            index = index,
                            colorSample = sample,
                            contentState = contentState,
                            selected = colorPaletteState.selectedColorSet == index,
                            onClick = {
                                scope.launch {
                                    dataStore.setSelectedColorSet(index)
                                    dataStore.setTextColor(sample.colorTxt.toArgb())
                                    dataStore.setBackgroundColor(sample.colorBg.toArgb())
                                }
                                colorPaletteViewModel.updateTextColor(sample.colorTxt)
                                colorPaletteViewModel.updateBackgroundColor(sample.colorBg)
                                colorPaletteViewModel.updateSelectedColorSet(index)
                            }
                        )
                    }
                }
                OutlinedIconButton(
                    onClick = {
                        openChangeColorMenu = !openChangeColorMenu
                    },
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        containerColor = colorPaletteState.backgroundColor
                    ),
                    border = BorderStroke(width = 2.dp, color = colorPaletteState.textColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        tint = colorPaletteState.textColor,
                        contentDescription = null
                    )
                }
            }
            Row(
                modifier = Modifier
                    .padding(top = 2.dp, bottom = 2.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LazyRow(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .weight(1f)
                        .wrapContentHeight(),
                ) {
                    itemsIndexed(
                        items = contentState.fontFamilies,
                    ) { index, sample ->
                        SampleFontItem(
                            index = index,
                            fontSample = sample,
                            fontName = contentState.fontNames[index],
                            selected = contentState.selectedFontFamilyIndex == index,
                            colorPaletteState = colorPaletteState,
                            onClick = {
                                scope.launch {
                                    dataStore.setFontFamily(index)
                                }
                                viewModel.onContentAction(
                                    ContentAction.UpdateSelectedFontFamilyIndex(
                                        index
                                    )
                                )
                            }
                        )
                    }
                }
                OutlinedIconButton(
                    onClick = {
                        viewModel.onContentAction(ContentAction.UpdateImagePaddingState(!contentState.imagePaddingState))
                    },
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        containerColor = if (contentState.imagePaddingState) colorPaletteState.textColor else Color.Transparent
                    ),
                    border = BorderStroke(width = 2.dp, color = colorPaletteState.textColor)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_image_padding),
                        contentDescription = null,
                        tint = if (contentState.imagePaddingState) colorPaletteState.backgroundColor else colorPaletteState.textColor
                    )
                }
            }
            if (contentState.book?.fileType != "cbz" && contentState.book?.fileType != "pdf/images") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier.width(with(density) { textWidth.toDp() }),
                        text = "Font Size",
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                        )
                    )
                    Slider(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .weight(1f),
                        value = contentState.fontSize.toFloat(),
                        onValueChange = { value ->
                            viewModel.onContentAction(ContentAction.UpdateFontSize(value.roundToInt()))
                        },
                        onValueChangeFinished = {
                            scope.launch {
                                dataStore.setFontSize(contentState.fontSize)
                            }
                        },
                        colors = SliderDefaults.colors(
                            activeTrackColor = colorPaletteState.textColor,
                            activeTickColor = colorPaletteState.containerColor,
                            inactiveTickColor = colorPaletteState.containerColor.copy(alpha = 0.5f),
                            inactiveTrackColor = colorPaletteState.textColor.copy(alpha = 0.5f),
                        ),
                        valueRange = 12f..48f,
                        thumb = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = colorPaletteState.textColor,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${contentState.fontSize}",
                                    style = TextStyle(
                                        color = colorPaletteState.containerColor,
                                        fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                                    )
                                )
                            }
                        },
                        steps = 8
                    )
                    OutlinedIconButton(
                        onClick = {
                            viewModel.onContentAction(ContentAction.UpdateTextAlign(!contentState.textAlign))
                        },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.outlinedIconButtonColors(
                            containerColor = colorPaletteState.textColor
                        ),
                        border = BorderStroke(width = 2.dp, color = colorPaletteState.textColor)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(
                                id = if (contentState.textAlign)
                                    R.drawable.ic_align_justify
                                else
                                    R.drawable.ic_align_left
                            ),
                            contentDescription = null,
                            tint = colorPaletteState.backgroundColor
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier.onGloballyPositioned {
                            textWidth = it.size.width
                        },
                        text = "Line Spacing",
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                        )
                    )
                    Slider(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .weight(1f),
                        value = contentState.lineSpacing.toFloat(),
                        onValueChange = { value ->
                            viewModel.onContentAction(ContentAction.UpdateLineSpacing(value.roundToInt()))
                        },
                        onValueChangeFinished = {
                            scope.launch {
                                dataStore.setLineSpacing(contentState.lineSpacing)
                            }
                        },
                        colors = SliderDefaults.colors(
                            activeTrackColor = colorPaletteState.textColor,
                            activeTickColor = colorPaletteState.containerColor,
                            inactiveTickColor = colorPaletteState.containerColor.copy(alpha = 0.5f),
                            inactiveTrackColor = colorPaletteState.textColor.copy(alpha = 0.5f),
                        ),
                        valueRange = 4f..24f,
                        thumb = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = colorPaletteState.textColor,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${contentState.lineSpacing}",
                                    style = TextStyle(
                                        color = colorPaletteState.containerColor,
                                        fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                                    )
                                )
                            }
                        },
                        steps = 9
                    )
                    OutlinedIconButton(
                        onClick = {
                            viewModel.onContentAction(ContentAction.UpdateTextIndent(!contentState.textIndent))
                        },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.outlinedIconButtonColors(
                            if (contentState.textIndent) colorPaletteState.textColor else Color.Transparent
                        ),
                        border = BorderStroke(width = 2.dp, color = colorPaletteState.textColor)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_text_indent),
                            contentDescription = null,
                            tint = if (contentState.textIndent) colorPaletteState.backgroundColor else colorPaletteState.textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
@UnstableApi
fun SampleColorItem(
    index: Int,
    colorSample: ColorSample,
    selected: Boolean,
    contentState: ContentState,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .then(
                if (index == 0)
                    Modifier.padding(end = 8.dp)
                else
                    Modifier.padding(start = 8.dp, end = 8.dp)
            )
            .size(40.dp)
            .clip(CircleShape)
            .background(color = colorSample.colorBg)
            .border(
                width = 2.dp,
                color = if (selected) colorSample.colorTxt else colorSample.colorBg,
                shape = CircleShape
            )
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Aa",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                color = colorSample.colorTxt,
                fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
            )
        )
    }
}

@Composable
fun SampleFontItem(
    index: Int,
    fontSample: FontFamily,
    fontName: String,
    colorPaletteState: ColorPalette,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .then(
                if (index == 0)
                    Modifier.padding(end = 8.dp)
                else
                    Modifier.padding(start = 8.dp, end = 8.dp)
            )
            .height(40.dp)
            .wrapContentWidth()
            .clip(CircleShape)
            .background(color = colorPaletteState.backgroundColor)
            .border(
                width = 2.dp,
                color = if (selected) colorPaletteState.textColor else colorPaletteState.backgroundColor,
                shape = CircleShape
            )
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = fontName,
            modifier = Modifier.padding(start = 10.dp, end = 10.dp),
            style = TextStyle(
                fontFamily = fontSample,
                fontWeight = FontWeight.Bold,
                color = colorPaletteState.textColor,
            )
        )
    }
}