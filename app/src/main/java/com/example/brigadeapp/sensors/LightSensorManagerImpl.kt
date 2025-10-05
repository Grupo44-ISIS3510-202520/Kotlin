package com.example.brigadeapp.sensors

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LightSensorManagerImpl(
    private val appContext: Context // usa applicationContext
) : LightSensorManager {

    private val sensorManager =
        appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val lightSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private fun isSupported(): Boolean {
        return appContext.packageManager
            .hasSystemFeature(PackageManager.FEATURE_SENSOR_LIGHT) && lightSensor != null
    }

    override fun getLightLevel(): Flow<Float> = callbackFlow {
        if (!isSupported()) {
            Log.w("LightSensor", "No ambient light sensor. Emitting 0f.")
            trySend(0f)
            awaitClose { /* nada que desregistrar */ }
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                val lux = event?.values?.firstOrNull()
                if (lux != null) {
                    Log.d("LightSensor", "Lux: $lux")
                    trySend(lux).isSuccess
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(
            listener,
            lightSensor,
            SensorManager.SENSOR_DELAY_UI // frecuencia razonable para UI
        )

        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
