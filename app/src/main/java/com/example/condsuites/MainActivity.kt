package com.example.condsuites

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.condsuites.data.db.AppDatabase
import com.example.condsuites.data.model.UserEntity
import com.example.condsuites.data.preferences.UserPreferences
import com.example.condsuites.service.FirestoreSyncManager
import com.example.condsuites.ui.AppMainLayout
import com.example.condsuites.ui.screens.LoginScreen
import com.example.condsuites.ui.theme.CondSuitesTheme
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CondSuitesTheme {
                CondSuitesApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CondSuitesApp() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val dao = db.appDao()
    val scope = rememberCoroutineScope()

    var currentUser by remember { mutableStateOf<UserEntity?>(null) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            UserPreferences.saveCurrentSessionUser(context, currentUser!!.username)
        } else {
            UserPreferences.saveCurrentSessionUser(context, "")
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
                    }
                }
            } catch (_: Exception) {}

            FirestoreSyncManager.startListening(context.applicationContext, dao, scope)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    if (currentUser == null) {
        LoginScreen(dao) { user -> currentUser = user }
    } else {
        AppMainLayout(
            dao = dao,
            currentUser = currentUser!!,
            onLogout = { currentUser = null },
            onUserSwitched = { currentUser = it },
            context = context,
            scope = scope
        )
    }
}
