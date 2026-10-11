package com.flashtalk.aac.ui

import android.content.Context
import android.os.Bundle
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import com.flashtalk.aac.R
import com.flashtalk.aac.utils.FontPreference
import com.flashtalk.aac.utils.TTSManager
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider

class SettingsActivity : BaseActivity() {

    private val prefs by lazy { getSharedPreferences(TTSManager.PREFS_NAME, Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRateSlider()
        setupPitchSlider()
        setupDarkModeSwitch()
        setupLargeTextSwitch()
        setupEditModeSwitch()
        setupSentenceStripModeSwitch()
        setupMascotModeSwitch()
        setupPureTextModeSwitch()
        setupFontStyleOptions()
    }

    private fun setupRateSlider() {
        findViewById<Slider>(R.id.sliderSpeechRate).apply {
            valueFrom = TTSManager.RATE_MIN
            valueTo = TTSManager.RATE_MAX
            value = prefs.getFloat(TTSManager.KEY_SPEECH_RATE, TTSManager.DEFAULT_RATE)
                .coerceIn(TTSManager.RATE_MIN, TTSManager.RATE_MAX)
            addOnChangeListener { _, newValue, _ ->
                prefs.edit().putFloat(TTSManager.KEY_SPEECH_RATE, newValue).apply()
            }
        }
    }

    private fun setupPitchSlider() {
        findViewById<Slider>(R.id.sliderPitch).apply {
            valueFrom = TTSManager.PITCH_MIN
            valueTo = TTSManager.PITCH_MAX
            value = prefs.getFloat(TTSManager.KEY_PITCH, TTSManager.DEFAULT_PITCH)
                .coerceIn(TTSManager.PITCH_MIN, TTSManager.PITCH_MAX)
            addOnChangeListener { _, newValue, _ ->
                prefs.edit().putFloat(TTSManager.KEY_PITCH, newValue).apply()
            }
        }
    }

    private fun setupDarkModeSwitch() {
        findViewById<MaterialSwitch>(R.id.switchDarkMode).apply {
            isChecked = prefs.getBoolean(TTSManager.KEY_DARK_MODE, false)
            setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean(TTSManager.KEY_DARK_MODE, isChecked).apply()
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }
    }

    private fun setupLargeTextSwitch() {
        findViewById<MaterialSwitch>(R.id.switchLargeText).apply {
            isChecked = prefs.getBoolean(TTSManager.KEY_LARGE_TEXT, false)
            setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean(TTSManager.KEY_LARGE_TEXT, isChecked).apply()
                recreate() // re-applies BaseActivity#attachBaseContext with the new fontScale
            }
        }
    }

    private fun setupEditModeSwitch() {
        findViewById<MaterialSwitch>(R.id.switchEditMode).apply {
            isChecked = prefs.getBoolean(TTSManager.KEY_EDIT_MODE, false)
            setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean(TTSManager.KEY_EDIT_MODE, isChecked).apply()
            }
        }
    }

    private fun setupSentenceStripModeSwitch() {
        findViewById<MaterialSwitch>(R.id.switchSentenceStripMode).apply {
            isChecked = prefs.getBoolean(TTSManager.KEY_SENTENCE_STRIP_MODE, false)
            setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean(TTSManager.KEY_SENTENCE_STRIP_MODE, isChecked).apply()
            }
        }
    }

    private fun setupMascotModeSwitch() {
        findViewById<MaterialSwitch>(R.id.switchMascotMode).apply {
            isChecked = prefs.getBoolean(TTSManager.KEY_MASCOT_MODE, false)
            setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean(TTSManager.KEY_MASCOT_MODE, isChecked).apply()
            }
        }
    }

    private fun setupPureTextModeSwitch() {
        findViewById<MaterialSwitch>(R.id.switchPureTextMode).apply {
            isChecked = prefs.getBoolean(TTSManager.KEY_PURE_TEXT_MODE, false)
            setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean(TTSManager.KEY_PURE_TEXT_MODE, isChecked).apply()
            }
        }
    }

    private fun setupFontStyleOptions() {
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupFontStyle)
        val checkedId = when (prefs.getString(TTSManager.KEY_FONT_STYLE, FontPreference.DEFAULT)) {
            FontPreference.DYSLEXIA_FRIENDLY -> R.id.radioFontDyslexia
            FontPreference.EASY_READ -> R.id.radioFontEasyRead
            else -> R.id.radioFontDefault
        }
        radioGroup.check(checkedId)
        radioGroup.setOnCheckedChangeListener { _, newCheckedId ->
            val fontStyle = when (newCheckedId) {
                R.id.radioFontDyslexia -> FontPreference.DYSLEXIA_FRIENDLY
                R.id.radioFontEasyRead -> FontPreference.EASY_READ
                else -> FontPreference.DEFAULT
            }
            prefs.edit().putString(TTSManager.KEY_FONT_STYLE, fontStyle).apply()
            recreate() // re-applies BaseActivity#setContentView's font pass with the new choice
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
