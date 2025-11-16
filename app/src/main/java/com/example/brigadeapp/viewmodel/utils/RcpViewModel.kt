package com.example.brigadeapp.viewmodel.utils

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.core.tts.GuidanceService
import com.example.brigadeapp.data.repository.OpenAIImpl
import com.example.brigadeapp.domain.usecase.RcpScript
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RcpViewModel @Inject constructor(
    application: Application,
    private val openAI: OpenAIImpl
) : AndroidViewModel(application) {

    private var isGuiding = false

    private val _instructions = MutableStateFlow<List<String>>(emptyList())
    val instructions: StateFlow<List<String>> = _instructions

    fun fetchInstructions(prompt: String) {
        viewModelScope.launch {
            val result = openAI.getInstructions(prompt)
            _instructions.value = result
        }
    }

    fun startGuidance(isOnline: Boolean) {
        if (isGuiding) return
        isGuiding = true
        GuidanceService.startGuidance(getApplication(), isOnline, openAI)
    }

    fun stopGuidance() {
        isGuiding = false
        GuidanceService.stopGuidance()
    }

    override fun onCleared() {
        super.onCleared()
    }
}