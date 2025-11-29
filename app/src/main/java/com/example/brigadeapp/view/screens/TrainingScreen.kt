package com.example.brigadeapp.view.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.brigadeapp.R
import com.example.brigadeapp.view.common.StandardScreen
import com.example.brigadeapp.view.theme.Blue
import com.example.brigadeapp.view.theme.SurfaceSoft
import com.example.brigadeapp.viewmodel.screens.TrainingViewModel
import com.example.brigadeapp.domain.utils.AnalyticsLogger
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.brigadeapp.domain.entity.LeaderboardEntry
import com.example.brigadeapp.domain.entity.Timeframe
import com.example.brigadeapp.viewmodel.screens.LeaderboardUiState


@Composable
fun TrainingScreen(
    onOpenCpr: () -> Unit,
    onBack: () -> Unit = {}
) {
    val vm: TrainingViewModel = hiltViewModel()
    val trainingModules by vm.trainingModules.collectAsState()
    val cprProgress by vm.cprProgress.collectAsState()
    val leaderboardState by vm.leaderboardState.collectAsState()


    val cprRatio = if (cprProgress.totalLessons > 0)
        cprProgress.lessonsVisited.toFloat() / (cprProgress.totalLessons.toFloat() + 1)
    else 0f

    StandardScreen(title = "Training", onBack = onBack) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
            // Pending courses section
            Text(
                "Courses to be completed",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
            )
            Spacer(Modifier.height(8.dp))

            val pendingTrainings = trainingModules.filter { module ->
                // Check completion status based on training type
                when (module.id) {
                    "cpr_basic" -> !cprProgress.completed
                    else -> true
                }
            }

            if (pendingTrainings.isEmpty()) {
                Text("No pending courses.")
            } else {
                pendingTrainings.forEach { module ->
                    TrainingCard(
                        badge = "Course",
                        title = module.title,
                        subtitle = module.description,
                        cta = "Open course",
                        imageUrl = module.imageUrl,
                        onClick = {
                            vm.onTrainingStarted(
                                trainingId = module.id,
                                title = module.title,
                                source = "training_list"
                            )

                            //TODO: Remove
                            // BQ Extra: SECONDARY - Firebase Analytics telemetry
                            AnalyticsLogger.logTrainingStarted(
                                trainingId = module.id,
                                title = module.title,
                                source = "training_list"
                            )

                            // Route to appropriate course screen
                            when (module.id) {
                                "cpr_basic" -> onOpenCpr()
                                else -> {
                                    // TODO: Add generic training screen or show "Coming soon"
                                    android.util.Log.w("TrainingScreen", "No screen for training: ${module.id}")
                                }
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(18.dp))

            // Completed courses section
            Text(
                "Completed courses",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
            )
            Spacer(Modifier.height(8.dp))

            val completedTrainings = trainingModules.filter { module ->
                when (module.id) {
                    "cpr_basic" -> cprProgress.completed
                    else -> false // Future trainings default to not completed
                }
            }

            if (completedTrainings.isEmpty()) {
                Text("—")
            } else {
                completedTrainings.forEach { module ->
                    TrainingCard(
                        badge = "Completed",
                        title = module.title,
                        subtitle = when (module.id) {
                            "cpr_basic" -> "You passed the final quiz."
                            else -> "You completed this training."
                        },
                        cta = "Review",
                        imageUrl = module.imageUrl,
                        onClick = {
                            // [BQ/Analytics] BQ2: Log review action
                            vm.onTrainingStarted(
                                trainingId = module.id,
                                title = module.title,
                                source = "training_list_review"
                            )

                            AnalyticsLogger.logTrainingStarted(
                                trainingId = module.id,
                                title = module.title,
                                source = "training_list_review"
                            )

                            // Route to appropriate course screen
                            when (module.id) {
                                "cpr_basic" -> onOpenCpr()
                                else -> {
                                    android.util.Log.w("TrainingScreen", "No screen for training: ${module.id}")
                                }
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(18.dp))

            // Progress section (currently CPR-specific, can be extended)
            Text(
                "Your Progress",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
            )
            Spacer(Modifier.height(8.dp))

            // Show progress for CPR if it exists in the training modules
            if (trainingModules.any { it.id == "cpr_basic" }) {
                ProgressItem(label = "CPR Course", progress = cprRatio)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Lessons: ${cprProgress.lessonsVisited}/${(cprProgress.totalLessons + 1)}  |  Quiz: ${cprProgress.quizScore}/${cprProgress.quizTotal}",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Training Leaderboard",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
            )
            Spacer(Modifier.height(8.dp))

            LeaderboardSection(
                state = leaderboardState,
                onSelectTimeframe = { vm.onLeaderboardTimeframeSelected(it) },
                onRefresh = { vm.onLeaderboardPullToRefresh() }
            )

        }
    }
}


@Composable
private fun TrainingCard(
    badge: String,
    title: String,
    subtitle: String,
    cta: String,
    imageUrl: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // [Concurrency] [Local storage] AsyncImage loads from URL with automatic caching
            if (imageUrl.isNotBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceSoft),
                    placeholder = painterResource(R.drawable.basic_first_aid),
                    error = painterResource(R.drawable.basic_first_aid)
                )
            } else {
                // Fallback to drawable if no URL provided
                Image(
                    painter = painterResource(R.drawable.basic_first_aid),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceSoft)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(2.dp))
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue)
                ) { Text(cta) }
            }
        }
    }
}

@Composable
private fun ProgressItem(label: String, progress: Float) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, style = MaterialTheme.typography.bodyLarge)
                Text("${((progress) * 100).toInt()}%", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (progress).coerceIn(0f, 1f) },
                trackColor = SurfaceSoft,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
private fun LeaderboardSection(
    state: LeaderboardUiState,
    onSelectTimeframe: (Timeframe) -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
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

        Spacer(Modifier.height(8.dp))

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
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
        } else {
            state.lastUpdatedMillis?.let { last ->
                Text(
                    text = "Last updated ${formatMinutesAgo(last)} min ago",
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.height(4.dp))
            }
        }

        // Pull-to-refresh (disabled offline)
        TextButton(
            onClick = onRefresh,
            enabled = !state.isOffline
        ) {
            Text("Refresh leaderboard")
        }

        Spacer(Modifier.height(4.dp))

        if (state.entries.isEmpty() && !state.isLoading) {
            Text("No leaderboard data yet.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 260.dp)
            ) {
                itemsIndexed(
                    items = state.entries,
                    key = { _, entry -> entry.userId }
                ) { index, entry ->
                    LeaderboardRow(rank = index + 1, entry = entry)
                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        if (state.isLoading) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
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
                containerColor = MaterialTheme.colorScheme.surface
            )
        },
        modifier = modifier
    ) {
        Text(label)
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
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = medalForRank(rank),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(entry.displayName, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Total: ${entry.totalCompleted} • Last 7 days: ${entry.weeklyCompleted}",
                    style = MaterialTheme.typography.bodySmall
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
