package com.flashtalk.aac.utils

/**
 * Resolves a free-form mascot "pose" name (FlashCard.mascotPose, e.g.
 * "happy" or "distressed") to the Android drawable resource name it'd need
 * to exist as — "mascot_happy", "mascot_distressed" — lowercased and
 * sanitised to valid resource-name characters.
 *
 * Pure — no Context, no resource lookup — so it's plain-JUnit testable.
 * The actual "does mascot_happy.png exist in this build" check needs a
 * Context (Resources.getIdentifier) and lives in FlashCardAdapter, right
 * next to the rendering decision it feeds.
 */
object MascotArt {
    private const val PREFIX = "mascot_"

    fun resourceNameFor(pose: String): String? {
        val sanitised = pose.trim().lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
        return sanitised.ifBlank { null }?.let { PREFIX + it }
    }
}
