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

## Build and test

```bash
./gradlew assembleDebug        # build
./gradlew installDebug         # install to connected device
./gradlew test                 # unit tests (currently none — see backlog)
./gradlew lint                 # always run before committing
```

## Architecture map

```
data/     Room entities (Category, FlashCard), DAOs, AppDatabase (seeds
          default categories on first run), AppRepository
ui/       One activity per screen + ViewModel + adapter. Main (category
          grid) → Category (card grid) → tap speaks via TTSManager.
          BaseActivity applies the "Large text" font-scale override that
          every other activity extends.
utils/    TTSManager (reads rate/pitch from SharedPreferences
          "FlashTalkSettings" on every speak() call), ImageSetImporter
          (ZIP/JSON bulk import)
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
5. **Seed data** lives in `AppDatabase.populateDatabase()`. Any schema
   change needs a proper Room migration. Do not just bump the version
   number and let it wipe — that's how you lose someone's custom cards
   over an app update, which is about as bad as this gets.
6. **Seed cards render an emoji glyph, not a photo.** `FlashCard.emoji` is
   the seed-card visual; `FlashCard.imagePath` is only ever set for
   custom photo cards. Don't reintroduce a
   `context.resources.getIdentifier(...)` drawable lookup for seed
   content — that lookup pattern is exactly the bug this design exists to
   avoid, and it'll come back as silently as it left.

## Design rules (accessibility is the product)

- Touch targets: 48dp absolute minimum, cards are 140dp. Keep them big —
  this isn't a density flex, it's the point.
- Contrast: WCAG AA minimum on every text/background pair.
- One tap = one utterance. Never require a double-tap or long-press for
  core communication, however tempting it is for some future feature.
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
  stale to fix — just nothing to show yet. Add real ones from an emulator
  before claiming any.
- Zero automated tests (BACKLOG.md item 6).
- If `PROJECT_OVERVIEW.md` is still floating around anywhere, don't trust
  it — it oversells completeness in a way `BACKLOG.md` immediately
  contradicts. `BACKLOG.md` is the source of truth on status, always.

Resolved in the current build (these used to be listed here as debts —
don't reintroduce them just because it'd be quicker):

- Seed cards no longer reference missing drawables; they render an emoji
  glyph (`FlashCard.emoji`), which sidesteps needing a symbol-set licence
  entirely rather than deferring it.
- "Large text" now applies a real `Configuration.fontScale` override
  (`BaseActivity`), instead of writing a preference nothing read.
- Deleting a card or category now removes its custom image file.
- Card touch feedback uses a `StateListAnimator`, not a manual
  `setOnTouchListener` that skipped `performClick()`.

## Skills that pair with this repo

If working in an environment with the TABARC skill set available:
`karpathy-code-discipline` for any non-trivial change, `kaizen` for
refactor passes, `professional-technical-writing` for USER_GUIDE edits,
`humanizer` for any prose that's started to read like a press release.
