package com.example.brigadeapp.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.brigadeapp.domain.model.RagCacheEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RagLocalStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "rag_cache_prefs"
        private const val KEY_CACHE_MAP = "cache_map"
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val gson = Gson()


    suspend fun saveCache(cacheMap: Map<String, RagCacheEntry>) = withContext(Dispatchers.IO) {
        try {
            val jsonString = gson.toJson(cacheMap)
            prefs.edit().putString(KEY_CACHE_MAP, jsonString).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadCache(): Map<String, RagCacheEntry> = withContext(Dispatchers.IO) {
        try {
            val jsonString = prefs.getString(KEY_CACHE_MAP, null) ?: return@withContext emptyMap()
            val type = object : TypeToken<Map<String, RagCacheEntry>>() {}.type
            gson.fromJson(jsonString, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }


    suspend fun clearCache() = withContext(Dispatchers.IO) {
        prefs.edit().remove(KEY_CACHE_MAP).apply()
    }


    suspend fun saveEntry(query: String, entry: RagCacheEntry) = withContext(Dispatchers.IO) {
        val currentCache = loadCache().toMutableMap()
        currentCache[query] = entry
        saveCache(currentCache)
    }
}