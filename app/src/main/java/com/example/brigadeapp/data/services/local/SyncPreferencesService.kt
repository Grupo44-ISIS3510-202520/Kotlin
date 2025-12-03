package com.example.brigadeapp.data.services.local

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class SyncPreferencesService @Inject constructor(
    private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
    }

    fun saveLastSyncTime(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_SYNC, timestamp).apply()
    }

    fun getLastSyncTime(): Long {
        return prefs.getLong(KEY_LAST_SYNC, 0L)
    }

    fun saveLastOpenAIResponseTime(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_OPENAI, timestamp).apply()
    }

    fun getLastOpenAIResponseTime(): Long {
        return prefs.getLong(KEY_LAST_OPENAI, 0L)
    }

    companion object {
        private const val KEY_LAST_SYNC = "last_sync_time"
        private const val KEY_LAST_OPENAI = "last_openai_response_time"
    }
}
