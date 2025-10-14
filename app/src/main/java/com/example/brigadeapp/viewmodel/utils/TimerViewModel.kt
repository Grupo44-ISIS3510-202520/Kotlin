package com.example.brigadeapp.viewmodel.utils

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor() : ViewModel() {
    private var startTime: Long? = null
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime

    fun startTimer() {
        startTime = System.currentTimeMillis()
    }

    fun stopTimer(): Long {
        val duration = System.currentTimeMillis() - (startTime ?: return 0L)
        _elapsedTime.value = duration
        return duration
    }
}
