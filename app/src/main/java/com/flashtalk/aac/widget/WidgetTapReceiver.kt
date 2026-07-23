package com.flashtalk.aac.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.flashtalk.aac.utils.TTSManager
import java.util.Locale

/**
 * Speaks a single card tapped from NeedsWidgetProvider's GridView. A
 * one-shot TextToSpeech instance, not TTSManager — TTSManager is built
 * around a long-lived Activity holding it between speak() calls, which
 * doesn't fit a BroadcastReceiver's short, one-off lifecycle. Reads the
 * same rate/pitch prefs TTSManager does, so the widget doesn't quietly
 * ignore whatever a caregiver set in Settings.
 */
class WidgetTapReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val speechText = intent.getStringExtra(EXTRA_SPEECH_TEXT)
        if (speechText.isNullOrBlank()) return

        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(TTSManager.PREFS_NAME, Context.MODE_PRIVATE)
        val pendingResult = goAsync()
        val handler = Handler(Looper.getMainLooper())
        var tts: TextToSpeech? = null

        val finishOnce = {
            handler.removeCallbacksAndMessages(null)
            tts?.shutdown()
            pendingResult.finish()
        }
        // Safety net: onDone/onError should always fire, but a broadcast
        // receiver that never calls finish() risks an ANR — better to cut
        // the (already brief) utterance short than hang.
        handler.postDelayed({ finishOnce() }, SAFETY_TIMEOUT_MS)

        tts = TextToSpeech(appContext) { status ->
            if (status != TextToSpeech.SUCCESS) {
                finishOnce()
                return@TextToSpeech
            }
            tts?.let { engine ->
                val result = engine.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.setLanguage(Locale.US)
                }
                engine.setSpeechRate(prefs.getFloat(TTSManager.KEY_SPEECH_RATE, TTSManager.DEFAULT_RATE))
                engine.setPitch(prefs.getFloat(TTSManager.KEY_PITCH, TTSManager.DEFAULT_PITCH))
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) = finishOnce()

                    @Deprecated("Deprecated in Java, still the callback the platform invokes")
                    override fun onError(utteranceId: String?) = finishOnce()
                })
                engine.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
            }
        }
    }

    companion object {
        const val EXTRA_SPEECH_TEXT = "speech_text"
        private const val UTTERANCE_ID = "flashtalk_widget_utterance"
        private const val SAFETY_TIMEOUT_MS = 4000L
    }
}
