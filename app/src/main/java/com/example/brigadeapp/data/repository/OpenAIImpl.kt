package com.example.brigadeapp.data.repository

import android.content.Context
import com.example.brigadeapp.data.services.OpenAIService
import com.example.brigadeapp.data.services.local.OpenAILocal
import com.example.brigadeapp.data.services.remote.OpenAIRemoteService
import com.example.brigadeapp.data.source.local.sensors.ConnectivityManagerObserver
import com.example.brigadeapp.domain.repository.OpenAIRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class OpenAIRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : OpenAIRepository {

    private val remoteService: OpenAIService by lazy { OpenAIRemoteService(context) }
    private val localService: OpenAILocal by lazy { OpenAILocal(context) }

    override suspend fun request(prompt: String): String {
        return try {
            val online = ConnectivityManagerObserver(context).observe().first()
            if (online) {
                val fetched = remoteService.request(prompt)
                if (fetched.isNotBlank()) {
                    localService.saveResponse(prompt, fetched)
                    fetched
                } else localService.request(prompt)
            } else {
                localService.request(prompt)
            }
        } catch (e: Exception) {
            // On error, fallback to local
            try {
                localService.request(prompt)
            } catch (s: Exception) {
                throw Exception("Error getting response: " + e.message + "\nFollows by\n" + s.message)
            }
        }
    }


    override suspend fun cachedResponse(prompt: String): String? {
        return try {
            val cached = localService.request(prompt)
            cached.ifBlank { null }
        } catch (e: Exception) {
            throw Exception("Error getting cached response: " + e.message)
        }
    }
}