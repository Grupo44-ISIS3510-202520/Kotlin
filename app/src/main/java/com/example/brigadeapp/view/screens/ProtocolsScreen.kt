@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.brigadeapp.view.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brigadeapp.R
import com.example.brigadeapp.domain.entity.Protocol
import com.example.brigadeapp.viewmodel.screens.ProtocolsViewModel
import com.example.brigadeapp.view.common.StandardScreen

@Composable
fun ProtocolsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: ProtocolsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val lux by viewModel.lux.collectAsState()
    val readingMode by viewModel.readingMode.collectAsState()
    val updatedCount by viewModel.updatedCount.collectAsState()
    val updatedProtocols by viewModel.updatedProtocols.collectAsState()
    val allProtocols by viewModel.protocols.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProtocolsAndCheckUpdates()
    }

    val screenBackgroundColor = if (readingMode) {
        MaterialTheme.colorScheme.background

    } else {
        Color(0xFFFBF8F2) // Color sepia
    }

    StandardScreen(title = "Protocols & Manuals", onBack = onBack) { inner ->
        Column(
            modifier
                .padding(inner)
                .fillMaxSize()
                .background(screenBackgroundColor)
                .padding(horizontal = 16.dp)
        ) {
            // Lógica para modo lectura
            if (readingMode) {
                ReadingModeBanner(lux)
                Spacer(Modifier.height(12.dp))
            } else {
                Text(
                    text = "Lux: %.2f".format(lux),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Search protocols...") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(Modifier.height(12.dp))
                if (updatedCount > 0) {
                    ProtocolsUpdatedBanner(updatedCount)
                    Spacer(Modifier.height(12.dp))
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(allProtocols.size) { i ->
                    val item = allProtocols[i]
                    val isUpdated = updatedProtocols.any { it.name == item.name }

                    ProtocolCard(
                        item = UiItem(
                            title = item.name,
                            subtitle = "Version ${item.version}",
                            bg = protocolColor(item.name),
                            iconRes = protocolIcon(item.name),
                            url = item.url,
                            updated = isUpdated
                        ),
                        readingMode = readingMode,
                        onClick = {
                            viewModel.markProtocolAsRead(item.name)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadingModeBanner(lux: Float) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_visibility),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Reading mode enabled (Lux: %.1f)".format(lux),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProtocolsUpdatedBanner(updatedCount: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_protocols),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "$updatedCount protocol(s) have been updated since your last session.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}


@Composable
private fun ProtocolCard(
    item: UiItem,
    onClick: () -> Unit,
    readingMode: Boolean
) {
    val titleStyle = if (readingMode) {
        MaterialTheme.typography.titleLarge
    } else {
        MaterialTheme.typography.titleMedium
    }
    val subtitleStyle = if (readingMode) {
        MaterialTheme.typography.bodyLarge
    } else {
        MaterialTheme.typography.bodyMedium
    }
    val subtitleColor = if (readingMode) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val cardBackgroundColor = if (readingMode) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.surface
    }
    val cardElevation = if (readingMode) 0.dp else 1.dp
    val cardModifier = if (readingMode) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            )
            .padding(vertical = 8.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = cardElevation,
        color = cardBackgroundColor,
        modifier = cardModifier
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.updated) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Updated",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
            }

            if (!readingMode) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(item.bg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
                Spacer(Modifier.width(14.dp))
            }

            Column(Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = titleStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    item.subtitle,
                    style = subtitleStyle,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!readingMode) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class UiItem(
    val title: String,
    val subtitle: String,
    val bg: Color,
    val iconRes: Int,
    val url: String,
    val updated: Boolean
)

private fun protocolIcon(name: String): Int = when {
    name.contains("Fire", true) -> R.drawable.ic_fire
    name.contains("Earthquake", true) -> R.drawable.ic_earthquake
    name.contains("Flood", true) -> R.drawable.ic_flood
    name.contains("Medical", true) -> R.drawable.ic_medical
    else -> R.drawable.ic_protocols
}

private fun protocolColor(name: String): Color = when {
    name.contains("Fire", true) -> Color(0xFFFFE4E8)
    name.contains("Earthquake", true) -> Color(0xFFFFF1D6)
    name.contains("Flood", true) -> Color(0xFFE6F4FF)
    name.contains("Medical", true) -> Color(0xFFFFE6F2)
    else -> Color(0xFFEFEFEF)
}

@Preview(showBackground = true)
@Composable
private fun ProtocolsPreview() {
    MaterialTheme { ProtocolsScreen() }
}