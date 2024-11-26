package com.capstone.bookshelf.feature.readbook.presentation.component.content

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderText(
    index: Int,
    content: HeaderContent,
    level: Int,
    style: TextStyle,
    isHighlighted: Boolean,
    isSpeaking: Boolean
) {
    val color = if(isHighlighted && isSpeaking)
        style.background
    else
        Color.Transparent
    val size = when(level){
        1 -> style.fontSize*2
        2 -> style.fontSize*1.5
        3 -> style.fontSize*1.17
        4 -> style.fontSize*1
        5 -> style.fontSize*0.83
        6 -> style.fontSize*0.67
        else -> style.fontSize
    }
    val tooltipState = rememberTooltipState(
        isPersistent = true
    )
    val scope = rememberCoroutineScope()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        tooltip = {
            var input by remember{ mutableStateOf("") }
            RichTooltip(
                modifier = Modifier
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = TooltipDefaults.richTooltipContainerShape
                    ),
                caretSize = TooltipDefaults.caretSize,
                title = {
                    Text(text = "Comment")
                },
                action = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            scope.launch {
                                tooltipState.dismiss()
                                tooltipState.onDispose()
                            }
                        }) {
                            Text("Close")
                        }
                        TextButton(onClick = {
                            scope.launch {
                                tooltipState.dismiss()
                                tooltipState.onDispose()
                                Log.d("test", "$index + $input")
                            }
                        }) {
                            Text("Send")
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.wrapContentHeight()
                    ) {
                        Row {
                            VerticalDivider(
                                modifier = Modifier
                                    .height(40.dp),
                                thickness = 2.dp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = content.content,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    color = Color.LightGray,
                                    fontStyle = FontStyle.Italic,
                                )
                            )
                        }
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            value = input,
                            onValueChange = { input = it },
                            maxLines = 3,
                            label = { Text("Enter your comment") },
                        )
                    }
                }
            )
        },
        state = tooltipState,
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
            text = content.content,
            style = TextStyle(
                fontSize = size,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = style.color,
                background = color
            )
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParagraphText(
    index: Int,
    content: ParagraphContent,
    style: TextStyle,
    isHighlighted: Boolean,
    isSpeaking: Boolean
) {
    val color = if(isHighlighted && isSpeaking)
        style.background
    else
        Color.Transparent
    val tooltipState = rememberTooltipState(
        isPersistent = true
    )
    val scope = rememberCoroutineScope()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        tooltip = {
//            var input by remember{ mutableStateOf("") }
//            RichTooltip(
//                modifier = Modifier
//                    .border(
//                        width = 2.dp,
//                        color = MaterialTheme.colorScheme.onPrimaryContainer,
//                        shape = TooltipDefaults.richTooltipContainerShape
//                    ),
//                title = {
//                    Text(text = "Comment")
//                },
//                action = {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.End
//                    ) {
//                        TextButton(onClick = {
//                            scope.launch {
//                                tooltipState.dismiss()
//                                tooltipState.onDispose()
//                            }
//                        }) {
//                            Text("Close")
//                        }
//                        TextButton(onClick = {
//                            scope.launch {
//                                tooltipState.dismiss()
//                                tooltipState.onDispose()
//                                Log.d("test", "$index + $input")
//                            }
//                        }) {
//                            Text("Send")
//                        }
//                    }
//                },
//                text = {
//                    Column(
//                        modifier = Modifier.wrapContentHeight()
//                    ) {
//                        Row {
//                            VerticalDivider(
//                                modifier = Modifier
//                                    .height(40.dp),
//                                thickness = 2.dp
//                            )
//                            Spacer(modifier = Modifier.width(4.dp))
//                            Text(
//                                text = content.content,
//                                maxLines = 2,
//                                overflow = TextOverflow.Ellipsis,
//                                style = TextStyle(
//                                    color = Color.LightGray,
//                                    fontStyle = FontStyle.Italic,
//                                )
//                            )
//                        }
//                        OutlinedTextField(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .verticalScroll(rememberScrollState()),
//                            value = input,
//                            onValueChange = { input = it },
//                            maxLines = 3,
//                            label = { Text("Enter your comment") },
//                        )
//                    }
//                }
//            )
            Box(
                modifier = Modifier.size(width = 400.dp, height = 200.dp).background(Color.Gray)
            )
        },
        state = tooltipState,
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            text = content.text.value,
            style = TextStyle(
                textIndent = style.textIndent,
                textAlign = style.textAlign,
                fontSize = style.fontSize,
                color = style.color,
                background = color,
                lineBreak = style.lineBreak,
            ),
        )
    }
}