package com.example.condsuites

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.condsuites.data.db.AppDatabase
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.data.preferences.UserPreferences
import com.example.condsuites.service.CondSuitesBackgroundService
import com.example.condsuites.service.FirestoreSyncManager
import com.example.condsuites.service.NotificationUtils
import com.example.condsuites.service.sync.UrgentOccurrenceScheduler
import com.example.condsuites.ui.AppMainLayout
import com.example.condsuites.ui.screens.LoginScreen
import com.example.condsuites.ui.theme.CondSuitesTheme
import com.example.condsuites.utils.AppErrorHandler
import com.example.condsuites.utils.ErrorPopupDialog
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    var notificationOccurrenceId by mutableStateOf<Long?>(null)

    private fun extractOccurrenceId(intent: Intent?): Long? {
        if (intent == null) return null
        val longVal = intent.getLongExtra("occurrenceId", 0L).takeIf { it != 0L }
        if (longVal != null) return longVal
        val strVal = intent.getStringExtra("occurrenceId")
        return strVal?.toLongOrNull()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationOccurrenceId = extractOccurrenceId(intent)
        enableEdgeToEdge()
        setContent {
            CondSuitesTheme {
                CondSuitesApp(
                    occurrenceIdState = notificationOccurrenceId,
                    onOccurrenceConsumed = { notificationOccurrenceId = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val occId = extractOccurrenceId(intent)
        if (occId != null) {
            notificationOccurrenceId = occId
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CondSuitesApp(
    occurrenceIdState: Long? = null,
    onOccurrenceConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val dao = db.appDao()
    val scope = rememberCoroutineScope()

    var currentUser by remember { mutableStateOf<UserEntity?>(null) }
    var isCheckingSession by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val savedUsername = UserPreferences.getCurrentSessionUser(context)
        if (savedUsername.isNotBlank()) {
            val user = withContext(Dispatchers.IO) {
                dao.getUserByUsername(savedUsername)
            }
            if (user != null) {
                currentUser = user
            }
        }
        isCheckingSession = false
    }

    LaunchedEffect(currentUser, isCheckingSession) {
        if (!isCheckingSession) {
            if (currentUser != null) {
                UserPreferences.saveCurrentSessionUser(context, currentUser!!.username)
            } else {
                UserPreferences.saveCurrentSessionUser(context, "")
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        android.util.Log.d("PERMISSIONS", "POST_NOTIFICATIONS granted: $isGranted")
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        try {
            if (dao.getUserCount() == 0) {
                val admin = UserEntity(username = "admin", password = "admin", role = "ADMIN")
                dao.insertUser(admin)
                FirestoreSyncManager.syncUser(admin)
            }
            try {
                FirebaseMessaging.getInstance().subscribeToTopic("occurrences")
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        android.util.Log.d("FCM_TOKEN", "Current FCM Token: $token")
                        if (!token.isNullOrEmpty()) {
                            NotificationUtils.sendTokenToServer(token)
                        }
                    }
                }
            } catch (_: Exception) {}

            CondSuitesBackgroundService.startService(context)
            UrgentOccurrenceScheduler.scheduleUrgentAlerts(context)
            UrgentOccurrenceScheduler.triggerImmediateCheck(context)
            FirestoreSyncManager.startListening(context.applicationContext, dao, scope)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(Unit) {
        AppErrorHandler.init { currentUser }
    }

    if (isCheckingSession) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    } else if (currentUser == null) {
        LoginScreen(dao) { user -> currentUser = user }
    } else {
        AppMainLayout(
            dao = dao,
            currentUser = currentUser!!,
            onLogout = { currentUser = null },
            onUserSwitched = { currentUser = it },
            context = context,
            scope = scope,
            initialOccurrenceId = occurrenceIdState,
            onOccurrenceConsumed = onOccurrenceConsumed
        )
    }

    val currentError = AppErrorHandler.currentError
    if (currentError != null && (AppErrorHandler.isTargetUser(currentUser) || AppErrorHandler.isTargetUser(UserPreferences.getCurrentSessionUser(context)))) {
        ErrorPopupDialog(
            errorText = currentError,
            onDismiss = { AppErrorHandler.clearError() }
        )
    }
}
