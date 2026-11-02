package com.example.condsuites.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.condsuites.MainActivity
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.NotificationLogEntity
import com.example.condsuites.data.preferences.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

object NotificationUtils {
    private val recentNotifications = java.util.concurrent.ConcurrentHashMap<String, Long>()
    private val localActions = java.util.concurrent.ConcurrentHashMap<String, Long>()
    private const val DEDUPLICATION_WINDOW_MS = 5000L
    private const val LOCAL_ACTION_WINDOW_MS = 10000L

    fun recordLocalAction(occurrenceId: Long, actionType: String = "") {
        if (occurrenceId != 0L) {
            val key = "${occurrenceId}_$actionType"
            localActions[key] = System.currentTimeMillis()
        }
    }

    fun isRecentLocalAction(occurrenceId: Long, actionType: String = ""): Boolean {
        if (occurrenceId == 0L) return false
        val key = "${occurrenceId}_$actionType"
        val timestamp = localActions[key] ?: return false
        val isRecent = (System.currentTimeMillis() - timestamp) < LOCAL_ACTION_WINDOW_MS
        if (!isRecent) {
            localActions.remove(key)
        }
        return isRecent
    }

    const val URGENT_NOTIFICATION_ID = 99991

    fun cancelAppNotification(context: Context, notificationId: Int) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        } catch (_: Exception) {}
    }

    fun showUrgentAlertNotification(
        context: Context,
        urgentCount: Int,
        topUrgentTitle: String,
        occurrenceId: Long?
    ) {
        if (!UserPreferences.isGlobalNotificationsEnabled(context) || !UserPreferences.isUrgentAlertsEnabled(context)) {
            return
        }

        val channelId = "condsuites_urgent_alerts_channel"
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (occurrenceId != null && occurrenceId != 0L) {
                putExtra("occurrenceId", occurrenceId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            URGENT_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (urgentCount == 1) {
            "🚨 Ocorrência Urgente Pendente!"
        } else {
            "🚨 $urgentCount Ocorrências Urgentes Pendentes!"
        }

        val text = if (urgentCount == 1) {
            "Atenção Síndico: \"$topUrgentTitle\" requer sua atenção imediata."
        } else {
            "Atenção Síndico: \"$topUrgentTitle\" e mais ${urgentCount - 1} ocorrência(s) precisam de atenção."
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertas de Ocorrências Urgentes (Síndico)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Lembretes periódicos para ocorrências marcadas como urgentes"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(URGENT_NOTIFICATION_ID, notificationBuilder.build())
    }

    fun showAppNotification(context: Context, title: String, message: String, notificationId: Int = 0, occurrenceId: Long? = null) {
        val currentTime = System.currentTimeMillis()

        // Clean up expired cache items
        recentNotifications.entries.removeIf { currentTime - it.value > DEDUPLICATION_WINDOW_MS }
        localActions.entries.removeIf { currentTime - it.value > LOCAL_ACTION_WINDOW_MS }

        // Suppress notification if this device recently executed a local action for this occurrence
        if (occurrenceId != null && occurrenceId != 0L) {
            if (isRecentLocalAction(occurrenceId, "EDIT") || isRecentLocalAction(occurrenceId, "URGENT")) {
                android.util.Log.d("NotificationUtils", "Notification suppressed due to recent local edit/urgency action: $title")
                return
            }
        }

        // Deduplicate rapid identical notifications
        val dedupeKey = "${occurrenceId ?: 0L}_${title.trim()}_${message.trim()}"
        val lastTime = recentNotifications[dedupeKey]
        if (lastTime != null && (currentTime - lastTime) < DEDUPLICATION_WINDOW_MS) {
            android.util.Log.d("NotificationUtils", "Duplicate notification suppressed within ${DEDUPLICATION_WINDOW_MS}ms: $title")
            return
        }
        recentNotifications[dedupeKey] = currentTime
        val channelId = "condsuites_notifications_channel"
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (occurrenceId != null && occurrenceId != 0L) {
                putExtra("occurrenceId", occurrenceId)
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            context, if (notificationId != 0) notificationId else 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "CondSuites Notificações",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de Ocorrências do CondSuites"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val idToUse = if (notificationId != 0) notificationId else System.currentTimeMillis().toInt()
        notificationManager.notify(idToUse, notificationBuilder.build())
    }

    fun sendFcmPushNotification(
        context: Context,
        dao: AppDao?, 
        title: String, 
        body: String, 
        senderUsername: String? = null, 
        occurrenceId: Long? = null
    ) {
        if (!UserPreferences.isGlobalNotificationsEnabled(context)) {
            return
        }

        val currentUsername = UserPreferences.getCurrentSessionUser(context)
        if (currentUsername.isNotBlank()) {
            val enabled = UserPreferences.isNotificationsEnabledForUser(context, currentUsername)
            if (!enabled) {
                return
            }
        }
        if (!senderUsername.isNullOrEmpty() && senderUsername != currentUsername) {
            if (!UserPreferences.isNotificationsEnabledForUser(context, senderUsername)) {
                return
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            var status = "Falha"
            try {
                val safeTitle = title.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ")
                val safeBody = body.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ")
                val safeSender = (senderUsername ?: "").replace("\\", "\\\\").replace("\"", "\\\"")
                val safeOccId = (occurrenceId ?: 0L).toString()

                val url = URL("https://fcm.googleapis.com/fcm/send")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "key=BIMRZzMow5QjcqslRr5kSHKZXxjQ-uZYXwllsajDgy_jlU50vwO9fvMvoh8RQrjl-TGDpB6WjoHy4hUCtYcMXbQ")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val jsonInputString = """
                    {
                      "to": "/topics/occurrences",
                      "priority": "high",
                      "data": {
                        "title": "$safeTitle",
                        "body": "$safeBody",
                        "senderUsername": "$safeSender",
                        "occurrenceId": "$safeOccId"
                      }
                    }
                """.trimIndent()

                conn.outputStream.use { os ->
                    val input = jsonInputString.toByteArray(Charsets.UTF_8)
                    os.write(input, 0, input.size)
                }

                val responseCode = conn.responseCode
                status = if (responseCode == 200) "Sucesso (200 OK)" else "Falha (Código $responseCode)"
            } catch (e: Exception) {
                status = "Erro: ${e.localizedMessage ?: "Desconhecido"}"
            } finally {
                if (dao != null) {
                    try {
                        val logId = dao.insertNotificationLog(NotificationLogEntity(title = title, message = body, status = status))
                        FirestoreSyncManager.syncNotificationLog(NotificationLogEntity(id = logId, title = title, message = body, status = status))
                    } catch (_: Exception) {}
                }
            }
        }
    }
}
