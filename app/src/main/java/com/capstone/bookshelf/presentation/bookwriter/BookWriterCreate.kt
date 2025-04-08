package com.capstone.bookshelf.presentation.bookwriter

// ... other imports ...
// ... other imports ...
import android.Manifest
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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

private const val TAG = "BookWriterCreate" // Screen specific tag

@Composable
fun BookWriterCreate(
    viewModel: BookWriterViewModel,
    onNavigateToBookContent: (String, Book) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        var isBookTitleError by remember { mutableStateOf(false) }
        var isAuthorNameError by remember { mutableStateOf(false) }
        var bookTitle by remember { mutableStateOf("") }
        var authorName by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        var coverImagePath by remember { mutableStateOf("") } // Store URI as String
        val context = LocalContext.current
        val focusManager = LocalFocusManager.current

        val bookId by viewModel.bookID.collectAsStateWithLifecycle()
        val book by viewModel.book.collectAsStateWithLifecycle()

        // --- State for Dialogs ---
        var showPermissionRationale by remember { mutableStateOf(false) }
        var showSettingsRedirect by remember { mutableStateOf(false) }

        // --- Modern Photo Picker Launcher ---
        val pickMediaLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia() // Use modern picker
        ) { uri: Uri? ->
            if (uri != null) {
                Log.d(TAG, "Photo Picker Selected URI: $uri")
                coverImagePath = uri.toString() // Store URI as string
            } else {
                Log.d(TAG, "Photo Picker: No media selected")
            }
        }

        // --- Permissions State Management ---
        val permissionsState = rememberMediaPermissionsState(
            permission = Manifest.permission.READ_MEDIA_IMAGES, // Request image permission
            onPermissionsResult = { permissionsResult ->
                Log.d(TAG, "Handling permission result: $permissionsResult")
                // Check results and launch picker if appropriate
                val imagesGranted = permissionsResult[Manifest.permission.READ_MEDIA_IMAGES] == true
                var userSelectedGranted = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    userSelectedGranted = permissionsResult[Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] == true
                }

                if (imagesGranted) {
                    Log.d(TAG, "READ_MEDIA_IMAGES granted (Full Access). Launching picker.")
                    pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } else if (userSelectedGranted) {
                    Log.d(TAG, "READ_MEDIA_VISUAL_USER_SELECTED granted (Partial Access). Launching picker.")
                    // Launch picker immediately after user grants partial access
                    pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } else {
                    Log.w(TAG, "Media permission denied.")
                    // The state holder will set showPermanentlyDenied if applicable
                }
            },
            onShowRationale = { launcher ->
                // Show your custom rationale dialog here
                RationaleDialog(
                    onConfirm = {
                        showPermissionRationale = false
                        Log.d(TAG, "Rationale confirmed, re-requesting permissions.")
                        launcher.launch(getRequiredPermissions(Manifest.permission.READ_MEDIA_IMAGES)) // Re-launch request
                    },
                    onDismiss = {
                        showPermissionRationale = false
                        Log.d(TAG, "Rationale dismissed.")
                    }
                )
            },
            onPermanentlyDenied = { openSettings ->
                // Show your custom settings redirect dialog here
                SettingsRedirectDialog(
                    onConfirm = {
                        showSettingsRedirect = false
                        Log.d(TAG, "Redirecting to App Settings.")
                        openSettings() // Call the function to open settings
                    },
                    onDismiss = {
                        showSettingsRedirect = false
                        Log.d(TAG, "Settings redirect dismissed.")
                    }
                )
            }
        )

        // Update composable dialog state based on permission state holder's flags
        // This part seems slightly redundant with the callbacks but ensures dialogs show/hide
        // Consider simplifying if the callbacks directly manage showing dialogs.
        // LaunchedEffect(permissionsState.shouldShowRationale) { showPermissionRationale = permissionsState.shouldShowRationale }
        // LaunchedEffect(permissionsState.shouldShowPermanentlyDenied) { showSettingsRedirect = permissionsState.shouldShowPermanentlyDenied }

        // Helper function to request permission or launch picker
        fun requestImage() {
            Log.d(TAG, "Requesting image...")
            if (permissionsState.hasPermission()) {
                Log.d(TAG, "Permission already granted. Launching picker.")
                pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else if (permissionsState.hasPartialAccess()) {
                Log.d(TAG, "Partial access exists. Launching picker.")
                pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                Log.d(TAG, "Permission not granted. Launching permission request.")
                permissionsState.launchPermissionRequest() // This handles rationale internally if needed
            }
        }


        // --- Navigation Effect ---
        LaunchedEffect(bookId) {
            if (bookId.isNotEmpty()) {
                onNavigateToBookContent(bookId, book.toDataClass())
            }
        }

        // --- UI Layout ---
        Column(
            // ... (modifier as before) ...
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { focusManager.clearFocus() }
                )
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically) // Added spacing
        ) {
            Text("ADD BOOK INFO", style = MaterialTheme.typography.titleLarge)

            // --- Text Fields (as before) ---
            OutlinedTextField(
                value = bookTitle,
                onValueChange = { bookTitle = it; isBookTitleError = it.isBlank() },
                isError = isBookTitleError, /* ... other params ... */
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                label = { Text("Book title") },
                supportingText = { if (isBookTitleError) Text("Book title cannot be empty") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )
            OutlinedTextField(
                value = authorName,
                onValueChange = { authorName = it; isAuthorNameError = it.isBlank() },
                isError = isAuthorNameError, /* ... other params ... */
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Author") },
                supportingText = { if (isAuthorNameError) Text("Author name cannot be empty") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Text("ADD COVER IMAGE", style = MaterialTheme.typography.titleLarge)

            // --- Image Display/Selection ---
            if (coverImagePath.isNotEmpty()) {
                AsyncImage(
                    model = coverImagePath, // Load directly from URI string
                    contentDescription = "Selected cover image",
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .height(300.dp)
                        .clickable { requestImage() } // Allow re-selecting
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        .clip(RoundedCornerShape(4.dp)),
                    // Consider adding error/placeholder for Coil
                )
            } else {
                // --- Placeholder/Button Row ---
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .height(300.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        .clickable { requestImage() }, // Click row to select
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            modifier = Modifier.size(50.dp),
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_add_image), // Use your icon
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Add book cover",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    // Removed the "OR" and default image for cleaner UI
                    // If you need a default placeholder, show it via AsyncImage's placeholder
                }
            }

            // --- Submit Button ---
            Button(
                modifier = Modifier.align(Alignment.End),
                onClick = {
                    isBookTitleError = bookTitle.isBlank() // Re-validate on click
                    isAuthorNameError = authorName.isBlank()
                    if (!isBookTitleError && !isAuthorNameError) {
                        viewModel.onAction(
                            BookWriterAction.AddBookInfo(
                                context = context,
                                bookTitle = bookTitle,
                                authorName = authorName,
                                coverImagePath = coverImagePath.ifEmpty { "error" } // Keep "error" marker if empty
                            )
                        )
                    }
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Next step")
                    Icon(
                        modifier = Modifier.padding(start = 8.dp).size(15.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_send), // Use your icon
                        contentDescription = null
                    )
                }
            }
        } // End Column

        // --- Show Permission Dialogs ---
        if (showPermissionRationale) {
            RationaleDialog(
                onConfirm = {
                    showPermissionRationale = false
                    Log.d(TAG, "Rationale confirmed, re-requesting permissions.")
                    permissionsState.launchPermissionRequest() // Re-launch request
                },
                onDismiss = {
                    showPermissionRationale = false
                    Log.d(TAG, "Rationale dismissed.")
                }
            )
        }
        if (showSettingsRedirect) {
            SettingsRedirectDialog(
                onConfirm = {
                    showSettingsRedirect = false
                    Log.d(TAG, "Redirecting to App Settings.")
                    permissionsState.openAppSettings() // Call the function to open settings
                },
                onDismiss = {
                    showSettingsRedirect = false
                    Log.d(TAG, "Settings redirect dismissed.")
                }
            )
        }

    } // End Surface
}

// Helper function to get required permissions array (can be local or in util)
private fun getRequiredPermissions(primaryPermission: String): Array<String> {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                (primaryPermission == Manifest.permission.READ_MEDIA_IMAGES || primaryPermission == Manifest.permission.READ_MEDIA_VIDEO) ->
            arrayOf(primaryPermission, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                (primaryPermission == Manifest.permission.READ_MEDIA_IMAGES ||
                        primaryPermission == Manifest.permission.READ_MEDIA_VIDEO ||
                        primaryPermission == Manifest.permission.READ_MEDIA_AUDIO) ->
            arrayOf(primaryPermission)
        primaryPermission == Manifest.permission.READ_EXTERNAL_STORAGE ->
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        else -> arrayOf(primaryPermission)
    }
}