package com.example.condsuites.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPreferences {
    private const val PREFS_NAME = "session_prefs"
    private const val KEY_CURRENT_USER = "current_session_username"

    fun saveCurrentSessionUser(context: Context, username: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CURRENT_USER, username).apply()
    }

    fun getCurrentSessionUser(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CURRENT_USER, "") ?: ""
    }

    private val REMEMBERED_USERNAMES = stringSetPreferencesKey("remembered_usernames")

    suspend fun saveUsername(context: Context, username: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[REMEMBERED_USERNAMES] ?: emptySet()
            prefs[REMEMBERED_USERNAMES] = current + username
        }
    }

    fun getRememberedUsernames(context: Context): Flow<Set<String>> {
        return context.dataStore.data.map { prefs ->
            prefs[REMEMBERED_USERNAMES] ?: emptySet()
        }
    }

    suspend fun removeUsername(context: Context, username: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[REMEMBERED_USERNAMES] ?: emptySet()
            prefs[REMEMBERED_USERNAMES] = current - username
        }
    }
}
