package com.flashtalk.aac.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.flashtalk.aac.R
import com.flashtalk.aac.utils.FontPreference
import com.flashtalk.aac.utils.TTSManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch

/**
 * Shown exactly once, before MainActivity ever builds its UI (see
 * MainActivity.onCreate) — gated by TTSManager.KEY_ONBOARDING_COMPLETE.
 * Deliberately not a BaseActivity: there's no profile/category data to
 * theme yet, and the font-style preference this screen is setting doesn't
 * exist as a stored value until the user actually picks one, so there's
 * nothing for BaseActivity's setContentView hook to apply on first show —
 * the live font preview below handles that instead, directly.
 */
class OnboardingActivity : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences(TTSManager.PREFS_NAME, Context.MODE_PRIVATE) }
    private var selectedFontStyle: String = FontPreference.DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        setupFontPreview()
        setupGetStartedButton()
    }

    private fun setupFontPreview() {
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupFontStyle)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedFontStyle = when (checkedId) {
                R.id.radioFontDyslexia -> FontPreference.DYSLEXIA_FRIENDLY
                R.id.radioFontEasyRead -> FontPreference.EASY_READ
                else -> FontPreference.DEFAULT
            }
            previewFont(selectedFontStyle)
        }
    }

    // Applies the chosen font to this screen immediately, so choosing is a
    // direct comparison rather than a leap of faith — doesn't touch
    // SharedPreferences, that only happens once "Get Started" is tapped.
    private fun previewFont(fontStyle: String) {
        val typeface = FontPreference.resolveTypeface(this, fontStyle) ?: return
        FontPreference.applyTo(findViewById(android.R.id.content), typeface)
    }

    private fun setupGetStartedButton() {
        findViewById<MaterialButton>(R.id.buttonGetStarted).setOnClickListener {
            val pureTextMode = findViewById<MaterialSwitch>(R.id.switchPureTextMode).isChecked
            prefs.edit()
                .putBoolean(TTSManager.KEY_PURE_TEXT_MODE, pureTextMode)
                .putString(TTSManager.KEY_FONT_STYLE, selectedFontStyle)
                .putBoolean(TTSManager.KEY_ONBOARDING_COMPLETE, true)
                .apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
