package com.example.condsuites.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPreferences {
    private const val PREFS_NAME = "session_prefs"
    private const val KEY_CURRENT_USER = "current_session_username"
    private const val KEY_NOTIF_ADMIN_SAMUEL = "notifications_enabled_admin_samuel"
    private const val KEY_GLOBAL_NOTIFICATIONS_ENABLED = "global_notifications_enabled"
    private const val KEY_URGENT_ALERTS_ENABLED = "urgent_alerts_enabled"

    fun setGlobalNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_GLOBAL_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun isGlobalNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_GLOBAL_NOTIFICATIONS_ENABLED, true)
    }

    fun setUrgentAlertsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_URGENT_ALERTS_ENABLED, enabled).apply()
    }

    fun isUrgentAlertsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_URGENT_ALERTS_ENABLED, true)
    }

    fun saveCurrentSessionUser(context: Context, username: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CURRENT_USER, username).apply()
    }

    fun getCurrentSessionUser(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CURRENT_USER, "") ?: ""
    }

    fun setNotificationsEnabledAdminSamuel(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NOTIF_ADMIN_SAMUEL, enabled).apply()
    }

    fun isNotificationsEnabledAdminSamuel(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIF_ADMIN_SAMUEL, true)
    }

    fun setNotificationsEnabledForUser(context: Context, username: String, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("notifications_enabled_$username", enabled).apply()
    }

    fun isNotificationsEnabledForUser(context: Context, username: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = "notifications_enabled_$username"
        if (!prefs.contains(key) && (username.equals("admin", ignoreCase = true) || username.equals("samuel", ignoreCase = true))) {
            return prefs.getBoolean(KEY_NOTIF_ADMIN_SAMUEL, true)
        }
        return prefs.getBoolean(key, true)
    }

    private val REMEMBERED_USERNAMES = stringSetPreferencesKey("remembered_usernames")

    suspend fun saveUsername(context: Context, username: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[REMEMBERED_USERNAMES] ?: emptySet()
            prefs[REMEMBERED_USERNAMES] = current + username
        }
    }

    suspend fun savePassword(context: Context, username: String, password: String) {
        context.dataStore.edit { prefs ->
            val key = stringPreferencesKey("password_$username")
            prefs[key] = password
        }
    }

    suspend fun getPassword(context: Context, username: String): String? {
        return context.dataStore.data.map { prefs ->
            prefs[stringPreferencesKey("password_$username")]
        }.firstOrNull()
    }

    fun getRememberedUsernames(context: Context): Flow<Set<String>> {
        return context.dataStore.data.map { prefs ->
            prefs[REMEMBERED_USERNAMES] ?: emptySet()
        }
    }

    private const val KEY_LAST_REMEMBERED_USERNAME = "last_remembered_username"

    suspend fun saveLastRememberedUsername(context: Context, username: String) {
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey(KEY_LAST_REMEMBERED_USERNAME)] = username
        }
    }

    suspend fun getLastRememberedUsername(context: Context): String? {
        return context.dataStore.data.map { prefs ->
            prefs[stringPreferencesKey(KEY_LAST_REMEMBERED_USERNAME)]
        }.firstOrNull()
    }

    suspend fun removeUsername(context: Context, username: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[REMEMBERED_USERNAMES] ?: emptySet()
            prefs[REMEMBERED_USERNAMES] = current - username
            prefs.remove(stringPreferencesKey("password_$username"))
            if (prefs[stringPreferencesKey(KEY_LAST_REMEMBERED_USERNAME)] == username) {
                prefs.remove(stringPreferencesKey(KEY_LAST_REMEMBERED_USERNAME))
            }
        }
    }
}
