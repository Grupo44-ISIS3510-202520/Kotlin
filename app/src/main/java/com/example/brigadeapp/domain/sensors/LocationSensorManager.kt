package com.example.brigadeapp.domain.sensors

import android.location.Location
import com.example.brigadeapp.data.sensors.CAMPUS_LAT
import com.example.brigadeapp.data.sensors.CAMPUS_LNG
import com.example.brigadeapp.data.sensors.CAMPUS_RADIUS_METERS
import com.example.brigadeapp.data.sensors.LatLng

interface LocationSensorManager {
    suspend fun getLastLocation(): Location?
    fun distanceMeters(a: LatLng, b: LatLng): Double
    fun isInsideRadius(
        point: LatLng,
        center: LatLng = LatLng(CAMPUS_LAT, CAMPUS_LNG),
        radiusMeters: Double = CAMPUS_RADIUS_METERS
    ): Boolean
}