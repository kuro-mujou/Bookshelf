package com.capstone.bookshelf.util

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Composable state holder function for managing media permissions.
 * Creates and remembers a [MediaPermissionsState] instance.
 *
 * @param permission The specific media permission to request.
 * @param onPermissionsResult Callback invoked after the permission request completes.
 * @param onShowRationale Composable lambda invoked when rationale should be shown.
 *        It receives the launcher instance to allow re-requesting permission after confirmation.
 * @param onPermanentlyDenied Composable lambda invoked if the permission is permanently denied.
 *        It receives a function to open the app settings.
 * @return An instance of [MediaPermissionsState] to manage the permission flow.
 */
@Composable
fun rememberMediaPermissionsState(
    permission: String,
    onPermissionsResult: (Map<String, Boolean>) -> Unit,
    onShowRationale: @Composable (rationaleLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>) -> Unit,
    onPermanentlyDenied: @Composable (openSettings: () -> Unit) -> Unit
): MediaPermissionsState {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var vShowRationale by remember { mutableStateOf(false) }
    var vShowPermanentlyDenied by remember { mutableStateOf(false) }

    val activity = context.findActivity()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResultMap: Map<String, Boolean> ->
        vShowRationale = false
        vShowPermanentlyDenied = false

        onPermissionsResult(permissionsResultMap)

        val primaryPermissionGranted = permissionsResultMap[permission] == true
        val isApi34OrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
        val requiresVisual =
            isApi34OrAbove && (permission == Manifest.permission.READ_MEDIA_IMAGES || permission == Manifest.permission.READ_MEDIA_VIDEO)
        val visualSelectedGranted = if (requiresVisual) {
            permissionsResultMap[Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] == true
        } else {
            false
        }

        if (!primaryPermissionGranted && !visualSelectedGranted) {
            if (activity != null) {
                val shouldShowRationaleUI =
                    activity.shouldShowRequestPermissionRationale(permission)
                if (shouldShowRationaleUI) {
                    vShowRationale = true // Trigger the rationale UI callback
                } else {
                    vShowPermanentlyDenied = true
                }
            }
        }
    }

    val state = remember(permission, permissionLauncher, context) {
        MediaPermissionsState(
            context = context,
            permission = permission,
            launcher = permissionLauncher,
            setShouldShowRationaleTrigger = { show -> vShowRationale = show },
            setShouldShowPermanentlyDeniedTrigger = { show -> vShowPermanentlyDenied = show }
        )
    }

    DisposableEffect(vShowRationale, vShowPermanentlyDenied, lifecycleOwner) {
        onDispose { }
    }

    if (vShowRationale) {
        onShowRationale(permissionLauncher)
    }
    if (vShowPermanentlyDenied) {
        onPermanentlyDenied { state.openAppSettings() }
    }

    return state
}


/**
 * State holder class for managing media permission requests. Returned by [rememberMediaPermissionsState].
 * Provides functions to check permission status, launch requests, and open app settings.
 */
class MediaPermissionsState internal constructor(
    private val context: Context,
    internal val permission: String,
    internal val launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    internal val setShouldShowRationaleTrigger: (Boolean) -> Unit,
    internal val setShouldShowPermanentlyDeniedTrigger: (Boolean) -> Unit
) {

    /** Checks if the primary media permission is currently granted. */
    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** Checks if partial access (Selected Photos on API 34+) is granted. */
    fun hasPartialAccess(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            (permission == Manifest.permission.READ_MEDIA_IMAGES || permission == Manifest.permission.READ_MEDIA_VIDEO)
        ) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            ) == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    /**
     * Launches the permission request flow, checking for rationale *before* launching
     * the system prompt. Call this for the initial user action (e.g., clicking a button).
     */
    fun launchPermissionRequest() {
        setShouldShowRationaleTrigger(false)
        setShouldShowPermanentlyDeniedTrigger(false)

        val permissionsToRequest = getRequiredPermissions(permission)

        if (permissionsToRequest.isEmpty()) {
            return
        }

        val activity = context.findActivity()

        if (activity != null && activity.shouldShowRequestPermissionRationale(permission)) {
            setShouldShowRationaleTrigger(true)
        } else {
            launcher.launch(permissionsToRequest)
        }
    }

    /** Opens the application's settings screen. */
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    /**
     * Gets the specific array of permission strings that should be requested
     * based on the primary permission and the current Android SDK version.
     */
    fun getPermissionsToRequest(): Array<String> {
        return getRequiredPermissions(permission)
    }

    /** Determines the actual permissions array to request based on SDK version. */
    private fun getRequiredPermissions(primaryPermission: String): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                    (primaryPermission == Manifest.permission.READ_MEDIA_IMAGES || primaryPermission == Manifest.permission.READ_MEDIA_VIDEO) -> {
                arrayOf(primaryPermission, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    (primaryPermission == Manifest.permission.READ_MEDIA_IMAGES ||
                            primaryPermission == Manifest.permission.READ_MEDIA_VIDEO ||
                            primaryPermission == Manifest.permission.READ_MEDIA_AUDIO) -> {
                arrayOf(primaryPermission)
            }

            primaryPermission == Manifest.permission.READ_EXTERNAL_STORAGE && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            else -> {
                arrayOf(primaryPermission)
            }
        }
    }
}

/** Helper function to find the Activity from a Context. */
fun Context.findActivity(): ComponentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    return null
}

/** Standard Composable for displaying a permission rationale dialog. */
@Composable
fun RationaleDialog(
    title: String = "Permission Needed",
    text: String = "To select a cover image, this app needs access to your photos. Please grant the permission when prompted.",
    confirmButtonText: String = "Continue",
    dismissButtonText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmButtonText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissButtonText) }
        }
    )
}

/** Standard Composable for displaying a dialog guiding the user to app settings. */
@Composable
fun SettingsRedirectDialog(
    title: String = "Permission Required",
    text: String = "Access to photos was denied. To select a cover image, please enable the Photos permission for this app in your device Settings.",
    confirmButtonText: String = "Go to Settings",
    dismissButtonText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmButtonText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissButtonText) }
        }
    )
}