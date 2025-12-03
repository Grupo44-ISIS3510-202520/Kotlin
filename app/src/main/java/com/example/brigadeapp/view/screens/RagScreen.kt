@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.brigadeapp.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brigadeapp.R
import com.example.brigadeapp.domain.model.RagState
import com.example.brigadeapp.domain.model.RagCacheEntry
import com.example.brigadeapp.view.common.StandardScreen
import com.example.brigadeapp.viewmodel.screens.RagViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RagScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: RagViewModel = hiltViewModel()
) {
    val ragState by viewModel.ragState.collectAsState()
    val cacheSize by viewModel.cacheSize.collectAsState()
    val cacheHistory by viewModel.cacheHistory.collectAsState()

    var query by remember { mutableStateOf("") }
    var showHistoryDialog by remember { mutableStateOf(false) }

    StandardScreen(
        title = "AI Assistant",
        onBack = onBack
    ) { inner ->
        Column(
            modifier = modifier
                .padding(inner)
                .fillMaxSize()
                .background(Color(0xFFFBF8F2))
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_protocols),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "$cacheSize answers cached",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    IconButton(onClick = {
                        viewModel.loadCacheHistory()
                        showHistoryDialog = true
                    }) {
                        Icon(
                            Icons.Outlined.History,
                            contentDescription = "View history",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { input ->
                    val limited = input.take(120)
                    val sanitized = limited.replace(Regex("[^\\p{L}\\p{N}\\p{P}\\p{Z}]"), "")
                    query = sanitized
                },
                placeholder = { Text("Ask about protocols...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (query.isNotBlank()) {
                                viewModel.askQuestion(query)
                            }
                        },
                        enabled = ragState !is RagState.Loading && query.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, "Send")
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            when (val state = ragState) {
                is RagState.Idle -> {
                    EmptyState()
                }

                is RagState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is RagState.Success -> {
                    RagAnswerCard(
                        answer = state.answer,
                        sources = state.sources,
                        fromCache = state.fromCache
                    )
                }

                is RagState.Error -> {
                    ErrorCard(message = state.message)
                }
            }
        }
    }

    if (showHistoryDialog) {
        CacheHistoryDialog(
            history = cacheHistory,
            onDismiss = { showHistoryDialog = false },
            onSelectEntry = { entry ->
                query = entry.query
                viewModel.useCachedQuery(entry)
                showHistoryDialog = false
            }
        )
    }
}


@Composable
private fun RagAnswerCard(
    answer: String,
    sources: List<String>,
    fromCache: Boolean
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Cache badge
            if (fromCache) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "From cache",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Answer header
            item {
                Text(
                    text = "Answer:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }

            // Spacer
            item { Spacer(Modifier.height(8.dp)) }

            // Sources header
            item {
                Text(
                    text = "Sources:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Sources list
            items(sources) { source ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_protocols),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = source,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CacheHistoryDialog(
    history: List<RagCacheEntry>,
    onDismiss: () -> Unit,
    onSelectEntry: (RagCacheEntry) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cached Answers",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Clear, "Close")
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No cached answers yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(history) { entry ->
                            CacheHistoryItem(
                                entry = entry,
                                onClick = { onSelectEntry(entry) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CacheHistoryItem(
    entry: RagCacheEntry,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = entry.query,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = entry.answer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_protocols),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${entry.sources.size} source(s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = formatTimestamp(entry.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> {
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

@Composable
private fun EmptyState() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_medical),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Ask me anything about the protocols",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "I can help you with information about the BEU protocols",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}