package com.example.brigadeapp.data.repository

import com.example.brigadeapp.domain.repository.ContextRepository
import com.example.brigadeapp.view.sensors.LightSensorManagerImpl
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ContextRepositoryImpl @Inject constructor(
    private val lightSensorManager: LightSensorManagerImpl
) : ContextRepository {

    override fun getLightLevel(): Flow<Float> = lightSensorManager.readLightLevel()
}
