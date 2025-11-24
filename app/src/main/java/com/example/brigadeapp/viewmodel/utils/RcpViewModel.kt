package com.example.brigadeapp.viewmodel.utils

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.helpers.tts.GuidanceService
import com.example.brigadeapp.domain.usecase.GetInstructionsUseCase
import com.example.brigadeapp.domain.usecase.GetCachedInstructionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RcpViewModel @Inject constructor(
    application: Application,
    private val getInstructionsUseCase: GetInstructionsUseCase,
    private val getCachedInstructionsUseCase: GetCachedInstructionsUseCase
) : AndroidViewModel(application) {

    private val _instructions = MutableStateFlow<List<String>>(emptyList())
    val instructions: StateFlow<List<String>> = _instructions
    val currentSpoken: StateFlow<String?> = GuidanceService.currentLine
    val isGuiding: StateFlow<Boolean> = GuidanceService.isRunning

    fun fetchInstructions(prompt: String) {
        viewModelScope.launch {
            val result = getInstructionsUseCase.invoke(prompt)
            _instructions.value = result
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