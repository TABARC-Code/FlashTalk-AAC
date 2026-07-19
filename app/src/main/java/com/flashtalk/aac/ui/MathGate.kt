package com.flashtalk.aac.ui

import android.content.Context
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.flashtalk.aac.R

/**
 * Not real security — a caregiver-facing speed bump so a curious or
 * impulsive user doesn't wander into Settings or Import mid-session
 * (BACKLOG.md P1 item 3). Gating Settings also covers the Edit mode
 * toggle inside it, so that doesn't need a second gate of its own.
 */
object MathGate {
    fun show(context: Context, onPassed: () -> Unit) {
        val a = (2..9).random()
        val b = (2..9).random()

        val input = EditText(context).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(context)
            .setTitle(R.string.settings_lock_title)
            .setMessage(context.getString(R.string.settings_lock_message, a, b))
            .setView(input)
            .setPositiveButton(R.string.continue_action) { _, _ ->
                if (input.text.toString().trim().toIntOrNull() == a + b) {
                    onPassed()
                } else {
                    Toast.makeText(context, R.string.settings_lock_wrong, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
