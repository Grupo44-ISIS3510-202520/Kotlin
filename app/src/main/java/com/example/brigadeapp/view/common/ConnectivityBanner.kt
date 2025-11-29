package com.example.brigadeapp.view.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.Circle

@Composable
fun ConnectivityBanner(
    isOnline: Boolean
) {
    val backgroundColor = if (isOnline) {
        Color(0xFFE4EEE5)
    } else {
        MaterialTheme.colorScheme.errorContainer
    }

    val iconColor = if (isOnline) {
        Color(0xFF378D3D)
    } else {
        Color(0xFFF44336)
    }

    val icon = if (isOnline) {
        Icons.Default.Wifi
    } else {
        Icons.Default.SignalWifiOff
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .background(color = backgroundColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (isOnline) "Conectado" else "Sin conexión",
            tint = iconColor
        )
    }
}