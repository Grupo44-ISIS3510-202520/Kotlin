package com.example.brigadeapp.helpers.tts

import android.content.Context
import android.util.Log
import com.example.brigadeapp.R
import com.example.brigadeapp.domain.usecase.GetInstructionsUseCase
import com.example.brigadeapp.data.source.local.RcpScript
import com.example.brigadeapp.domain.usecase.GetCachedInstructionsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.ref.WeakReference
import kotlin.coroutines.cancellation.CancellationException

object GuidanceService {
    private val _currentLine = MutableStateFlow<String?>(null)
    val currentLine: StateFlow<String?> get() = _currentLine

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> get() = _isRunning

    private var voiceRef: WeakReference<VoiceGuidance>? = null
    private var metronomeRef: WeakReference<Metronome>? = null
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
            metro = Metronome(context.applicationContext, playSound = playSound)
            metronomeRef = WeakReference(metro)
        }
        return metro
    }

    fun startGuidance(
        context: Context,
        getInstructions: GetInstructionsUseCase,
        getCachedInstructions: GetCachedInstructionsUseCase
    ) {
        if (running.getAndSet(true)) return
        _isRunning.value = true

        val voice = getOrCreateVoice(context)

        currentJob = CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
            val prompt = context.getString(R.string.RCP_Prompt)

            val cached = try {
                getCachedInstructions.invoke(prompt)
            } catch (e: Exception) {
                null
            }

            val instructions: List<Any>
            val playSound: Boolean

            if (!cached.isNullOrEmpty()) {
                instructions = cached
                playSound = false
            } else {
                instructions = RcpScript.initialSteps
                playSound = true

                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        getInstructions.invoke(prompt)
                    } catch (e: Exception) {
                        Log.e("GuidanceService", "Background fetch failed: ${e.message}")
                    }
                }
            }

            val metronome = getOrCreateMetronome(context, playSound = playSound)
            val firstLine: Any = instructions[0]

            for (i in 0 until instructions.size) {
                if (!running.get()) return@launch
                val getString: Any = instructions[i]
                val step = if (getString is Int) {
                    context.getString(getString)
                } else {
                    getString
                }
                _currentLine.value = step as String
                voice.initializeIfNeeded(context)
                voice.speak(step)
                try {
                    delay(7000)
                } catch (e: CancellationException) {
                    return@launch
                }
            }

            // Begin compressions guidance
            if (firstLine is Int) {
                try {
                    delay(1500)
                } catch (e: CancellationException) {
                    return@launch
                }

                if (!running.get()) return@launch
                val startMsg = context.getString(RcpScript.START_COMPRESSIONS)
                _currentLine.value = startMsg
                voice.speak(startMsg)

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

                    _currentLine.value = null
                    val currentCycle = cycle + 1
                    if (currentCycle % changeInterval == 0) {
                        if (!running.get()) return@launch
                        val nextMsg = context.getString(RcpScript.NEXT_CYCLE)
                        _currentLine.value = nextMsg
                        voice.speak(nextMsg)
                    }
                }
                metronome.stop()
                if (running.get()) {
                    _currentLine.value = context.getString(RcpScript.STOP_COMPRESSIONS)
                    voice.speak(context.getString(RcpScript.STOP_COMPRESSIONS))
                }
            }
            } finally {
                running.set(false)
                _currentLine.value = null
                _isRunning.value = false
            }
        }
    }

    fun stopGuidance() {
        running.set(false)

        currentJob?.cancel()
        currentJob = null
        metronomeRef?.get()?.stop()
        voiceRef?.get()?.stopSpeaking()
        _currentLine.value = null
        _isRunning.value = false
    }

    fun release() {
        running.set(false)
        metronomeRef?.get()?.release()
        voiceRef?.get()?.shutdown()
        voiceRef?.clear()
        metronomeRef?.clear()
        _currentLine.value = null
        _isRunning.value = false
    }
}
