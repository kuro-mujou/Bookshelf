package com.capstone.bookshelf.presentation.bookcontent.bottomBar.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.BottomBarState
import com.capstone.bookshelf.presentation.bookcontent.bottomBar.model.ColorSample
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPalette
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPaletteViewModel
import com.capstone.bookshelf.presentation.bookcontent.component.colorpicker.ColorPicker
import com.capstone.bookshelf.util.DataStoreManger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun BottomBarTheme(
    colorPaletteViewModel: ColorPaletteViewModel,
    bottomBarState : BottomBarState,
    dataStore: DataStoreManger,
    colorPaletteState: ColorPalette,
) {
    var openColorPickerForBackground by remember { mutableStateOf(false) }
    var openColorPickerForText by remember { mutableStateOf(false) }
    var selectedColorSet by remember { mutableIntStateOf(0) }
    var openChangeColorMenu by remember { mutableStateOf(false) }
    var fontSize by remember { mutableIntStateOf(16) }
    val scope = rememberCoroutineScope()
    if (openColorPickerForBackground) {
        ColorPicker(
            onDismiss = {
                openColorPickerForBackground = false
            },
            onColorSelected = {
                colorPaletteViewModel.updateBackgroundColor(it)
                selectedColorSet = 18
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
                selectedColorSet = 18
                scope.launch {
                    dataStore.setSelectedColorSet(18)
                    dataStore.setTextColor(it.toArgb())
                }
                openColorPickerForText = false
            }
        )
    }
    LaunchedEffect(Unit){
        selectedColorSet = dataStore.selectedColorSet.first()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(colorPaletteState.backgroundColor)
    ) {
        if(openChangeColorMenu){
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .border(width = 3.dp, color = Color.Gray)
                        .background(color = colorPaletteState.backgroundColor)
                        .clickable(
                            onClick = {
                                openColorPickerForBackground = true
                            }
                        ),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        text = "Background",
                        style = TextStyle(color = colorPaletteState.textColor)
                    )
                }
                Box(
                    modifier = Modifier
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
                        style = TextStyle(color = colorPaletteState.backgroundColor)
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
                        selected = selectedColorSet == index,
                        onClick = {
                            scope.launch {
                                dataStore.setSelectedColorSet(index)
                                dataStore.setTextColor(sample.colorTxt.toArgb())
                                dataStore.setBackgroundColor(sample.colorBg.toArgb())
                                selectedColorSet = index
                            }
                            colorPaletteViewModel.updateTextColor(sample.colorTxt)
                            colorPaletteViewModel.updateBackgroundColor(sample.colorBg)
                        }
                    )
                }
            }
            OutlinedIconButton(
                onClick = {
                    openChangeColorMenu = !openChangeColorMenu
                },
                modifier = Modifier
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
        Row (
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ){
            Text(
                text = "Font Size",
                style = TextStyle(color = colorPaletteState.textColor)
            )
            Row(
                modifier = Modifier.wrapContentSize(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedIconButton(
                    onClick = {

                    },
                    modifier = Modifier
                        .size(30.dp),
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        containerColor = colorPaletteState.backgroundColor
                    ),
                    border = BorderStroke(width = 2.dp, color = colorPaletteState.textColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        tint = colorPaletteState.textColor,
                        contentDescription = null
                    )
                }
                Text(
                    text = "Font Size",
                    style = TextStyle(color = colorPaletteState.textColor)
                )
                OutlinedIconButton(
                    onClick = {

                    },
                    modifier = Modifier
                        .size(30.dp),
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        containerColor = colorPaletteState.backgroundColor
                    ),
                    border = BorderStroke(width = 2.dp, color = colorPaletteState.textColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        tint = colorPaletteState.textColor,
                        contentDescription = null
                    )
                }
            }
            OutlinedButton(
                onClick = {
                    //lam sau
                },
                contentPadding = PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                ),
            ) {
                Text(
                    text = "Add Font",
                    style = TextStyle(color = colorPaletteState.textColor)
                )
            }
        }
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}
@Composable
fun SampleColorItem(
    colorSample: ColorSample,
    selected: Boolean,
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
                color = colorSample.colorTxt
            )
        )
    }
}