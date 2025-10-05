package com.example.brigadeapp.sensors

import kotlinx.coroutines.flow.Flow

/** Abstracción del sensor de luz (lux) como Flow<Float>. */
interface LightSensorManager {
    fun getLightLevel(): Flow<Float>
}
