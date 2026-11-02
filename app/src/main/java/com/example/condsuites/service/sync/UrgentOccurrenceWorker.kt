package com.example.condsuites.service.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.condsuites.data.db.AppDatabase
import com.example.condsuites.data.preferences.UserPreferences
import com.example.condsuites.service.NotificationUtils

class UrgentOccurrenceWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            if (!UserPreferences.isGlobalNotificationsEnabled(applicationContext) ||
                !UserPreferences.isUrgentAlertsEnabled(applicationContext)
            ) {
                return Result.success()
            }

            val database = AppDatabase.getDatabase(applicationContext)
            val dao = database.appDao()

            val activeUserUsername = UserPreferences.getCurrentSessionUser(applicationContext)
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
                        context = applicationContext,
                        urgentCount = urgentOccurrences.size,
                        topUrgentTitle = firstOcc.title.ifBlank { "Sem título" },
                        occurrenceId = firstOcc.id
                    )
                } else {
                    NotificationUtils.cancelAppNotification(
                        applicationContext,
                        NotificationUtils.URGENT_NOTIFICATION_ID
                    )
                }
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
