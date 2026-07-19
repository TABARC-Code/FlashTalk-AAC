# Backlog

Prioritised. Work top-down — the ordering isn't decorative, it reflects
what actually matters more. Each item's sized roughly: S (under an hour),
M (half a day), L (a day or more, and probably an underestimate). Move
completed items to CHANGELOG.md rather than just deleting them; future-me
will want to know what happened here.

## P0 — the app is dishonest without these

_All clear. See CHANGELOG.md for what got fixed (seed card images,
"Large text" actually doing something, delete cleaning up after itself).
I'm not pretending these were hard — they were mostly embarrassing._

_Seed imagery specifically: the vocabulary covers 266 real cards across
7 categories, each with a hand-picked emoji glyph, sourced from a CSV
that's the actual seed data (not hardcoded Kotlin). A proper symbol set
is still an open, separate decision — whatever gets chosen has to work
for a commercial product, which rules out more than it lets in. Not
blocking anything; the emoji stand-in works fine for now._

## P1 — core UX gaps

_Also all clear. Edit/delete (gated behind an off-by-default Edit mode
toggle), the speech bar, the Settings/Import maths gate, and category
export are all in and tested — see CHANGELOG.md. What's genuinely not
watched running yet: none of this has been seen on an actual device or
emulator, only compiled, linted, and unit-tested. That's worth keeping
in mind before calling any of it finished-finished._

## P2 — engineering hygiene

_kapt → KSP is done, and the dead Glide annotation processor came out
with it (this app never declared an `AppGlideModule`, so that dependency
was never doing anything). Also done: 30 unit tests across CSV parsing,
manifest parsing, DiffUtil callbacks, the export round-trip, and
Repository CRUD via Robolectric — up from the single CSV-parser test
this item started with._

1. **[S] Locale-aware seed data.** The vocabulary CSV is English-only.
   Since it's now an asset rather than hardcoded Kotlin, the honest path
   is locale-specific CSV variants (`communicards_vocabulary_v1_fr.csv`
   and so on) picked by device locale at seed time, not Android string
   resources — bulk-translating 266 rows through `strings.xml` would be
   miserable to maintain. TTS already follows the device locale via
   `Locale.getDefault()`; the vocabulary itself is what's still stuck in
   English.

2. **[S] Screenshots.** Take real ones on an emulator, drop them in
   `docs/screenshots/`. Nothing currently references phantom images, so
   this is purely additive — there's no lie to fix, just a gap to fill.

3. **[M] UI/instrumented tests.** Everything so far runs on the JVM
   (plain JUnit or Robolectric) precisely because there's no emulator in
   the environment this got built in. The actual on-screen behaviour of
   Edit mode, the speech bar, and the maths gate has never been watched
   running — only reasoned about and unit-tested at the logic layer.
   Espresso tests for the core tap-to-speak flow would close that gap.

## P3 — future, only after the above

4. **[L] Sentence strip mode**, optional and off by default — tap cards
   to build a strip, tap the strip to speak the whole phrase.
   PECS-adjacent, and deliberately not the default experience.
5. **[L] Multiple profiles.**
6. **[M] Home-screen widget** for the Needs category.
7. **[L] Switch-access scanning support.**

## Rejected / not doing

- Cloud sync and accounts — conflicts with the offline/privacy commitment
  outright, not a "maybe later"
- Analytics of any kind — same reason, and no, "just crash reports"
  doesn't get a pass either
- Gamification — this is a communication tool, not a game, and treating
  it like one would be a genuinely bad idea for the people using it
