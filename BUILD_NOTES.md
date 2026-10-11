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

## Locale-aware vocabulary, and where I drew the line

The mechanism first: `AppDatabase` lists what's actually in
`assets/vocabulary/`, picks `communicards_vocabulary_v1_<lang>.csv` if
the device locale has one, and falls back to the English file otherwise.
The selection logic is a pure function taking a language tag and a set
of filenames — no Context, no asset access — so it's fully unit-tested
without Robolectric. Adding a language from here really is just
"translate the CSV, drop it in the folder." No code change required,
which was the entire point of making vocabulary an asset instead of
Kotlin literals back when this started.

Then I translated all 266 rows into French myself, in one pass, and
this is the part worth being honest about rather than quietly shipping.
I'm reasonably confident in most of it — the vocabulary is short,
concrete, mostly unambiguous words ("Oui", "Stop", "Fatigué"), the kind
of thing a competent non-native speaker gets right without much risk.
I am specifically **not** confident enough in `health_feelings_emergency`
to call it done. "Seizure warning" became "Crise à venir," "allergic
reaction" became "Réaction allergique" — these read fine to me, but
"fine to me" isn't the bar for a phrase someone might need to get right
in an actual emergency, and I'm not a fluent French speaker, let alone a
clinician. I've flagged it three times now (here, `CLAUDE.md`,
`BACKLOG.md`) specifically so it can't quietly become "the French one's
done" in someone's head. It parses, it displays, the mechanism works.
The words in the one category where a wrong word actually costs
something haven't been checked by anyone who'd know if they're wrong.

Checked what I could check without a fluent reader: every id matches
the English file exactly, same order, same colours, same emoji, same
priority flags — a script confirmed it, not just a read-through. That's
the part testing can verify. The words themselves needed a human who
isn't me, and didn't get one yet.

## Sentence strip mode, and keeping it genuinely optional

First item off P3, and the one bit of this app that's structurally
closer to PECS-style sentence assembly than the tap-once design everyone
else in this document keeps insisting on. Which is exactly why it's
off by default and stays that way unless a caregiver turns it on
deliberately in Settings — the same pattern Edit mode already
established, and for the same reason: nothing about the ordinary
tap-to-speak path is allowed to change for someone who never asked for
this.

The implementation is almost entirely UI plumbing once you accept that
constraint. `CategoryActivity` already branched card taps through a
single `onCardClick` lambda, so turning that into a two-way branch (speak
immediately, or append to an in-memory list) cost nothing structurally.
The one piece I did pull out deliberately is the sentence-joining itself
— `SentenceStrip`, a plain object, no Context, no Activity dependency —
because I've been burned once already this session by logic that looked
too simple to bother testing (the Gson defaults bug, see above), and
"join some strings together" is exactly the kind of thing that looks too
simple right up until someone's card list is empty or a label and its
speech text genuinely differ.

The strip bar itself replaces the speech bar rather than sitting
alongside it — the two are mutually exclusive by design, and showing
both would just be confusing about which one's currently doing anything.
Deliberately not persisted anywhere: it's scratch state for building one
sentence, not vocabulary, so it clears itself when the mode's switched
off or the screen's re-entered. Nothing there anyone would miss.

## Sixty-eight new cards, and why I didn't just rebuild the CSV

A caregiver sent over a proper brainstorm of AAC/PECS vocabulary — some
of it read off a real example board, most of it an expanded "what would
an autistic user actually need" list running to Emergency, Communication
Difficulty, Regulation, Autism-specific self-disclosure, and a good few
other headings. The instinct when handed a list like that is to treat it
as the new spec and rebuild the category structure around it. I didn't,
and it's worth saying why: the existing 266-card set wasn't a rough
draft. It's the clinically-structured vocabulary this app has been built
around since the very first CSV import, French translation and all, and
throwing that away to match a different person's brainstormed category
names would have been a straightforward regression dressed up as
progress.

