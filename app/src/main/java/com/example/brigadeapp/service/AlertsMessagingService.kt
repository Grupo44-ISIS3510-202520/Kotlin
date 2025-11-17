package com.example.brigadeapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.brigadeapp.MainActivity
import com.example.brigadeapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class AlertsMessagingService : FirebaseMessagingService() {

    companion object {
        const val ALERTS_CHANNEL_ID = "alerts_channel"
        const val ALERTS_CHANNEL_NAME = "Emergency Alerts"
        const val TAG = "AlertsMessagingSvc"

        val VIBRATION_EMERGENCY = longArrayOf(0, 500, 200, 500, 200, 500, 200, 500)
        val VIBRATION_WARNING = longArrayOf(0, 300, 200, 300)
        val VIBRATION_INFO = longArrayOf(0, 250)

        val AMPLITUDE_EMERGENCY = intArrayOf(0, 255, 0, 255, 0, 255, 0, 255)
        val AMPLITUDE_WARNING = intArrayOf(0, 200, 0, 200)
        val AMPLITUDE_INFO = intArrayOf(0, 150)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "MESSAGE RECEIVED")
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Notification: ${remoteMessage.notification?.title}")
        Log.d(TAG, "Data: ${remoteMessage.data}")

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "New Alert"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["message"]
            ?: "There is a new emergency alert"

        val alertId = remoteMessage.data["alertId"] ?: ""
        val type = remoteMessage.data["type"] ?: "info"

        Log.d(TAG, "Type: $type | Title: $title")

        sendNotification(title, body, alertId, type)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
    }

    private fun sendNotification(
        title: String,
        messageBody: String,
        alertId: String,
        type: String
    ) {
        Log.d(TAG, "Creating notification type: $type")

        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("open_alerts", true)
            putExtra("alert_id", alertId)
            putExtra("alert_type", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            Random.nextInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (priority, color, vibrationPattern) = when (type.lowercase()) {
            "emergency" -> Triple(
                NotificationCompat.PRIORITY_MAX,
                android.graphics.Color.RED,
                VIBRATION_EMERGENCY
            )
            "warning" -> Triple(
                NotificationCompat.PRIORITY_HIGH,
                android.graphics.Color.rgb(255, 152, 0),
                VIBRATION_WARNING
            )
            else -> Triple(
                NotificationCompat.PRIORITY_DEFAULT,
                android.graphics.Color.BLUE,
                VIBRATION_INFO
            )
        }

        val notificationBuilder = NotificationCompat.Builder(this, ALERTS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_protocols)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(vibrationPattern)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .setColor(color)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)

        if (type.lowercase() == "emergency") {
            triggerEmergencyVibration()
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = Random.nextInt()

        notificationManager.notify(notificationId, notificationBuilder.build())

        Log.d(TAG, "Notification shown ID: $notificationId (Type: $type)")
    }

    private fun triggerEmergencyVibration() {
        try {
            Log.d(TAG, "Activating emergency vibration...")

            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (!vibrator.hasVibrator()) {
                Log.w(TAG, "This device does not have a vibrator")
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(
                    VIBRATION_EMERGENCY,
                    AMPLITUDE_EMERGENCY,
                    -1
                )
                vibrator.vibrate(effect)
                Log.d(TAG, "Vibration activated (Android 8+)")
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(VIBRATION_EMERGENCY, -1)
                Log.d(TAG, "Vibration activated (Android < 8)")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error activating vibration", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ALERTS_CHANNEL_ID,
                ALERTS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for emergency alerts"
                enableVibration(true)
                vibrationPattern = VIBRATION_EMERGENCY
                setShowBadge(true)
                enableLights(true)
                lightColor = android.graphics.Color.RED
                setSound(
                    android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                    null
                )
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d(TAG, "Notification channel created")
        }
    }
}