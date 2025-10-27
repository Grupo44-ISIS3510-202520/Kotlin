package com.example.brigadeapp.core.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import com.example.brigadeapp.R

class Metronome(context: Context) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    @RawRes
    private val beepSound = R.raw.beep

    private var beepId = 0
    private var isPlaying = false

    init {
        beepId = soundPool.load(context, beepSound, 1)
    }

    fun start() {
        if (!isPlaying && beepId != 0) {
            isPlaying = true
            playBeepLoop()
        }
    }

    private fun playBeepLoop() {
        if (!isPlaying) return

        soundPool.play(beepId, 2.0f, 2.0f, 1, 0, 1f)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            playBeepLoop()
        }, 580)
    }

    fun stop() {
        isPlaying = false
        soundPool.autoPause()
    }

    fun release() {
        stop()
        soundPool.release()
    }
}