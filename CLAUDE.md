# CLAUDE.md — FlashTalk AAC

Project context for Claude Code. Read this before touching anything —
genuinely, most of the mistakes I'd expect an agent to make here are
avoidable just by reading this file first.

## What this is

An Android AAC (Augmentative and Alternative Communication) app for
autistic users. Single-tap flashcard communication: tap a card, it
speaks. That's the whole product, and I mean that literally — resist the
urge to make it cleverer. Simplicity is the feature, not a placeholder
for features that haven't arrived yet.

Author: TABARC-Code. UK English throughout — code comments, strings,
docs, commit messages, the lot. "Mum" not "Mom", "colour" in prose (the
Android APIs use `color`, obviously, that's not negotiable, don't go
renaming SDK methods to make a point).

## Stack

- Kotlin, MVVM, Room + LiveData, Coroutines, Glide, Material Components
- minSdk 24, targetSdk 34
- Single module: `app/`
- No DI framework, deliberately. The app's small enough that Hilt would
  be ceremony, not architecture. Don't add it without asking first.
- KSP for annotation processing (Room), not kapt — kapt's gone entirely.
  Glide's compiler artifact is gone too, not migrated: this app never
  declares an `AppGlideModule`, so it was doing nothing.

## Build and test

```bash
./gradlew assembleDebug        # build
./gradlew installDebug         # install to connected device
./gradlew test                 # unit tests — 38 of them, see below
./gradlew lint                 # always run before committing
```

Tests run on the JVM via Robolectric where they touch Room/Context
(`AppRepositoryTest`); everything else (CSV parsing, manifest parsing,
DiffUtil callbacks, the exporter) is plain, Context-free JUnit. No
emulator or device needed for any of it, which matters in an environment
without one.

## Architecture map

```
data/     Room entities (Category, FlashCard), DAOs, AppDatabase (seeds
          from assets/vocabulary/ on first run — 266 cards, 7 categories,
          real production vocabulary, not a toy set. File picked by
          device locale, e.g. communicards_vocabulary_v1_fr.csv, falling
          back to the English default — see vocabularyAssetNameFor),
          AppRepository
ui/       One activity per screen + ViewModel + adapter. Main (category
          grid) → Category (card grid) → tap speaks via TTSManager.
          BaseActivity applies the "Large text" font-scale override that
          every other activity extends. MathGate is a shared maths-
          question dialog gating Settings/Import entry.
utils/    TTSManager (reads rate/pitch from SharedPreferences
          "FlashTalkSettings" on every speak() call), ImageSetImporter
          (ZIP/JSON bulk import), ImageSetExporter (the reverse — same
          manifest schema, reused rather than duplicated), SentenceStrip
          (pure displayText/speechText joining for sentence strip mode —
          no Context, so it's plain-JUnit testable)
FlashTalkApplication  Applies the stored dark-mode preference at process
          start, before any activity is created.
```

Key invariants. Not suggestions — these are the bits that, if broken,
turn this from an AAC app into a mildly insulting toy:

1. **Tap-to-speak latency matters.** Nothing between card tap and
   `tts.speak()` may block. No network, no blocking disk reads on that
   path. (SharedPreferences reads are the one exception, and only because
   Android caches them in memory after first load — don't take that as
   licence to add other "quick" reads on the hot path.)
2. **Everything is offline-first.** No cloud, no analytics, no telemetry,
   no INTERNET permission in the manifest at all. This is a privacy
   commitment, not an oversight, and it doesn't get relaxed because
   someone thinks a crash reporter would be handy.
3. **Custom images live in internal storage** (`filesDir/custom_images`).
   Deleting a card or category cleans up its image file via
   `AppRepository.deleteCard`/`deleteCategory` — keep that behaviour if
   you touch either path. Don't reintroduce the leak; it was only ever
   fixed because I happened to be writing that code fresh anyway.
4. **TTS settings** are read from SharedPreferences by TTSManager on
   every speak. SettingsActivity writes to the same keys (see the
   TTSManager companion object for the actual names). Keep them in sync —
   this is exactly the kind of thing that silently drifts apart across a
   few PRs and nobody notices until Settings stops doing anything.
