package com.example.condsuites.service

import com.example.condsuites.data.preferences.UserPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CondSuitesFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "CondSuites"
        val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: "Nova ocorrência registrada."
        val senderUsername = remoteMessage.data["senderUsername"]
        val occurrenceIdStr = remoteMessage.data["occurrenceId"]

        val currentUsername = UserPreferences.getCurrentSessionUser(this)
        if (!senderUsername.isNullOrEmpty() && senderUsername == currentUsername) {
            return
        }

        val notifId = occurrenceIdStr?.toIntOrNull() ?: (title + body).hashCode()
        NotificationUtils.showAppNotification(this, title, body, notifId)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}
