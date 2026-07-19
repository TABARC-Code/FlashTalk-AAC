# Build notes

Not a changelog, not a spec — just what actually happened while this got
built, and why, written down before I forget the reasoning. If you're
reading this months later wondering "why on earth did past-me do it this
way", this is the file for you.

## Why this replaced the first attempt

There was an earlier go at this repo — a React Native/Expo scaffold,
built before the real docs (CLAUDE.md, BACKLOG.md, the full Kotlin
CODE_REFERENCE.md) had actually turned up. Fair enough at the time, but
once the real spec landed it was obviously the wrong stack entirely —
MVVM/Room/Glide/Coroutines native Android, not a JS app pretending to be
one. So the RN scaffold got binned wholesale and this is what's here
instead. Nothing lost, it's still sat in git history if anyone's morbidly
curious.

## The seed image problem

This was the whole reason BACKLOG.md flagged item 1 as P0: sixty-odd seed
cards, and every single one pointed at a drawable that was never
bundled. Grey boxes, the lot. A proper symbol set was on the table, but
that's a real licensing and cost decision — not one to make quietly by
just picking one and bundling it.

Went with emoji instead, for now. Not because it's clever, but because
it makes an entire category of future problem simply not exist while
that decision's still open. No licence to track, no asset pipeline, no
"did we attribute this correctly" conversation before a release.
`FlashCard` got an `emoji` field sat next to `imagePath`; seed cards use
one, custom photo cards use the other. It's a small change with an
outsized effect on how much I have to think about this later — and it's
a stand-in, not a decision about what the final symbols will be.

## The photo permission that isn't

Original plan (and the docs described it this way) was the classic
`READ_EXTERNAL_STORAGE` / `READ_MEDIA_IMAGES` dance, branching on API
level, because that's what everyone's always done for gallery access.
Except you don't need to any more. `ActivityResultContracts.GetContent()`
hands back a `content://` Uri from whatever picker the OS shows, no
permission grant required, on every API level back to 24. So the
manifest has no storage permission and no INTERNET permission at all —
genuinely nothing to ask for. Slightly annoyed it took writing this app
to remember that, given how much of the internet is still copy-pasting
the old permission-request boilerplate.

## Small bugs that don't need to exist

A few things BACKLOG.md listed as known issues (from whatever the app
looked like before) just... didn't get written into this version, because
writing them correctly costs the same as writing them wrong:

- **Touch feedback.** The old pattern was a raw `setOnTouchListener`
  toggling elevation/scale by hand, which is the exact shape of bug lint
  flags for skipping `performClick()` (breaks TalkBack's synthesised
  clicks). Swapped it for a `StateListAnimator` on the card view instead —
  no touch listener in Kotlin at all, animation lives in XML where it
  belongs.
- **Image cleanup on delete.** Deleting a card or category now actually
  removes the photo file from `filesDir/custom_images` first. Leaving
  that as a "known leak" when I'm writing the delete path fresh anyway
  would've been a bit daft.
- **Large text.** Previously a switch that wrote a preference nobody
  read — about as useful as a light switch wired to nothing. Now it's a
  `Configuration.fontScale` override in a shared `BaseActivity`, so every
  activity picks it up for free.

None of this is scope creep, to be clear — BACKLOG's P1/P2/P3 items
(edit-in-place, export, settings lock, tests, kapt→KSP) are all still
untouched and still listed, on purpose. This lot was just "don't
deliberately write the bug when the fix is the same amount of typing".

## Gradle, or: an afternoon I won't get back

Small saga. Generating the wrapper via `gradle wrapper --gradle-version
8.2` failed outright because the sandbox's proxy won't reach
`services.gradle.org` for the version-check step. Running it with no
explicit version worked fine (uses whatever's installed locally — 8.14.3
here), which then meant the AGP version pinned in the original
`build.gradle` template (8.1.2) was now too old for that Gradle version.
Bumped AGP to 8.5.2 and Kotlin to 1.9.24, then hand-edited
`gradle-wrapper.properties` back down to Gradle 8.7 for the actual
project (a coherent, known-good pairing with AGP 8.5.2) — the wrapper
jar itself doesn't care what version it's pointed at, so this was safe to
do after the fact. Whoever eventually opens this in Android Studio will
just download 8.7 like normal; none of this weirdness is visible to them.

## Verifying it actually builds, not just "should"

Wasn't going to write forty-odd files and just assert it compiles. Pulled
down the Android SDK command-line tools, installed platform 34 +
build-tools 34, wrote a throwaway `local.properties`, and ran it for
real:

- `./gradlew assembleDebug` → a genuine 6.5MB `app-debug.apk` came out
  the other end.
- `./gradlew lintDebug` → failed first try, and rightly so: the two menu
  XMLs used `android:showAsAction` instead of `app:showAsAction`
  (AppCompat wants the latter), which is a real error, not a nag.
  Fixed both, plus a handful of genuinely unused colour resources and a
  missing `inputType` on the icon field. Re-ran, clean.

What's *not* verified: an actual emulator or device run. No AVD in this
environment, so the tap-to-speak flow, the photo picker, dark mode
switching, and the import UI have never actually been looked at running.
Compiling clean and lint being quiet is a real signal, but it's not the
same as watching it work.

## The vocabulary overhaul

The original 56-card, 8-category set was always a starter list, never
the real thing — it existed so the app had *something* to seed on first
run. It got replaced with a proper 266-card, 7-category vocabulary
(Core & Social, Actions & Requests, Physical Needs & Self-Care, Health/
Feelings/Emergencies, Sensory & Comfort, Objects & Leisure, Places/Time/
Sequence), supplied as a CSV rather than dictated from scratch here.

