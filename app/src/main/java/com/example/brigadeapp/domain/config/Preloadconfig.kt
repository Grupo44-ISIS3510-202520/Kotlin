package com.example.brigadeapp.domain.config

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreloadConfig @Inject constructor(
    @ApplicationContext private val context: Context,
    private val connectivityManager: ConnectivityManager
) {
    companion object {
        const val DEFAULT_ENABLED = true
        const val DEFAULT_WIFI_ONLY = true
        const val DEFAULT_MAX_PRELOAD_COUNT = 10
        const val DEFAULT_PRELOAD_DELAY_MS = 3000L
        const val DEFAULT_MIN_BATTERY_PERCENTAGE = 20
        const val DEFAULT_MIN_STORAGE_MB = 50L
    }

    var enabled: Boolean = DEFAULT_ENABLED
    var wifiOnly: Boolean = DEFAULT_WIFI_ONLY
    var maxPreloadCount: Int = DEFAULT_MAX_PRELOAD_COUNT
    var preloadDelayMs: Long = DEFAULT_PRELOAD_DELAY_MS
    var minBatteryPercentage: Int = DEFAULT_MIN_BATTERY_PERCENTAGE
    var minStorageMB: Long = DEFAULT_MIN_STORAGE_MB

    fun shouldPreload(): PreloadDecision {
        if (!enabled) {
            return PreloadDecision.Denied("Preload disabled in configuration")
        }

        val networkCheck = checkNetworkConditions()
        if (!networkCheck.allowed) {
            return PreloadDecision.Denied(networkCheck.reason)
        }

        val batteryCheck = checkBatteryConditions()
        if (!batteryCheck.allowed) {
            return PreloadDecision.Denied(batteryCheck.reason)
        }

        val storageCheck = checkStorageConditions()
        if (!storageCheck.allowed) {
            return PreloadDecision.Denied(storageCheck.reason)
        }

        return PreloadDecision.Allowed
    }

    private fun checkNetworkConditions(): ConditionCheck {
        val network = connectivityManager.activeNetwork
            ?: return ConditionCheck(false, "No internet connection")

        val capabilities = connectivityManager.getNetworkCapabilities(network)
            ?: return ConditionCheck(false, "No network capabilities")

        if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            return ConditionCheck(false, "No internet access")
        }

        if (wifiOnly) {
            val hasWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            if (!hasWifi) {
                return ConditionCheck(
                    false,
                    "Wi-Fi only mode enabled, currently on mobile data"
                )
            }
        }

        return ConditionCheck(true, "Connection available")
    }

    private fun checkBatteryConditions(): ConditionCheck {
        val batteryManager =
            context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                ?: return ConditionCheck(
                    true,
                    "Could not check battery, allowing preload"
                )

        val batteryLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } else {
            100
        }

        if (batteryLevel < minBatteryPercentage) {
            return ConditionCheck(
                false,
                "Low battery ($batteryLevel% < $minBatteryPercentage%)"
            )
        }

        return ConditionCheck(true, "Battery level is sufficient ($batteryLevel%)")
    }

    private fun checkStorageConditions(): ConditionCheck {
        return try {
            val stat = StatFs(context.cacheDir.absolutePath)
            val availableBytes =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    stat.availableBlocksLong * stat.blockSizeLong
                } else {
                    @Suppress("DEPRECATION")
                    stat.availableBlocks.toLong() * stat.blockSize.toLong()
                }

            val availableMB = availableBytes / (1024 * 1024)

            if (availableMB < minStorageMB) {
                return ConditionCheck(
                    false,
                    "Not enough storage (${availableMB}MB < ${minStorageMB}MB)"
                )
            }

            ConditionCheck(true, "Enough storage (${availableMB}MB)")
        } catch (e: Exception) {
            ConditionCheck(true, "Could not check storage, allowing preload")
        }
    }

    fun getDeviceInfo(): DeviceInfo {
        val batteryManager =
            context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

        val network = connectivityManager.activeNetwork
        val capabilities = network?.let { connectivityManager.getNetworkCapabilities(it) }

        val networkType = when {
            capabilities == null -> "No connection"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile data"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Unknown"
        }

        val batteryLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        } else {
            -1
        }

        val stat = StatFs(context.cacheDir.absolutePath)
        val availableStorageMB =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024)
            } else {
                @Suppress("DEPRECATION")
                (stat.availableBlocks.toLong() * stat.blockSize.toLong()) / (1024 * 1024)
            }

        return DeviceInfo(
            networkType = networkType,
            batteryLevel = batteryLevel,
            availableStorageMB = availableStorageMB,
            preloadEnabled = enabled,
            wifiOnlyMode = wifiOnly
        )
    }
}

sealed class PreloadDecision {
    object Allowed : PreloadDecision()
    data class Denied(val reason: String) : PreloadDecision()
}

private data class ConditionCheck(
    val allowed: Boolean,
    val reason: String
)

data class DeviceInfo(
    val networkType: String,
    val batteryLevel: Int,
    val availableStorageMB: Long,
    val preloadEnabled: Boolean,
    val wifiOnlyMode: Boolean
)
