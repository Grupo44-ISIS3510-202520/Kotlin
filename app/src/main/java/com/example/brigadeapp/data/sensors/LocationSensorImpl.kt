package com.example.brigadeapp.data.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.example.brigadeapp.domain.sensors.LocationSensorManager
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import com.google.android.gms.location.Priority


data class LatLng(val lat: Double, val lng: Double)

const val CAMPUS_LAT = 4.6026783
const val CAMPUS_LNG = -74.0653568
const val CAMPUS_RADIUS_METERS = 250.0

class LocationSensorImpl (private val context: Context) : LocationSensorManager {
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(context) }

    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            fused.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                null
            ).addOnSuccessListener { loc ->
                if (!cont.isCompleted) cont.resume(loc)
            }.addOnFailureListener {
                fused.lastLocation
                    .addOnSuccessListener { last -> if (!cont.isCompleted) cont.resume(last) }
                    .addOnFailureListener { if (!cont.isCompleted) cont.resume(null) }
            }
        }



    // Performance improvement: The code now extracts rad conversion constant to avoid repeated calculation
    private companion object {
        const val EARTH_RADIUS_METERS = 6371000.0
        const val DEG_TO_RAD = PI / 180.0
    }

    override fun distanceMeters(a: LatLng, b: LatLng): Double {
        // Performance improvement: The code now uses inline rad conversion to avoid function call overhead
        val dLat = (b.lat - a.lat) * DEG_TO_RAD
        val dLon = (b.lng - a.lng) * DEG_TO_RAD
        val lat1 = a.lat * DEG_TO_RAD
        val lat2 = b.lat * DEG_TO_RAD
        val sinDLat = sin(dLat / 2)
        val sinDLon = sin(dLon / 2)
        val h = sinDLat * sinDLat + cos(lat1) * cos(lat2) * sinDLon * sinDLon
        return 2 * EARTH_RADIUS_METERS * atan2(sqrt(h), sqrt(1 - h))
    }

    override fun isInsideRadius(point: LatLng, center: LatLng, radiusMeters: Double): Boolean {
        return distanceMeters(point, center) <= radiusMeters
    }
}