Three decisions worth writing down:

- **The CSV is an asset, parsed at seed time — not transcribed into
  Kotlin.** 266 rows of `FlashCard(...)` constructor calls would be a
  genuinely unpleasant file to review, let alone maintain. Instead
  `AppDatabase` reads `assets/vocabulary/communicards_vocabulary_v1.csv`
  and parses it with a small hand-rolled, quote-aware line splitter
  (needed because category names like "Health, Feelings & Emergencies"
  have a comma inside quotes — a naive `split(",")` would silently
  mangle every one of those). Wrote actual unit tests for that parser
  rather than trusting it by eye, which is exactly the kind of "this
  looks obviously right" code that isn't, until it meets a row nobody
  tested.
- **Label and speech text are now different fields.** Cards like
  "Bathroom / Toilet" want to display the alt term but only speak
  "Bathroom" — the old single `text` field couldn't do that without
  either cluttering the display or the speech. `speechText` defaults to
  the label if nothing else is given, so nothing else broke.
- **"Urgent" cards get an actual visual accent now** — a red border,
  regardless of which category colour they happen to sit in. Emergency,
  Stop, Seizure warning, that sort of thing. Data already distinguished
  standard from urgent priority; not using it anywhere would've been
  pointless.

Also added: an `enabled` flag per card, respected by the card-list query
but not yet exposed as a toggle anywhere — plumbing for later, not a
feature today.

## Working through the rest of P1 and P2

The instruction was "keep working until the app is complete," which I've
taken to mean: finish BACKLOG's P1 (core UX gaps) properly, and as much
of P2 (engineering hygiene) as doesn't need an emulator. Not P3 — those
are each their own feature (sentence strips, profiles, a widget,
switch-access scanning), explicitly marked "future, only after the
above," and treating them as part of "complete" would be scope
invention, not scope completion.

**Edit mode, not raw long-press.** BACKLOG flagged the risk plainly: a
kid mid-communication landing in a delete confirmation is the app
actively working against the person it exists for. The fix isn't
cleverness, it's a light switch — an off-by-default toggle in Settings,
and when it's off, `CategoryAdapter`/`FlashCardAdapter` don't have a
long-click listener *attached at all*. Not a listener that checks a flag
and no-ops — genuinely absent. That distinction matters more than it
sounds: a listener that's there-but-inert is still a thing TalkBack and
accidental long-presses can trip over.

**Repeat button over Toast.** Barely worth its own paragraph, except
that it's exactly the kind of "obviously fine" UI choice that turns out
to actively work against the product once you think about who's using
it. A Toast that vanishes in two seconds is useless to anyone processing
slower than that, and there was never a way to hear the word again
without tapping the card a second time.

**The maths gate is a speed bump, on purpose.** Two random single-digit
numbers, add them, get past Settings/Import. It would take a curious
seven-year-old about four seconds to defeat this, and that's fine — it's
not meant to stop anyone determined, it's meant to stop *accidental*
wandering. Real access control was never the ask.

**Export reuses the import schema, not a parallel one.** `ImageSetExporter
.buildManifest` constructs `ImageSetImporter.ImportManifest`/`CardData`
directly rather than declaring its own shape. Two independently-evolving
definitions of "what a manifest.json looks like" is exactly how an
export ends up producing a file the importer can't read back — the
failure mode this was built specifically to avoid.

**A real bug, caught by writing the tests for the above.** `parseManifest`
used `Gson().fromJson(json, ImportManifest::class.java)` and trusted the
Kotlin constructor's default values (`categoryIcon: String = "📦"`) to
fill in anything missing from the JSON. They don't. Gson populates data
classes through reflection, not by calling the constructor, so a field
absent from the JSON lands as a genuine `null` — on a property Kotlin's
type system insists is non-null. That's not a hypothetical: it's exactly
the minimal-manifest case `example_imports/README.md` tells people is
safe to rely on. Writing `ImageSetImporterTest` surfaced it inside a
minute; inspection alone hadn't. Fixed by parsing into a `JsonObject`
and constructing the real objects through their actual constructors,
with `?:` filling every gap. The lesson isn't "Gson is bad," it's that a
Kotlin default value is only as real as the code path that's supposed to
trigger it.

**kapt → KSP, and Glide's compiler just left.** The version pairing
matters (`1.9.24-1.0.20` for Kotlin 1.9.24 — get this wrong and the
build fails in a way that doesn't obviously point at a version
mismatch). While in there: this app has never declared an
`AppGlideModule`, so Glide's annotation processor was compiling for a
generated `GlideApp` API that doesn't exist anywhere in the codebase.
Removed rather than migrated — there was nothing to migrate.

**Thirty tests, mostly Context-free on purpose.** Every pure function
(CSV parsing, manifest parsing, DiffUtil callbacks, the exporter's
manifest-building) lives in a companion object specifically so a plain
JUnit test can call it without an Android runtime standing behind it.
The one place that genuinely needs Room + Context —
`AppRepositoryTest` — pulled in Robolectric rather than skip the
coverage, since there's no emulator here to run an instrumented test on
instead. `Room.inMemoryDatabaseBuilder` directly, not
`AppDatabase.getDatabase()`, so the tests get a database they control
rather than the full 266-card seed.

## What's deliberately still missing

See BACKLOG.md for the ordered, current list. Short version: no card
reordering, no favourites/history, seed vocabulary is still English-only,
no real screenshots, and — the one that actually matters most — none of
this has been watched running on a device or emulator. Compiling clean,
linting clean, and thirty passing unit tests are real signal. They are
not the same thing as tapping the app and watching it work.
