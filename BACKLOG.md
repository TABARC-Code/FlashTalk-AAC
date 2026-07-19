# Backlog

Prioritised. Work top-down — the ordering isn't decorative, it reflects
what actually matters more. Each item's sized roughly: S (under an hour),
M (half a day), L (a day or more, and probably an underestimate). Move
completed items to CHANGELOG.md rather than just deleting them; future-me
will want to know what happened here.

## P0 — the app is dishonest without these

_All clear as of the current build. See CHANGELOG.md for what got fixed
(seed card images, "Large text" actually doing something, delete cleaning
up after itself). I'm not pretending these were hard — they were mostly
embarrassing._

_Seed imagery specifically: the vocabulary now covers 266 real cards
across 7 categories, each with a hand-picked emoji glyph, sourced from a
CSV that's the actual seed data (not hardcoded Kotlin). A proper symbol
set is still an open, separate decision — whatever gets chosen has to
work for a commercial product, which rules out more than it lets in. Not
blocking anything; the emoji stand-in works fine for now._

## P1 — core UX gaps

1. **[M] Edit and delete for cards and categories.** Long-press → context
   menu (edit text, change image, delete with confirm). The category
   "Edit" menu item is currently a TODO toast, which I'm leaving as an
   honest placeholder rather than a silent no-op. Design constraint, and
   it's the annoying part of this ticket: entry to edit mode has to be
   hard to trigger by accident. A kid mid-communication landing in a
   delete confirmation isn't a UX nitpick, it's the app actively working
   against the person it's meant to help. Worth considering a
   settings-gated "edit mode" toggle instead of raw long-press.

2. **[M] Speech bar instead of Toasts.** Toasts are the wrong idiom here
   and I knew it going in — they vanish before anyone slower than me can
   read them, and there's no way to repeat what was just said. Replace
   with a persistent bar at the top of CategoryActivity showing the
   last-spoken word, with a repeat button. Every serious AAC app does
   this for a reason.

3. **[S] Settings lock.** A caregiver-facing concern, not a security
   one: users shouldn't be able to wander into Settings or Import
   mid-session and undo someone else's setup. A simple maths-question
   gate (standard in kids' apps for exactly this reason) on Settings,
   Import, and edit mode would do it.

4. **[M] Export.** Import exists and actually works (ZIP and JSON both);
   export doesn't exist at all, which is the more glaring gap of the two
   given there's no cloud backup as an alternative. Write a category out
   to the same ZIP+manifest format ImageSetImporter already reads.
   Round-trip test before calling it done: export → wipe → import →
   identical, byte for byte if you can manage it.

## P2 — engineering hygiene

5. **[S] Migrate kapt → KSP.** kapt is deprecated and everyone knows it;
   Room and Glide both support KSP already. Bump AGP/Gradle/dependencies
   to current stable while you're in there — no reason to do this twice.

6. **[M] More tests.** `AppDatabaseCsvTest` covers the CSV parser
   (quoted commas, doubled-quote escaping, empty fields) — that's a
   start, not the finish. Still need: ImageSetImporter's manifest
   parsing (happy path, malformed JSON, missing images — the zip-slip
   guard and the 50MB cap are exactly the kind of thing that breaks
   silently under a later refactor with no test to catch it), Repository
   CRUD against an in-memory Room database, and the DiffUtil callbacks.
   No UI tests yet. Foundation first, always.

7. **[S] Locale-aware seed data.** The vocabulary CSV is English-only.
   Since it's now an asset rather than hardcoded Kotlin, the honest path
   is locale-specific CSV variants (`communicards_vocabulary_v1_fr.csv`
   and so on) picked by device locale at seed time, not Android string
   resources — bulk-translating 266 rows through `strings.xml` would be
   miserable to maintain. TTS already follows the device locale via
   `Locale.getDefault()`; the vocabulary itself is what's still stuck in
   English.

8. **[S] Screenshots.** Take real ones on an emulator, drop them in
   `docs/screenshots/`. Nothing currently references phantom images, so
   this is purely additive — there's no lie to fix, just a gap to fill.

## P3 — future, only after the above

9. **[L] Sentence strip mode**, optional and off by default — tap cards
   to build a strip, tap the strip to speak the whole phrase.
   PECS-adjacent, and deliberately not the default experience.
10. **[L] Multiple profiles.**
11. **[M] Home-screen widget** for the Needs category.
12. **[L] Switch-access scanning support.**

## Rejected / not doing

- Cloud sync and accounts — conflicts with the offline/privacy commitment
  outright, not a "maybe later"
- Analytics of any kind — same reason, and no, "just crash reports"
  doesn't get a pass either
- Gamification — this is a communication tool, not a game, and treating
  it like one would be a genuinely bad idea for the people using it
