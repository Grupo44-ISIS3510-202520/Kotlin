package com.example.brigadeapp.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.brigadeapp.domain.entity.CachedReport

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailBottomSheet(
    report: CachedReport,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(scrollState)
        ) {
            // Header Section
            Text(
                text = "Report Details",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "ID: ${report.reportId}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(20.dp))

            // Image
            if (report.imageUrl != null) {
                CachedImage(
                    imageUrl = report.imageUrl,
                    contentDescription = "Report image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(Modifier.height(20.dp))
            }

            // Type Section
            InfoSection(
                label = "Type",
                value = report.type
            )
            
            Spacer(Modifier.height(16.dp))

            // Description Section
            InfoSection(
                label = "Description",
                value = report.description
            )
            
            Spacer(Modifier.height(16.dp))

            // Place and Date & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoSectionWithIcon(
                    icon = Icons.Default.LocationOn,
                    label = "Place",
                    value = report.place,
                    modifier = Modifier.weight(1f)
                )
                
                InfoSectionWithIcon(
                    icon = Icons.Default.AccessTime,
                    label = "Date & Time",
                    value = formatTimestamp(report.timestamp),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(Modifier.height(16.dp))

            // Elapsed Time and Follow-up Report
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoSectionWithIcon(
                    icon = Icons.Default.Timer,
                    label = "Elapsed Time",
                    value = formatElapsedTime(report.elapsedTime),
                    modifier = Modifier.weight(1f)
                )
                
                InfoSectionWithIcon(
                    icon = Icons.Default.CheckCircle,
                    label = "Follow-up Report",
                    value = if (report.isFollowUp) "Yes" else "No",
                    modifier = Modifier.weight(1f)
                )
            }

            if (report.latitude != null && report.longitude != null) {
                Spacer(Modifier.height(16.dp))
                InfoSection(
                    label = "Coordinates",
                    value = "Lat: ${String.format("%.6f", report.latitude)}, Lng: ${String.format("%.6f", report.longitude)}"
                )
            }

            if (report.audioUrl != null) {
                Spacer(Modifier.height(16.dp))
                InfoSection(
                    label = "Audio",
                    value = "Available"
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun InfoSectionWithIcon(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun InfoSection(
    label: String,
    value: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatTimestamp(timestamp: String): String {
    return try {
        timestamp.replace("T", " • ").substringBefore(".")
    } catch (e: Exception) {
        throw IllegalArgumentException("Invalid timestamp format: $timestamp")
    }
}

private fun formatElapsedTime(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    
    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "$seconds seconds"
    }
}
