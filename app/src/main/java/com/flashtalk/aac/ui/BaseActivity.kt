package com.flashtalk.aac.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.flashtalk.aac.utils.FontPreference
import com.flashtalk.aac.utils.TTSManager

/**
 * Applies two cross-cutting display preferences every Activity needs,
 * before/after the usual lifecycle points so subclasses don't each have to
 * remember to:
 * - "Large text": a Configuration.fontScale override — resolves BACKLOG.md
 *   item 2 (the switch used to save a preference nothing read).
 * - Font style (default/dyslexia-friendly/Easy Read, CLAUDE.md invariant
 *   16): applied to every TextView under the content view, right after
 *   setContentView inflates it. This only covers each Activity's own
 *   static layout — a RecyclerView's item views don't exist yet at this
 *   point, so adapters that render user-facing text (CategoryAdapter,
 *   FlashCardAdapter, ProfileAdapter) apply the same FontPreference.applyTo
 *   themselves, at bind time.
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

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        applyFontPreference()
    }

    override fun setContentView(view: View) {
        super.setContentView(view)
        applyFontPreference()
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams) {
        super.setContentView(view, params)
        applyFontPreference()
    }

    private fun applyFontPreference() {
        val prefs = getSharedPreferences(TTSManager.PREFS_NAME, Context.MODE_PRIVATE)
        val fontStyle = prefs.getString(TTSManager.KEY_FONT_STYLE, FontPreference.DEFAULT) ?: FontPreference.DEFAULT
        val typeface = FontPreference.resolveTypeface(this, fontStyle) ?: return
        val content = findViewById<View>(android.R.id.content) ?: return
        FontPreference.applyTo(content, typeface)
    }

    companion object {
        private const val LARGE_TEXT_FONT_SCALE = 1.3f
    }
}
