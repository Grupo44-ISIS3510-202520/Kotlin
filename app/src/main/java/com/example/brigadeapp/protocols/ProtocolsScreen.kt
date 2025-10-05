@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.brigadeapp.protocols

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brigadeapp.R
import com.example.brigadeapp.presentation.viewmodel.ProtocolsViewModel
import com.example.brigadeapp.ui.common.StandardScreen

@Composable
fun ProtocolsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onClickItem: () -> Unit = {},
    viewModel: ProtocolsViewModel? = null
) {

    val lux: Float =
        viewModel?.lux?.collectAsState(initial = 0f)?.value ?: 12f

    val readingMode: Boolean =
        viewModel?.readingMode?.collectAsState(initial = false)?.value ?: true

    val updatedCount: Int =
        viewModel?.updatedCount?.collectAsState(initial = 0)?.value ?: 3

    LaunchedEffect(viewModel) {
        viewModel?.checkUpdates()
    }

    val items = listOf(
        UiItem("Fire Emergency", "Fire safety procedures", Color(0xFFFFE4E8), R.drawable.ic_fire),
        UiItem("Earthquake Emergency", "Earthquake safety measures", Color(0xFFFFF1D6), R.drawable.ic_earthquake),
        UiItem("Flood Emergency", "Flood response guidelines", Color(0xFFE6F4FF), R.drawable.ic_flood),
        UiItem("Medical Emergency", "Medical emergency protocols", Color(0xFFFFE6F2), R.drawable.ic_medical)
    )

    StandardScreen(title = "Protocols & Manuals", onBack = onBack) { inner ->
        Column(
            modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            if (readingMode) {
                ReadingModeBanner(lux)
                Spacer(Modifier.height(12.dp))
            }

            // Valor actual del sensor (debug)
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
                ProtocolsUpdatedBanner(updatedCount = updatedCount)
                Spacer(Modifier.height(12.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(items.size) { i ->
                    ProtocolCard(items[i], onClickItem, readingMode)
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
    val titleStyle = if (readingMode)
        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    else MaterialTheme.typography.titleMedium

    val subtitleColor = if (readingMode)
        MaterialTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            Column(Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = titleStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class UiItem(
    val title: String,
    val subtitle: String,
    val bg: Color,
    val iconRes: Int
)

@Preview(showBackground = true)
@Composable
private fun ProtocolsPreview() {
    MaterialTheme { ProtocolsScreen() }
}
