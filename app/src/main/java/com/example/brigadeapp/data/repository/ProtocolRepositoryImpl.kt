package com.example.brigadeapp.data.repository

import com.example.brigadeapp.domain.model.Protocol
import com.example.brigadeapp.domain.repository.ProtocolRepository

class ProtocolRepositoryImpl : ProtocolRepository {
    override suspend fun getUpdatedSince(sinceTs: Long): List<Protocol> {
        val now = System.currentTimeMillis()
        // DEMO: simular que 2 protocolos fueron actualizados
        val fakeUpdates = listOf(
            Protocol("fire", "Fire Emergency", now),
            Protocol("flood", "Flood Emergency", now)
        )
        return if (sinceTs < now) fakeUpdates else emptyList()
    }
}