5. **Seed data** lives in `assets/vocabulary/communicards_vocabulary_v1.csv`,
   parsed by `AppDatabase.populateDatabase()` on first run — not hardcoded
   Kotlin, deliberately, because 266 rows of Kotlin literals is nobody's
   idea of maintainable. Any *schema* change (new FlashCard/Category
   fields) still needs a proper Room migration once this app has real
   installs. It didn't need one for the current vocabulary overhaul only
   because there were zero installs to protect — that reasoning expires
   the moment this ships. Don't just bump the version and let it wipe
   after that point; that's how you lose someone's custom cards over an
   app update, which is about as bad as this gets.
6. **Seed cards render an emoji glyph, not a photo.** `FlashCard.emoji` is
   the seed-card visual; `FlashCard.imagePath` is only ever set for
   custom photo cards. Don't reintroduce a
   `context.resources.getIdentifier(...)` drawable lookup for seed
   content — that lookup pattern is exactly the bug this design exists to
   avoid, and it'll come back as silently as it left.
7. **`FlashCard.text` is the label; `FlashCard.speechText` is what's
   spoken.** They differ on purpose for cards like "Bathroom / Toilet"
   (label) → "Bathroom" (speech). `CategoryActivity.speakCard()` must
   keep using `speechText` for TTS and `text` for the on-screen label/
   Toast — swapping them back to a single field quietly breaks every
   card with an alternate term in its label.
8. **`FlashCard.priority == "urgent"` gets a visual accent** (currently a
   red `MaterialCardView` stroke in `FlashCardAdapter`) and
   **`FlashCard.enabled == false` cards are excluded** from
   `FlashCardDao.getCardsByCategory` — but not from
   `getCardsByCategorySync`, which the delete-cleanup path uses and needs
   to see every card, enabled or not. Don't "simplify" that query to a
   single shared version; it's two queries on purpose.
9. **Edit mode is off by default, and long-press listeners aren't just
   inert when it's off — they're not attached at all.** `CategoryAdapter
   .onCategoryLongClick`/`FlashCardAdapter.onCardLongClick` are nullable
   `var`s that `MainActivity`/`CategoryActivity` set to `null` whenever
   the `KEY_EDIT_MODE` preference is off, in `onResume()`. This is the
   actual safety mechanism the "hard to trigger accidentally" design
   constraint depends on — don't refactor it into a listener that just
   checks a flag and no-ops, because a no-op listener is still a listener
   that TalkBack and accidental long-presses can still find.
10. **`MathGate.show()` is a caregiver speed bump, not access control.**
    It gates Settings and Import from `MainActivity`'s menu. Don't wire
    it in front of individual actions inside Settings (the Edit mode
    toggle doesn't get its own gate) — one gate at the door is the
    design, not one at every room.
11. **Non-English vocabulary CSVs are draft translations, not clinically
    reviewed ones.** `communicards_vocabulary_v1_fr.csv` exists and is
    wired up correctly, but it's a single-pass translation, not a
    native-speaker or SLT-reviewed one — treat the
    `health_feelings_emergency` category in any translated file as
    needing that review specifically before anyone relies on it for a
    real emergency. Don't add another language file and assume it's
    "done" just because it parses and displays.
12. **Sentence strip mode is off by default and mutually exclusive with
    the ordinary speech bar, not layered on top of it.** When
    `KEY_SENTENCE_STRIP_MODE` is off, `CategoryActivity` behaves exactly
    as if the feature didn't exist — card taps speak immediately, the
    strip bar stays `View.GONE`. When it's on, the strip bar replaces the
    speech bar (only one is visible at a time) and taps append to an
    in-memory `strip` list instead of speaking. Don't merge the two bars
    or make strip mode change tap behaviour while leaving the old speech
    bar visible — that's two competing metaphors on screen at once, not
    one coherent mode switch.

## Design rules (accessibility is the product)

- Touch targets: 48dp absolute minimum, cards are 140dp. Keep them big —
  this isn't a density flex, it's the point.
- Contrast: WCAG AA minimum on every text/background pair.
- One tap = one utterance, by default, always. Never require a
  double-tap or long-press for core communication, however tempting it
  is for some future feature. Sentence strip mode is the one sanctioned
  exception, and only because it's opt-in and off by default (invariant
  12) — that's the difference between "a deliberate, chosen mode" and
  "breaking the rule."
- No sudden animation, no flashing, no autoplaying sound other than the
  requested utterance. Sensory sensitivity isn't a nice-to-have here.
- Everything must work with TalkBack. Add contentDescription to any new
  ImageView. Press feedback goes through a StateListAnimator
  (`res/animator/card_press_animator.xml`), never a raw
  `setOnTouchListener` that skips `performClick()` — that's a specific,
  recurring bug, not a hypothetical one.
