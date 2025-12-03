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


@Composable
fun TrainingScreen(
    onOpenTraining: (trainingId: String, title: String) -> Unit,
    onOpenLeaderboard: () -> Unit,
    onBack: () -> Unit = {}
) {
    val vm: TrainingViewModel = hiltViewModel()
    val trainingModules by vm.trainingModules.collectAsState()
    val allTrainingsProgress by vm.allTrainingsProgress.collectAsState()

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
                val progress = allTrainingsProgress[module.id]
                val completed = (progress?.get("completed") as? Boolean) ?: false
                !completed
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

                            AnalyticsLogger.logTrainingStarted(
                                trainingId = module.id,
                                title = module.title,
                                source = "training_list"
                            )

                            onOpenTraining(module.id, module.title)
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
                val progress = allTrainingsProgress[module.id]
                val completed = (progress?.get("completed") as? Boolean) ?: false
                completed
            }

            if (completedTrainings.isEmpty()) {
                Text("—")
            } else {
                completedTrainings.forEach { module ->
                    TrainingCard(
                        badge = "Completed",
                        title = module.title,
                        subtitle = "You passed the final quiz.",
                        cta = "Review",
                        imageUrl = module.imageUrl,
                        onClick = {
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

                            onOpenTraining(module.id, module.title)
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(18.dp))

            // Progress section (dynamic for all trainings)
            Text(
                "Your Progress",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
            )
            Spacer(Modifier.height(8.dp))

            if (trainingModules.isEmpty()) {
                Text("No trainings available.")
            } else {
                trainingModules.forEach { module ->
                    val progress = allTrainingsProgress[module.id]
                    val lessonsVisited = (progress?.get("lessonsVisited") as? Number)?.toInt() ?: 0
                    val totalLessons = (progress?.get("totalLessons") as? Number)?.toInt() ?: module.totalLessons
                    val quizScore = (progress?.get("quizScore") as? Number)?.toInt() ?: 0
                    val quizTotal = (progress?.get("quizTotal") as? Number)?.toInt() ?: 0
                    val quizVisited = (progress?.get("quizVisited") as? Boolean) ?: false
                    
                    // Calculate progress ratio
                    // If quiz visited, add 1 to numerator for the quiz page (but not to denominator)
                    val totalPages = totalLessons + 1 // lessons + quiz page
                    val visitedPages = if (quizVisited) lessonsVisited + 1 else lessonsVisited
                    val progressRatio = if (totalPages > 0) {
                        visitedPages.toFloat() / totalPages.toFloat()
                    } else 0f
                    
                    ProgressItem(
                        label = module.title,
                        progress = progressRatio,
                        lessonsVisited = lessonsVisited,
                        totalLessons = totalLessons,
                        quizScore = quizScore,
                        quizTotal = quizTotal
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // Leaderboard button
            Button(
                onClick = {
                    vm.onLeaderboardViewed()
                    onOpenLeaderboard()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("View Leaderboard")
            }

            Spacer(Modifier.height(24.dp))
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
private fun ProgressItem(
    label: String,
    progress: Float,
    lessonsVisited: Int,
    totalLessons: Int,
    quizScore: Int,
    quizTotal: Int
) {
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
            Spacer(Modifier.height(8.dp))
            Text(
                "Lessons: $lessonsVisited/$totalLessons  |  Quiz: $quizScore/$quizTotal",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
