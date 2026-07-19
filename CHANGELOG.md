# Changelog

All notable changes to FlashTalk AAC, documented properly rather than
reconstructed from memory later. Format's based on
[Keep a Changelog](https://keepachangelog.com/en/1.0.0/); versioning
follows [SemVer](https://semver.org/spec/v2.0.0.html). I'm sticking to
both conventions because reinventing a changelog format is a special
kind of waste of everyone's time.

## [Unreleased]

### Planned
- Edit existing flashcards
- Delete cards and categories via the UI (the repository layer already
  supports this — it's a screen away, not a rewrite)
- Reorder cards within categories
- Export categories to ZIP
- Favourites / frequently used cards
- Usage history and statistics

## [1.0.0] - 2026-07-19

### Added - Initial Release

#### Core Features
- Single-tap communication with immediate text-to-speech output
- 8 pre-loaded categories, 56 flashcards total
- Text-to-speech with adjustable speed (0.5x–2.0x) and pitch (0.5x–2.0x),
  following the device's locale rather than assuming US English
- Visual feedback on card tap (press animation via StateListAnimator,
  plus a Toast)

#### Categories & Cards
Seed cards render as an emoji glyph rather than a bundled photo. Worth
repeating why, because it looks like a shortcut and isn't one: it means
the app never had to settle a symbol-set licence (ARASAAC, Mulberry, or
otherwise) before it could ship with cards that actually look like
something. Custom cards use real photos, as you'd expect.

- **Food & Drink**: 9 cards (water, juice, milk, hungry, thirsty, snack, breakfast, lunch, dinner)
- **Feelings**: 8 cards (happy, sad, angry, scared, excited, tired, confused, love)
- **Activities**: 8 cards (play, read, watch TV, music, outside, walk, bath, sleep)
- **People**: 7 cards (mom, dad, brother, sister, friend, teacher, doctor)
- **Places**: 7 cards (home, school, park, store, bathroom, bedroom, car)
- **Needs**: 7 cards (help, stop, wait, more, all done, hurt, bathroom)
- **Yes/No**: 4 cards (yes, no, please, thank you)
- **Time**: 6 cards (now, later, today, tomorrow, morning, night)

#### Customisation
- Unlimited custom categories, with custom names and colours
- Unlimited custom flashcards, with personal photos
- Photo selection via the system photo/document picker — no storage or
  media permission requested, on any supported Android version, because
  it turns out you never needed to ask in the first place
- Deleting a custom card or category also removes its stored photo file,
  properly, not just from the database

#### Import System
- Import flashcard sets from ZIP files (manifest.json plus images)
- Import from JSON files (definitions only, with an optional per-card
  emoji fallback for when there's no image to show)
- Zip-slip-safe extraction, a 50MB import cap, and per-card warnings when
  a referenced image is missing rather than the import just quietly
  failing on you
- Example import sets included: Animals, Colours, Transportation

#### Settings
- Speech rate slider
- Voice pitch slider
- Dark mode toggle, applied at process start and immediately on change
- Large text toggle that genuinely applies a font-scale override, unlike
  a previous version of this idea
- Persistent settings storage via SharedPreferences

#### Accessibility
- High-contrast design, WCAG AA
- Large touch targets (140dp cards, 48dp+ buttons)
- Dark mode for photosensitivity
- A large-text mode that actually works
- TalkBack-correct press feedback (StateListAnimator, not a manual touch
  listener that quietly skips `performClick()`)
- Simple, clear navigation — nothing clever, on purpose

#### Technical
- **Architecture**: MVVM
- **Database**: Room with LiveData
- **Language**: 100% Kotlin
- **UI**: Material Design Components
- **Async**: Kotlin Coroutines
- **Image loading**: Glide, custom photo cards only
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Permissions**: none beyond what the system picker grants per use —
  no INTERNET, no storage/media permission, and that's not an oversight

#### Documentation
- README.md, USER_GUIDE.md, CLAUDE.md, BACKLOG.md, DESCRIPTION.md,
  BUILD_NOTES.md
- Import guide in example_imports/README.md
- Example flashcard sets with manifests

#### Repository
- MIT Licence
- .gitignore for Android projects
- CONTRIBUTING.md with guidelines

### Known Limitations (v1.0)
- No sentence building — single word/phrase only, and that's the design,
  not a gap
- No data export yet (import only)
- No undo
- No edit-in-place for existing cards/categories (the repository
  supports it; no screen calls it yet)
- Single user profile
- No cloud backup, by design, not by oversight
- English only for now
- No automated tests yet, which I'd rather admit than gloss over

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for how to get involved. Bugs and
feature requests go through GitHub Issues.
