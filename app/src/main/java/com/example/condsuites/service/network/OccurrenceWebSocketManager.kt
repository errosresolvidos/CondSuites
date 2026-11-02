package com.example.condsuites.service.network

import android.content.Context
import com.example.condsuites.data.preferences.UserPreferences
import com.example.condsuites.service.NotificationUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import com.example.condsuites.data.dao.AppDao
import com.example.condsuites.data.model.SyncStatus

object OccurrenceWebSocketManager {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    private fun getWebSocketUrl(context: Context): String {
        return "wss://api.condsuites.com/v1/ws" // Replace with actual URL
    }

    fun startListening(context: Context, dao: AppDao, scope: CoroutineScope) {
        if (webSocket != null) return

        val request = Request.Builder()
            .url(getWebSocketUrl(context))
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // Connection opened
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val action = json.optString("action", "NEW_MESSAGE") // e.g. "EDIT_OCCURRENCE", "NEW_MESSAGE"
                    val title = json.optString("title", "CondSuites")
                    val body = json.optString("body", "Nova atualização na ocorrência.")
                    val occurrenceIdStr = json.optString("occurrenceId", "").takeIf { it.isNotEmpty() }
                    val senderUsername = json.optString("senderUsername", "").takeIf { it.isNotEmpty() }

                    // Trigger a REST pull to fetch the updated occurrence data into Room DB
                    if (occurrenceIdStr != null) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val occId = occurrenceIdStr.toLong()
                                val existingOcc = dao.getOccurrenceEntityById(occId)
                                
                                // Fetch the updated occurrence from the backend
                                val updatedOcc = OccurrenceApiClient.getOccurrence(context, occId)
                                if (updatedOcc != null) {
                                    updatedOcc.syncStatus = SyncStatus.SYNCED.name
                                    dao.insertOccurrenceReplace(updatedOcc)
                                    
                                    val isCoreFieldChanged = existingOcc != null && (
                                        updatedOcc.title != existingOcc.title ||
                                        updatedOcc.apartment != existingOcc.apartment ||
                                        updatedOcc.status != existingOcc.status ||
                                        updatedOcc.isUrgent != existingOcc.isUrgent ||
                                        updatedOcc.type != existingOcc.type ||
                                        updatedOcc.occurrenceType != existingOcc.occurrenceType
                                    )

                                    // Check if it's an edit and we should notify
                                    if (action == "EDIT_OCCURRENCE" && isCoreFieldChanged && existingOcc != null) {
                                        val currentUsername = UserPreferences.getCurrentSessionUser(context)
                                        if (currentUsername.isNotEmpty() && !updatedOcc.createdByUsername.equals(currentUsername, ignoreCase = true)) {
                                            val currentUser = dao.getUserByUsername(currentUsername)
                                            if (currentUser != null && com.example.condsuites.data.model.canUserSeeOccurrence(updatedOcc, currentUser)) {
                                                val notifTitle = if (updatedOcc.isUrgent && !existingOcc.isUrgent) "🚨 Ocorrência URGENTE" else "Ocorrência Editada"
                                                val notifBody = if (updatedOcc.isUrgent && !existingOcc.isUrgent) {
                                                    "A ocorrência '${updatedOcc.title}' foi destacada como URGENTE."
                                                } else {
                                                    "A ocorrência '${updatedOcc.title}' foi atualizada."
                                                }
                                                NotificationUtils.showAppNotification(
                                                    context,
                                                    notifTitle,
                                                    notifBody,
                                                    occId.toInt(),
                                                    occId
                                                )
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    if (!UserPreferences.isGlobalNotificationsEnabled(context)) {
                        return
                    }

                    val currentUsername = UserPreferences.getCurrentSessionUser(context)
                    if (!senderUsername.isNullOrEmpty() && senderUsername.equals(currentUsername, ignoreCase = true)) {
                        return
                    }

                    if (currentUsername.isNotBlank()) {
                        val notificationsEnabled = UserPreferences.isNotificationsEnabledForUser(context, currentUsername)
                        if (!notificationsEnabled) {
                            return
                        }
                    }

                    val notifId = occurrenceIdStr?.toIntOrNull() ?: (title + body).hashCode()
                    NotificationUtils.showAppNotification(context, title, body, notifId, occurrenceIdStr?.toLongOrNull())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                OccurrenceWebSocketManager.webSocket = null
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                OccurrenceWebSocketManager.webSocket = null
                // Implement reconnect logic if necessary
            }
        })
    }

    fun stopListening() {
        webSocket?.close(1000, "App closed")
        webSocket = null
    }
}