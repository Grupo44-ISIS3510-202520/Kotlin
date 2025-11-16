package com.example.brigadeapp.core.tts

import android.content.Context
import com.example.brigadeapp.data.repository.OpenAIImpl
import com.example.brigadeapp.domain.usecase.RcpScript
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.lang.ref.WeakReference
import kotlin.coroutines.cancellation.CancellationException

object GuidanceService {

    private var voiceRef: WeakReference<VoiceGuidance>? = null
    private var metronomeRef: WeakReference<Metronome>? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val running = AtomicBoolean(false)
    private var currentJob: Job? = null

    private fun getOrCreateVoice(context: Context): VoiceGuidance {
        var voice = voiceRef?.get()
        if (voice == null) {
            voice = VoiceGuidance(context.applicationContext)
            voiceRef = WeakReference(voice)
        }
        return voice
    }

    private fun getOrCreateMetronome(context: Context, playSound: Boolean): Metronome {
        var metro = metronomeRef?.get()

        if (metro == null) {
            metro?.release()
            metro = Metronome(context.applicationContext, playSound = playSound)
            metronomeRef = WeakReference(metro)
        }
        return metro
    }

    fun startGuidance(context: Context, isOnline: Boolean, openAI: OpenAIImpl) {
        if (running.getAndSet(true)) return

        val voice = getOrCreateVoice(context)
        val metronome = getOrCreateMetronome(context, playSound = !isOnline)

        currentJob = scope.launch {
            val instructions: List<String> = try {
                if (isOnline) {
                    openAI.getInstructions("Give me the steps for CPR")
                } else {
                    RcpScript.initialSteps
                }
            } catch (e: Exception) {
                RcpScript.initialSteps
            }

            for (step in instructions) {
                if (!running.get()) return@launch
                voice.initializeIfNeeded(context)
                voice.speak(step)
                try {
                    delay(9000)
                } catch (e: CancellationException) {
                    return@launch
                }
            }

            // Begin compressions guidance
            try {
                delay(1500)
            } catch (e: CancellationException) {
                return@launch
            }

            if (!running.get()) return@launch
            voice.speak(RcpScript.START_COMPRESSIONS)

            try {
                delay(1500)
            } catch (e: CancellationException) {
                return@launch
            }

            if (!running.get()) return@launch
            metronome.start()

            val totalCycles = 120
            val changeInterval = 30

            repeat(totalCycles) { cycle ->
                if (!running.get()) return@launch
                try {
                    delay(1000)
                } catch (e: CancellationException) {
                    return@launch
                }

                val currentCycle = cycle + 1
                if (currentCycle % changeInterval == 0) {
                    if (!running.get()) return@launch
                    voice.speak(RcpScript.NEXT_CYCLE)
                }
            }
            metronome.stop()
            if (running.get()) {
                voice.speak(RcpScript.STOP_COMPRESSIONS)
            }
            running.set(false)
        }
    }

    fun stopGuidance() {
        running.set(false)

        currentJob?.cancel()
        currentJob = null
        metronomeRef?.get()?.stop()
        voiceRef?.get()?.stopSpeaking()
    }

    fun release() {
        running.set(false)
        metronomeRef?.get()?.release()
        voiceRef?.get()?.shutdown()
        voiceRef?.clear()
        metronomeRef?.clear()
    }
}
