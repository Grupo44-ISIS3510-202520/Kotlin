package com.example.brigadeapp.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.*

data class LatLng(val lat: Double, val lng: Double)

const val CAMPUS_LAT = 4.6026783
const val CAMPUS_LNG = -74.0653568
const val CAMPUS_RADIUS_METERS = 250.0

interface LocationClient {
    suspend fun getLastLocation(): Location?
    fun distanceMeters(a: LatLng, b: LatLng): Double
    fun isInsideRadius(
        point: LatLng,
        center: LatLng = LatLng(CAMPUS_LAT, CAMPUS_LNG),
        radiusMeters: Double = CAMPUS_RADIUS_METERS
    ): Boolean
}

class FusedLocationClient(private val context: Context) : LocationClient {
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(context) }

    @SuppressLint("MissingPermission") // el caller debe pedir permisos en runtime
    override suspend fun getLastLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            fused.lastLocation
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        }

    // Haversine (puedes cambiar por Location.distanceBetween si prefieres)
    override fun distanceMeters(a: LatLng, b: LatLng): Double {
        val R = 6371000.0
        fun rad(d: Double) = d * PI / 180.0
        val dLat = rad(b.lat - a.lat)
        val dLon = rad(b.lng - a.lng)
        val lat1 = rad(a.lat)
        val lat2 = rad(b.lat)
        val sinDLat = sin(dLat / 2)
        val sinDLon = sin(dLon / 2)
        val h = sinDLat * sinDLat + cos(lat1) * cos(lat2) * sinDLon * sinDLon
        return 2 * R * atan2(sqrt(h), sqrt(1 - h))
    }

    override fun isInsideRadius(point: LatLng, center: LatLng, radiusMeters: Double): Boolean {
        return distanceMeters(point, center) <= radiusMeters
    }
}
