package com.capstone.bookshelf.presentation.bookwriter.component

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.capstone.bookshelf.R
import com.capstone.bookshelf.util.calculateHeaderSize

@SuppressLint("SdCardPath")
@Composable
fun ComponentContainer(
    paragraph: Paragraph,
    index: Int,
    focusRequester: FocusRequester,
    onSizeChange: () -> Unit,
    onAdd: (Int, Int) -> Unit,
    onEditing: (String) -> Unit,
    onDelete: (Int) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onVisibilityChange: (Int, Boolean) -> Unit,
    focusedItem: (Int) -> Unit
) {
    var text by remember(paragraph.text) { mutableStateOf(paragraph.text) }
    val linkPattern = Regex("""/data/user/0/com\.capstone\.bookshelf/files/[^ ]*""")
    val headerPatten = Regex("""<h([1-6])[^>]*>(.*?)</h([1-6])>""")
    val headerLevel = Regex("""<h([1-6])>.*?</h\1>""")
    val htmlTagPattern = Regex(pattern = """<[^>]+>""")
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                onEditing(it.toString())
            }
        }
    )
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.TopEnd
    ) {
        Column(
            modifier = Modifier
                .padding(top = 4.dp, bottom = 4.dp,start = 8.dp, end = 8.dp)
                .fillMaxWidth()
                .background(color = Color(91, 72, 0, 255), RoundedCornerShape(15.dp))
        ) {
            AnimatedVisibility(
                visible = paragraph.isControllerVisible,
            ) {
                when (paragraph.type){
                    ParagraphType.TITLE -> {

                    }
                    ParagraphType.SUBTITLE,ParagraphType.PARAGRAPH, ParagraphType.IMAGE,ParagraphType.ADD_IMAGE -> {
                        TopIndicator(
                            onAddParagraph = {
                                onAdd(index, index - 1)
                            },
                            onAddImage = {
                                onAdd(index, index - 1)
                            },
                            onAddSubTitle = {
                                onAdd(index, index - 1)
                            },
                        )
                    }
                }
            }
            when (paragraph.type){
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
                            .onSizeChanged{
                                onSizeChange()
                            },
                        value = htmlTagPattern.replace(text, replacement = ""),
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
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                onAdd(index, index + 1)
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
                            .onSizeChanged{
                                onSizeChange()
                            },
                        value = text,
                        onValueChange = {
                            text = it
                            onEditing(it)
                        },
                        placeholder = {
                            Text(
                                text = "subtitle $index",
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
                        textStyle = TextStyle(
                            fontSize = calculateHeaderSize(paragraph.headerLevel!!).sp,
                            textAlign = TextAlign.Center,
                        )
                    )
                }
                ParagraphType.PARAGRAPH -> {
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
                            .onSizeChanged{
                                onSizeChange()
                            },
                        value = text,
                        onValueChange = {
                            text = it
                            onEditing(it)
                        },
                        placeholder = {
                            Text(
                                text = "paragraph $index",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    textIndent = TextIndent(32.sp)
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
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                onAdd(index, index + 1)
                            }
                        ),
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            textIndent = TextIndent(32.sp)
                        )
                    )
                }
                ParagraphType.IMAGE -> {
                    AsyncImage(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .height(300.dp),
                        model = text,
                        contentDescription = null,
                    )
                }
                ParagraphType.ADD_IMAGE -> {
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
                }
            }
            AnimatedVisibility(
                visible = paragraph.isControllerVisible,
            ) {
                when (paragraph.type){
                    ParagraphType.TITLE -> {
                        ChapterTitleIndicator(
                            onAddParagraph = {
                                onAdd(index, index + 1)
                            },
                            onAddImage = {
                                onAdd(index, index + 1)
                            },
                            onAddSubTitle = {
                                onAdd(index, index + 1)
                            }
                        )
                    }
                    ParagraphType.PARAGRAPH,ParagraphType.SUBTITLE,ParagraphType.IMAGE,ParagraphType.ADD_IMAGE -> {
                        BottomIndicator(
                            onAddParagraph = {
                                onAdd(index, index + 1)
                            },
                            onAddImage = {
                                onAdd(index, index + 1)
                            },
                            onAddSubTitle = {
                                onAdd(index, index + 1)
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
                }
            }
        }
        IconButton(
            onClick = {
                onVisibilityChange(index, !paragraph.isControllerVisible)
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_dot_menu), contentDescription = "Menu")
        }
    }
}