# CLAUDE.md — FlashTalk AAC

Project context for Claude Code. Read this before touching anything.

## What this is

An Android AAC (Augmentative and Alternative Communication) app for autistic
users. Single-tap flashcard communication: tap a card, it speaks. That's the
whole product. Resist the urge to make it cleverer — simplicity IS the feature.

Author: TABARC-Code. UK English throughout: code comments, strings, docs,
commit messages. "Mum" not "Mom", "colour" in prose (though Android APIs use
`color`, obviously).

## Stack

- Kotlin, MVVM, Room + LiveData, Coroutines, Glide, Material Components
- minSdk 24, targetSdk 34
- Single module: `app/`
- No DI framework (deliberate — app is small; don't add Hilt without asking)

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

Key invariants — do not break these:

1. **Tap-to-speak latency matters.** Nothing between card tap and
   `tts.speak()` may block. No network, no blocking disk reads on that
   path (SharedPreferences reads are the one exception — Android caches
   them in memory after first load, so this doesn't block in practice).
2. **Everything is offline-first.** No cloud, no analytics, no telemetry,
   no INTERNET permission in the manifest at all. This is a privacy
   commitment, not an oversight — don't add it back for a "nice to have".
3. **Custom images live in internal storage** (`filesDir/custom_images`).
   Deleting a card or category cleans up its image file via
   `AppRepository.deleteCard`/`deleteCategory` — keep that behaviour if
   you touch either path; don't reintroduce the leak.
4. **TTS settings** are read from SharedPreferences by TTSManager on every
   speak. SettingsActivity writes to the same keys (see TTSManager
   companion object for key names). Keep them in sync.
5. **Seed data** lives in `AppDatabase.populateDatabase()`. Any schema
   change needs a Room migration — do not just bump the version and wipe.
6. **Seed cards render an emoji glyph, not a photo.** `FlashCard.emoji` is
   the seed-card visual; `FlashCard.imagePath` is only ever set for custom
   photo cards. Don't reintroduce a `context.resources.getIdentifier(...)`
   drawable lookup for seed content — that's the bug this design avoids.

## Design rules (accessibility is the product)

- Touch targets: 48dp absolute minimum, cards are 140dp — keep them big
- Contrast: WCAG AA minimum on every text/background pair
- One tap = one utterance. Never require a double-tap or long-press for
  core communication
- No sudden animation, no flashing, no autoplaying sound other than the
  requested utterance (sensory sensitivity)
- Everything must work with TalkBack. Add contentDescription to any new
  ImageView. Press feedback goes through a StateListAnimator
  (`res/animator/card_press_animator.xml`), never a raw
  `setOnTouchListener` that skips `performClick()`
- Support landscape and tablets — AAC devices are usually tablets

## Workflow expectations

- Read `BACKLOG.md` for prioritised work. Items are ordered; do them in
  order unless told otherwise.
- Before implementing: state assumptions, confirm scope if ambiguous.
  A wrong implementation costs more than a question.
- After implementing: run lint, state what you tested and what you
  couldn't (no emulator in this environment — flag anything that needs
  manual device testing).
- Update `CHANGELOG.md` [Unreleased] section with each change.
- Feedback loop: when a fix reveals a design flaw, note it in BACKLOG.md
  rather than silently working around it. Negative findings are as
  valuable as positive ones.
- Documentation and code must not drift. If you change behaviour, grep
  README.md, USER_GUIDE.md, and DESCRIPTION.md for stale claims.

## Known honesty debts

Things earlier docs overclaimed, or that are still genuinely missing.
Don't repeat the pattern:

- No screenshots exist despite this being a natural README addition — none
  are referenced yet either, so there's nothing stale to fix, just
  nothing to show. Add real ones from an emulator before claiming any.
- Zero automated tests (BACKLOG.md item 9).
- `PROJECT_OVERVIEW.md`, if it's still floating around, oversells
  completeness — `BACKLOG.md` is the source of truth on status, not that
  file.

Resolved in this build (previously listed here as debts — don't
reintroduce them):
- Seed cards no longer reference missing drawables; they render an emoji
  glyph (`FlashCard.emoji`), decided without needing to settle a
  symbol-set licence.
- "Large text" now applies a real `Configuration.fontScale` override
  (`BaseActivity`) instead of writing a preference nothing read.
- Deleting a card or category now removes its custom image file.
- Card touch feedback uses a `StateListAnimator`, not a manual
  `setOnTouchListener` that skipped `performClick()`.

## Skills that pair with this repo

If working in an environment with the TABARC skill set available:
`karpathy-code-discipline` for any non-trivial change, `kaizen` for
refactor passes, `professional-technical-writing` for USER_GUIDE edits,
`humanizer` for any prose that reads like a press release.
