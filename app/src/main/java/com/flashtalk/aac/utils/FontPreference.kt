package com.flashtalk.aac.utils

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.flashtalk.aac.R

/**
 * The three font choices offered in onboarding/Settings. DEFAULT means "use
 * whatever the system/theme already provides" — no override, no Typeface to
 * load, nothing to apply.
 *
 * DYSLEXIA_FRIENDLY (OpenDyslexic) and EASY_READ (Atkinson Hyperlegible) are
 * both bundled, SIL Open Font License 1.1 fonts — see
 * THIRD_PARTY_LICENSES.md. They serve different goals: OpenDyslexic's
 * weighted letter bottoms aim to reduce letter rotation/confusion for
 * dyslexic readers, Atkinson Hyperlegible is designed for general low-vision
 * legibility, which is what "Easy Read" means in this app.
 */
object FontPreference {
    const val DEFAULT = "default"
    const val DYSLEXIA_FRIENDLY = "dyslexia"
    const val EASY_READ = "easy_read"

    // Pure — no Context, no resource lookup — so it's plain-JUnit testable.
    // Returns null for DEFAULT (and any unrecognised value): no font
    // resource to load, callers should leave the view's typeface alone.
    fun fontResIdFor(value: String): Int? = when (value) {
        DYSLEXIA_FRIENDLY -> R.font.opendyslexic
        EASY_READ -> R.font.atkinson_hyperlegible
        else -> null
    }

    // Context-dependent half of the above — actually loads the Typeface.
    // Returns null for DEFAULT, same contract as fontResIdFor.
    fun resolveTypeface(context: Context, value: String): Typeface? {
        val resId = fontResIdFor(value) ?: return null
        return ResourcesCompat.getFont(context, resId)
    }

    // Recursively applies a Typeface to every TextView (and subclass —
    // Button, EditText, …) under the given view. Used both for static
    // layouts (BaseActivity, once per setContentView) and for RecyclerView
    // item views (once per bind, from each adapter — a view walked here at
    // setContentView time doesn't exist yet when a RecyclerView inflates
    // its rows later).
    fun applyTo(view: View, typeface: Typeface) {
        if (view is TextView) {
            view.typeface = typeface
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyTo(view.getChildAt(i), typeface)
            }
        }
    }
}
