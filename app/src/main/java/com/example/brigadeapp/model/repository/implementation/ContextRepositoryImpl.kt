package com.example.brigadeapp.model.repository.implementation

import com.example.brigadeapp.model.repository.interfaces.ContextRepository
import com.example.brigadeapp.view.sensors.LightSensorManagerImpl
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ContextRepositoryImpl @Inject constructor(
    private val lightSensorManager: LightSensorManagerImpl
) : ContextRepository {

    override fun getLightLevel(): Flow<Float> = lightSensorManager.readLightLevel()
}
