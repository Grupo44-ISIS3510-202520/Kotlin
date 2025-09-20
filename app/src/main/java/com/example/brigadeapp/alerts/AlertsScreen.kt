package com.example.brigadeapp.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brigadeapp.ui.common.StandardScreen

//colores
import com.example.brigadeapp.ui.theme.PastelPink
import com.example.brigadeapp.ui.theme.PastelPeach
import com.example.brigadeapp.ui.theme.PastelBlue
import com.example.brigadeapp.ui.theme.PastelRose

data class AlertItem(
    val title: String,
    val subtitle: String,
    val time: String,
    val severity: String,
    val color: Color
)

@Composable
fun AlertsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onClickAlert: () -> Unit = {}
) {

    val items = listOf(
        AlertItem("Fire near Building A", "Evacuate immediately", "10:32 AM", "High", PastelPink),
        AlertItem("Earthquake drill", "Starts in 5 minutes", "9:55 AM", "Info", PastelPeach),
        AlertItem("Flood warning", "Avoid basement areas", "Yesterday", "Medium", PastelBlue),
        AlertItem("First Aid Training", "Room 203", "Yesterday", "Info", PastelRose)
    )

    StandardScreen(title = "Notifications", onBack = onBack) { inner ->
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(items.size) { i ->
                AlertCard(items[i], onClickAlert)
            }
        }
    }
}

@Composable
private fun AlertCard(item: AlertItem, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxSize()
    ) {
        androidx.compose.foundation.layout.Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Warning,
                contentDescription = null,
                tint = Color(0xFF444444)
            )

            androidx.compose.foundation.layout.Spacer(Modifier.padding(horizontal = 14.dp))

            androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }


        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AlertsPreview() {
    MaterialTheme {
        AlertsScreen()
    }
}
