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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

object NotificationUtils {
    fun cancelAppNotification(context: Context, notificationId: Int) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        } catch (_: Exception) {}
    }

    fun showAppNotification(context: Context, title: String, message: String, notificationId: Int = 0) {
        val channelId = "condsuites_notifications_channel"
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
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
        dao: AppDao?, 
        title: String, 
        body: String, 
        senderUsername: String? = null, 
        occurrenceId: Long? = null
    ) {
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
                      "content_available": true,
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
                        dao.insertNotificationLog(NotificationLogEntity(title = title, message = body, status = status))
                    } catch (_: Exception) {}
                }
            }
        }
    }
}
