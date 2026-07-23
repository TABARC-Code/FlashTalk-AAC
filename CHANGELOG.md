# Changelog

All notable changes to FlashTalk AAC, documented properly rather than
reconstructed from memory later. Format's based on
[Keep a Changelog](https://keepachangelog.com/en/1.0.0/); versioning
follows [SemVer](https://semver.org/spec/v2.0.0.html). I'm sticking to
both conventions because reinventing a changelog format is a special
kind of waste of everyone's time.

## [Unreleased]

### Added — Home-screen widget
- **`NeedsWidgetProvider`**: a home-screen widget showing one configured
  category's cards in a `GridView`, tap a card to speak it without
  opening the app at all. Configuration (`WidgetConfigActivity`, launched
  automatically when the widget's placed) only offers the shared
  vocabulary (`Category.profileId == 0L`) — a widget lives outside any
  profile's in-app session, so a profile's own custom category isn't a
  coherent thing to point it at.
- **`WidgetTapReceiver`**: a one-shot `TextToSpeech` per tap, not
  `TTSManager` — a `BroadcastReceiver`'s lifecycle doesn't suit a
  long-lived TTS instance the way an Activity's does. Still reads the
  same `KEY_SPEECH_RATE`/`KEY_PITCH` preference keys `TTSManager` does,
  so the widget doesn't quietly speak at a different rate/pitch than the
  one a caregiver already set in Settings.
- Widget-id-to-category mapping lives in its own SharedPreferences file,
  cleaned up when a widget's removed from the home screen
  (`AppWidgetProvider.onDeleted`).
- **Known trade-off, stated plainly**: the widget refreshes on its own
  roughly every 30 minutes (`updatePeriodMillis`, Android's minimum
  granularity), not the instant a card's edited in-app — wiring live
  invalidation into every card/category mutation path was more coupling
  than felt earned for a first pass. It self-heals within half an hour;
  it just isn't instant.
- 6 new unit tests: `NeedsWidgetProviderTest` covers the widget-id →
  category preference mapping (Robolectric, since it needs a real
  Context); `AppRepositoryTest` gained cases for
  `getSharedCategoriesSync` and `getEnabledCardsByCategorySync`.

### Added — Multiple profiles
- **`Profile`** entity (name, emoji icon) plus a `profileId` column on
  `Category`: `0L` (the default) means shared/global vocabulary, visible
  to every profile — every seeded CSV category stays at `0L` forever. Any
  other value is a specific profile's own custom category, visible only
  to that profile. Chosen over giving every profile a full independent
  copy of the 334-card vocabulary specifically for efficiency — no
  duplicated rows, no per-profile reseeding on creation. The named
  trade-off: editing or deleting a *shared* category still affects every
  profile, since there's genuinely only one copy of it.
- **`ProfileActivity`**: switch profiles by tapping one (writes a pref,
  `MainActivity.onResume()` picks it up, same pattern as Edit mode/strip
  mode); add a profile via the FAB; edit or delete one via long-press,
  gated by the same off-by-default Edit mode toggle as categories/cards.
  Deleting the last remaining profile is refused outright — the app must
  never open onto zero profiles. Reachable from `MainActivity`'s menu,
  behind `MathGate` like Settings and Import.
- A `Profile` named "Default" is seeded alongside the vocabulary on first
  run, so the app never opens with zero profiles.
- New category creation (the `+` FAB, and ZIP/JSON import) is scoped to
  whichever profile is currently active, via `MainActivity.currentProfileId`
  passed through to `MainViewModel`/`ImportViewModel`.
- 7 new unit tests: `ProfileAdapterDiffTest` (DiffUtil callback) and
  three `AppRepositoryTest` cases covering profile-scoped category
  visibility and cascade delete (owned categories and their custom
  images removed; shared vocabulary and other profiles' categories left
  alone).

### Added — Vocabulary expansion (68 new cards, 2 new categories)
- A caregiver-supplied brainstorm of AAC/PECS vocabulary (Emergency
  contacts, Communication Difficulty, Regulation/stimming, Autism-specific
  self-disclosure, specific foods and family roles, and more) was
  cross-checked line-by-line against the existing 266-card set — most of
  it was already covered under a different label (Yes/No/Please, Toilet,
  fidget toy, sunglasses, GP, and dozens more). Only the 68 cards that
  were genuinely new got added, appended after the existing rows so every
  existing id, sort order, and category assignment is untouched.
- Most new cards slotted into the existing 7 categories: 9 specific foods
  (Fruit, Sandwich, Tea, Milk…) into Physical Needs & Self-Care, 13 new
  emergency/feelings cards (Call ambulance, Call police, Anxiety attack,
  Panic attack, Lonely, Embarrassed…) into Health/Feelings/Emergencies, 6
  new named family roles (Mum, Dad, Partner, Nurse…) into Core & Social,
  and smaller additions to the rest.
- Two concepts didn't fit any existing category, so two new ones were
  added: **Communication Support** (7 cards — "I can't talk right now",
  "Speak slowly", "Write it down") and **About Me** (8 cards — "I'm
  autistic", "I need routine", "Please don't rush me"). `AppDatabase
  .CATEGORY_ICONS` updated with icons for both (🗣️, 🧩).
- 266 cards / 7 categories → **334 cards / 9 categories**. Both the
  English and French CSVs got every new row, kept in the same order and
  count — the French additions are the same single-pass, not-clinically-
  reviewed translation as the original 266 (see the existing entry below
  and `BACKLOG.md` item 1); several of the new cards are emergency
  contact actions, so that caveat matters more now, not less.

### Added — Sentence strip mode
- Optional, off by default (Settings → Sentence strip mode). On:
  `CategoryActivity` stops speaking a card the moment it's tapped and
  instead appends it to an in-memory strip, shown in a dedicated strip
  bar that replaces the ordinary speech bar while the mode's active.
  Tapping the strip speaks the whole built sentence and clears it;
  a separate control clears it without speaking. Off (the default):
  tap-to-speak is exactly as before, nothing about the existing path
  changed.
- Sentence-building lives in `SentenceStrip`, a Context-free object with
  `displayText`/`speechText` — the former joins cards' on-screen labels,
  the latter joins `speechText`, same split `speakCard` already used for
  a single card. Four unit tests cover it.
- The strip is intentionally not persisted anywhere: it's a scratchpad
  for building one sentence, not vocabulary data, so it resets on
  category re-entry or when the mode's toggled off.

### Added — Locale-aware seed vocabulary
- `AppDatabase` now picks the seed CSV by device locale
  (`communicards_vocabulary_v1_<lang>.csv`), falling back to the English
  default if no matching file is bundled. Selection logic
  (`vocabularyAssetNameFor`) is pure and unit-tested against present/
  absent/case-insensitive locale matches.
- French (`communicards_vocabulary_v1_fr.csv`) is the first bundled
  translation — all 266 rows, same ids/colours/emoji/priority as the
  English file, only the label/speech text/category names translated.
  **This is a single-pass translation, not a native-speaker or
  SLT-reviewed one.** Most of it is short, unambiguous words where that
  matters little ("Oui", "Non", "Stop"). The `health_feelings_emergency`
  category is the one place it genuinely matters — "seizure warning" →
  "Crise à venir" and similar need a fluent speaker or clinician to check
  before anyone relies on them in an actual emergency. Flagged in
  `CLAUDE.md` and `BACKLOG.md` so it doesn't get lost.
- Adding another language from here is "translate the CSV, drop it in
  `assets/vocabulary/`" — no code changes required.

### Changed
- Seed vocabulary replaced wholesale: 56 cards / 8 categories →
  **266 cards / 7 categories** (Core & Social, Actions & Requests,
  Physical Needs & Self-Care, Health/Feelings/Emergencies, Sensory &
  Comfort, Objects & Leisure, Places/Time/Sequence), sourced from a real
  clinically-structured vocabulary set rather than a starter list.
- Vocabulary now lives in a bundled CSV
  (`assets/vocabulary/communicards_vocabulary_v1.csv`) parsed at seed
  time, not hand-written as Kotlin. 266 rows of Kotlin data literals
  wasn't a maintainable format for anyone who isn't touching Kotlin day
  to day.
- Every seed card now has its own hand-picked emoji, not a shared
  generic one — same placeholder-glyph approach as before, just applied
  properly across the full set instead of a 56-card starter list.

### Added
- `FlashCard.speechText`: what's actually spoken, separate from the
  on-screen label. Matters for cards like "Bathroom / Toilet" (label) →
  "Bathroom" (speech) — previously the same field did both jobs.
- `FlashCard.priority` ("standard" | "urgent"): urgent cards (Stop,
  Emergency, Seizure warning, and the like) now get a visible red border
  in the card grid, regardless of which category they're in.
- `FlashCard.enabled`: respected by the card list query, so a card can
  exist without being shown — no UI to toggle this yet, but the data
  layer supports it.
- First unit tests in the project: `AppDatabaseCsvTest` covers the CSV
  parser's quoted-comma and doubled-quote-escaping behaviour, since
  that's exactly the kind of thing that breaks silently under a later
  refactor with nothing to catch it.

### Added — Edit mode, speech bar, settings lock, export
- **Edit mode**, off by default, toggled in Settings. On: long-press a
  category to rename/re-icon or delete it (cascades to its cards and
  their photos); long-press a card to change its text/photo or delete
  it. Off (the default): no long-click listener is attached at all, not
  a listener that happens to do nothing — that distinction is the actual
  safety mechanism, see `CLAUDE.md` invariant 9.
- **Persistent speech bar** in `CategoryActivity`, replacing the Toast on
  card tap. Shows the last-spoken card's label with a repeat button.
  Toasts vanish before anyone slower than instant can read them and
  can't be replayed; a bar that stays put and repeats fixes both.
- **Settings/Import lock**: a maths-question dialog (`MathGate`) gates
  entry to Settings and Import from the main menu. Not security — a
  caregiver-facing speed bump so a curious or impulsive user doesn't
  wander in mid-session. One gate at the door; the Edit mode toggle
  inside Settings doesn't get a second one.
- **Category export**: `ImageSetExporter` writes a category and its
  cards back out in the exact ZIP+manifest format `ImageSetImporter`
  already reads (custom photos bundled in, emoji-only cards get their
  glyph stored as `icon`). Reuses `ImageSetImporter`'s own manifest data
  classes rather than a second copy of the schema.

### Fixed
- **Gson doesn't apply Kotlin constructor defaults for missing JSON
  fields** — it populates data classes via reflection, bypassing the
  constructor, so an optional field like `category_icon` landed as an
  actual `null` at runtime rather than falling back to `"📦"`, for any
  manifest that omitted it. That's precisely the minimal-JSON case
  `example_imports/README.md` promises works. `ImageSetImporter
  .parseManifest` now parses into a `JsonObject` and builds
  `ImportManifest`/`CardData` through their real constructors instead,
  so the documented defaults actually apply. Found by writing
  `ImageSetImporterTest`, not by inspection — worth remembering next
  time a data class default looks obviously fine.

### Changed
- kapt → KSP for Room's annotation processing. Glide's compiler artifact
  was removed outright rather than migrated: this app has never declared
  an `AppGlideModule`, so it was doing nothing and had nothing to
  migrate.
- 30 unit tests now exist, up from 5: CSV parsing, manifest parsing
  (happy path, malformed JSON, missing images), DiffUtil callbacks for
  both adapters, the export round-trip, and Repository CRUD against an
  in-memory Room database via Robolectric (no emulator needed).

### Planned
- Reorder cards within categories
- Favourites / frequently used cards
- Usage history and statistics
- More bundled languages (mechanism's done; each one is now just a
  translated CSV)
- Native-speaker/SLT review of the French translation, especially
  `health_feelings_emergency`
- Real screenshots for the README — blocked, no emulator available
- UI/instrumented tests — blocked for the same reason; everything so far
  is unit-tested, nothing has been watched running on a device

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
the app never had to settle a symbol-set licence before it could ship
with cards that actually look like something. Custom cards use real
photos, as you'd expect. (This starter set was superseded shortly after
— see [Unreleased] above for the real 266-card vocabulary.)

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
