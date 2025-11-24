package com.example.brigadeapp.data.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LightSensorManagerImpl(private val context: Context) {

    private val sensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val lightSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    fun readLightLevel(): Flow<Float> = callbackFlow {
        if (lightSensor == null) {
            trySend(0f)
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                val lux = event?.values?.get(0) ?: 0f
                trySend(lux)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(
            listener,
            lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}