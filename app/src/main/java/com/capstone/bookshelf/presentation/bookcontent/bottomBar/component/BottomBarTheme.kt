package com.capstone.bookshelf.presentation.bookcontent.bottomBar.component

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
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
    viewModel : ContentViewModel,
    colorPaletteViewModel: ColorPaletteViewModel,
    contentState : ContentState,
    colorPaletteState: ColorPalette,
    hazeState: HazeState,
    style: HazeStyle,
    dataStore: DataStoreManager,
) {
    var openColorPickerForBackground by remember { mutableStateOf(false) }
    var openColorPickerForText by remember { mutableStateOf(false) }
    var openChangeColorMenu by remember { mutableStateOf(false) }
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
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                    Modifier.hazeEffect(
                        state = hazeState,
                        style = style
                    )
                }else{
                    Modifier.background(colorPaletteState.containerColor)
                }
            ),
    ) {
        if(openChangeColorMenu){
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp,end = 4.dp)
                        .weight(1f)
                        .height(40.dp)
                        .border(width = 3.dp, color = Color.Gray)
                        .background(color = colorPaletteState.containerColor)
                        .clickable(
                            onClick = {
                                openColorPickerForBackground = true
                            }
                        ),
                    contentAlignment = Alignment.Center
                ){
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
                        .padding(start = 4.dp,end = 8.dp)
                        .weight(1f)
                        .height(40.dp)
                        .border(width = 3.dp, color = Color.Gray)
                        .background(color = colorPaletteState.textColor)
                        .clickable(
                            onClick = {
                                openColorPickerForText = true
                            }
                        ),
                    contentAlignment = Alignment.Center
                ){
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
        Spacer(modifier = Modifier.height(4.dp))
        Row (
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically
        ){
            LazyRow(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .weight(1f)
            ) {
                itemsIndexed(
                    items = colorPaletteState.colorSamples,
                ) {index, sample->
                    SampleColorItem(
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
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(40.dp),
                colors = IconButtonDefaults. outlinedIconButtonColors(
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
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        ) {
            itemsIndexed(
                items = contentState.fontFamilies,
            ) {index, sample->
                SampleFontItem(
                    fontSample = sample,
                    fontName = contentState.fontNames[index],
                    selected = contentState.selectedFontFamilyIndex == index,
                    colorPaletteState = colorPaletteState,
                    onClick = {
                        scope.launch {
                            dataStore.setFontFamily(index)
                        }
                        viewModel.onContentAction(dataStore,ContentAction.UpdateSelectedFontFamilyIndex(index))
                    }
                )
            }
        }
        if(contentState.book?.fileType != "cbz") {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.padding(start = 8.dp).width(120.dp),
                    text = "Font Size",
                    style = TextStyle(
                        color = colorPaletteState.textColor,
                        fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                    )
                )
                Slider(
                    modifier = Modifier.padding(end = 8.dp).fillMaxWidth(),
                    value = contentState.fontSize.toFloat(),
                    onValueChange = { value ->
                        viewModel.onContentAction(
                            dataStore,
                            ContentAction.UpdateFontSize(value.roundToInt())
                        )
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
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.padding(start = 8.dp).width(120.dp),
                    text = "Line Spacing",
                    style = TextStyle(
                        color = colorPaletteState.textColor,
                        fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                    )
                )
                Slider(
                    modifier = Modifier.padding(end = 8.dp).fillMaxWidth(),
                    value = contentState.lineSpacing.toFloat(),
                    onValueChange = { value ->
                        viewModel.onContentAction(
                            dataStore,
                            ContentAction.UpdateLineSpacing(value.roundToInt())
                        )
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
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 4.dp)
                        .weight(1f)
                        .height(40.dp)
                        .border(width = 1.dp, color = colorPaletteState.textColor)
                        .background(color = colorPaletteState.containerColor)
                        .clickable(
                            onClick = {
                                viewModel.onContentAction(
                                    dataStore,
                                    ContentAction.UpdateTextAlign(!contentState.textAlign)
                                )
                                scope.launch {
                                    dataStore.setTextAlign(!contentState.textAlign)
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val displayState = if (contentState.textAlign) "Justify" else "Left"
                    Text(
                        text = "Align: $displayState",
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp, end = 8.dp)
                        .weight(1f)
                        .height(40.dp)
                        .border(width = 1.dp, color = colorPaletteState.textColor)
                        .background(color = colorPaletteState.containerColor)
                        .clickable(
                            onClick = {
                                viewModel.onContentAction(
                                    dataStore,
                                    ContentAction.UpdateTextIndent(!contentState.textIndent)
                                )
                                scope.launch {
                                    dataStore.setTextIndent(!contentState.textIndent)
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val displayState = if (contentState.textIndent) "Indent" else "No Indent"
                    Text(
                        text = displayState,
                        style = TextStyle(
                            color = colorPaletteState.textColor,
                            fontFamily = contentState.fontFamilies[contentState.selectedFontFamilyIndex]
                        )
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}
@Composable
@UnstableApi
fun SampleColorItem(
    colorSample: ColorSample,
    selected: Boolean,
    contentState: ContentState,
    onClick: () -> Unit
){
    Box(
        modifier = Modifier
            .padding(8.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(color = colorSample.colorBg)
            .border(
                width = 4.dp,
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
    fontSample: FontFamily,
    fontName: String,
    colorPaletteState: ColorPalette,
    selected: Boolean,
    onClick: () -> Unit
){
    Box(
        modifier = Modifier
            .padding(8.dp)
            .height(40.dp)
            .wrapContentWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(color = colorPaletteState.backgroundColor)
            .border(
                width = 4.dp,
                color = if (selected) colorPaletteState.textColor else colorPaletteState.backgroundColor,
                shape = RoundedCornerShape(20.dp)
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