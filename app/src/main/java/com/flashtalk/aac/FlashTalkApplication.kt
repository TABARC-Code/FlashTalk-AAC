package com.flashtalk.aac

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.flashtalk.aac.utils.TTSManager

class FlashTalkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences(TTSManager.PREFS_NAME, Context.MODE_PRIVATE)
        val darkMode = prefs.getBoolean(TTSManager.KEY_DARK_MODE, false)
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
