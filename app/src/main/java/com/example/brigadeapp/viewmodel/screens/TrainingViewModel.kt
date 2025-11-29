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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.example.brigadeapp.domain.entity.LeaderboardEntry
import com.example.brigadeapp.domain.entity.LeaderboardSnapshot
import com.example.brigadeapp.domain.entity.Timeframe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update



data class LeaderboardUiState(
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val selectedTimeframe: Timeframe = Timeframe.ALL_TIME,
    val entries: List<LeaderboardEntry> = emptyList(),
    val lastUpdatedMillis: Long? = null,
    val errorMessage: String? = null
)


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


    // --- Training leaderboard UI state ---
    private val _leaderboardState = MutableStateFlow(LeaderboardUiState())
    val leaderboardState: StateFlow<LeaderboardUiState> = _leaderboardState


    val trainingModules: StateFlow<List<TrainingModule>> =
        getTrainingModules()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


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

                // Update leaderboard offline banner
                _leaderboardState.update { it.copy(isOffline = !isOnline) }

                if (isOnline && wasOffline) {
                    // Just reconnected - flush pending updates
                    Log.d("TrainingViewModel", "Reconnected! Flushing pending training updates...")
                    val result = repo.flushPendingUpdates()
                    if (result.isSuccess) {
                        Log.d("TrainingViewModel", "Successfully flushed all pending updates")
                    } else {
                        Log.w("TrainingViewModel", "Some updates failed to flush: ${result.exceptionOrNull()?.message}")
                    }

                    // Eventual connectivity: refresh leaderboard in background
                    refreshCurrentLeaderboard(force = true)
                }

                wasOffline = !isOnline
            }
        }

        // Initial leaderboard load
        viewModelScope.launch {
            loadInitialLeaderboard()
        }
    }


    fun onVisitedPage(pageIndex: Int, totalPages: Int) {
        viewModelScope.launch { repo.markLessonVisited(pageIndex, totalPages) }
    }

    fun onQuizSubmitted(correct: Int, total: Int) {
        viewModelScope.launch {
            repo.submitQuiz(correct, total)

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

    private suspend fun loadInitialLeaderboard() {
        val timeframe = _leaderboardState.value.selectedTimeframe

        // 1) Emit cached data immediately (disk or memory)
        val cached = repo.getCachedLeaderboard(timeframe)
        _leaderboardState.update {
            it.copy(
                entries = cached.entries,
                lastUpdatedMillis = cached.lastUpdatedMillis,
                isLoading = true
            )
        }

        // 2) Try remote refresh (respecting TTL)
        refreshCurrentLeaderboard(force = false)
    }

    private fun refreshCurrentLeaderboard(force: Boolean) {
        val timeframe = _leaderboardState.value.selectedTimeframe
        viewModelScope.launch {
            val result = repo.refreshLeaderboard(timeframe, force)
            _leaderboardState.update {
                it.copy(
                    entries = result.entries,
                    lastUpdatedMillis = result.lastUpdatedMillis,
                    isLoading = false
                )
            }
        }
    }

    // --- Public events for the UI ---

    fun onLeaderboardTimeframeSelected(timeframe: Timeframe) {
        if (timeframe == _leaderboardState.value.selectedTimeframe) return

        _leaderboardState.update {
            it.copy(selectedTimeframe = timeframe, isLoading = true)
        }

        viewModelScope.launch {
            // Show cached first
            val cached = repo.getCachedLeaderboard(timeframe)
            _leaderboardState.update {
                it.copy(
                    entries = cached.entries,
                    lastUpdatedMillis = cached.lastUpdatedMillis
                )
            }

            // Then try remote (TTL / offline safe)
            refreshCurrentLeaderboard(force = false)
        }
    }

    fun onLeaderboardPullToRefresh() {
        if (_leaderboardState.value.isOffline) {
            // Pull-to-refresh disabled offline
            return
        }
        _leaderboardState.update { it.copy(isLoading = true) }
        refreshCurrentLeaderboard(force = true)
    }

}
