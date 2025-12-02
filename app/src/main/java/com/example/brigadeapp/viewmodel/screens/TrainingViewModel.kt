package com.example.brigadeapp.viewmodel.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.data.repository.CprProgress
import com.example.brigadeapp.domain.entity.Lesson
import com.example.brigadeapp.domain.entity.QuizQuestion
import com.example.brigadeapp.domain.entity.TrainingModule
import com.example.brigadeapp.domain.repository.TrainingRepository
import com.example.brigadeapp.domain.entity.AuthClient
import com.example.brigadeapp.domain.usecase.GetTrainingLessons
import com.example.brigadeapp.domain.usecase.GetTrainingModules
import com.example.brigadeapp.domain.usecase.GetTrainingQuizQuestions
import com.example.brigadeapp.domain.usecase.ObserveConnectivityUseCase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject



@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val repo: TrainingRepository,
    private val auth: AuthClient,
    private val db: FirebaseFirestore,
    getTrainingModules: GetTrainingModules,
    private val getTrainingLessons: GetTrainingLessons,
    private val getTrainingQuizQuestions: GetTrainingQuizQuestions,
    observeConnectivityUseCase: ObserveConnectivityUseCase
) : ViewModel() {

    val trainingModules: StateFlow<List<TrainingModule>> =
        getTrainingModules()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTrainingsProgress: StateFlow<Map<String, Map<String, Any>>> =
        repo.observeAllTrainingsProgress()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val cprProgress: StateFlow<CprProgress> =
        repo.observeCprProgress()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CprProgress())


    val cprLessons: StateFlow<List<Lesson>> =
        getTrainingLessons("cpr_basic")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cprQuizQuestions: StateFlow<List<QuizQuestion>> =
        getTrainingQuizQuestions("cpr_basic")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        var wasOffline = false
        viewModelScope.launch {
            observeConnectivityUseCase().collect { isOnline ->
                Log.d("TrainingViewModel", "Connectivity changed: isOnline=$isOnline")

                if (isOnline && wasOffline) {
                    // Just reconnected - flush pending updates
                    Log.d("TrainingViewModel", "Reconnected! Flushing pending training updates...")
                    val result = repo.flushPendingUpdates()
                    if (result.isSuccess) {
                        Log.d("TrainingViewModel", "Successfully flushed all pending updates")
                    } else {
                        Log.w("TrainingViewModel", "Some updates failed to flush: ${result.exceptionOrNull()?.message}")
                    }
                }

                wasOffline = !isOnline
            }
        }
    }


    fun onVisitedPage(pageIndex: Int, totalPages: Int) {
        viewModelScope.launch { repo.markLessonVisited("cpr_basic", pageIndex, totalPages) }
    }

    fun onQuizSubmitted(correct: Int, total: Int) {
        viewModelScope.launch {
            repo.submitQuiz("cpr_basic", correct, total)

            logQuizSubmissionToFirestore(
                trainingId = "cpr_basic",
                score = correct,
                totalQuestions = total
            )
        }
    }

    fun onTrainingStarted(trainingId: String, title: String, source: String) {
        viewModelScope.launch {
            logTrainingStartToFirestore(trainingId, title, source)
        }
    }

    private suspend fun logTrainingStartToFirestore(
        trainingId: String,
        title: String,
        source: String
    ) {
        try {
            val uid = auth.currentUser?.uid
            val email = auth.currentUser?.email

            val data = hashMapOf(
                "eventType" to "training_started",
                "trainingId" to trainingId,
                "title" to title,
                "source" to source,
                "uid" to (uid ?: "unknown"),
                "email" to (email ?: "unknown"),
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("feature_analysis_quiz")
                .add(data)
                .await()

            android.util.Log.d("TrainingViewModel", "BQ2: Logged training start: $trainingId from $source")
        } catch (e: Exception) {
            android.util.Log.w("TrainingViewModel", "BQ2: Error logging training start (will sync when online): ${e.message}")
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

            val passed = score >= (totalQuestions * 0.6) // 60% passing threshold
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

            android.util.Log.d("TrainingViewModel", "BQ2: Logged quiz submission: $trainingId, score=$score/$totalQuestions, passed=$passed")
        } catch (e: Exception) {
            android.util.Log.w("TrainingViewModel", "BQ2: Error logging quiz submission (will sync when online): ${e.message}")
        }
    }

}
