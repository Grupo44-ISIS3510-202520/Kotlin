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
import com.example.brigadeapp.viewmodel.screens.GenericTrainingViewModel
import com.example.brigadeapp.domain.utils.AnalyticsLogger


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericTrainingScreen(
    trainingId: String,
    trainingTitle: String,
    onBack: () -> Unit
) {
    val vm: GenericTrainingViewModel = hiltViewModel()

    // Load training content for this specific training ID
    LaunchedEffect(trainingId) {
        vm.loadTraining(trainingId)
    }

    val lessons by vm.lessons.collectAsState()
    val questions by vm.quizQuestions.collectAsState()
    val initialPageIndex by vm.initialPageIndex.collectAsState()
    val isCompleted by vm.isCompleted.collectAsState()

    var pageIndex by remember { mutableStateOf(0) }
    
    // Restore saved position when initialPageIndex is loaded
    LaunchedEffect(initialPageIndex) {
        initialPageIndex?.let { savedIndex ->
            pageIndex = savedIndex
        }
    }

    val totalPages = lessons.size

    // Persist page visit (only if lessons are loaded)
    LaunchedEffect(pageIndex, totalPages, trainingId) {
        if (totalPages > 0) {
            vm.onVisitedPage(trainingId, pageIndex, totalPages)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(trainingTitle) },
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
                    if (currentLesson.title.isNotBlank()) {
                        Text(
                            currentLesson.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    Text(
                        currentLesson.content,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Navigation buttons
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (pageIndex > 0) {
                        Button(onClick = { pageIndex-- }) {
                            Text("Previous")
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    Button(onClick = { pageIndex++ }) {
                        Text("Next")
                    }
                }
            } else {
                // Mark quiz as visited when user enters this page
                LaunchedEffect(Unit) {
                    vm.onQuizPageEntered(trainingId)
                }
                
                // Quiz page or completion message
                if (isCompleted) {
                    // Training already completed - show review message
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Training Completed!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "You have already passed this training.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "You can review the lessons using the back button.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = onBack) {
                            Text("Back to Trainings")
                        }
                    }
                } else {
                    // Quiz page
                    QuizPage(
                        questions = questions,
                        onSubmit = { correct, total ->
                            vm.onQuizSubmitted(trainingId, trainingTitle, correct, total)
                            
                            val passed = total > 0 && correct.toFloat() / total >= 0.8f
                            AnalyticsLogger.logTrainingQuizSubmitted(
                                trainingId = trainingId,
                                score = correct,
                                totalQuestions = total,
                                passed = passed
                            )
                        },
                        onBack = onBack,
                        onBackToLesson = { pageIndex = totalPages - 1 }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizPage(
    questions: List<com.example.brigadeapp.domain.entity.QuizQuestion>,
    onSubmit: (correct: Int, total: Int) -> Unit,
    onBack: () -> Unit,
    onBackToLesson: () -> Unit
) {
    var answers by remember { mutableStateOf(List(questions.size) { -1 }) }
    var showResults by remember { mutableStateOf(false) }

    if (showResults) {
        val correct = answers.indices.count { answers[it] == questions[it].correctIndex }
        val total = questions.size
        val passed = total > 0 && correct.toFloat() / total >= 0.8f

        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (passed) "Quiz Passed!" else "Quiz Failed",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Score: $correct / $total",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (passed) "You need 80% to pass. Congratulations!" else "You need 80% to pass. Try again!",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    onSubmit(correct, total)
                    onBack()
                }
            ) {
                Text("Done")
            }
        }
    } else {
        Column(
            Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onBackToLesson,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text("Back")
                }
                Text(
                    "Final Quiz",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(80.dp)) // Balance the layout
            }
            Spacer(Modifier.height(16.dp))

            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                questions.forEachIndexed { qIndex, question ->
                    Text(
                        "${qIndex + 1}. ${question.text}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))

                    question.options.forEachIndexed { optIndex, opt ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = answers[qIndex] == optIndex,
                                    onClick = {
                                        answers = answers.toMutableList().apply { this[qIndex] = optIndex }
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = answers[qIndex] == optIndex,
                                onClick = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(opt, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { showResults = true },
                enabled = answers.all { it != -1 },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit Quiz")
            }
        }
    }
}
