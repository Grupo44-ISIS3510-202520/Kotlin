package com.example.brigadeapp.data.repository

import com.example.brigadeapp.sensors.LightSensorManagerImpl
import com.example.brigadeapp.domain.repository.ContextRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ContextRepositoryImpl @Inject constructor(
    private val lightSensorManager: LightSensorManagerImpl
) : ContextRepository {

    override fun getLightLevel(): Flow<Float> = lightSensorManager.readLightLevel()
}
