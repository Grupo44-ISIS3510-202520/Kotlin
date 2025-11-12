package com.example.brigadeapp.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class MCQ(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CprQuizScreen(
    onBack: () -> Unit,
    onSubmit: (score: Int, total: Int) -> Unit
) {
    val questions = remember {
        listOf(
            MCQ(
                question = "Compression rate for adult CPR is:",
                options = listOf("60–80/min", "100–120/min", "140–160/min", "80–100/min"),
                correctIndex = 1
            ),
            MCQ(
                question = "Compression-to-breath ratio (adult single rescuer):",
                options = listOf("15:2", "5:1", "30:2", "20:2"),
                correctIndex = 2
            ),
            MCQ(
                question = "After turning on the AED you should:",
                options = listOf("Begin compressions", "Remove pads", "Follow voice prompts"),
                correctIndex = 2
            )
        )
    }

    // Persist answers across recompositions/rotation
    val mapSaver: Saver<MutableMap<Int, Int>, Any> = listSaver(
        save = { list -> list.entries.map { it.key to it.value } },
        restore = { pairs ->
            mutableStateMapOf<Int, Int>().apply { putAll(pairs.associate { it.first as Int to it.second as Int }) }
        }
    )
    val answers = rememberSaveable(saver = mapSaver) { mutableStateMapOf<Int, Int>() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CPR Training") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            val allAnswered = answers.size == questions.size
            ExtendedFloatingActionButton(
                onClick = {
                    val score = questions.indices.count { idx -> answers[idx] == questions[idx].correctIndex }
                    onSubmit(score, questions.size)
                },
                expanded = true,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.navigationBarsPadding(),
                text = { Text(if (allAnswered) "Submit" else "Submit (${answers.size}/${questions.size})") },
                icon = {}
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Final Quiz",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Answer all questions. You need 80% to pass.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            itemsIndexed(questions) { idx, q ->
                QuestionCard(
                    index = idx + 1,
                    mcq = q,
                    selectedIndex = answers[idx],
                    onSelect = { option -> answers[idx] = option }
                )
            }

            // Extra bottom space so the FAB doesn’t cover the last answers
            item { Spacer(Modifier.height(96.dp)) }
        }
    }
}

@Composable
private fun QuestionCard(
    index: Int,
    mcq: MCQ,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            text = "$index) ${mcq.question}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 6.dp)
        )
        mcq.options.forEachIndexed { optIdx, label ->
            OptionRow(
                text = label,
                selected = selectedIndex == optIdx,
                onClick = { onSelect(optIdx) }
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun OptionRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    androidx.compose.material3.ListItem(
        headlineContent = { Text(text) },
        leadingContent = {
            RadioButton(
                selected = selected,
                onClick = onClick
            )
        },
        modifier = Modifier.padding(horizontal = 6.dp)
    )
}
