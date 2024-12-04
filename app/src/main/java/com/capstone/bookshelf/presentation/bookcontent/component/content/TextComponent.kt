package com.capstone.bookshelf.presentation.bookcontent.component.content
//
//import androidx.compose.foundation.gestures.detectTapGestures
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Button
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.IntOffset
//import androidx.compose.ui.unit.IntRect
//import androidx.compose.ui.unit.IntSize
//import androidx.compose.ui.unit.LayoutDirection
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.window.Dialog
//import androidx.compose.ui.window.PopupPositionProvider
//import com.capstone.bookshelf.presentation.bookcontent.BookContentViewModel
//import com.capstone.bookshelf.presentation.bookcontent.state.ContentUIState
//
//
//@Composable
//fun HeaderText(
//    index: Int,
//    uiState: ContentUIState,
//    bookContentViewModel: BookContentViewModel,
//    content: HeaderContent,
//    level: Int,
//    style: TextStyle,
//    isHighlighted: Boolean,
//    isSpeaking: Boolean
//) {
//    val color = if(isHighlighted && isSpeaking)
//        style.background
//    else
//        Color.Transparent
//    val size = when(level){
//        1 -> style.fontSize*2
//        2 -> style.fontSize*1.5
//        3 -> style.fontSize*1.17
//        4 -> style.fontSize*1
//        5 -> style.fontSize*0.83
//        6 -> style.fontSize*0.67
//        else -> style.fontSize
//    }
//    var isSelected by remember { mutableStateOf(false) }
//    Text(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(start = 16.dp, end = 16.dp)
//            .pointerInput(Unit){
//                detectTapGestures(
//                    onTap = {
//                        if(isSelected){
//                            isSelected = false
//                            bookContentViewModel.updateIsSelectedParagraph(false)
//                        }
//                    },
//                    onLongPress = {
//                        if(!uiState.isSelectedParagraph){
//                            isSelected = true
//                            bookContentViewModel.updateIsSelectedParagraph(true)
//                        }
//                    }
//                )
//            },
//        text = content.content,
//        style = TextStyle(
//            fontSize = size,
//            fontWeight = FontWeight.Bold,
//            textAlign = TextAlign.Center,
//            color = style.color,
//            background = color
//        )
//    )
//}
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ParagraphText(
//    index: Int,
//    uiState: ContentUIState,
//    bookContentViewModel: BookContentViewModel,
//    content: ParagraphContent,
//    style: TextStyle,
//    isHighlighted: Boolean,
//    isSpeaking: Boolean
//) {
//    val color = if(isHighlighted && isSpeaking)
//        style.background
//    else
//        Color.Transparent
//    var isSelected by remember { mutableStateOf(false) }
//    var openDialog by remember { mutableStateOf(false) }
//    LaunchedEffect(uiState.commentButtonClicked){
//        if(uiState.commentButtonClicked){
//            openDialog = true
//        }
//    }
//    if(openDialog){
//        Dialog(
//            onDismissRequest = {
//                openDialog = false
//                isSelected = false
//                bookContentViewModel.updateIsSelectedParagraph(false)
//                bookContentViewModel.updateCommentButtonClicked(false)
//            }
//        ) {
//            Surface(
//                modifier = Modifier.fillMaxSize()
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(16.dp)
//                ){
//                    Button(onClick = {
//                        openDialog = false
//                        isSelected = false
//                        bookContentViewModel.updateIsSelectedParagraph(false)
//                        bookContentViewModel.updateCommentButtonClicked(false)
//                    }){
//                        Text(text = "Close")
//                    }
//                }
//            }
//        }
//    }
//
//    Text(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(start = 16.dp, end = 16.dp, top = 16.dp).pointerInput(Unit){
//                detectTapGestures(
//                    onTap = {
//                        if(isSelected){
//                            isSelected = false
//                            bookContentViewModel.updateIsSelectedParagraph(false)
//                        }
//                    },
//                    onLongPress = {
//                        if(!uiState.isSelectedParagraph){
//                            isSelected = true
//                            bookContentViewModel.updateIsSelectedParagraph(true)
//                        }
//                    }
//                )
//            },
//        text = content.text.value,
//        style = TextStyle(
//            textIndent = style.textIndent,
//            textAlign = style.textAlign,
//            fontSize = style.fontSize,
//            color = style.color,
//            background = color,
//            lineBreak = style.lineBreak,
//        ),
//    )
//}
//@Composable
//fun customPopupPositionProvider(): PopupPositionProvider {
//    val tooltipAnchorSpacing = 0
//    return remember(tooltipAnchorSpacing) {
//        object : PopupPositionProvider {
//            override fun calculatePosition(
//                anchorBounds: IntRect,
//                windowSize: IntSize,
//                layoutDirection: LayoutDirection,
//                popupContentSize: IntSize
//            ): IntOffset {
//                val x = anchorBounds.width/2 - popupContentSize.width/2
//                val y = anchorBounds.top
//                return IntOffset(x, y)
//            }
//        }
//    }
//}
////@OptIn(ExperimentalMaterial3Api::class)
////@Composable
////fun HeaderText(
////    index: Int,
////    content: HeaderContent,
////    level: Int,
////    style: TextStyle,
////    isHighlighted: Boolean,
////    isSpeaking: Boolean
////) {
////    val color = if(isHighlighted && isSpeaking)
////        style.background
////    else
////        Color.Transparent
////    val size = when(level){
////        1 -> style.fontSize*2
////        2 -> style.fontSize*1.5
////        3 -> style.fontSize*1.17
////        4 -> style.fontSize*1
////        5 -> style.fontSize*0.83
////        6 -> style.fontSize*0.67
////        else -> style.fontSize
////    }
////    val tooltipState = rememberTooltipState(
////        isPersistent = true
////    )
////    val scope = rememberCoroutineScope()
////    TooltipBox(
////        positionProvider = customPopupPositionProvider(),
////        tooltip = {
////            var input by remember{ mutableStateOf("") }
////            RichTooltip(
////                modifier = Modifier
////                    .border(
////                        width = 2.dp,
////                        color = MaterialTheme.colorScheme.onPrimaryContainer,
////                        shape = TooltipDefaults.richTooltipContainerShape
////                    ),
////                caretSize = TooltipDefaults.caretSize,
////                title = {
////                    Text(text = "Comment")
////                },
////                action = {
////                    Row(
////                        modifier = Modifier.fillMaxWidth(),
////                        horizontalArrangement = Arrangement.End
////                    ) {
////                        TextButton(onClick = {
////                            scope.launch {
////                                tooltipState.dismiss()
////                                tooltipState.onDispose()
////                            }
////                        }) {
////                            Text("Close")
////                        }
////                        TextButton(onClick = {
////                            scope.launch {
////                                tooltipState.dismiss()
////                                tooltipState.onDispose()
////                                Log.d("test", "$index + $input")
////                            }
////                        }) {
////                            Text("Send")
////                        }
////                    }
////                },
////                text = {
////                    Column(
////                        modifier = Modifier.wrapContentHeight()
////                    ) {
////                        Row {
////                            VerticalDivider(
////                                modifier = Modifier
////                                    .height(40.dp),
////                                thickness = 2.dp
////                            )
////                            Spacer(modifier = Modifier.width(4.dp))
////                            Text(
////                                text = content.content,
////                                maxLines = 2,
////                                overflow = TextOverflow.Ellipsis,
////                                style = TextStyle(
////                                    color = Color.LightGray,
////                                    fontStyle = FontStyle.Italic,
////                                )
////                            )
////                        }
////                        OutlinedTextField(
////                            modifier = Modifier
////                                .fillMaxWidth()
////                                .verticalScroll(rememberScrollState()),
////                            value = input,
////                            onValueChange = { input = it },
////                            maxLines = 3,
////                            label = { Text("Enter your comment") },
////                        )
////                    }
////                }
////            )
////        },
////        state = tooltipState,
////    ) {
////        Text(
////            modifier = Modifier
////                .fillMaxWidth()
////                .padding(start = 16.dp, end = 16.dp),
////            text = content.content,
////            style = TextStyle(
////                fontSize = size,
////                fontWeight = FontWeight.Bold,
////                textAlign = TextAlign.Center,
////                color = style.color,
////                background = color
////            )
////        )
////    }
////}