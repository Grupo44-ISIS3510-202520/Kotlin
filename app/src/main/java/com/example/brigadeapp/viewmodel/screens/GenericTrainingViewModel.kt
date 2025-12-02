package com.example.brigadeapp.viewmodel.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.domain.entity.Lesson
import com.example.brigadeapp.domain.entity.QuizQuestion
import com.example.brigadeapp.domain.repository.TrainingRepository
import com.example.brigadeapp.domain.entity.AuthClient
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class GenericTrainingViewModel @Inject constructor(
    private val repo: TrainingRepository,
    private val auth: AuthClient,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons: StateFlow<List<Lesson>> = _lessons

    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions

    fun loadTraining(trainingId: String) {
        viewModelScope.launch {
            // Load lessons
            repo.observeLessons(trainingId).collect { lessonsList ->
                _lessons.value = lessonsList
            }
        }

        viewModelScope.launch {
            // Load quiz questions
            repo.observeQuizQuestions(trainingId).collect { questionsList ->
                _quizQuestions.value = questionsList
            }
        }
    }

    fun onVisitedPage(trainingId: String, pageIndex: Int, totalPages: Int) {
        viewModelScope.launch {
            repo.markLessonVisited(pageIndex, totalPages)
        }
    }

    fun onQuizSubmitted(trainingId: String, title: String, correct: Int, total: Int) {
        viewModelScope.launch {
            repo.submitQuiz(correct, total)

            logQuizSubmissionToFirestore(
                trainingId = trainingId,
                score = correct,
                totalQuestions = total
            )
        }
    }

    private suspend fun logQuizSubmissionToFirestore(
        trainingId: String,
        score: Int,
        totalQuestions: Int
    ) {
        try {
            val uid = auth.currentUser?.uid
            val email = auth.currentUser?.email

            val passed = score >= (totalQuestions * 0.6)
            val scorePercentage = if (totalQuestions > 0) {
                (score.toDouble() / totalQuestions * 100).toInt()
            } else 0

            val data = hashMapOf(
                "eventType" to "training_quiz_submitted",
                "trainingId" to trainingId,
                "score" to score,
                "totalQuestions" to totalQuestions,
                "passed" to passed,
                "scorePercentage" to scorePercentage,
                "uid" to (uid ?: "unknown"),
                "email" to (email ?: "unknown"),
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("feature_analysis_quiz")
                .add(data)
                .await()

            Log.d("GenericTrainingViewModel", "Logged quiz submission: $trainingId, score=$score/$totalQuestions, passed=$passed")
        } catch (e: Exception) {
            Log.w("GenericTrainingViewModel", "Error logging quiz submission: ${e.message}")
        }
    }
}
