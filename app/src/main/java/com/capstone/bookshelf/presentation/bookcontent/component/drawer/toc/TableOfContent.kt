package com.capstone.bookshelf.presentation.bookcontent.component.drawer.toc
//
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.wrapContentHeight
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyListState
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.ModalDrawerSheet
//import androidx.compose.material3.NavigationDrawerItem
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableIntStateOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalFocusManager
//import androidx.compose.ui.platform.LocalSoftwareKeyboardController
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.input.KeyboardType
//import com.capstone.bookshelf.presentation.bookcontent.BookContentViewModel
//import com.capstone.bookshelf.presentation.bookcontent.state.ContentUIState
//
//@Composable
//fun TableOfContents(
//    bookContentViewModel: BookContentViewModel,
//    uiState : ContentUIState,
//    drawerLazyColumnState: LazyListState,
//    onDrawerItemClick: (Int) -> Unit,
//) {
//    var searchInput by remember { mutableStateOf("") }
//    val tableOfContents by bookContentViewModel.tableOfContents
//    var targetDatabaseIndex by remember { mutableIntStateOf(-1) }
//    var flag by remember { mutableStateOf(false) }
//    val keyboardController = LocalSoftwareKeyboardController.current
//    val focusManager = LocalFocusManager.current
//    LaunchedEffect(uiState.currentBookIndex, uiState.currentChapterIndex) {
//        bookContentViewModel.getTableOfContents(uiState.currentBookIndex)
//    }
//    LaunchedEffect(flag) {
//        if(flag){
//            drawerLazyColumnState.scrollToItem(targetDatabaseIndex)
//            flag = false
//        }
//    }
//    LaunchedEffect(uiState.drawerState) {
//        if (!uiState.drawerState) {
//            searchInput = ""
//            targetDatabaseIndex = -1
//            flag = false
//            keyboardController?.hide()
//            focusManager.clearFocus()
//        }
//    }
//    ModalDrawerSheet {
//        OutlinedTextField(
//            value = searchInput,
//            onValueChange = { newValue ->
//                if (newValue.all { it.isDigit() }) {
//                    searchInput = newValue
//                }
//            },
//            label = { Text("Enter a chapter number") },
//            keyboardOptions = KeyboardOptions(
//                keyboardType = KeyboardType.Number,
//                imeAction = ImeAction.Done
//            ),
//            keyboardActions = KeyboardActions(
//                onDone = {
//                    val chapterIndex = searchInput.toIntOrNull()
//                    if (chapterIndex != null) {
//                        targetDatabaseIndex = chapterIndex
//                        flag = true
//                    }
//                }
//            ),
//            modifier = Modifier.fillMaxWidth()
//        )
//        LazyColumn(
//            modifier = Modifier.fillMaxSize(),
//            state = drawerLazyColumnState,
//        ) {
//            items(tableOfContents) { tocItem ->
//                NavigationDrawerItem(
//                    label = {
//                        Text(
//                            text = tocItem.title,
//                            style = TextStyle(
//                                color = if (tableOfContents.indexOf(tocItem) == targetDatabaseIndex) {
//                                    Color.Green
//                                } else {
//                                    MaterialTheme.colorScheme.primary
//                                }
//                            )
//                        )
//                    },
//                    selected = tableOfContents.indexOf(tocItem) == uiState.currentChapterIndex,
//                    onClick = {
//                        onDrawerItemClick(tableOfContents.indexOf(tocItem))
//                    },
//                    modifier = Modifier.wrapContentHeight()
//                )
//            }
//        }
//    }
//}