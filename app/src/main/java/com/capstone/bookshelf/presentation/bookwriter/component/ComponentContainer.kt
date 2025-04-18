package com.capstone.bookshelf.presentation.bookwriter.component

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookwriter.BookWriterState
import com.capstone.bookshelf.util.calculateHeaderSize
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SdCardPath")
@Composable
fun ComponentContainer(
    bookWriterState: BookWriterState,
    paragraph: Paragraph,
    index: Int,
    focusRequester: FocusRequester,
    onSizeChange: (Int) -> Unit,
    onAdd: (Int, Int, ParagraphType) -> Unit,
    onEditing: (String) -> Unit,
    onDelete: (Int) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onVisibilityChange: (Int, Boolean) -> Unit,
    focusedItem: (Int) -> Unit
) {
    var text by remember(paragraph.text) { mutableStateOf(paragraph.text) }
    val state = rememberRichTextState()
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                onEditing(it.toString())
            }
        }
    )
    LaunchedEffect(Unit) {
        state.toggleParagraphStyle(
            ParagraphStyle(
                textAlign = TextAlign.Justify,
                textIndent = TextIndent(32.sp)
            )
        )
        state.setText(paragraph.text)
    }
    LaunchedEffect(bookWriterState.selectedItem) {
        if (bookWriterState.selectedItem != index)
            state.clearSpanStyles()
    }
    LaunchedEffect(bookWriterState.toggleBold) {
        if (bookWriterState.selectedItem == index) {
            if (bookWriterState.toggleBold)
                state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
            else
                state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Normal))
        }
    }
    LaunchedEffect(bookWriterState.toggleItalic) {
        if (bookWriterState.selectedItem == index) {
            if (bookWriterState.toggleItalic)
                state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
            else
                state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Normal))
        }
    }
    LaunchedEffect(bookWriterState.toggleUnderline) {
        if (bookWriterState.selectedItem == index) {
            if (bookWriterState.toggleUnderline) {
                state.removeSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                state.addSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
            } else if (!bookWriterState.toggleStrikethrough)
                state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.None))
        }
    }
    LaunchedEffect(bookWriterState.toggleStrikethrough) {
        if (bookWriterState.selectedItem == index) {
            if (bookWriterState.toggleStrikethrough) {
                state.removeSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                state.addSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
            } else if (!bookWriterState.toggleUnderline)
                state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.None))
        }
    }
    LaunchedEffect(bookWriterState.toggleAlign) {
        if (bookWriterState.selectedItem == index) {
            when (bookWriterState.toggleAlign) {
                1 -> {
                    state.toggleParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Justify,
                            textIndent = TextIndent(32.sp)
                        )
                    )
                }

                2 -> {
                    state.toggleParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Start,
                            textIndent = TextIndent(32.sp)
                        )
                    )
                }

                3 -> {
                    state.toggleParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Center,
                            textIndent = TextIndent.None
                        )
                    )
                }
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.TopEnd
    ) {
        Column(
            modifier = Modifier
                .padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    RoundedCornerShape(15.dp)
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = paragraph.isControllerVisible,
            ) {
                when (paragraph.type) {
                    ParagraphType.TITLE -> {

                    }

                    ParagraphType.SUBTITLE, ParagraphType.PARAGRAPH, ParagraphType.IMAGE -> {
                        TopIndicator(
                            onAddParagraph = {
                                onVisibilityChange(index, false)
                                onAdd(index, index - 1, ParagraphType.PARAGRAPH)
                            },
                            onAddImage = {
                                onAdd(index, index - 1, ParagraphType.IMAGE)
                            },
                            onAddSubTitle = {
                                onVisibilityChange(index, false)
                                onAdd(index, index - 1, ParagraphType.SUBTITLE)
                            },
                        )
                    }

                    else -> {}
                }
            }
            when (paragraph.type) {
                ParagraphType.TITLE -> {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    focusedItem(index)
                                }
                                if (it.isFocused && paragraph.isControllerVisible) {
                                    onVisibilityChange(index, false)
                                } else if (!it.isFocused && paragraph.isControllerVisible) {
                                    onVisibilityChange(index, false)
                                }
                            }
                            .onSizeChanged {
                                onSizeChange(index)
                            },
                        value = bookWriterState.htmlTagPattern.replace(text, replacement = ""),
                        onValueChange = {
                            text = it
                            onEditing(it)
                        },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                onVisibilityChange(index, false)
                                onAdd(index, index + 1, ParagraphType.PARAGRAPH)
                            }
                        ),
                        textStyle = TextStyle(
                            fontSize = calculateHeaderSize(paragraph.headerLevel!!).sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    )
                }

                ParagraphType.SUBTITLE -> {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    focusedItem(index)
                                }
                                if (it.isFocused && paragraph.isControllerVisible) {
                                    onVisibilityChange(index, false)
                                } else if (!it.isFocused && paragraph.isControllerVisible) {
                                    onVisibilityChange(index, false)
                                }
                            }
                            .onSizeChanged {
                                onSizeChange(index)
                            },
                        value = text,
                        onValueChange = {
                            text = it
                            onEditing(it)
                        },
                        placeholder = {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = "subtitle - $index",
                                style = TextStyle(
                                    fontSize = calculateHeaderSize(paragraph.headerLevel).sp,
                                    textAlign = TextAlign.Center,
                                )
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        textStyle = TextStyle(
                            fontSize = calculateHeaderSize(paragraph.headerLevel!!).sp,
                            textAlign = TextAlign.Center,
                        )
                    )
                }

                ParagraphType.PARAGRAPH -> {
                    RichTextEditor(
                        state = state,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    focusedItem(index)
                                }
                                if (it.isFocused && paragraph.isControllerVisible) {
                                    onVisibilityChange(index, false)
                                } else if (!it.isFocused && paragraph.isControllerVisible) {
                                    onVisibilityChange(index, false)
                                }
                            }
                            .onSizeChanged {
                                onSizeChange(index)
                            },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                onVisibilityChange(index, false)
                                onAdd(index, index + 1, ParagraphType.PARAGRAPH)
                            }
                        ),
                        colors = RichTextEditorDefaults.richTextEditorColors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            containerColor = Color.Transparent,
                        ),
                        placeholder = {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = "paragraph - $index",
                                style = TextStyle(
                                    textIndent = state.currentParagraphStyle.textIndent,
                                    textAlign = state.currentParagraphStyle.textAlign
                                )
                            )
                        },
                    )
                }

                ParagraphType.IMAGE -> {
                    if (text == "") {
                        IconButton(
                            modifier = Modifier.size(70.dp),
                            onClick = {
                                imagePicker.launch("image/*")
                            }
                        ) {
                            Icon(
                                modifier = Modifier.size(50.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_add_image),
                                contentDescription = null
                            )
                        }
                        Text(text = "Pick Image")
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        AsyncImage(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(15.dp)),
                            model = text,
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }

                else -> {}
            }
            AnimatedVisibility(
                visible = paragraph.isControllerVisible,
            ) {
                when (paragraph.type) {
                    ParagraphType.TITLE -> {
                        ChapterTitleIndicator(
                            onAddParagraph = {
                                onVisibilityChange(index, false)
                                onAdd(index, index + 1, ParagraphType.PARAGRAPH)
                            },
                            onAddImage = {
                                onAdd(index, index + 1, ParagraphType.IMAGE)
                            },
                            onAddSubTitle = {
                                onVisibilityChange(index, false)
                                onAdd(index, index + 1, ParagraphType.SUBTITLE)
                            }
                        )
                    }

                    ParagraphType.PARAGRAPH, ParagraphType.SUBTITLE, ParagraphType.IMAGE -> {
                        BottomIndicator(
                            onAddParagraph = {
                                onVisibilityChange(index, false)
                                onAdd(index, index + 1, ParagraphType.PARAGRAPH)
                            },
                            onAddImage = {
                                onAdd(index, index + 1, ParagraphType.IMAGE)
                            },
                            onAddSubTitle = {
                                onVisibilityChange(index, false)
                                onAdd(index, index + 1, ParagraphType.SUBTITLE)
                            },
                            onDelete = {
                                onDelete(index)
                            },
                            onMoveUp = {
                                onMoveUp(index)
                            },
                            onMoveDown = {
                                onMoveDown(index)
                            }
                        )
                    }

                    else -> {}
                }
            }
        }
        IconButton(
            onClick = {
                onVisibilityChange(index, !paragraph.isControllerVisible)
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_dot_menu),
                contentDescription = "Menu"
            )
        }
    }
}