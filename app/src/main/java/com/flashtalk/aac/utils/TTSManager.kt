package com.flashtalk.aac.utils

import android.content.Context
import android.content.SharedPreferences
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * Reads speech rate/pitch from SharedPreferences on every speak() call so
 * that a change in SettingsActivity takes effect on the very next tap,
 * without needing to re-create this manager. Key names are the single
 * source of truth SettingsActivity must write to (CLAUDE.md invariant 4).
 */
class TTSManager(private val context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    interface InitializationListener {
        fun onInitialized(success: Boolean)
    }

    fun initialize(listener: InitializationListener? = null) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.let { tts ->
                    val result = tts.setLanguage(Locale.getDefault())
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts.setLanguage(Locale.US)
                    }
                    isInitialized = true
                    listener?.onInitialized(true)
                }
            } else {
                Log.e(TAG, "TTS initialization failed")
                isInitialized = false
                listener?.onInitialized(false)
            }
        }
    }

    fun speak(text: String) {
        if (!isInitialized) {
            initialize(object : InitializationListener {
                override fun onInitialized(success: Boolean) {
                    if (success) performSpeak(text)
                }
            })
        } else {
            performSpeak(text)
        }
    }

    private fun performSpeak(text: String) {
        val tts = textToSpeech ?: return
        tts.setSpeechRate(prefs.getFloat(KEY_SPEECH_RATE, DEFAULT_RATE))
        tts.setPitch(prefs.getFloat(KEY_PITCH, DEFAULT_PITCH))
        tts.stop()
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "flashtalk_utterance")
    }

    fun stop() {
        textToSpeech?.stop()
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }

    companion object {
        private const val TAG = "TTSManager"
        const val PREFS_NAME = "FlashTalkSettings"
        const val KEY_SPEECH_RATE = "speech_rate"
        const val KEY_PITCH = "pitch"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_LARGE_TEXT = "large_text"
        const val KEY_EDIT_MODE = "edit_mode"
        const val DEFAULT_RATE = 0.9f
        const val DEFAULT_PITCH = 1.0f
        const val RATE_MIN = 0.5f
        const val RATE_MAX = 2.0f
        const val PITCH_MIN = 0.5f
        const val PITCH_MAX = 2.0f
    }
}
