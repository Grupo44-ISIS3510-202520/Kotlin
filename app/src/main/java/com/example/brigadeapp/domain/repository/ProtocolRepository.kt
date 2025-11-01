package com.example.brigadeapp.domain.repository

import com.example.brigadeapp.domain.entity.Protocol
import java.io.File

interface ProtocolRepository {
    suspend fun getAllProtocols(): List<Protocol>
    suspend fun getUpdatedProtocols(localVersions: Map<String, String>): List<Protocol>
    suspend fun readLocalVersions(): Map<String, String>
    suspend fun saveLocalVersions(versions: Map<String, String>)
    suspend fun getProtocolFile(protocol: Protocol): Result<File>

    fun getCacheSize(): Long
    suspend fun clearCache()
}