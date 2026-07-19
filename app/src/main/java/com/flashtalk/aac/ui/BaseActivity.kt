package com.flashtalk.aac.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.flashtalk.aac.utils.TTSManager

/**
 * Applies the "Large text" preference as a Configuration.fontScale override
 * so every sp-based size in the app scales together — resolves BACKLOG.md
 * item 2 (the switch used to save a preference nothing read).
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences(TTSManager.PREFS_NAME, Context.MODE_PRIVATE)
        val largeText = prefs.getBoolean(TTSManager.KEY_LARGE_TEXT, false)
        val scale = if (largeText) LARGE_TEXT_FONT_SCALE else 1.0f

        val configuration = newBase.resources.configuration
        if (configuration.fontScale != scale) {
            configuration.fontScale = scale
            val scaledContext = newBase.createConfigurationContext(configuration)
            super.attachBaseContext(scaledContext)
        } else {
            super.attachBaseContext(newBase)
        }
    }

    companion object {
        private const val LARGE_TEXT_FONT_SCALE = 1.3f
    }
}
