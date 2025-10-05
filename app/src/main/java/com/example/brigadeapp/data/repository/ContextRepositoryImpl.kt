package com.example.brigadeapp.data.repository

import com.example.brigadeapp.domain.repository.ContextRepository
import com.example.brigadeapp.sensors.LightSensorManager
import kotlinx.coroutines.flow.Flow

class ContextRepositoryImpl(
    private val lightSensorManager: LightSensorManager
) : ContextRepository {

    override fun getLightLevel(): Flow<Float> {
        return lightSensorManager.getLightLevel()
    }
}
