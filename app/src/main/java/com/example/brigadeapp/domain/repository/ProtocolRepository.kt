package com.example.brigadeapp.domain.repository

import com.example.brigadeapp.domain.entity.Protocol

interface ProtocolRepository {
    suspend fun getProtocols(): List<Protocol>
}
