@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.brigadeapp.protocols

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Opacity
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brigadeapp.ui.common.StandardScreen

import com.example.brigadeapp.ui.theme.PastelPink
import com.example.brigadeapp.ui.theme.PastelPeach
import com.example.brigadeapp.ui.theme.PastelBlue
import com.example.brigadeapp.ui.theme.PastelRose

@Composable
fun ProtocolsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onClickItem: () -> Unit = {}
) {
    val items = listOf(
        UiItem("Fire Emergency",    "Fire safety procedures",    PastelPink,  Icons.Outlined.Whatshot),
        UiItem("Earthquake...",     "Earthquake safety...",      PastelPeach, Icons.Outlined.Warning),
        UiItem("Flood Emergency",   "Flood response...",         PastelBlue,  Icons.Outlined.Opacity),
        UiItem("Medical Emergency", "Medical emergency...",      PastelRose,  Icons.Outlined.MedicalServices),
    )
    StandardScreen(title = "Protocols & Manuals", onBack = onBack) { inner ->
        Column(
            modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search protocols...") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(items.size) { i ->
                    ProtocolCard(items[i], onClick = onClickItem)
                }
            }
        }
    }
}

@Composable
private fun ProtocolCard(item: UiItem, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = Color(0xFF444444))
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                Icons.Rounded.ChevronRight,
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
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Preview(showBackground = true)
@Composable
private fun ProtocolsPreview() {
    MaterialTheme {
        ProtocolsScreen()
    }
}