So instead of rebuilding, I cross-checked the new list against the
existing one, label by label. Most of it was already there under a
different heading — "Toilet" and "Bathroom" both already exist, "Fidget
toy" and "Sunglasses" already exist, the days of the week and half a
dozen relevant places already exist. Once the genuine overlaps were
stripped out, 68 cards were left that the app actually didn't have:
specific foods (there was "Breakfast/Lunch/Dinner" but no actual food
items — fruit, a sandwich, tea, milk), named family roles (only generic
"Family" existed, not "Mum" or "Dad"), a run of specific emergency
contact actions ("Call ambulance", "Call police", "I'm lost"), and two
clusters of cards that didn't fit any existing category at all —
communication breakdown phrases ("I can't talk right now", "Speak
slowly", "Write it down") and autism self-disclosure phrases ("I'm
autistic", "I need routine", "Please don't rush me"). Those became two
new categories, Communication Support and About Me, rather than being
forced into Core & Social or Health/Feelings/Emergencies where they'd
have sat oddly.

Every new row got appended to the end of the CSV rather than inserted
inline, specifically so none of the existing 266 rows' ids, sort orders,
or category groupings moved even slightly — `Category.order` is set the
first time a category_id is seen in the file, so new rows for an
existing category (say, nine new food items landing in
`physical_self_care`) don't touch where that category sits in the grid.
A small Python script did the actual row generation (id slugs, sort
order continuing from 267, per-card emoji), checked afterwards for
duplicate ids, row-count parity between English and French, and that
every field was actually populated — the same kind of "verified with a
script, not just a read-through" approach the French translation got the
first time round.

Which brings up French again: I translated all 68 new cards myself, same
single-pass, not-clinically-reviewed basis as the original 266 — see
above. Several of the new cards are specifically emergency actions
("Appeler une ambulance", "Appeler la police"), so the existing caveat
about `health_feelings_emergency` needing a native-speaker or SLT review
before anyone relies on it applies more, not less, now that there's more
of it.

## Multiple profiles, and the trade-off I chose on purpose

The honest version of this decision, because it's the kind of thing that
looks like a shortcut until you say the trade-off out loud: profiles
share one copy of the seeded vocabulary rather than each getting their
own independent 334-card set. I picked that deliberately, for efficiency
— no duplicated rows per profile, no reseeding delay every time someone
adds one, and a much smaller `Category` change (one nullable-by-default
`profileId` column) than a genuinely separate-vocabulary-per-profile
design would have needed.

The cost of that choice, stated plainly rather than buried: if someone
edits or deletes a *shared* category, every profile on the device sees
that change, because there's only the one copy of it. For the vast
majority of shared devices — a family, a classroom set — that's fine;
the seeded vocabulary is right for everybody, and it's the custom
categories layered on top that actually need to be personal. It would
stop being fine for a use case where profiles need to diverge on the
*shared* words too, and if that ever turns out to matter in practice,
the honest fix is giving profiles their own seeded copy at creation time
— a bigger change, not a bug fix, and not one to make speculatively
before anyone's actually hit the limitation.

Mechanically: `Category.profileId` defaults to `0L`, which every seeded
row keeps forever — `CategoryDao.getCategoriesForProfile` is `WHERE
profileId = 0 OR profileId = :profileId`, so a profile always sees the
shared set plus whatever's actually theirs. A new category (via the `+`
FAB or an import) gets stamped with whichever profile is active at the
time, threaded through from `MainActivity.currentProfileId` down to
`MainViewModel`/`ImportViewModel` — easy to miss, since it's not obvious
from either ViewModel's signature alone that this matters, but skipping
it would mean a profile's own new category silently becoming global.

Switching happens the same way every other toggle in this app persists
across screens: `ProfileActivity` just writes a `SharedPreferences` key
and finishes, and `MainActivity.onResume()` reads it back and re-queries
— identical in shape to how Edit mode and sentence strip mode already
work, so there was no new pattern to invent here, just one more
`onResume` reading one more key.

One thing I refused to let happen regardless of how the rest turned
out: deleting the last profile. `ProfileActivity` checks the count
before offering the delete confirmation at all, and refuses outright if
it's down to one. An AAC app that could open onto zero profiles would
be considerably worse than one that shares a bit more vocabulary than
some future use case might prefer.

## The widget, and the Android plumbing nobody warns you about

Home-screen widgets are the one part of this app that genuinely doesn't
run in the same world as the rest of it. Everything else here is an
Activity with a ViewModel talking to Room through coroutines — perfectly
ordinary. A widget's `RemoteViews` live in the launcher's process, not
this app's, and can only be built from a small, fixed set of view types
and a small, fixed set of things you're allowed to do with them (no
arbitrary click listeners, no direct database access from the click
itself). Getting that plumbing right mattered more here than the actual
feature logic did.

Three pieces, each solving a different constraint. `NeedsWidgetProvider`
handles the widget's lifecycle (`onUpdate`, `onDeleted`) — importantly,
`onUpdate` runs on the *main* thread (it's a `BroadcastReceiver`
callback), so it can't touch Room directly without crashing; it uses
`goAsync()` plus a coroutine to do the actual database work off the main
thread before finishing. `NeedsWidgetRemoteViewsService` is the opposite
case — its `RemoteViewsFactory` methods are individually documented by
Android as already running on a background thread the widget host
provides for exactly this purpose, so blocking Room calls there
(`runBlocking`) are the correct, expected pattern, not a shortcut I got
lazy about. Two structurally similar-looking pieces of code, opposite
threading rules, and getting them backwards would have meant either a
`CalledFromWrongThreadException`-style crash or an ANR, in two different
ways nobody would think to test until an actual device ran it.

