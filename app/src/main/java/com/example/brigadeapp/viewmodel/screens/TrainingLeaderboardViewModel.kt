package com.example.brigadeapp.viewmodel.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.domain.entity.LeaderboardEntry
import com.example.brigadeapp.domain.entity.Timeframe
import com.example.brigadeapp.domain.repository.TrainingRepository
import com.example.brigadeapp.domain.usecase.ObserveConnectivityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaderboardUiState(
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val selectedTimeframe: Timeframe = Timeframe.ALL_TIME,
    val entries: List<LeaderboardEntry> = emptyList(),
    val lastUpdatedMillis: Long? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class TrainingLeaderboardViewModel @Inject constructor(
    private val repo: TrainingRepository,
    observeConnectivityUseCase: ObserveConnectivityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState

    init {
        var wasOffline = false
        viewModelScope.launch {
            observeConnectivityUseCase().collect { isOnline ->
                Log.d("TrainingLeaderboardViewModel", "Connectivity changed: isOnline=$isOnline")

                _uiState.update { it.copy(isOffline = !isOnline) }

                if (isOnline && wasOffline) {
                    Log.d("TrainingLeaderboardViewModel", "Reconnected! Refreshing leaderboard...")
                    refreshCurrentLeaderboard(force = true)
                }

                wasOffline = !isOnline
            }
        }

        // Initial load
        viewModelScope.launch {
            loadInitialLeaderboard()
        }
    }

    private suspend fun loadInitialLeaderboard() {
        val timeframe = _uiState.value.selectedTimeframe

        // 1) Emit cached data immediately
        val cached = repo.getCachedLeaderboard(timeframe)
        _uiState.update {
            it.copy(
                entries = cached.entries,
                lastUpdatedMillis = cached.lastUpdatedMillis,
                isLoading = true
            )
        }

        // 2) Try remote refresh
        refreshCurrentLeaderboard(force = false)
    }

    private fun refreshCurrentLeaderboard(force: Boolean) {
        val timeframe = _uiState.value.selectedTimeframe
        viewModelScope.launch {
            val result = repo.refreshLeaderboard(timeframe, force)
            _uiState.update {
                it.copy(
                    entries = result.entries,
                    lastUpdatedMillis = result.lastUpdatedMillis,
                    isLoading = false
                )
            }
        }
    }

    fun onTimeframeSelected(timeframe: Timeframe) {
        if (timeframe == _uiState.value.selectedTimeframe) return

        _uiState.update {
            it.copy(selectedTimeframe = timeframe, isLoading = true)
        }

        viewModelScope.launch {
            // Show cached first
            val cached = repo.getCachedLeaderboard(timeframe)
            _uiState.update {
                it.copy(
                    entries = cached.entries,
                    lastUpdatedMillis = cached.lastUpdatedMillis
                )
            }

            // Then try remote
            refreshCurrentLeaderboard(force = false)
        }
    }

    fun onPullToRefresh() {
        if (_uiState.value.isOffline) {
            return
        }
        _uiState.update { it.copy(isLoading = true) }
        refreshCurrentLeaderboard(force = true)
    }
}
