package com.example.brigadeapp.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class RagRequest(
    val query: String
)

@Serializable
data class RagResponse(
    val answer: String,
    val sources: List<String>
)

@Serializable
data class RagCacheEntry(
    val query: String,
    val answer: String,
    val sources: List<String>,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L
    }

    fun isExpired(): Boolean {
        return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS
    }
}

sealed class RagState {
    object Idle : RagState()
    object Loading : RagState()
    data class Success(
        val answer: String,
        val sources: List<String>,
        val fromCache: Boolean = false
    ) : RagState()
    data class Error(val message: String) : RagState()
}