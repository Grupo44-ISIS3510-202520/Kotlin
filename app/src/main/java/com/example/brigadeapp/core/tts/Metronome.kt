package com.example.brigadeapp.core.tts

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

// Metronome now runs a silent ticker (no beep) to drive timing for compressions
class Metronome(context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null
    private val isPlaying = AtomicBoolean(false)

    fun start() {
        if (isPlaying.getAndSet(true)) return
        job = scope.launch {
            while (isPlaying.get()) {
                // tick every 580ms (approx 103 BPM) without producing a sound
                delay(580)
            }
        }
    }

    fun stop() {
        isPlaying.set(false)
        job?.cancel()
        job = null
    }

    fun release() {
        stop()
    }
}