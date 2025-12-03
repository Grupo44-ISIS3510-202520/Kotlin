package com.example.brigadeapp.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brigadeapp.domain.entity.LeaderboardEntry
import com.example.brigadeapp.domain.entity.Timeframe
import com.example.brigadeapp.view.theme.Blue
import com.example.brigadeapp.viewmodel.screens.LeaderboardUiState
import com.example.brigadeapp.viewmodel.screens.TrainingLeaderboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingLeaderboardScreen(
    onBack: () -> Unit
) {
    val vm: TrainingLeaderboardViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
    ) {
        // Top bar with back button
        CenterAlignedTopAppBar(
            title = { Text("Training Leaderboard") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        // Content
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LeaderboardContent(
                state = uiState,
                onSelectTimeframe = { vm.onTimeframeSelected(it) },
                onRefresh = { vm.onPullToRefresh() }
            )
        }
    }
}

@Composable
private fun LeaderboardContent(
    state: LeaderboardUiState,
    onSelectTimeframe: (Timeframe) -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Timeframe toggle
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimeframeButton(
                label = "All time",
                selected = state.selectedTimeframe == Timeframe.ALL_TIME,
                onClick = { onSelectTimeframe(Timeframe.ALL_TIME) },
                modifier = Modifier.weight(1f)
            )
            TimeframeButton(
                label = "Last 7 days",
                selected = state.selectedTimeframe == Timeframe.LAST_7_DAYS,
                onClick = { onSelectTimeframe(Timeframe.LAST_7_DAYS) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Offline banner
        if (state.isOffline) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = buildString {
                        append("You are offline. Showing cached leaderboard data")
                        state.lastUpdatedMillis?.let {
                            append(" (last updated ${formatMinutesAgo(it)} min ago).")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
        } else {
            state.lastUpdatedMillis?.let { last ->
                Text(
                    text = "Last updated ${formatMinutesAgo(last)} min ago",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        // Pull-to-refresh button
        TextButton(
            onClick = onRefresh,
            enabled = !state.isOffline && !state.isLoading
        ) {
            Text("Refresh leaderboard")
        }

        Spacer(Modifier.height(8.dp))

        // Loading indicator
        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }

        // Leaderboard list
        if (state.entries.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No leaderboard data yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = state.entries,
                    key = { _, entry -> entry.userId }
                ) { index, entry ->
                    LeaderboardRow(rank = index + 1, entry = entry)
                }
            }
        }
    }
}

@Composable
private fun TimeframeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = if (selected) {
            ButtonDefaults.buttonColors(containerColor = Blue)
        } else {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = modifier,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (selected) 2.dp else 1.dp
        )
    ) {
        Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun LeaderboardRow(
    rank: Int,
    entry: LeaderboardEntry
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = medalForRank(rank),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    entry.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    entry.emailPrefix,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Total: ${entry.totalCompleted} • Last 7 days: ${entry.weeklyCompleted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

private fun medalForRank(rank: Int): String = when (rank) {
    1 -> "🥇"
    2 -> "🥈"
    3 -> "🥉"
    else -> "$rank."
}

private fun formatMinutesAgo(lastUpdatedMillis: Long): Int {
    val diff = System.currentTimeMillis() - lastUpdatedMillis
    val minutes = (diff / 60_000L).toInt()
    return minutes.coerceAtLeast(0)
}
