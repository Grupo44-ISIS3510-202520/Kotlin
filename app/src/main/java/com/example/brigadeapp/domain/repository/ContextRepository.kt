package com.example.brigadeapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface ContextRepository {
    fun getLightLevel(): Flow<Float>
}
