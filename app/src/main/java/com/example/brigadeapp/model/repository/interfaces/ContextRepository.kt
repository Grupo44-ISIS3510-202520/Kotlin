package com.example.brigadeapp.model.repository.interfaces

import kotlinx.coroutines.flow.Flow

interface ContextRepository {
    fun getLightLevel(): Flow<Float>
}
