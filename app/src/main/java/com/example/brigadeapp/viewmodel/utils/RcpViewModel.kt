package com.example.brigadeapp.viewmodel.utils

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brigadeapp.core.tts.Metronome
import com.example.brigadeapp.core.tts.VoiceGuidance
import com.example.brigadeapp.domain.usecase.RcpScript
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RcpViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val voiceGuidance = VoiceGuidance(application.applicationContext)
    private val metronome = Metronome(application.applicationContext)
    private var isGuiding = false

    fun startGuidance() {
        if (isGuiding) return
        isGuiding = true

        voiceGuidance.initialize {
            viewModelScope.launch {
                // Step 1: Initial instructions
                RcpScript.initialSteps.forEach { step ->
                    voiceGuidance.speak(step)
                    delay(9000) // Wait 9 seconds per instruction
                }

                // Step 2: Begin compressions
                delay(1500)

                runCprGuidance()
                voiceGuidance.speak(RcpScript.STOP_COMPRESSIONS)
                onCleared()
            }
        }
    }

    suspend fun runCprGuidance() = coroutineScope {
        voiceGuidance.speak(RcpScript.START_COMPRESSIONS)
        delay(1500)
        metronome.start()

        val totalCycles = 120
        val changeInterval = 30

        repeat(totalCycles) { cycle ->
            delay(1000)

            val currentCycle = cycle + 1
            if (currentCycle % changeInterval == 0) {
                voiceGuidance.speak(RcpScript.NEXT_CYCLE)
            }
        }

        metronome.stop()
    }

    fun stopGuidance() {
        isGuiding = false
        metronome.stop()
        voiceGuidance.shutdown()
    }

    override fun onCleared() {
        super.onCleared()
        metronome.stop()
        voiceGuidance.shutdown()
    }
}