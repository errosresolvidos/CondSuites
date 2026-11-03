package com.example.condsuites.service.sync

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.condsuites.data.db.AppDatabase
import com.example.condsuites.data.preferences.UserPreferences
import com.example.condsuites.service.NotificationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UrgentOccurrenceAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (UserPreferences.isGlobalNotificationsEnabled(context) &&
                    UserPreferences.isUrgentAlertsEnabled(context) &&
                    UrgentOccurrenceScheduler.isAlertAllowedNow(context)
                ) {
                    val db = AppDatabase.getDatabase(context)
                    val dao = db.appDao()

                    val activeUserUsername = UserPreferences.getCurrentSessionUser(context)
                    var shouldAlert = true
                    if (activeUserUsername.isNotBlank()) {
                        val user = dao.getUserByUsername(activeUserUsername)
                        if (user != null && user.role != "Síndico" && user.role != "ADMIN") {
                            shouldAlert = false
                        }
                    }

                    if (shouldAlert) {
                        val urgentOccurrences = dao.getUrgentUnresolvedOccurrences()
                        if (urgentOccurrences.isNotEmpty()) {
                            val firstOcc = urgentOccurrences.first()
                            NotificationUtils.showUrgentAlertNotification(
                                context = context,
                                urgentCount = urgentOccurrences.size,
                                topUrgentTitle = firstOcc.title.ifBlank { "Sem título" },
                                occurrenceId = firstOcc.id
                            )
                        } else {
                            NotificationUtils.cancelAppNotification(
                                context,
                                NotificationUtils.URGENT_NOTIFICATION_ID
                            )
                        }
                    }
                }
                // Schedule next alarm check
                UrgentOccurrenceScheduler.scheduleAlarm(context)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
