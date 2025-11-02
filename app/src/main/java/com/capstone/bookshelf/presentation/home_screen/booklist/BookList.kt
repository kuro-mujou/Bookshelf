package com.capstone.bookshelf.presentation.home_screen.booklist

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.bookshelf.R
import com.capstone.bookshelf.app.Route
import com.capstone.bookshelf.presentation.component.BookChip
import com.capstone.bookshelf.presentation.home_screen.booklist.component.AsyncImportBookViewModel
import com.capstone.bookshelf.presentation.home_screen.booklist.component.BookMenuBottomSheet
import com.capstone.bookshelf.presentation.home_screen.booklist.component.ExpandableFab
import com.capstone.bookshelf.presentation.home_screen.booklist.component.GridBookView
import com.capstone.bookshelf.presentation.home_screen.booklist.component.ListBookView
import com.capstone.bookshelf.presentation.home_screen.booklist.component.MiniFabItems
import com.capstone.bookshelf.presentation.home_screen.component.DriveInputLinkDialog
import com.capstone.bookshelf.util.DataStoreManager
import com.capstone.bookshelf.util.DeviceConfiguration
import com.capstone.bookshelf.util.ImportBook
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
)
@Composable
fun BookList(
    bookListViewModel: BookListViewModel,
    importBookViewModel: AsyncImportBookViewModel,
    dataStoreManager: DataStoreManager,
    navigateTo: (Route) -> Unit,
    onAction: (BookListAction) -> Unit,
) {
    val bookListState by bookListViewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val gridState = rememberLazyStaggeredGridState()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val isKeyboardVisible = WindowInsets.isImeVisible
    var searchText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var showDriveInputLinkDialog by remember { mutableStateOf(false) }
    var fabExpanded by remember { mutableStateOf(false) }
    var showFab by remember { mutableStateOf(true) }
    var specialIntent by remember { mutableStateOf("null") }
    val context = LocalContext.current
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)
    val columnsStaggeredGridCount by remember(deviceConfiguration) {
        mutableStateOf(
            if (deviceConfiguration == DeviceConfiguration.PHONE_PORTRAIT || deviceConfiguration == DeviceConfiguration.TABLET_PORTRAIT)
                StaggeredGridCells.Fixed(2)
            else
                StaggeredGridCells.Fixed(3)
        )
    }
    val importBookLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        ImportBook(
            context = context,
            scope = scope,
            specialIntent = specialIntent
        ).processIntentUri(uri)
    }
    val fabItems = listOf(
        MiniFabItems(
            icon = R.drawable.ic_add_epub,
            title = "Import EPUB",
            tint = if (isSystemInDarkTheme())
                Color(255, 250, 160)
            else
                Color(131, 105, 83),
            onClick = {
                specialIntent = "null"
                fabExpanded = false
                importBookLauncher.launch(arrayOf("application/epub+zip"))
            }
        ),
        MiniFabItems(
            icon = R.drawable.ic_add_epub,
            title = "Import CBZ",
            tint = if (isSystemInDarkTheme())
                Color(255, 250, 160)
            else
                Color(131, 105, 83),
            onClick = {
                specialIntent = "null"
                fabExpanded = false
                importBookLauncher.launch(arrayOf("application/vnd.comicbook+zip", "application/octet-stream"))
            }
        ),
        MiniFabItems(
            icon = R.drawable.ic_add_epub,
            title = "Import PDF with page render",
            tint = if (isSystemInDarkTheme())
                Color(255, 250, 160)
            else
                Color(131, 105, 83),
            onClick = {
                specialIntent = "PAGE"
                fabExpanded = false
                importBookLauncher.launch(
                    arrayOf("application/pdf")
                )
            }
        ),
        MiniFabItems(
            icon = R.drawable.ic_add_epub,
            title = "Import PDF with text/image extraction",
            tint = if (isSystemInDarkTheme())
                Color(255, 250, 160)
            else
                Color(131, 105, 83),
            onClick = {
                specialIntent = "TEXT"
                fabExpanded = false
                importBookLauncher.launch(
                    arrayOf("application/pdf")
                )
            }
        ),
        MiniFabItems(
            icon = R.drawable.ic_add_epub,
            title = "Import EPUB via Google Drive",
            tint = if (isSystemInDarkTheme())
                Color(255, 250, 160)
            else
                Color(131, 105, 83),
            onClick = {
                specialIntent = "null"
                fabExpanded = false
                showDriveInputLinkDialog = true
            }
        ),
        MiniFabItems(
            icon = R.drawable.ic_write_ebook,
            title = "Write new Book",
            tint = if (isSystemInDarkTheme())
                Color(155, 212, 161)
            else
                Color(52, 105, 63),
            onClick = {
                fabExpanded = false
                navigateTo(Route.WriteBook(""))
            }
        )
    )
    LaunchedEffect(Unit) {
        bookListViewModel.onAction(BookListAction.UpdateBookListType(dataStoreManager.bookListView.first()))
    }
    LaunchedEffect(isKeyboardVisible) {
        if (!isKeyboardVisible) {
            focusManager.clearFocus()
        }
    }
    LaunchedEffect(drawerState.currentValue) {
        showFab = drawerState.isClosed
    }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl ) {
        ModalNavigationDrawer(
            gesturesEnabled = drawerState.isOpen,
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr){
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .fillMaxHeight()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    top = WindowInsets.safeContent
                                        .only(WindowInsetsSides.Top)
                                        .asPaddingValues()
                                        .calculateTopPadding(),
                                    end = WindowInsets.safeContent
                                        .only(WindowInsetsSides.End)
                                        .asPaddingValues()
                                        .calculateEndPadding(LayoutDirection.Ltr)
                                )
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(all = 4.dp)
                                    .fillMaxWidth(),
                                text = "Filter",
                                style = TextStyle(
                                    textAlign = TextAlign.Center
                                )
                            )
                            FlowRow(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(all = 8.dp)
                                    .verticalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                bookListState.categories.forEach { categoryChip ->
                                    BookChip(
                                        selected = categoryChip.isSelected,
                                        color = Color(categoryChip.color),
                                        onClick = {
                                            onAction(BookListAction.ChangeChipState(categoryChip))
                                        }
                                    ) {
                                        Text(text = categoryChip.name)
                                    }
                                }
                            }
                            if (!bookListState.categories.none { it.isSelected }){
                                Button(
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally),
                                    onClick = {
                                        onAction(BookListAction.ResetChipState)
                                    }
                                ) {
                                    Text(text = "Reset")
                                }
                            }
                        }
                    }
                }
            },
            content = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr){
                    Column(
                        modifier = Modifier
                            .padding(
                                top = WindowInsets.safeContent
                                    .only(WindowInsetsSides.Top)
                                    .asPaddingValues()
                                    .calculateTopPadding(),
                                end = WindowInsets.safeContent
                                    .only(WindowInsetsSides.End)
                                    .asPaddingValues()
                                    .calculateEndPadding(LayoutDirection.Ltr)
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { focusManager.clearFocus() }
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!bookListState.isOnDeleteBooks) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                IconButton(
                                    onClick = {
                                        onAction(BookListAction.UpdateBookListType())
                                    }
                                ) {
                                    if (bookListState.listViewType == 1) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_grid_view),
                                            contentDescription = "Grid",
                                            tint = if (isSystemInDarkTheme())
                                                Color(154, 204, 250)
                                            else
                                                Color(45, 98, 139)
                                        )
                                    } else if (bookListState.listViewType == 0) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_list_view),
                                            contentDescription = "List",
                                            tint = if (isSystemInDarkTheme())
                                                Color(154, 204, 250)
                                            else
                                                Color(45, 98, 139)
                                        )
                                    }
                                }
                                OutlinedTextField(
                                    value = searchText,
                                    onValueChange = {
                                        searchText = it
                                    },
                                    modifier = Modifier
                                        .weight(1f),
                                    placeholder = {
                                        Text(text = "Search")
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(25.dp)
                                )
                                Box {
                                    IconButton(onClick = { expanded = !expanded }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Delete") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = ImageVector.vectorResource(R.drawable.ic_delete),
                                                    contentDescription = "Delete Icon",
                                                    tint = if (isSystemInDarkTheme())
                                                        Color(250, 160, 160)
                                                    else
                                                        Color(194, 59, 34)
                                                )
                                            },
                                            onClick = {
                                                bookListViewModel.onAction(BookListAction.OnDeletingBooks(true))
                                                expanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Sort") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = ImageVector.vectorResource(R.drawable.ic_bookmark_star),
                                                    contentDescription = "Sorting Icon",
                                                    tint = if (bookListState.isSortedByFavorite)
                                                        if (isSystemInDarkTheme())
                                                            Color(155, 212, 161)
                                                        else
                                                            Color(52, 105, 63)
                                                    else Color.Gray
                                                )
                                            },
                                            onClick = {
                                                bookListViewModel.onAction(
                                                    BookListAction.OnBookListBookmarkClick(!bookListState.isSortedByFavorite)
                                                )
                                                expanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Filter") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = ImageVector.vectorResource(R.drawable.ic_filter),
                                                    contentDescription = "Filter Icon",
                                                    tint = if (isSystemInDarkTheme())
                                                        Color(255, 250, 160)
                                                    else
                                                        Color(131, 105, 83),
                                                )
                                            },
                                            onClick = {
                                                scope.launch {
                                                    expanded = false
                                                    drawerState.open()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        if (bookListState.isOnDeleteBooks) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                IconButton(
                                    onClick = {
                                        bookListViewModel.onAction(
                                            BookListAction.OnConfirmDeleteBooks
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_confirm),
                                        contentDescription = "Confirm Delete Icon",
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        bookListViewModel.onAction(
                                            BookListAction.OnDeletingBooks(false)
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_cancel),
                                        contentDescription = "Cancel Delete Icon",
                                    )
                                }
                            }
                        }
                        Crossfade(targetState = bookListState.listViewType) { option ->
                            when (option) {
                                -1 -> {
                                    Box(modifier = Modifier.fillMaxSize())
                                }

                                0 -> {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier
                                            .fillMaxSize(),
                                    ) {
                                        items(
                                            items = bookListState.bookList.filter {
                                                it.title.contains(searchText, ignoreCase = true) ||
                                                        it.authors.joinToString(",")
                                                            .contains(searchText, ignoreCase = true)
                                            },
                                            key = { it.id }
                                        ) {
                                            ListBookView(
                                                book = it,
                                                bookListState = bookListState,
                                                onItemClick = {
                                                    if (!bookListState.isOnDeleteBooks) {
                                                        onAction(BookListAction.OnBookClick(it))
                                                    }
                                                },
                                                onItemLongClick = {
                                                    if (!bookListState.isOnDeleteBooks) {
                                                        bookListViewModel.onAction(
                                                            BookListAction.OnBookLongClick(it, true)
                                                        )
                                                        scope.launch {
                                                            sheetState.show()
                                                        }
                                                    }
                                                },
                                                onItemDoubleClick = {
                                                    if (!bookListState.isOnDeleteBooks) {
                                                        onAction(
                                                            BookListAction.OnViewBookDetailClick(
                                                                it
                                                            )
                                                        )
                                                    }
                                                },
                                                onItemStarClick = {
                                                    bookListViewModel.onAction(
                                                        BookListAction.OnBookBookmarkClick(it)
                                                    )
                                                },
                                                onItemCheckBoxClick = { isChecked, book ->
                                                    bookListViewModel.onAction(
                                                        BookListAction.OnBookCheckBoxClick(
                                                            isChecked,
                                                            book
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }

                                1 -> {
                                    LazyVerticalStaggeredGrid(
                                        columns = columnsStaggeredGridCount,
                                        verticalItemSpacing = 8.dp,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(all = 8.dp),
                                        state = gridState,
                                        content = {
                                            items(
                                                items = bookListState.bookList.filter {
                                                    it.title.contains(
                                                        searchText,
                                                        ignoreCase = true
                                                    ) ||
                                                            it.authors.joinToString(",")
                                                                .contains(
                                                                    searchText,
                                                                    ignoreCase = true
                                                                )
                                                },
                                                key = { it.id }
                                            ) {
                                                GridBookView(
                                                    book = it,
                                                    bookListState = bookListState,
                                                    onItemClick = {
                                                        if (!bookListState.isOnDeleteBooks) {
                                                            onAction(BookListAction.OnBookClick(it))
                                                        }
                                                    },
                                                    onItemLongClick = {
                                                        if (!bookListState.isOnDeleteBooks) {
                                                            bookListViewModel.onAction(
                                                                BookListAction.OnBookLongClick(
                                                                    it,
                                                                    true
                                                                )
                                                            )
                                                            scope.launch {
                                                                sheetState.show()
                                                            }
                                                        }
                                                    },
                                                    onItemDoubleClick = {
                                                        if (!bookListState.isOnDeleteBooks) {
                                                            onAction(
                                                                BookListAction.OnViewBookDetailClick(
                                                                    it
                                                                )
                                                            )
                                                        }
                                                    },
                                                    onItemStarClick = {
                                                        bookListViewModel.onAction(
                                                            BookListAction.OnBookBookmarkClick(it)
                                                        )
                                                    },
                                                    onItemCheckBoxClick = { isChecked, book ->
                                                        bookListViewModel.onAction(
                                                            BookListAction.OnBookCheckBoxClick(
                                                                isChecked,
                                                                book
                                                            )
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    if (bookListState.isOpenBottomSheet) {
                        BookMenuBottomSheet(
                            sheetState = sheetState,
                            state = bookListState,
                            onDismiss = {
                                bookListViewModel.onAction(BookListAction.OnBookLongClick(null, false))
                            },
                            onViewBookDetails = {
                                bookListViewModel.onAction(BookListAction.OnViewBookDetailClick(book = bookListState.selectedBook!!))
                                onAction(BookListAction.OnViewBookDetailClick(bookListState.selectedBook!!))
                            },
                            onDeleteBook = {
                                bookListViewModel.onAction(BookListAction.OnBookDeleteClick(book = bookListState.selectedBook!!))
                            }
                        )
                    }
                }
            }
        )
    }
    if (showDriveInputLinkDialog) {
        DriveInputLinkDialog(
            onDismiss = { showDriveInputLinkDialog = false },
            onConfirm = { link ->
                ImportBook(
                    context = context,
                    scope = scope,
                    specialIntent = "null"
                ).importBookViaGoogleDrive(link)
            }
        )
    }
    AnimatedVisibility(
        visible = showFab,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ExpandableFab(
            items = fabItems,
            expanded = fabExpanded,
            onToggle = { fabExpanded = !fabExpanded },
            onDismiss = { fabExpanded = false }
        )
    }
}