Tapping a card is its own small maze. A `GridView`-backed collection
widget can't just call code on click — it fires a `PendingIntent`
template, merged with a per-item "fill-in" `Intent` from the factory,
landing wherever that template points. I pointed it at a
`BroadcastReceiver` (`WidgetTapReceiver`) rather than an Activity,
specifically so nothing visible flashes on screen for a tap that's
supposed to feel instant. The one non-obvious requirement: the template
`PendingIntent` has to be created with `FLAG_MUTABLE`, not the
`FLAG_IMMUTABLE` every general Android security guide tells you to
prefer — collection widgets need the fill-in intent's extras to actually
merge in, and an immutable template silently refuses that. Left a
comment on it in the code specifically so a future security-conscious
pass doesn't "fix" it into something that quietly breaks every tap.

`WidgetTapReceiver` doesn't reuse `TTSManager`, and that's worth
explaining rather than looking like an oversight — `TTSManager` is built
around an Activity holding one instance alive between calls;
`onReceive()` gets a few seconds to do its work and then it's gone, no
Activity to hold anything. So it builds and tears down its own
`TextToSpeech` per tap, with a `goAsync()` `PendingResult` held open
until an `UtteranceProgressListener` says the speech actually finished
(plus a 4-second safety-net timeout, in case that callback never fires —
better to cut a tap short than risk an ANR over a receiver that never
calls `finish()`). It does still read the same `KEY_SPEECH_RATE`/
`KEY_PITCH` preference keys `TTSManager` uses, though, precisely so the
widget doesn't quietly speak at a different rate than whatever a
caregiver already dialled in.

