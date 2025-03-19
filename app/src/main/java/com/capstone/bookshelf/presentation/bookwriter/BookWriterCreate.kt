package com.capstone.bookshelf.presentation.bookwriter

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.capstone.bookshelf.R
import com.capstone.bookshelf.data.mapper.toDataClass
import com.capstone.bookshelf.domain.wrapper.Book

@Composable
fun BookWriterCreate(
    viewModel: BookWriterViewModel,
    onNavigateToBookContent: (String,Book) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        var isBookTitleError by remember { mutableStateOf(false) }
        var isAuthorNameError by remember { mutableStateOf(false) }
        var bookTitle by remember { mutableStateOf("") }
        var authorName by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        var coverImagePath by remember { mutableStateOf("") }
        val context = LocalContext.current
        val imagePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri ->
                uri?.let {
                    coverImagePath = it.toString()
                }
            }
        )
        val focusManager = LocalFocusManager.current
        val bookId by viewModel.bookID.collectAsStateWithLifecycle()
        val book by viewModel.book.collectAsStateWithLifecycle()
        LaunchedEffect(bookId) {
            if (bookId.isNotEmpty()) {
                onNavigateToBookContent(bookId,book.toDataClass())
            }
        }
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        focusManager.clearFocus()
                    }
                )
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ADD BOOK INFO",
                modifier = Modifier,
                style = TextStyle(
                    fontSize = MaterialTheme.typography.titleLarge.fontSize
                ),
            )
            OutlinedTextField(
                value = bookTitle,
                onValueChange = {
                    bookTitle = it
                    isBookTitleError = it.isBlank()
                },
                isError = isBookTitleError,
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                label = {
                    Text(text = "Book title")
                },
                supportingText = {
                    if (isBookTitleError) {
                        Text( text = "Book title cannot be empty" )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                )
            )
            OutlinedTextField(
                value = authorName,
                onValueChange = {
                    authorName = it
                    isAuthorNameError = it.isBlank()
                },
                isError = isAuthorNameError,
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth(),
                label = {
                    Text(text = "Author")
                },
                supportingText = {
                    if (isAuthorNameError) {
                        Text( text = "Author name cannot be empty" )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    }
                )
            )
            Text(
                text = "ADD COVER IMAGE",
                style = TextStyle(
                    fontSize = MaterialTheme.typography.titleLarge.fontSize
                ),
            )
            if(coverImagePath.isNotEmpty()){
                AsyncImage(
                    modifier = Modifier
                            .padding(top = 8.dp)
                            .height(300.dp),
                    model = coverImagePath,
                    contentDescription = null,
                )
            } else {
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .height(300.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
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
                            Text(
                                text = "Add new book cover",
                                style = TextStyle(
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                    Text(
                        text = "OR",
                        style = TextStyle(
                            fontSize = MaterialTheme.typography.titleLarge.fontSize
                        )
                    )
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = R.mipmap.book_cover_not_available,
                            contentDescription = null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = 4.dp,
                                    color = Color.Gray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .width(150.dp)
                        )
                    }
                }
            }
            Button(
                modifier = Modifier.align(Alignment.End),
                onClick = {
                    if(bookTitle.isBlank() || authorName.isBlank()){
                        if(bookTitle.isBlank()){
                            isBookTitleError = true
                        }
                        if(authorName.isBlank()){
                            isAuthorNameError = true
                        }
                    } else {
                        viewModel.onAction(
                            BookWriterAction.AddBookInfo(
                                context = context,
                                bookTitle = bookTitle,
                                authorName = authorName,
                                coverImagePath = coverImagePath.ifEmpty { "error" }
                            )

                        )
                    }
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Next step"
                    )
                    Icon(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(15.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_send),
                        contentDescription = null
                    )
                }
            }
        }
    }
}