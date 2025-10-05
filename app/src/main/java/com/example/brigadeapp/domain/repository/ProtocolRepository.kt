package com.example.brigadeapp.domain.repository

import com.example.brigadeapp.domain.model.Protocol

interface ProtocolRepository {
    suspend fun getUpdatedSince(sinceTs: Long): List<Protocol>
}
