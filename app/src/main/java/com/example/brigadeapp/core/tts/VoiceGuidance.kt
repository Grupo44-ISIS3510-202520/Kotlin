package com.example.brigadeapp.core.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceGuidance(private val context: Context) {
    private var tts: TextToSpeech? = null

    fun initialize(onInit: () -> Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.forLanguageTag("en-US")

                tts?.setSpeechRate(0.85f)
                tts?.setPitch(1.0f)
                onInit()
            }
        }
    }

    // Ensure tts is initialized; used by GuidanceService when speaking from background
    fun initializeIfNeeded(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.forLanguageTag("en-US")
                    tts?.setSpeechRate(0.85f)
                    tts?.setPitch(1.0f)
                }
            }
        }
    }

    fun speak(text: String) {
        // Use QUEUE_ADD so queued guidance continues smoothly
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, null)
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

}