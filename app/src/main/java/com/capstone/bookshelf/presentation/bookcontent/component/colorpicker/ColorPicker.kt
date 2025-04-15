package com.capstone.bookshelf.presentation.bookcontent.component.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.toColorInt
import com.capstone.bookshelf.util.toHsl
import kotlinx.coroutines.delay

@Composable
fun ColorPicker(
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    Dialog(
        onDismissRequest = { onDismiss()},
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val blue400 = Color(0xff42A5F5)
                var hue by remember { mutableFloatStateOf(0f) }
                var saturation by remember { mutableFloatStateOf(0.5f) }
                var lightness by remember { mutableFloatStateOf(0.5f) }
                val color = Color.hsl(hue = hue, saturation = saturation, lightness = lightness)
                var inputHex by remember { mutableStateOf("") }
                var sendError by remember { mutableStateOf(false) }
                LaunchedEffect (sendError){
                    if(sendError){
                        delay(2000)
                        sendError = false
                    }
                }
                Text(
                    text = "COLOR",
                    color = blue400,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp)
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            color,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = inputHex,
                    onValueChange = {
                        if (it.length < 7)
                            inputHex = it
                    },
                    supportingText = {
                        if(sendError)
                            Text(
                                text = "Incorrect HEX code (0-6, A-F), please re-enter",
                                style = TextStyle(
                                    color = Color.Red
                                )
                            )
                    },
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp)
                        .fillMaxWidth()
                        .focusRequester(focusRequester) ,
                    textStyle = TextStyle(
                        textAlign = TextAlign.Center
                    ),
                    label = {
                        Text(text = "Input HEX code")
                    },
                    prefix = {
                        Text(text = "#")
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                try {
                                    Color("#$inputHex".toColorInt()).toHsl().let {
                                        hue = it[1]
                                        saturation = it[2]
                                        lightness = it[3]
                                    }
                                    focusManager.clearFocus()
                                }catch (e: Exception){
                                    sendError = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = if(sendError) Color.Red else MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if(sendError) Color.Red else MaterialTheme.colorScheme.primary,
                        focusedTextColor = if(sendError) Color.Red else MaterialTheme.colorScheme.onBackground
                    )
                )
                Box(contentAlignment = Alignment.Center) {
                    ColorPickerWheel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        selectionRadius = 8.dp,
                        hue = hue.toInt()
                    ) { hueChange ->
                        hue = hueChange.toFloat()
                    }
                    SaturationRhombus(
                        modifier = Modifier.size(200.dp),
                        hue = hue,
                        saturation = saturation,
                        lightness = lightness,
                        selectionRadius = 8.dp
                    ) { s, l ->
                        saturation = s
                        lightness = l
                    }
                }
                ColorSlider(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp)
                        .fillMaxWidth(),
                    title = "Hue",
                    titleColor = Color.Red,
                    gradientColorsReversed = listOf(
                        Color.Red,
                        Color.Yellow,
                        Color.Green,
                        Color.Cyan,
                        Color.Blue,
                        Color.Magenta,
                        Color.Red
                    ),
                    rgb = hue,
                    onColorChanged = {
                        hue = it
                    },
                    valueRange = 0f..360f
                )
                ColorSlider(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp)
                        .fillMaxWidth(),
                    title = "Saturation",
                    titleColor = Color.Green,
                    gradientColorsReversed = listOf(
                        Color.hsl(hue,0f / 100f,50f / 100f),
                        Color.hsl(hue,100f / 100f,50f / 100f),
                    ),
                    rgb = saturation * 100f,
                    onColorChanged = {
                        saturation = it / 100f
                    },
                    valueRange = 0f..100f
                )
                ColorSlider(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp)
                        .fillMaxWidth(),
                    title = "Lightness",
                    titleColor = Color.Blue,
                    gradientColorsReversed = listOf(
                        Color.hsl(0f,0f / 100f,0f / 100f),
                        Color.hsl(hue,100f / 100f,50f / 100f),
                        Color.hsl(0f,0f / 100f,100f / 100f),
                    ),
                    rgb = lightness * 100f,
                    onColorChanged = {
                        lightness = it / 100f
                    },
                    valueRange = 0f..100f
                )
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ){
                    Button(
                        onClick = {
                            onDismiss()
                        }
                    ) {
                        Text(text = "Cancel")
                    }
                    Button(
                        onClick = {
                            onColorSelected(color)
                            onDismiss()
                        }
                    ) {
                        Text(text = "Confirm")
                    }
                }
            }
        }
    }
}