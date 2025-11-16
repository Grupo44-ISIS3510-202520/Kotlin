package com.example.brigadeapp.core.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import com.example.brigadeapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Metronome supports two modes:
 * - playSound = true -> uses SoundPool to play the beep (used when offline)
 * - playSound = false -> silent ticker (no audible beep)
 */
class Metronome(context: Context, private val playSound: Boolean) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null
    private val isPlaying = AtomicBoolean(false)

    // SoundPool resources
    private var soundPool: SoundPool? = null
    @RawRes
    private val beepSound = R.raw.beep
    private var beepId = 0

    init {
        if (playSound) {
            soundPool = SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .build()
            // load may be async; keep id
            soundPool?.let { sp ->
                beepId = sp.load(context, beepSound, 1)
            }
        }
    }

    fun start() {
        if (isPlaying.getAndSet(true)) return

        if (playSound) {
            // start playing beeps via handler loop on main thread
            playBeepLoop()
        } else {
            job = scope.launch {
                while (isPlaying.get()) {
                    delay(580)
                }
            }
        }
    }

    private fun playBeepLoop() {
        if (!isPlaying.get()) return
        val sp = soundPool ?: return
        if (beepId != 0) {
            sp.play(beepId, 2.0f, 2.0f, 1, 0, 1f)
        }
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            playBeepLoop()
        }, 580)
    }

    fun stop() {
        isPlaying.set(false)
        job?.cancel()
        job = null
        soundPool?.autoPause()
    }

    fun release() {
        stop()
        soundPool?.release()
        soundPool = null
    }
}