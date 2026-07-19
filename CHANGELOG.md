# Changelog

All notable changes to FlashTalk AAC will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned
- Edit existing flashcards
- Delete cards and categories (via UI — repository support already exists)
- Reorder cards within categories
- Export categories to ZIP
- Favorites/frequently used cards
- Usage history and statistics

## [1.0.0] - 2026-07-19

### Added - Initial Release

#### Core Features
- Single-tap communication with immediate text-to-speech output
- 8 pre-loaded categories with 56 flashcards
- Text-to-speech with adjustable speed (0.5x - 2.0x) and pitch (0.5x - 2.0x),
  following the device's locale
- Visual feedback on card tap (press animation via StateListAnimator + Toast)

#### Categories & Cards
Seed cards render as an emoji glyph rather than a bundled photo — this
sidesteps needing to settle a symbol-set licence (ARASAAC, Mulberry, etc.)
before shipping. Custom cards use real photos as normal.

- **Food & Drink**: 9 cards (water, juice, milk, hungry, thirsty, snack, breakfast, lunch, dinner)
- **Feelings**: 8 cards (happy, sad, angry, scared, excited, tired, confused, love)
- **Activities**: 8 cards (play, read, watch TV, music, outside, walk, bath, sleep)
- **People**: 7 cards (mom, dad, brother, sister, friend, teacher, doctor)
- **Places**: 7 cards (home, school, park, store, bathroom, bedroom, car)
- **Needs**: 7 cards (help, stop, wait, more, all done, hurt, bathroom)
- **Yes/No**: 4 cards (yes, no, please, thank you)
- **Time**: 6 cards (now, later, today, tomorrow, morning, night)

#### Customization
- Create unlimited custom categories with custom names and colors
- Add unlimited custom flashcards with personal photos
- Photo selection via the system photo/document picker — no storage or
  media permission requested on any supported Android version
- Deleting a custom card or category also removes its stored photo file

#### Import System
- Import flashcard sets from ZIP files (manifest.json + images)
- Import from JSON files (card definitions only, with an optional
  per-card emoji fallback)
- Zip-slip-safe extraction, 50MB import cap, and per-card warnings when a
  referenced image is missing from the set rather than a silent failure
- Example import sets included (Animals, Colors, Transportation)

#### Settings
- Speech rate adjustment slider
- Voice pitch adjustment slider
- Dark mode toggle (applied at process start and immediately on change)
- Large text toggle (applies a real font-scale override app-wide)
- Persistent settings storage via SharedPreferences

#### Accessibility
- High-contrast design (WCAG AA compliant)
- Large touch targets (140dp cards, 48dp+ buttons)
- Dark mode for photosensitivity
- Functional large-text mode
- TalkBack-correct press feedback (StateListAnimator, not a manual touch
  listener that skips `performClick()`)
- Simple, clear navigation

#### Technical
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room with LiveData for reactive updates
- **Language**: 100% Kotlin
- **UI**: Material Design Components
- **Async**: Kotlin Coroutines
- **Image Loading**: Glide with caching (custom photo cards only)
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Permissions**: none beyond what the system picker grants per-use —
  no INTERNET, no storage/media permission

#### Documentation
- README.md, USER_GUIDE.md, CLAUDE.md, BACKLOG.md, DESCRIPTION.md
- Import guide in example_imports/README.md
- Example flashcard sets with manifests

#### Repository
- MIT License
- .gitignore for Android projects
- CONTRIBUTING.md with guidelines

### Known Limitations (v1.0)
- No sentence building (single-word/phrase only, by design)
- No data export (import only)
- No undo functionality
- No edit-in-place for existing cards/categories (repository support
  exists; no UI yet)
- Single user profile
- No cloud backup — by design, not an oversight
- English only (multi-language planned)
- No automated tests yet

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for how to contribute to this project.

Report bugs and request features via GitHub Issues.
