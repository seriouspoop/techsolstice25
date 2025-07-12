package com.example.financetracker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.financetracker.service.SmsProcessingService
import com.example.financetracker.ui.TransactionListScreen
import com.example.financetracker.ui.theme.FinanceTrackerTheme
import com.example.financetracker.util.Constants
import com.example.financetracker.util.Logger // Use your logger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.i("MainActivity onCreate") // Use your logger

        setContent {
            FinanceTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PermissionWrapper()
                }
            }
        }
    }
}

@Composable
fun PermissionWrapper() {
    val context = LocalContext.current
    var hasSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }
    // Notification permission needed for Android 13+ (API 33) for the foreground service notification
    val requiresNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (!requiresNotificationPermission) true // No need if below API 33
            else ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Combined permission state
    val hasAllPermissions = hasSmsPermission && hasNotificationPermission
    var permissionsRequested by remember { mutableStateOf(false) }

    // Create launchers for permissions
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            Logger.i("READ_SMS permission result: $isGranted")
            hasSmsPermission = isGranted
            permissionsRequested = true // Mark as requested even if denied
            if(isGranted && !hasNotificationPermission && requiresNotificationPermission) {
                // If SMS granted, now ask for Notification if needed
                // This sequential request is often better UX
            } else if (isGranted && hasNotificationPermission) {
                startSmsService(context)
            }
        }
    )

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            Logger.i("POST_NOTIFICATIONS permission result: $isGranted")
            hasNotificationPermission = isGranted
            permissionsRequested = true // Mark as requested
            if (isGranted && hasSmsPermission) {
                startSmsService(context)
            }
        }
    )

    // Effect to launch permission requests if needed when the composable enters composition
    LaunchedEffect(key1 = hasAllPermissions) {
        if (!hasAllPermissions && !permissionsRequested) {
            val permissionsToRequest = mutableListOf<String>()
            if (!hasSmsPermission) permissionsToRequest.add(Manifest.permission.READ_SMS)
            if (!hasNotificationPermission && requiresNotificationPermission) permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)

            // Request sequentially for better UX (SMS first)
            if (!hasSmsPermission) {
                Logger.i("Requesting READ_SMS permission.")
                smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
            } else if (!hasNotificationPermission && requiresNotificationPermission) {
                Logger.i("Requesting POST_NOTIFICATIONS permission.")
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            // If both already granted, start service directly
            else if (hasSmsPermission && hasNotificationPermission) {
                startSmsService(context)
            }
        } else if (hasAllPermissions) {
            // Permissions might have been granted *before* this composition
            startSmsService(context)
        }
    }


    // UI based on permission status
    if (hasAllPermissions) {
        Logger.d("All required permissions granted. Showing TransactionListScreen.")
        TransactionListScreen()
        // Optional: Trigger initial past SMS sync once after permissions granted
        LaunchedEffect(Unit) { // Run only once when permissions are confirmed granted
            triggerPastSmsSync(context)
        }
    } else {
        // Show rationale or permission request UI
        PermissionRationaleScreen(
            hasSmsPermission,
            hasNotificationPermission,
            requiresNotificationPermission,
            smsPermissionLauncher,
            notificationPermissionLauncher,
            context
        )
    }
}

@Composable
fun PermissionRationaleScreen(
    hasSmsPermission: Boolean,
    hasNotificationPermission: Boolean,
    requiresNotificationPermission: Boolean,
    smsLauncher: ManagedActivityResultLauncher<String, Boolean>,
    notificationLauncher: ManagedActivityResultLauncher<String, Boolean>,
    context: Context
) {
    var showSmsRationale by remember { mutableStateOf(false) }
    var showNotificationRationale by remember { mutableStateOf(false) }

    // Determine which permission is missing or needs rationale
    val missingSms = !hasSmsPermission
    val missingNotification = requiresNotificationPermission && !hasNotificationPermission

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Permissions Required",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (missingSms) {
            Text(
                "This app needs permission to read your SMS messages to automatically detect and track bank transactions. We only look for messages from known bank senders.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
            // You can add a check here for shouldShowRequestPermissionRationale if needed
            Button(onClick = {
                Logger.i("Requesting READ_SMS permission from rationale.")
                smsLauncher.launch(Manifest.permission.READ_SMS)
            }) {
                Text("Grant SMS Permission")
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (missingNotification) {
            Text(
                "On Android 13 and newer, permission is also needed to show a notification that the background service is running. This is required by Android.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = {
                Logger.i("Requesting POST_NOTIFICATIONS permission from rationale.")
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }) {
                Text("Grant Notification Permission")
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Button to open app settings if permissions permanently denied
        Button(onClick = { openAppSettings(context) }) {
            Text("Open App Settings")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Warning: Requesting SMS permission may cause issues with Google Play Store submission.",
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )

    }
}

// Helper function to start the service
fun startSmsService(context: Context) {
    Logger.i("Attempting to start SmsProcessingService.")
    val serviceIntent = Intent(context, SmsProcessingService::class.java).apply {
        action = Constants.ACTION_START_SERVICE // Indicate it's a startup call
    }
    try {
        ContextCompat.startForegroundService(context, serviceIntent)
        Logger.i("SmsProcessingService started successfully.")
    } catch (e: Exception) {
        Logger.e("Failed to start SmsProcessingService", e)
        // Handle error, maybe show a message to the user
    }
}

// Helper function to trigger the initial sync of past SMS
fun triggerPastSmsSync(context: Context) {
    Logger.i("Triggering initial sync of past SMS messages.")
    val serviceIntent = Intent(context, SmsProcessingService::class.java).apply {
        action = Constants.ACTION_PROCESS_PAST_SMS
    }
    try {
        ContextCompat.startForegroundService(context, serviceIntent)
        Logger.i("Intent sent to process past SMS.")
    } catch (e: Exception) {
        Logger.e("Failed to send intent for past SMS processing", e)
    }
}


// Helper function to open app settings
fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}
