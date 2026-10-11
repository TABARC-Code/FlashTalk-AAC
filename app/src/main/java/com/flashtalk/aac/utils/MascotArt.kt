package com.flashtalk.aac.utils

/**
 * Resolves a free-form mascot "pose" name (FlashCard.mascotPose, e.g.
 * "happy" or "distressed") — optionally combined with a chosen "character"
 * (which mascot — the dino, or whatever else gets added later) and
 * "season" (a seasonal reskin, e.g. "halloween") — to the Android drawable
 * resource name(s) it'd need to exist as. All three are free-form strings,
 * not a fixed enum: CLAUDE.md invariant 15/17 — the character/pose/season
 * taxonomy is the mascot designer's creative call, not something to invent
 * here. "mascot" artwork is deliberately spelled out this plainly rather
 * than abbreviated, since resource names are the one place this scheme is
 * actually documented for whoever eventually names the asset files.
 *
 * Pure — no Context, no resource lookup — so it's plain-JUnit testable.
 * The actual "does this drawable exist in this build" check needs a
 * Context (Resources.getIdentifier) and lives in FlashCardAdapter, right
 * next to the rendering decision it feeds.
 */
object MascotArt {
    private const val PREFIX = "mascot_"

    fun resourceNameFor(pose: String): String? {
        val sanitisedPose = sanitise(pose)
        return sanitisedPose.ifBlank { null }?.let { PREFIX + it }
    }

    // The full fallback chain to try, most specific first:
    //   mascot_<character>_<season>_<pose>  (a character's seasonal pose)
    //   mascot_<character>_<pose>           (that character's ordinary pose)
    //   mascot_<pose>                       (no character chosen/matched —
    //                                        same name resourceNameFor
    //                                        already produces, so existing
    //                                        mascot_<pose> artwork from
    //                                        before character/season
    //                                        existed keeps working)
    // character and season are both optional — blank means "not set," not
    // an error — so a caregiver who's only picked a pose keeps working
    // exactly as before this was added.
    fun resourceNameCandidates(character: String, season: String, pose: String): List<String> {
        val sanitisedPose = sanitise(pose).ifBlank { return emptyList() }
        val sanitisedCharacter = sanitise(character)
        val sanitisedSeason = sanitise(season)

        val candidates = mutableListOf<String>()
        if (sanitisedCharacter.isNotBlank() && sanitisedSeason.isNotBlank()) {
            candidates += "$PREFIX${sanitisedCharacter}_${sanitisedSeason}_$sanitisedPose"
        }
        if (sanitisedCharacter.isNotBlank()) {
            candidates += "$PREFIX${sanitisedCharacter}_$sanitisedPose"
        }
        candidates += "$PREFIX$sanitisedPose"
        return candidates.distinct()
    }

    private fun sanitise(value: String): String =
        value.trim().lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
}
