package com.example.brigadeapp.domain.repository

import com.example.brigadeapp.domain.entity.Protocol

interface ProtocolRepository {
    suspend fun getAllProtocols(): List<Protocol>
    suspend fun getUpdatedProtocols(localVersions: Map<String, String>): List<Protocol>
}