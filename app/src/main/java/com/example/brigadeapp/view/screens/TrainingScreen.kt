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
import com.example.brigadeapp.R
import com.example.brigadeapp.view.common.StandardScreen
import com.example.brigadeapp.view.theme.Blue
import com.example.brigadeapp.view.theme.SurfaceSoft
import com.example.brigadeapp.viewmodel.screens.TrainingViewModel

@Composable
fun TrainingScreen(
    onOpenCpr: () -> Unit,
    onBack: () -> Unit = {}
) {
    val vm: TrainingViewModel = hiltViewModel()
    val progress by vm.cprProgress.collectAsState()

    val ratio = if (progress.totalLessons > 0)
        progress.lessonsVisited.toFloat() / (progress.totalLessons.toFloat() + 1)
    else 0f
    val completed = progress.completed

    StandardScreen(title = "Training", onBack = onBack) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "Courses to be completed",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
            )
            Spacer(Modifier.height(8.dp))

            if (!completed) {
                TrainingCard(
                    badge = "Course",
                    title = "CPR Training",
                    subtitle = "Recognize cardiac arrest, deliver effective compressions and breaths, and use the AED safely.",
                    cta = "Open course",
                    imageRes = R.drawable.basic_first_aid,
                    onClick = onOpenCpr
                )
            } else {
                Text("No pending courses.")
            }

            Spacer(Modifier.height(18.dp))
            Text(
                "Completed courses",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
            )
            Spacer(Modifier.height(8.dp))

            if (completed) {
                TrainingCard(
                    badge = "Completed",
                    title = "CPR Training",
                    subtitle = "You passed the final quiz.",
                    cta = "Review",
                    imageRes = R.drawable.basic_first_aid,
                    onClick = onOpenCpr
                )
            } else {
                Text("—")
            }

            Spacer(Modifier.height(18.dp))
            Text(
                "Your Progress",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
            )
            Spacer(Modifier.height(8.dp))

            ProgressItem(label = "CPR Course", progress = ratio)
            Spacer(Modifier.height(8.dp))
            Text(
                "Lessons: ${progress.lessonsVisited}/${(progress.totalLessons + 1)}  |  Quiz: ${progress.quizScore}/${progress.quizTotal}",
                style = MaterialTheme.typography.labelLarge
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
    @DrawableRes imageRes: Int,
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
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceSoft)
            )

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
