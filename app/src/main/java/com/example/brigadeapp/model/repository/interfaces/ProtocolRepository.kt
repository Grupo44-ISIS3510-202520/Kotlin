package com.example.brigadeapp.model.repository.interfaces

import com.example.brigadeapp.model.domain.Protocol

interface ProtocolRepository {
    suspend fun getProtocols(): List<Protocol>
}
