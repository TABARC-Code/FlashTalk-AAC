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
bundled. Grey boxes, the lot. Three ways out were on the table —
ARASAAC (CC BY-NC-SA, and that NC clause is exactly the kind of thing
that turns into a licensing headache six months in), Mulberry Symbols
(safer, still a dependency to track), or just... use emoji.

Went with emoji. Not because it's clever, but because it makes an entire
category of future problem simply not exist. No licence to track, no
asset pipeline, no "did we attribute this correctly" Slack thread at 11pm
before a release. `FlashCard` got an `emoji` field sat next to
`imagePath`; seed cards use one, custom photo cards use the other. It's
a two-line change with an outsized effect on how much I have to think
about this later.

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

## What's deliberately still broken (or missing)

Nothing here is an accident — see BACKLOG.md for the ordered list, but
the short version: no edit/delete UI (repository support exists, no
screen wired to it yet), no export, no settings lock, zero automated
tests, still on kapt instead of KSP, seed vocabulary is still
hardcoded English. All flagged, all intentional, all next.
