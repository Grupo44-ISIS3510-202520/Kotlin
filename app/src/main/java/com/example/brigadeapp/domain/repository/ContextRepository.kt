package com.example.brigadeapp.domain.repository

import kotlinx.coroutines.flow.Flow

/** Expone datos de contexto del dispositivo (ej. luz ambiental). */
interface ContextRepository {
    fun getLightLevel(): Flow<Float>
}
