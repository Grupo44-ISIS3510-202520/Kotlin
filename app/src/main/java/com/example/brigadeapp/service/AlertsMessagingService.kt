package com.example.brigadeapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
        const val ALERTS_CHANNEL_NAME = "Alertas de Emergencia"
        const val TAG = "AlertsMessagingSvc"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "MENSAJE RECIBIDO")
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Notification title: ${remoteMessage.notification?.title}")
        Log.d(TAG, "Notification body: ${remoteMessage.notification?.body}")
        Log.d(TAG, "Data: ${remoteMessage.data}")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        // Obtener título y mensaje
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Nueva Alerta"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["message"]
            ?: "Hay una nueva alerta de emergencia"

        val alertId = remoteMessage.data["alertId"] ?: ""
        val type = remoteMessage.data["type"] ?: "info"

        Log.d(TAG, "📝 Procesando - Título: $title, Body: $body")

        // Mostrar notificación
        sendNotification(title, body, alertId, type)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, " Nuevo token FCM: $token")
    }

    private fun sendNotification(
        title: String,
        messageBody: String,
        alertId: String,
        type: String
    ) {
        Log.d(TAG, "Creando notificación...")

        createNotificationChannel()

        // Intent para abrir la app al hacer clic
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

        // Construir notificación
        val notificationBuilder = NotificationCompat.Builder(this, ALERTS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_protocols)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = Random.nextInt()

        notificationManager.notify(notificationId, notificationBuilder.build())

        Log.d(TAG, "Notificación mostrada con ID: $notificationId")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ALERTS_CHANNEL_ID,
                ALERTS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de alertas de emergencia"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
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

            Log.d(TAG, "Canal de notificación creado")
        }
    }
}