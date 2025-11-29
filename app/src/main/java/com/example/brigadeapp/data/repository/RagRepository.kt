package com.example.brigadeapp.data.repository

import android.content.Context
import com.example.brigadeapp.R
import com.example.brigadeapp.data.local.RagCache
import com.example.brigadeapp.data.local.RagLocalStorage
import com.example.brigadeapp.domain.model.RagRequest
import com.example.brigadeapp.domain.model.RagResponse
import com.example.brigadeapp.domain.model.RagCacheEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.client.plugins.timeout
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RagRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: HttpClient,
    private val cache: RagCache,
    private val localStorage: RagLocalStorage
) {
    private val baseUrl: String by lazy {
        context.getString(R.string.rag_base_url)
    }

    private val apiKeyHeader: String by lazy {
        context.getString(R.string.rag_api_key_header)
    }

    private val apiKey: String by lazy {
        context.getString(R.string.rag_api_key)
    }

    companion object {
        private const val CHAT_ENDPOINT = "/chat"
        private const val TIMEOUT_MS = 30_000L
    }

    suspend fun initializeCache() = withContext(Dispatchers.IO) {
        try {
            val savedCache = localStorage.loadCache()
            savedCache.forEach { (_, entry) ->
                if (!entry.isExpired()) {
                    cache.put(entry.query, entry.answer, entry.sources)
                }
            }
            cache.cleanExpired()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getAnswer(query: String): Result<Pair<RagResponse, Boolean>> = withContext(Dispatchers.IO) {
        try {
            val cachedEntry = cache.get(query)
            if (cachedEntry != null) {
                val response = RagResponse(
                    answer = cachedEntry.answer,
                    sources = cachedEntry.sources
                )
                return@withContext Result.success(response to true)
            }

            val response: RagResponse = httpClient.post("$baseUrl$CHAT_ENDPOINT") {
                contentType(ContentType.Application.Json)
                header(apiKeyHeader, apiKey)

                timeout {
                    requestTimeoutMillis = TIMEOUT_MS
                    connectTimeoutMillis = TIMEOUT_MS
                    socketTimeoutMillis = TIMEOUT_MS
                }

                setBody(RagRequest(query = query))
            }.body()

            cache.put(query, response.answer, response.sources)
            localStorage.saveEntry(
                query,
                RagCacheEntry(
                    query = query,
                    answer = response.answer,
                    sources = response.sources
                )
            )

            Result.success(response to false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        cache.clear()
        localStorage.clearCache()
    }

    suspend fun getCacheSize(): Int = cache.size()

    suspend fun getCacheHistory(): List<RagCacheEntry> = withContext(Dispatchers.IO) {
        try {
            val cacheMap = localStorage.loadCache()
            cacheMap.values.filter { !it.isExpired() }.toList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}