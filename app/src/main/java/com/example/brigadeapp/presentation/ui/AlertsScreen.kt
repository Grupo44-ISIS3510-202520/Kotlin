@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.brigadeapp.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.brigadeapp.R

data class AlertItem(
    val iconRes: Int,
    val iconBg: Color,
    val title: String,
    val subtitle: String,
    val time: String
)

@Composable
fun AlertsScreen(
    modifier: Modifier = Modifier,
    onMenu: () -> Unit = {},
    onClickAlert: (AlertItem) -> Unit = {}
) {
    val items = listOf(
        AlertItem(R.drawable.ic_alert,    Color(0xFFFFE2E1), "Fire Alarm - Building A", "Critical Alert", "Now"),
        AlertItem(R.drawable.ic_medical,  Color(0xFFEFF2F6), "Injured Student",        "Medical Emergency", "5 min ago"),
        AlertItem(R.drawable.ic_security, Color(0xFFEFF2F6), "Suspicious Activity",     "Security Alert", "30 min ago"),
        AlertItem(R.drawable.ic_training, Color(0xFFEFF2F6), "Upcoming Training",       "Reminder", "1 hr ago"),
        AlertItem(R.drawable.ic_campus,   Color(0xFFEFF2F6), "Campus Closure",          "General Announcement", "2 hrs ago"),
    )

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
            items(items.size) { i ->
                NotificationCard(item = items[i], onClick = { onClickAlert(items[i]) })
            }
        }
    }
}

@Composable
private fun NotificationCard(item: AlertItem, onClick: () -> Unit) {
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
                    .background(item.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = null,
                    tint = Color.Unspecified // respeta colores del SVG
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Timestamp
            Text(
                text = item.time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
