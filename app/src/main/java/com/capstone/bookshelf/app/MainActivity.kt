package com.capstone.bookshelf.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController
import com.capstone.bookshelf.theme.BookShelfTheme
import com.capstone.bookshelf.util.ImportBook

@UnstableApi
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        handleIncomingFile(intent)
        setContent {
            BookShelfTheme {
                Surface {
                    val navController = rememberNavController()
                    val requestNotificationPermission =
                        rememberNotificationPermissionLauncher(this) {}
                    LaunchedEffect(Unit) {
                        requestNotificationPermission()
                    }
                    SetupNavGraph(
                        navController = navController
                    )
                }
            }
        }
        volumeControlStream = android.media.AudioManager.STREAM_MUSIC
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingFile(intent)
    }

    private fun handleIncomingFile(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            if (uri != null) {
                ImportBook(
                    context = this,
                    scope = this.lifecycleScope,
                    specialIntent = "null"
                ).processIntentUri(uri)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun rememberNotificationPermissionLauncher(
    context: Context,
    onPermissionGranted: () -> Unit
): () -> Unit {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            onPermissionGranted()
        }
    }
    return remember {
        {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                onPermissionGranted()
            } else {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}