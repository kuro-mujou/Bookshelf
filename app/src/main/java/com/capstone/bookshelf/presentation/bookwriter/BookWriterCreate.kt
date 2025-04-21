package com.capstone.bookshelf.presentation.bookwriter

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.capstone.bookshelf.R
import com.capstone.bookshelf.data.mapper.toDataClass
import com.capstone.bookshelf.domain.wrapper.Book
import com.capstone.bookshelf.util.RationaleDialog
import com.capstone.bookshelf.util.SettingsRedirectDialog
import com.capstone.bookshelf.util.rememberMediaPermissionsState

@Composable
fun BookWriterCreate(
    viewModel: BookWriterViewModel,
    onNavigateToBookContent: (String, Book) -> Unit
) {
    var bookTitle by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf("") }
    var coverImageUriString by remember { mutableStateOf<String?>(null) }
    var isBookTitleError by remember { mutableStateOf(false) }
    var isAuthorNameError by remember { mutableStateOf(false) }

    var showPermissionRationale by remember { mutableStateOf(false) }
    var showSettingsRedirect by remember { mutableStateOf(false) }

    var rationaleLauncherInstance by remember {
        mutableStateOf<ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>?>(null)
    }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val bookId by viewModel.bookID.collectAsStateWithLifecycle()
    val book by viewModel.book.collectAsStateWithLifecycle()

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coverImageUriString = uri.toString()
        }
    }

    val primaryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val mediaPermissionState = rememberMediaPermissionsState(
        permission = primaryPermission,
        onPermissionsResult = { permissionsResult ->
            val primaryPermissionGranted = permissionsResult[primaryPermission] == true
            var userSelectedGranted = false
            if (primaryPermission == Manifest.permission.READ_MEDIA_IMAGES &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
            ) {
                userSelectedGranted =
                    permissionsResult[Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] == true
            }
            if (primaryPermissionGranted || userSelectedGranted) {
                pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        },
        onShowRationale = { launcher ->
            rationaleLauncherInstance = launcher
            showPermissionRationale = true
        },
        onPermanentlyDenied = {
            showSettingsRedirect = true
        }
    )

    fun requestImageAccess() {
        when {
            mediaPermissionState.hasPermission() || mediaPermissionState.hasPartialAccess() -> {
                pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            else -> {
                mediaPermissionState.launchPermissionRequest()
            }
        }
    }

    LaunchedEffect(bookId) {
        if (bookId.isNotEmpty()) {
            val bookData = book.toDataClass()
            if (true) {
                onNavigateToBookContent(bookId, bookData)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .systemBarsPadding()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { focusManager.clearFocus() }
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("ADD BOOK INFO", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = bookTitle,
                onValueChange = {
                    bookTitle = it
                    isBookTitleError = it.isBlank()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Book title *") },
                isError = isBookTitleError,
                supportingText = { if (isBookTitleError) Text("Book title cannot be empty") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true
            )

            OutlinedTextField(
                value = authorName,
                onValueChange = {
                    authorName = it
                    isAuthorNameError = it.isBlank()
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Author *") },
                isError = isAuthorNameError,
                supportingText = { if (isAuthorNameError) Text("Author name cannot be empty") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                singleLine = true
            )

            Text("ADD COVER IMAGE", style = MaterialTheme.typography.headlineSmall)

            if (coverImageUriString != null) {
                AsyncImage(
                    model = coverImageUriString,
                    contentDescription = "Selected cover image",
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .wrapContentSize()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .clickable { requestImageAccess() },
                )
            } else {
                Column(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .clickable { requestImageAccess() },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        modifier = Modifier.size(60.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_add_image),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Tap to add book cover",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                onClick = {
                    isBookTitleError = bookTitle.isBlank()
                    isAuthorNameError = authorName.isBlank()
                    if (!isBookTitleError && !isAuthorNameError) {
                        focusManager.clearFocus()
                        viewModel.onAction(
                            BookWriterAction.AddBookInfo(
                                context = context,
                                bookTitle = bookTitle.trim(),
                                authorName = authorName.trim(),
                                coverImagePath = coverImageUriString ?: "error"
                            )
                        )
                    }
                },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Next step")
                    Spacer(modifier = Modifier.size(8.dp))
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_send),
                        contentDescription = null
                    )
                }
            }
        }

        if (showPermissionRationale) {
            val currentRationaleLauncher = rationaleLauncherInstance
            if (currentRationaleLauncher != null) {
                RationaleDialog(
                    onConfirm = {
                        showPermissionRationale = false
                        rationaleLauncherInstance = null
                        val permissionsToRequest = mediaPermissionState.getPermissionsToRequest()
                        currentRationaleLauncher.launch(permissionsToRequest)
                    },
                    onDismiss = {
                        showPermissionRationale = false
                        rationaleLauncherInstance = null
                    }
                )
            } else {
                showPermissionRationale = false
            }
        }

        if (showSettingsRedirect) {
            SettingsRedirectDialog(
                onConfirm = {
                    showSettingsRedirect = false
                    mediaPermissionState.openAppSettings()
                },
                onDismiss = {
                    showSettingsRedirect = false
                }
            )
        }

    }
}