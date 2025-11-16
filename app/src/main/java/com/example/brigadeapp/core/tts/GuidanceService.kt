package com.example.brigadeapp.core.tts

import android.content.Context
import com.example.brigadeapp.data.repository.OpenAIImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

object GuidanceService {
    private var voice: VoiceGuidance? = null
    private var metronome: Metronome? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val running = AtomicBoolean(false)
    private var currentJob: kotlinx.coroutines.Job? = null

    private fun ensureVoiceInit(context: Context) {
        if (voice == null) voice = VoiceGuidance(context.applicationContext)
    }

    fun startGuidance(context: Context, isOnline: Boolean, openAI: OpenAIImpl) {
        if (running.getAndSet(true)) return
        ensureVoiceInit(context)

        // create metronome based on connectivity: play sound only when offline
        metronome?.release()
        metronome = Metronome(context.applicationContext, playSound = !isOnline)

        currentJob = scope.launch {
            val instructions: List<String> = try {
                if (isOnline) {
                    openAI.getInstructions("Give me the steps for CPR")
                } else {
                    com.example.brigadeapp.domain.usecase.RcpScript.initialSteps
                }
            } catch (e: Exception) {
                com.example.brigadeapp.domain.usecase.RcpScript.initialSteps
            }

            // Speak initial instructions
            for (step in instructions) {
                if (!running.get()) return@launch
                voice?.initializeIfNeeded(context)
                // speak only if still running; speak will be cancelled if job is cancelled
                voice?.speak(step)
                // wait for the instruction duration; if stopped, cancel
                try {
                    kotlinx.coroutines.delay(9000)
                } catch (e: kotlinx.coroutines.CancellationException) {
                    return@launch
                }
            }

            // Begin compressions guidance
            try {
                kotlinx.coroutines.delay(1500)
            } catch (e: kotlinx.coroutines.CancellationException) {
                return@launch
            }

            if (!running.get()) return@launch
            voice?.speak(com.example.brigadeapp.domain.usecase.RcpScript.START_COMPRESSIONS)

            try {
                kotlinx.coroutines.delay(1500)
            } catch (e: kotlinx.coroutines.CancellationException) {
                return@launch
            }

            if (!running.get()) return@launch
            metronome?.start()

            val totalCycles = 120
            val changeInterval = 30

            repeat(totalCycles) { cycle ->
                if (!running.get()) return@launch
                try {
                    kotlinx.coroutines.delay(1000)
                } catch (e: kotlinx.coroutines.CancellationException) {
                    return@launch
                }

                val currentCycle = cycle + 1
                if (currentCycle % changeInterval == 0) {
                    if (!running.get()) return@launch
                    voice?.speak(com.example.brigadeapp.domain.usecase.RcpScript.NEXT_CYCLE)
                }
            }

            metronome?.stop()
            if (running.get()) {
                voice?.speak(com.example.brigadeapp.domain.usecase.RcpScript.STOP_COMPRESSIONS)
            }
            running.set(false)
        }
    }

    fun stopGuidance() {
        running.set(false)
        // cancel running job so it doesn't enqueue further speech or start metronome
        currentJob?.cancel()
        currentJob = null
        metronome?.stop()
        voice?.stopSpeaking()
        // do not shutdown TTS to allow continuity when returning to screen
    }

    fun release() {
        running.set(false)
        metronome?.release()
        voice?.shutdown()
        voice = null
        metronome = null
    }
}