The scope cut I want on the record: the widget refreshes on a roughly
30-minute timer (`updatePeriodMillis`, which Android clamps to that
floor regardless of what's requested) rather than the instant a card's
edited in the app. Making it instant would mean threading a
"notify any widgets watching this category" call through every existing
card/category mutation path — `CategoryActivity`'s edit/delete,
`MainActivity`'s edit/delete, the importer, the exporter — coupling the
whole app to a feature that, realistically, most people will glance at
a few times a day rather than watch for real-time updates. Thirty
minutes of staleness felt like the honest trade, not a corner cut
silently.

## Mascot mode, and shipping half a feature on purpose

A caregiver sent over two mockup sheets of an original mascot character
they designed — a dinosaur, AI-generated only as a "what would this look
like" demo, the actual design is theirs. Their plan: not one piece of
art per card, but a reusable set of maybe twenty pose variations, each
usable across however many of the 334 cards actually fit that tone.
Before building anything I asked whether they wanted separate reusable
art or baked-in card templates, and recommended separate — the app
already owns a lot of per-card rendering (category colour, the urgent
red border, font-scale-aware labels) that a baked-in design would
either lose or have to duplicate per colour. They confirmed: opt-in,
default off.

What I didn't do, and want on the record as a deliberate choice rather
than something half-finished: I didn't invent a pose taxonomy. It would
have been easy to map "health_feelings_emergency + urgent → distressed",
"core_social → friendly", and so on, and wire up eight or so guessed
pose names right now. I didn't, because that's not an engineering
decision — it's the character designer's creative call, and guessing it
for them risks either committing to names that don't match what they
actually produce, or quietly steering their design toward whatever I
happened to pick. So `FlashCard.mascotPose` is a genuinely free-form
string, blank on every seed card, and the lookup
(`MascotArt.resourceNameFor` → `Resources.getIdentifier`) resolves
whatever pose name eventually gets set against whatever `mascot_*.png`
eventually gets bundled — the mechanism doesn't care what the names are,
on purpose.

The result is a toggle that's completely real — persisted, tested,
wired through `CategoryActivity`'s existing onResume-reads-prefs
pattern — and completely invisible in its current effect, since turning
it on changes nothing for any of the 334 existing cards. That's an
unusual thing to ship deliberately, so it's spelled out in three places
(CLAUDE.md invariant 15, the Settings explainer text itself, and here)
rather than left for someone to discover by toggling it and wondering if
it's broken.

One more thing worth recording: this is the point in the session where
the container actually got reset mid-conversation — lost the local
clone, the Android SDK, everything but the pushed git history. Redid the
SDK setup (command-line tools, platform 34, build-tools 34, accept
licenses, write a throwaway `local.properties`) exactly the way the
first verification pass describes above, which is the whole reason that
section exists as a reusable recipe rather than a one-off aside.

## Onboarding, pure text mode, and two real fonts

Two requests arrived together: a button at setup for an image-free, pure
text option, and dyslexia-friendly/Easy Read font support. Both needed
an actual onboarding screen, since "on install" means before the app's
normal UI exists yet — but both also needed to NOT be onboarding-only,
since locking an accessibility preference behind a screen you only see
once is a real design mistake, not a convenience. So every choice
`OnboardingActivity` makes also lives as an ordinary `SettingsActivity`
switch, and I built Settings' version first conceptually, then had
onboarding just be an earlier, friendlier entry point to the same
stored preferences.

The gating itself needed more care than it looks like it should.
`MainActivity.onCreate` checks `KEY_ONBOARDING_COMPLETE` before
`setContentView`, and redirects + `finish()`s if it's unset. The gotcha:
Android still calls `onResume()` once on the way out after a `finish()`
called from `onCreate()` — it doesn't just stop dead. `MainActivity`'s
`onResume` reads prefs and touches `categoryAdapter`, which was never
initialised in the onboarding-redirect branch (`setupRecyclerView()`
never ran), so without a guard this crashes on literally every first
launch — the one path that most needs to work. Fixed with
`if (!::categoryAdapter.isInitialized) return` at the top of
`onResume()`, and commented clearly in CLAUDE.md invariant 16 so the
next finish()-from-onCreate path doesn't reintroduce the same bug
blind.

Pure text mode's precedence rule matters more than it sounds: it has to
beat a custom photo and Mascot mode's artwork, not just the plain emoji
fallback, or "pure text" would quietly mean "pure text, except for the
cards someone's already customised" — a confusing half-measure for
exactly the reader it's meant to help. `FlashCardAdapter.bind()` checks
it first, before any of the photo/mascot/emoji branching even runs.

Font style turned into the more interesting engineering problem, because
there are two genuinely different places text gets rendered and they
need two different mechanisms. A static screen's text exists the moment
`setContentView` returns, so `BaseActivity` can walk the whole content
view tree once, right after inflation, and set a custom `Typeface` on
every `TextView` it finds — reliable, version-proof (`ResourcesCompat
.getFont` plus manual `.typeface =` assignment works identically back to
API 14, unlike leaning on theme-level `fontFamily` cascading through
Material's `TextAppearance` styles, which I didn't trust enough to rely
on alone without being able to verify it against a real device). But a
RecyclerView's item views don't exist at `setContentView` time — they
get inflated later, as the list scrolls — so `BaseActivity`'s one-shot
walk can't reach card labels, category names, or profile names, which
are arguably the most important text in the whole app to get legible.
Fixed by giving `CategoryAdapter`/`FlashCardAdapter`/`ProfileAdapter`
their own `customTypeface` field, applied at bind time, set by their
hosting Activity's `onResume` — the exact same shape as Edit mode and
Mascot mode already used, so no new pattern needed inventing, just the
existing one extended to a third mutable field.

For the fonts themselves: OpenDyslexic and Atkinson Hyperlegible, both
fetched from their own GitHub repositories, both SIL Open Font License
1.1 — verified by actually downloading and reading `OFL.txt` from each
repo rather than assuming, since this is a project that already ruled
out ARASAAC specifically over a non-commercial licence clause. SIL OFL
is unambiguous: free to embed in a commercial app, the only real
restriction is not selling the font file on its own, which was never
the plan. Regular and Bold weights for both, wired through font-family
XML resources with both `android:` and `app:` attribute pairs — the
native `android:fontFamily` XML support only starts at API 26, and this
app's `minSdk` is 24, so the `app:`-prefixed duplicates are what
actually make OpenDyslexic/Atkinson Hyperlegible work correctly on API
24-25 via AppCompat's font-compat loading. Full attribution in the new
`THIRD_PARTY_LICENSES.md`, plus each font's own `OFL.txt` bundled
unmodified in `assets/font_licenses/` — the license itself has to travel
with the font, not just get mentioned in a README somewhere.

## No emulator, and it's not just "not set up yet"

Checked properly before writing this off: no `/dev/kvm`, no `vmx`/`svm`
in `/proc/cpuinfo`, no nested virtualisation exposed. That's not a
missing SDK component or a config flag — it's the underlying hardware
access an Android emulator needs, genuinely absent from this container.
Software-rendered emulation without acceleration exists in theory but is
slow enough to be impractical for anything beyond a toy demo, and
wouldn't represent real device behaviour reliably even if it ran.
Screenshots and instrumented UI tests (BACKLOG P2 items 2 and 3) are
blocked on this specifically, not on time or effort — worth knowing the
difference before someone spends an hour trying to coax an AVD out of a
container that structurally can't run one.

## What's deliberately still missing

See BACKLOG.md for the ordered, current list. Short version: no card
reordering, no favourites/history, the French translation needs a
fluent-speaker review (see above), no real screenshots, no instrumented
tests — the last two genuinely blocked by the missing emulator, not
skipped. Compiling clean, linting clean, and sixty-two passing unit
tests are real signal. They are not the same thing as tapping the app
and watching it work.
