package com.example.brigadeapp.domain.usecase

import com.example.brigadeapp.data.sensors.LatLng
import javax.inject.Inject

class IsInsideCampusUseCase @Inject constructor(
    private val locationClient: LocationSensorManager
) {
    suspend operator fun invoke(): Boolean {
        val lastLocation = locationClient.getLastLocation() ?: return false
        val current = LatLng(lastLocation.latitude, lastLocation.longitude)
        return locationClient.isInsideRadius(current)
    }
}