package com.capstone.bookshelf.presentation.bookdetail

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.capstone.bookshelf.R
import com.capstone.bookshelf.presentation.bookdetail.component.BookChip
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalLayoutApi::class)
@Composable
fun BookDetailScreenRoot(
    viewModel: BookDetailViewModel,
    onBackClick: () -> Unit,
    onBookMarkClick: () -> Unit,
    onDrawerItemClick: (Int) -> Unit,
    onReadBookClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tocLazyColumnState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val style = HazeMaterials.ultraThin(Color(0xFF181C20))
    val hazeState = remember { HazeState() }
    val focusRequester = remember { FocusRequester() }
    var canvasHeight by remember { mutableFloatStateOf(0f) }
    var targetSearchIndex by remember { mutableIntStateOf(-1) }
    var searchInput by remember { mutableStateOf("") }
    var flag by remember { mutableStateOf(false) }
    var enableSearch by remember { mutableStateOf(false) }
    val isImeVisible = WindowInsets.isImeVisible
    LaunchedEffect(flag) {
        if(flag){
            tocLazyColumnState.scrollToItem(targetSearchIndex)
            searchInput = ""
            flag = false
        }
    }
    LaunchedEffect(isImeVisible) {
        if (!isImeVisible) {
            focusManager.clearFocus()
        }
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ){
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(with(LocalDensity.current) { canvasHeight.toDp() })
                .clip(RoundedCornerShape(bottomEnd = 30.dp, bottomStart = 30.dp))
            ) {
                AsyncImage(
                    model = if(state.book?.coverImagePath == "error")
                        R.mipmap.book_cover_not_available
                    else
                        state.book?.coverImagePath,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                Modifier
                                    .hazeSource(state = hazeState)
                            }
                            else
                                Modifier
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(bottomEnd = 30.dp, bottomStart = 30.dp))
                    .onGloballyPositioned {
                        canvasHeight = it.size.height.toFloat()
                    }
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                        .wrapContentHeight()
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onBackClick,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                onBookMarkClick()
                            },
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_bookmark),
                                contentDescription = null,
                                tint = if (state.isSortedByFavorite)
                                    if(isSystemInDarkTheme())
                                        Color(155, 212, 161)
                                    else
                                        Color(52, 105, 63)
                                else
                                    Color.Gray,
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .width(125.dp)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 30.dp, bottomEnd = 8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(0.5f))
                        ) {
                            AsyncImage(
                                model = if(state.book?.coverImagePath == "error")
                                    R.mipmap.book_cover_not_available
                                else
                                    state.book?.coverImagePath,
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .padding(2.dp)
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 30.dp, bottomEnd = 8.dp))
                            )
                        }
                        Column(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 30.dp))
                                .then(
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        Modifier
                                            .background(Color.Transparent)
                                            .hazeEffect(
                                                state = hazeState,
                                                style = style
                                            )
                                    } else {
                                        Modifier
                                    }
                                )
                        ) {
                            Text(
                                modifier = Modifier.padding(4.dp),
                                text = state.book?.title ?: "",
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                                    fontWeight = FontWeight.Medium
                                ),
                            )
                            Text(
                                modifier = Modifier.padding(4.dp),
                                text = state.book?.authors?.joinToString(",") ?: "",
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                ),
                            )
                        }
                    }
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            state = tocLazyColumnState,
        ) {
            item{
                Text(
                    text = "Category",
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp),
                    style = TextStyle(
                        textAlign = TextAlign.Center,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        fontWeight = FontWeight.Medium
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    if(state.book?.categories?.isNotEmpty() == true){
                        state.book?.categories?.forEach {
                            BookChip {
                                Text(text = it)
                            }
                        }
                    } else {
                        Text(
                            text = "no category available",
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp),
                            style = TextStyle(
                                textIndent = TextIndent(firstLine = 20.sp),
                                textAlign = TextAlign.Justify,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize
                            )
                        )
                    }
                }
                Text(
                    text = "Description",
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp),
                    style = TextStyle(
                        textAlign = TextAlign.Center,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = state.book?.description ?: "no description available",
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp),
                    style = TextStyle(
                        textIndent = TextIndent(firstLine = 20.sp),
                        textAlign = TextAlign.Justify,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    )
                )
            }
            stickyHeader {
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                enableSearch = !enableSearch
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ){
                            Icon(
                                imageVector = if(enableSearch)
                                    ImageVector.vectorResource(R.drawable.ic_up)
                                else
                                    ImageVector.vectorResource(R.drawable.ic_down),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Text(
                            text = "Table of Content",
                            modifier = Modifier.align(Alignment.Center),
                            style = TextStyle(
                                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    AnimatedVisibility(
                        visible = enableSearch
                    ) {
                        OutlinedTextField(
                            value = searchInput,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() }) {
                                    searchInput = newValue
                                }
                            },
                            label = {
                                Text(
                                    text = "Enter a chapter number",
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    val chapterIndex = searchInput.toIntOrNull()
                                    if (chapterIndex != null) {
                                        targetSearchIndex =
                                            if (chapterIndex < state.tableOfContents.size)
                                                chapterIndex
                                            else
                                                state.tableOfContents.size - 1
                                        flag = true
                                        focusManager.clearFocus()
                                    }
                                }
                            ),
                            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        )
                    }
                }
            }
            itemsIndexed(
                items = state.tableOfContents,
                key = { _, tocItem -> tocItem.index }
            ) {index, tocItem ->
                NavigationDrawerItem(
                    label = {
                        Text(
                            text = tocItem.title,
                            style =
                                if (state.tableOfContents.indexOf(tocItem) == targetSearchIndex) {
                                    TextStyle(
                                        color =  MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                                    )
                                } else {
                                    TextStyle(
                                        fontSize = 14.sp,
                                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                                    )
                                },
                        )
                    },
                    selected = state.tableOfContents.indexOf(tocItem) == targetSearchIndex,
                    onClick = {
                        onDrawerItemClick(index)
                    },
                    modifier = Modifier.padding(4.dp,2.dp,4.dp,2.dp).wrapContentHeight(),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor =  if (state.tableOfContents.indexOf(tocItem) == targetSearchIndex) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            Color.Transparent
                        },
                        selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                    ),
                )
            }
        }
        Button(
            onClick = {
                onReadBookClick()
            },
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding()
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Read Book",
                style = TextStyle(
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}