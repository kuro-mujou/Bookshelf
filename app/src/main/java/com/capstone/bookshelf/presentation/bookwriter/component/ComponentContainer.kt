package com.capstone.bookshelf.presentation.bookwriter.component

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.uri.Uri
import coil.compose.AsyncImage
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookwriter.BookWriterState
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ComponentContainer(
    state: RichTextState,
    bookWriterState: BookWriterState,
    paragraph: Paragraph,
    onAddAbove: (ParagraphType) -> Unit,
    onAddBelow: (ParagraphType) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onVisibilityChange: (Boolean) -> Unit,
    focusedItem: () -> Unit,
    onFocusRequestedAndCleared: () -> Unit,
    onImageSelected: (Uri) -> Unit,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val isImeVisible = WindowInsets.isImeVisible
    val focusRequester = remember { FocusRequester() }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                onImageSelected(it)
            }
        }
    )
    LaunchedEffect(bookWriterState.itemToFocusId, paragraph.id) {
        if (bookWriterState.itemToFocusId == paragraph.id) {
            if (paragraph.type != ParagraphType.IMAGE) {
                delay(150)
                focusRequester.requestFocus()
                onFocusRequestedAndCleared()
            }
        }
    }
    LaunchedEffect(
        bookWriterState.selectedItem,
        isImeVisible,
        paragraph.id,
        state.annotatedString.length
    ) {
        if (bookWriterState.selectedItem == paragraph.id && isImeVisible) {
            coroutineScope.launch {
                delay(50)
                bringIntoViewRequester.bringIntoView()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .combinedClickable(
                hapticFeedbackEnabled = false,
                onClick = {},
                onLongClick = {
                    onVisibilityChange(!paragraph.isControllerVisible)
                }
            ),
        contentAlignment = Alignment.TopEnd
    ) {
        Column(
            modifier = Modifier
                .padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    RoundedCornerShape(15.dp)
                )
                    then (
                    if (paragraph.isControllerVisible) {
                        Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(15.dp)
                        )
                    } else {
                        Modifier
                    }
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = paragraph.isControllerVisible,
            ) {
                when (paragraph.type) {
                    ParagraphType.PARAGRAPH, ParagraphType.IMAGE -> {
                        TopIndicator(
                            onAddParagraph = {
                                onVisibilityChange(false)
                                onAddAbove(ParagraphType.PARAGRAPH)
                            },
                            onAddImage = {
                                onAddAbove(ParagraphType.IMAGE)
                            },
                        )
                    }

                    else -> {}
                }
            }
            when (paragraph.type) {
                ParagraphType.TITLE,
                ParagraphType.PARAGRAPH -> {
                    RichTextEditor(
                        state = state,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    focusedItem()
                                    onVisibilityChange(false)
                                }
                            },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next,
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                onVisibilityChange(false)
                                onAddBelow(ParagraphType.PARAGRAPH)
                            }
                        ),
                        colors = RichTextEditorDefaults.richTextEditorColors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            containerColor = Color.Transparent,
                        ),
                        placeholder = {
                            RichText(
                                modifier = Modifier.fillMaxWidth(),
                                state = RichTextState().apply {
                                    toggleParagraphStyle(
                                        ParagraphStyle(
                                            textIndent = TextIndent(firstLine = 20.sp)
                                        )
                                    )
                                    setText(
                                        "Type here..."
                                    )
                                }
                            )
                        },
                        textStyle = TextStyle(
                            textIndent = TextIndent()
                        )
                    )
                }

                ParagraphType.IMAGE -> {
                    if (state.toText() == "") {
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
                            model = state.toText(),
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
                                onVisibilityChange(false)
                                onAddBelow(ParagraphType.PARAGRAPH)
                            },
                            onAddImage = {
                                onAddBelow(ParagraphType.IMAGE)
                            },
                        )
                    }

                    ParagraphType.PARAGRAPH, ParagraphType.IMAGE -> {
                        BottomIndicator(
                            onAddParagraph = {
                                onVisibilityChange(false)
                                onAddBelow(ParagraphType.PARAGRAPH)
                            },
                            onAddImage = {
                                onAddBelow(ParagraphType.IMAGE)
                            },
                            onDelete = {
                                onDelete()
                            },
                            onMoveUp = {
                                onMoveUp()
                            },
                            onMoveDown = {
                                onMoveDown()
                            }
                        )
                    }

                    else -> {}
                }
            }
        }
        IconButton(
            onClick = {
                onVisibilityChange(!paragraph.isControllerVisible)
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_dot_menu),
                contentDescription = "Menu"
            )
        }
    }
}