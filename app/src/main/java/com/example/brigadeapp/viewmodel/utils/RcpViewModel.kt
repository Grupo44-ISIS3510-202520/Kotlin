package com.example.brigadeapp.viewmodel.utils

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.helpers.tts.GuidanceService
import com.example.brigadeapp.domain.usecase.GetInstructionsUseCase
import com.example.brigadeapp.domain.usecase.GetCachedInstructionsUseCase
import com.example.brigadeapp.data.services.local.SyncPreferencesService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RcpViewModel @Inject constructor(
    application: Application,
    private val getInstructionsUseCase: GetInstructionsUseCase,
    private val getCachedInstructionsUseCase: GetCachedInstructionsUseCase,
    private val syncPreferences: SyncPreferencesService
) : AndroidViewModel(application) {

    private val _instructions = MutableStateFlow<List<String>>(emptyList())
    val instructions: StateFlow<List<String>> = _instructions
    val currentSpoken: StateFlow<String?> = GuidanceService.currentLine
    val isGuiding: StateFlow<Boolean> = GuidanceService.isRunning

    private val _lastOpenAIResponseTime = MutableStateFlow<Long>(0L)
    val lastOpenAIResponseTime: StateFlow<Long> = _lastOpenAIResponseTime

    init {
        refreshTimestamp()
        // Observe timestamp changes periodically when guidance is running
        viewModelScope.launch {
            isGuiding.collect { isRunning ->
                if (isRunning) {
                    // Refresh timestamp more aggressively at start, then slower
                    repeat(10) { // Check every 500ms for 5 seconds
                        delay(500)
                        refreshTimestamp()
                    }
                    // Then check every 2 seconds while still running
                    while (isGuiding.value) {
                        delay(2000)
                        refreshTimestamp()
                    }
                }
            }
        }
    }

    fun refreshTimestamp() {
        val currentTime = syncPreferences.getLastOpenAIResponseTime()
        _lastOpenAIResponseTime.value = currentTime
    }

    fun fetchInstructions(prompt: String) {
        viewModelScope.launch {
            val result = getInstructionsUseCase.invoke(prompt)
            _instructions.value = result
            refreshTimestamp()
        }
    }

    fun startGuidance() {
        if (!isGuiding.value) {
            GuidanceService.startGuidance(
                getApplication(),
                getInstructionsUseCase,
                getCachedInstructionsUseCase
            )
        }
    }

    fun stopGuidance() {
        GuidanceService.stopGuidance()
    }

    override fun onCleared() {
        super.onCleared()
    }
}