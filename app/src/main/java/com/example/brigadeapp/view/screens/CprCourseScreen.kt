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
import com.example.brigadeapp.domain.entity.QuizQuestion
import com.example.brigadeapp.viewmodel.screens.TrainingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CprCourseScreen(
    onBack: () -> Unit
) {
    val vm: TrainingViewModel = hiltViewModel()

    val pages = remember {
        listOf(
            // 1
            """
            Scene Safety & Activation
            • Ensure the scene is safe for you and the victim.
            • Tap the victim and shout: “Are you OK?”.
            • If no response, shout for help and ask someone to call emergency services and bring an AED.
            • Check breathing: look for normal chest rise for no more than 10 seconds.
            """.trimIndent(),
            // 2
            """
            High-Quality Chest Compressions
            • Hand position: heel of one hand on the center of the chest (lower half of sternum), other hand on top.
            • Rate: 100–120 compressions/min.
            • Depth: at least ~5 cm (2 in) in adults.
            • Full chest recoil after each compression.
            • Minimize interruptions; switch compressors every 2 minutes if possible.
            """.trimIndent(),
            // 3
            """
            Airway & Breaths
            • Open airway with head-tilt, chin-lift (unless spinal trauma suspected).
            • Give 2 effective breaths after 30 compressions (ratio 30:2 for a single rescuer).
            • Each breath ~1 second, visible chest rise, avoid excessive ventilation.
            """.trimIndent(),
            // 4
            """
            AED Usage
            • Power on the AED and follow voice prompts immediately.
            • Expose the chest and attach pads as indicated (right upper chest / left side).
            • Ensure nobody is touching the victim during rhythm analysis and shock delivery.
            • Resume compressions immediately after a shock or “no shock advised”.
            """.trimIndent()
        )
    }

    // Quiz
    val questions = remember {
        listOf(
            QuizQuestion("q1", "Compression rate for adult CPR is:", listOf("60–80/min", "100–120/min", "140–160/min", "80–100/min"), 1),
            QuizQuestion("q2", "Compression-to-breath ratio (adult, single rescuer):", listOf("15:2", "5:1", "30:2", "20:2"), 2),
            QuizQuestion("q3", "After turning on the AED you should:", listOf("Begin compressions", "Remove pads", "Follow voice prompts", "Turn it off"), 2),
            QuizQuestion("q4", "Compression depth (adult):", listOf("~2 inches / 5 cm", "1 cm", "3 cm", "6–7 cm"), 0),
            QuizQuestion("q5", "Allow ___ between compressions:", listOf("partial recoil", "full recoil", "no recoil", "only if tired"), 1)
        )
    }

    var pageIndex by remember { mutableStateOf(0) }
    val totalPages = pages.size

    // Persist page visit
    LaunchedEffect(pageIndex) { vm.onVisitedPage(pageIndex, totalPages) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CPR Training") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
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

            if (pageIndex < totalPages) {
                Text("Lesson ${pageIndex + 1} of $totalPages", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))

                Column(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(pages[pageIndex], style = MaterialTheme.typography.bodyLarge, lineHeight = 20.sp)
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
                Text("Final Quiz", fontWeight = FontWeight.Black)
                Spacer(Modifier.height(12.dp))


                val answers = remember {
                    mutableStateListOf<Int>().apply { repeat(questions.size) { add(-1) } }
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
                        val correct = questions.indices.count { i -> answers[i] == questions[i].correctIndex }
                        vm.onQuizSubmitted(correct, questions.size)
                        onBack()
                    }
                ) { Text(if (allAnswered) "Submit Quiz" else "Answer all questions") }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { pageIndex = totalPages - 1 }) { Text("Review last lesson") }
            }
        }
    }
}