- Support landscape and tablets. AAC devices are usually tablets, and
  testing only in portrait phone mode will hide problems.

## Workflow expectations

- Read `BACKLOG.md` for prioritised work. Items are ordered for a reason;
  do them in order unless told otherwise.
- Before implementing: state assumptions, confirm scope if it's
  ambiguous. A wrong implementation costs more than the question would
  have.
- After implementing: run lint, and say plainly what you tested and what
  you couldn't (there's no emulator in this environment by default — flag
  anything that genuinely needs a real device or AVD).
- Update `CHANGELOG.md`'s `[Unreleased]` section as you go, not
  retroactively at the end when half of it's been forgotten.
- Feedback loop: if a fix reveals a design flaw, write it into
  `BACKLOG.md` rather than quietly working around it. A negative finding
  is still a finding.
- Documentation and code must not drift apart. If behaviour changes, grep
  README.md, USER_GUIDE.md, and DESCRIPTION.md for anything that's now
  wrong. Stale docs are worse than no docs, because people trust them.

## Known honesty debts

Things earlier docs overclaimed, or that are still genuinely missing.
I'd rather list these plainly than have someone discover them the hard
way:

- No screenshots exist. None are referenced either, so there's nothing
  stale to fix — just nothing to show yet. And genuinely blocked, not
  just undone: no `/dev/kvm`, no vmx/svm CPU flags in this environment,
  so there's no emulator to take them on until this runs somewhere with
  real virtualisation.
- Still no proper symbol imagery — emoji glyphs are the current stand-in
  across all 266 cards, correctly, but they're a placeholder, not a
  destination. What replaces them is an open decision, not this file's
  to make.
- The French vocabulary file is a single-pass translation (mine), not a
  native-speaker or SLT-reviewed one. See invariant 11 — the
  Health/Feelings/Emergencies category specifically needs that review
  before anyone trusts it for a real emergency.
- No UI/instrumented tests exist — everything runs on the JVM (plain
  JUnit or Robolectric). Same root cause as the screenshots: no
  emulator, not a choice to skip it.
- If `PROJECT_OVERVIEW.md` is still floating around anywhere, don't trust
  it — it oversells completeness in a way `BACKLOG.md` immediately
  contradicts. `BACKLOG.md` is the source of truth on status, always.

Resolved in the current build (these used to be listed here as debts —
don't reintroduce them just because it'd be quicker):

- Seed cards no longer reference missing drawables; they render an emoji
  glyph (`FlashCard.emoji`) instead.
- The vocabulary itself went from a 56-card, 8-category placeholder set
  to a real 266-card, 7-category one, sourced from a bundled CSV rather
  than hardcoded Kotlin.
- "Large text" now applies a real `Configuration.fontScale` override
  (`BaseActivity`), instead of writing a preference nothing read.
- Deleting a card or category now removes its custom image file.
- Card touch feedback uses a `StateListAnimator`, not a manual
  `setOnTouchListener` that skipped `performClick()`.
- Seed vocabulary is no longer hardcoded to English — locale-based CSV
  selection exists and works (invariant 11), even though only French is
  bundled today.
- Edit and delete for both cards and categories, gated behind an
  off-by-default Edit mode toggle (invariant 9).
- Toasts on card tap replaced with a persistent speech bar (last-spoken
  text + repeat button) in `CategoryActivity`.
- A maths-question gate on Settings/Import (`MathGate`, invariant 10).
- Export: `ImageSetExporter` writes a category back out in the same
  ZIP+manifest format `ImageSetImporter` reads. Round-trip covered by
  `ImageSetExporterTest`.
- kapt → KSP, and the unused Glide annotation processor removed outright.
- Sentence strip mode (invariant 12): off by default, opt-in via
  Settings, builds a short sentence from several tapped cards instead of
  speaking each one immediately.
- 38 unit tests now exist, up from zero: CSV parsing, manifest parsing
  (including a real Gson-vs-Kotlin-defaults bug the tests caught — see
  `BUILD_NOTES.md`), DiffUtil callbacks, sentence-strip joining, the
  export round-trip, and Repository CRUD via Robolectric.

## Skills that pair with this repo

If working in an environment with the TABARC skill set available:
`karpathy-code-discipline` for any non-trivial change, `kaizen` for
refactor passes, `professional-technical-writing` for USER_GUIDE edits,
`humanizer` for any prose that's started to read like a press release.
