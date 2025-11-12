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

    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

}