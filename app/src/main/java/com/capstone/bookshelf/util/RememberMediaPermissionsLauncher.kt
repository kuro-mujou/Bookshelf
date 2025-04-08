package com.capstone.bookshelf.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Composable state holder for managing media permissions (READ_MEDIA_IMAGES or READ_MEDIA_VIDEO)
 * and handling Android 14+ Selected Photos Access.
 *
 * @param permission The specific media permission to request (e.g., Manifest.permission.READ_MEDIA_IMAGES).
 * @param onPermissionsResult Callback invoked after the permission request completes.
 *        Provides a map of permissions to their granted status. You typically check
 *        READ_MEDIA_IMAGES/VIDEO and READ_MEDIA_VISUAL_USER_SELECTED here.
 * @param onShowRationale Optional callback to show a rationale dialog before re-requesting.
 *        Should return true if the dialog was shown (to prevent immediate re-request), false otherwise.
 * @param onPermanentlyDenied Optional callback invoked if the permission is permanently denied.
 *        Typically shows a dialog directing the user to settings.
 */
@Composable
fun rememberMediaPermissionsState(
    permission: String,
    onPermissionsResult: (Map<String, Boolean>) -> Unit,
    onShowRationale: @Composable (launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>) -> Unit = {},
    onPermanentlyDenied: @Composable (openSettings: () -> Unit) -> Unit = { }
): MediaPermissionsState {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }
    var showPermanentlyDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResultMap: Map<String, Boolean> ->
        showRationale = false
        showPermanentlyDenied = false
        onPermissionsResult(permissionsResultMap)
        val primaryPermissionGranted = permissionsResultMap[permission] == true
        if (!primaryPermissionGranted) {
            val requiresVisual = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                    (permission == Manifest.permission.READ_MEDIA_IMAGES || permission == Manifest.permission.READ_MEDIA_VIDEO)
            val visualSelectedGranted = if(requiresVisual) permissionsResultMap[Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] == true else false
            if (!visualSelectedGranted) {
                val activity = context.findActivity()
                if (activity != null && !activity.shouldShowRequestPermissionRationale(permission)) {
                    showPermanentlyDenied = true
                }
            }
        }
    }
    val state = remember(permissionLauncher) {
        MediaPermissionsState(
            context = context,
            permission = permission,
            launcher = permissionLauncher,
            setShouldShowRationale = { show -> showRationale = show },
            setShouldShowPermanentlyDenied = { show -> showPermanentlyDenied = show }
        )
    }
    if (showRationale) {
        onShowRationale(permissionLauncher)
    }
    if (showPermanentlyDenied) {
        onPermanentlyDenied { state.openAppSettings() }
    }
    return state
}

/**
 * State holder class for managing media permission requests.
 */
class MediaPermissionsState internal constructor(
    private val context: Context,
    internal val permission: String,
    internal val launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    internal val setShouldShowRationale: (Boolean) -> Unit,
    internal val setShouldShowPermanentlyDenied: (Boolean) -> Unit
) {

    /** Checks if the primary media permission is currently granted. */
    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if partial access (Selected Photos) is granted. Only relevant on API 34+.
     * Returns false if not on API 34+ or if the permission isn't IMAGES/VIDEO.
     */
    fun hasPartialAccess(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            (permission == Manifest.permission.READ_MEDIA_IMAGES || permission == Manifest.permission.READ_MEDIA_VIDEO)) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    /**
     * Launches the permission request flow.
     * It determines the necessary permissions based on the Android version.
     * Handles showing rationale if needed.
     */
    fun launchPermissionRequest() {
        setShouldShowRationale(false)
        setShouldShowPermanentlyDenied(false)

        val permissionsToRequest = getRequiredPermissions(permission)

        if (permissionsToRequest.isEmpty()) {
            return
        }

        val activity = context.findActivity()
        if (activity != null && activity.shouldShowRequestPermissionRationale(permission)) {
            setShouldShowRationale(true)
        } else {
            launcher.launch(permissionsToRequest)
        }
    }

    /** Opens the application's settings screen. */
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {

        }
    }

    /** Determines the actual permissions array to request based on SDK version. */
    private fun getRequiredPermissions(primaryPermission: String): Array<String> {
        return when {
            // Android 14+ and requesting IMAGES or VIDEO
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                    (primaryPermission == Manifest.permission.READ_MEDIA_IMAGES || primaryPermission == Manifest.permission.READ_MEDIA_VIDEO) ->
                arrayOf(primaryPermission, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)

            // Android 13 (or 14+ but not IMAGES/VIDEO)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    (primaryPermission == Manifest.permission.READ_MEDIA_IMAGES ||
                            primaryPermission == Manifest.permission.READ_MEDIA_VIDEO ||
                            primaryPermission == Manifest.permission.READ_MEDIA_AUDIO) ->
                arrayOf(primaryPermission)

            // Below Android 13 (Legacy Storage) - Check your minSdk/targetSdk
            primaryPermission == Manifest.permission.READ_EXTERNAL_STORAGE ->
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE) // Or include WRITE if needed and < API 29

            // Fallback for other permissions or unexpected cases
            else -> arrayOf(primaryPermission)
        }
    }
}

/**Helper function to get Activity from Context (might need refinement based on your context source)**/
fun Context.findActivity(): android.app.Activity? {
    var context = this
    while (context is android.content.ContextWrapper) {
        if (context is android.app.Activity) return context
        context = context.baseContext
    }
    return null
}
@Composable
fun RationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Needed") },
        text = { Text("This app needs access to your photos to allow you to select an image.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Grant Permission") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SettingsRedirectDialog(
    message: String = "Photo access was denied. Please enable it in App Settings to select images.",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Required") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Go to Settings") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}