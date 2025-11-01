@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.brigadeapp.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brigadeapp.R
import com.example.brigadeapp.domain.entity.Alert
import com.example.brigadeapp.viewmodel.screens.AlertsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AlertsScreen(
    modifier: Modifier = Modifier,
    onMenu: () -> Unit = {},
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val alerts by viewModel.alerts.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onMenu) {
                        Icon(imageVector = Icons.Outlined.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
        ) {
            // 2. Usamos la lista 'alerts' del ViewModel
            items(alerts) { alert ->
                NotificationCard(
                    alert = alert,
                    onClick = { /* Lógica de clic aquí */ }
                )
            }
        }
    }
}

@Composable
private fun NotificationCard(alert: Alert, onClick: () -> Unit) {
    val iconRes = alertIcon(alert.type)
    val iconBg = alertColor(alert.type)
    val time = alert.timestamp.toFormattedString()

    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon “pill”
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = alert.title, // Título desde Firebase
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = alert.message, // Mensaje desde Firebase
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            // Timestamp
            Text(
                text = time, // Tiempo desde Firebase
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


private fun alertIcon(type: String): Int = when (type.lowercase()) {
    "emergency" -> R.drawable.ic_alert
    "medical" -> R.drawable.ic_medical
    "security" -> R.drawable.ic_security
    "info" -> R.drawable.ic_training
    else -> R.drawable.ic_campus
}

private fun alertColor(type: String): Color = when (type.lowercase()) {
    "emergency" -> Color(0xFFFFE2E1)
    "medical" -> Color(0xFFEFF2F6)
    "security" -> Color(0xFFEFF2F6)
    "info" -> Color(0xFFEFF2F6)
    else -> Color(0xFFEFF2F6)
}

private fun com.google.firebase.Timestamp?.toFormattedString(): String {
    if (this == null) return ""

    val millis = this.toDate().time
    val now = System.currentTimeMillis()
    val diff = now - millis

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    return when {
        minutes < 1 -> "Now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hr ago"
        else -> {
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            sdf.format(this.toDate())
        }
    }
}

