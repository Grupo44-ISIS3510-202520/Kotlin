package com.example.brigadeapp.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.brigadeapp.viewmodel.screens.TrainingViewModel
import com.example.brigadeapp.domain.utils.AnalyticsLogger


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CprCourseScreen(
    onBack: () -> Unit
) {
    val vm: TrainingViewModel = hiltViewModel()

    // [Concurrency] Observe dynamic content from Firestore
    val lessons by vm.cprLessons.collectAsState()
    val questions by vm.cprQuizQuestions.collectAsState()

    var pageIndex by remember { mutableStateOf(0) }
    val totalPages = lessons.size

    // Persist page visit (only if lessons are loaded)
    LaunchedEffect(pageIndex, totalPages) {
        if (totalPages > 0) {
            vm.onVisitedPage(pageIndex, totalPages)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CPR Training") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Loading state
            if (lessons.isEmpty() && questions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Loading training content from Firestore...")
                    }
                }
                return@Scaffold
            }

            // Lesson pages
            if (pageIndex < totalPages) {
                val currentLesson = lessons[pageIndex]

                Text(
                    "Lesson ${pageIndex + 1} of $totalPages",
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))

                Column(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Display lesson title if available
                    if (currentLesson.title.isNotBlank()) {
                        Text(
                            currentLesson.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    // Display lesson content
                    Text(
                        currentLesson.content,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        enabled = pageIndex > 0,
                        onClick = { pageIndex-- }
                    ) { Text("Back") }

                    Button(onClick = { pageIndex++ }) {
                        Text(if (pageIndex == totalPages - 1) "Go to Quiz" else "Next")
                    }
                }
            } else {
                // Quiz page
                Text("Final Quiz", fontWeight = FontWeight.Black)
                Spacer(Modifier.height(12.dp))

                // Check if quiz questions are loaded
                if (questions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(16.dp))
                            Text("Loading quiz questions...")
                        }
                    }
                    return@Scaffold
                }

                val answers = remember {
                    mutableStateListOf<Int>().apply {
                        repeat(questions.size) { add(-1) }
                    }
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    questions.forEachIndexed { idx, q ->
                        Spacer(Modifier.height(8.dp))
                        Text(q.text, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(6.dp))

                        q.options.forEachIndexed { optIdx, opt ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = answers[idx] == optIdx,
                                        onClick = { answers[idx] = optIdx },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = answers[idx] == optIdx,
                                    onClick = { answers[idx] = optIdx }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(opt)
                            }
                        }
                    }
                }

                val allAnswered = answers.none { it == -1 }
                Spacer(Modifier.height(12.dp))
                Button(
                    enabled = allAnswered,
                    onClick = {
                        val correct = questions.indices.count { i ->
                            answers[i] == questions[i].correctIndex
                        }
                        val total = questions.size

                        vm.onQuizSubmitted(correct, total)

                        // TODO: Remove
                        // BQ Extra: Firebase Analytics telemetry
                        val passed = correct >= (total * 0.6) // 60% passing threshold
                        AnalyticsLogger.logTrainingQuizSubmitted(
                            trainingId = "cpr_basic",
                            score = correct,
                            totalQuestions = total,
                            passed = passed
                        )

                        onBack()
                    }
                ) {
                    Text(if (allAnswered) "Submit Quiz" else "Answer all questions")
                }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    enabled = totalPages > 0,
                    onClick = { pageIndex = totalPages - 1 }
                ) {
                    Text("Review last lesson")
                }
            }
        }
    }
}
