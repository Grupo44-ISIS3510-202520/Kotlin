package com.example.brigadeapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.example.brigadeapp.domain.entity.FirebaseAuthClient
import com.example.brigadeapp.view.components.AppScaffold
import com.example.brigadeapp.view.screens.AuthNavHost
import com.example.brigadeapp.view.theme.BrigadeAppTheme
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Permiso de notificaciones concedido")
            setupFCM()
        } else {
            Log.w(TAG, "Permiso de notificaciones denegado")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicitar permisos y configurar FCM
        askNotificationPermission()

        setContent {
            BrigadeAppTheme {
                val auth = remember { FirebaseAuthClient() }
                val user = auth.authState.collectAsState(initial = auth.currentUser).value

                if (user == null) {
                    AuthNavHost(
                        onAuthCompleted = { /* no-op */ }
                    )
                } else {
                    AppScaffold(auth = auth)
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "Permiso ya concedido")
                    setupFCM()
                }
                else -> {
                    Log.d(TAG, "Solicitando permiso")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d(TAG, "Android < 13 - Permiso automático")
            setupFCM()
        }
    }

    private fun setupFCM() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Error obteniendo token FCM", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d(TAG, "Token FCM: $token")

            // Suscribirse al topic de alertas
            FirebaseMessaging.getInstance().subscribeToTopic("alerts")
                .addOnCompleteListener { subscribeTask ->
                    if (subscribeTask.isSuccessful) {
                        Log.d(TAG, "Suscrito al topic 'alerts'")
                    } else {
                        Log.e(TAG, "Error suscribiéndose", subscribeTask.exception)
                    }
                }
        }
    }
}