@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.brigadeapp.view.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.brigadeapp.view.common.StandardScreen
import com.example.brigadeapp.viewmodel.screens.ProtocolsViewModel

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
    val protocols by viewModel.protocols.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkUpdates()
    }

    val itemsToShow = if (protocols.isNotEmpty()) {
        protocols.map {
            UiItem(
                title = it.name,
                subtitle = "Version ${it.version} • Updated ${it.lastUpdate}",
                bg = Color(0xFFE6F4FF),
                iconRes = R.drawable.ic_protocols,
                url = it.url
            )
        }
    } else listOf(
        UiItem("Fire Emergency", "Fire safety procedures", Color(0xFFFFE4E8), R.drawable.ic_fire, ""),
        UiItem("Earthquake Emergency", "Earthquake safety measures", Color(0xFFFFF1D6), R.drawable.ic_earthquake, ""),
        UiItem("Flood Emergency", "Flood response guidelines", Color(0xFFE6F4FF), R.drawable.ic_flood, ""),
        UiItem("Medical Emergency", "Medical emergency protocols", Color(0xFFFFE6F2), R.drawable.ic_medical, "")
    )

    val animatedBackground by animateColorAsState(
        targetValue = if (readingMode) Color(0xFF121212) else MaterialTheme.colorScheme.background,
        animationSpec = spring()
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedBackground)
            .animateContentSize()
    ) {
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

                Text(
                    text = "Lux: %.2f".format(lux),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (readingMode) Color(0xFFDADADA) else MaterialTheme.colorScheme.onSurfaceVariant
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
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (readingMode) Color(0xFF1E1E1E) else Color.Transparent,
                        unfocusedContainerColor = if (readingMode) Color(0xFF1E1E1E) else Color.Transparent,
                        cursorColor = if (readingMode) Color.White else MaterialTheme.colorScheme.primary,
                        focusedTextColor = if (readingMode) Color.White else MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = if (readingMode) Color(0xFFDADADA) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(Modifier.height(12.dp))

                if (updatedCount > 0) {
                    ProtocolsUpdatedBanner(updatedCount)
                    Spacer(Modifier.height(12.dp))
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(itemsToShow.size) { i ->
                        ProtocolCard(
                            item = itemsToShow[i],
                            readingMode = readingMode,
                            onClick = {
                                val url = itemsToShow[i].url
                                if (url.isNotEmpty()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
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
    val cardColor = if (readingMode) Color(0xFF1E1E1E) else MaterialTheme.colorScheme.surface
    val textColor = if (readingMode) Color(0xFFF5F5F5) else MaterialTheme.colorScheme.onSurface
    val subtitleColor = if (readingMode) Color(0xFFD1D1D1) else MaterialTheme.colorScheme.onSurfaceVariant
    val iconBg = if (readingMode) item.bg.copy(alpha = 0.3f) else item.bg

    val titleStyle = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.SemiBold,
        color = textColor
    )

    Surface(
        color = cardColor,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 3.dp,
        shadowElevation = if (readingMode) 0.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier
                .padding(14.dp)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = null,
                    tint = if (readingMode) Color(0xFFFFC107) else Color.Unspecified
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = titleStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = if (readingMode) Color(0xFFDADADA) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class UiItem(
    val title: String,
    val subtitle: String,
    val bg: Color,
    val iconRes: Int,
    val url: String
)

@Preview(showBackground = true)
@Composable
private fun ProtocolsPreview() {
    MaterialTheme { ProtocolsScreen() }
}
