package com.example.brigadeapp.data.local

import com.example.brigadeapp.domain.model.RagCacheEntry
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RagCache @Inject constructor() {

    private val mutex = Mutex()

    private val cache = mutableMapOf<String, RagCacheEntry>()

    companion object {
        private const val MAX_CACHE_SIZE = 50
    }


    private fun normalizeQuery(query: String): String {
        return query.trim()
            .lowercase()
            .replace(Regex("\\s+"), " ")
    }


    suspend fun get(query: String): RagCacheEntry? = mutex.withLock {
        val normalizedQuery = normalizeQuery(query)
        val entry = cache[normalizedQuery]

        return@withLock if (entry != null && !entry.isExpired()) {
            entry
        } else {
            if (entry != null) {
                cache.remove(normalizedQuery)
            }
            null
        }
    }

    suspend fun put(query: String, answer: String, sources: List<String>) = mutex.withLock {
        val normalizedQuery = normalizeQuery(query)

        if (cache.size >= MAX_CACHE_SIZE) {
            val oldestKey = cache.minByOrNull { it.value.timestamp }?.key
            oldestKey?.let { cache.remove(it) }
        }

        cache[normalizedQuery] = RagCacheEntry(
            query = query,
            answer = answer,
            sources = sources,
            timestamp = System.currentTimeMillis()
        )
    }


    suspend fun clear() = mutex.withLock {
        cache.clear()
    }


    suspend fun cleanExpired() = mutex.withLock {
        val expiredKeys = cache.filter { it.value.isExpired() }.keys
        expiredKeys.forEach { cache.remove(it) }
    }


    suspend fun size(): Int = mutex.withLock {
        cache.size
    }
}