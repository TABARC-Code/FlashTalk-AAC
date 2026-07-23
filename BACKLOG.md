# Backlog

Prioritised. Work top-down — the ordering isn't decorative, it reflects
what actually matters more. Each item's sized roughly: S (under an hour),
M (half a day), L (a day or more, and probably an underestimate). Move
completed items to CHANGELOG.md rather than just deleting them; future-me
will want to know what happened here.

## P0 — the app is dishonest without these

_All clear. See CHANGELOG.md for what got fixed (seed card images,
"Large text" actually doing something, delete cleaning up after itself).
I'm not pretending these were hard — they were mostly embarrassing._

_Seed imagery specifically: the vocabulary covers 334 real cards across
9 categories, each with a hand-picked emoji glyph, sourced from a CSV
that's the actual seed data (not hardcoded Kotlin). A proper symbol set
is still an open, separate decision — whatever gets chosen has to work
for a commercial product, which rules out more than it lets in. Not
blocking anything; the emoji stand-in works fine for now._

## P1 — core UX gaps

_Also all clear. Edit/delete (gated behind an off-by-default Edit mode
toggle), the speech bar, the Settings/Import maths gate, and category
export are all in and tested — see CHANGELOG.md. What's genuinely not
watched running yet: none of this has been seen on an actual device or
emulator, only compiled, linted, and unit-tested. That's worth keeping
in mind before calling any of it finished-finished._

## P2 — engineering hygiene

_kapt → KSP is done, and the dead Glide annotation processor came out
with it (this app never declared an `AppGlideModule`, so that dependency
was never doing anything). Also done: 30 unit tests across CSV parsing,
manifest parsing, DiffUtil callbacks, the export round-trip, and
Repository CRUD via Robolectric — up from the single CSV-parser test
this item started with._

1. ~~**[S] Locale-aware seed data.**~~ **Done for French, mechanism done
   for everyone else.** `AppDatabase` now picks
   `communicards_vocabulary_v1_<lang>.csv` by device locale
   (`vocabularyAssetNameFor`, tested), falling back to the English file
   when no variant is bundled. Only French is bundled today — every row
   (334 of them now, after the vocabulary expansion below) translated in
   one pass, by me, not a fluent speaker, not an SLT. Fine for a lot of
   it; **not** fine to trust blind for `health_feelings_emergency`
   specifically ("seizure warning", "allergic reaction", "call ambulance"
   and the like) — those need a native-speaker or clinical review before
   anyone relies on them for a real emergency. Flagged in `BUILD_NOTES.md`
   too so it doesn't get lost. Adding another language is now "translate a
   CSV, drop it in `assets/vocabulary/`" — no code changes required.

~~**[M] Expand the seed vocabulary.**~~ **Done.** A caregiver-supplied
brainstorm (visible AAC/PECS board cards plus a much longer "what an
autistic user might need" list — Emergency, Communication Difficulty,
Regulation, Autism-specific self-disclosure, and more) got cross-checked
against the existing 266 cards line by line rather than dumped in
wholesale — most of it (Yes/No/Please/Toilet/fidget toy/sunglasses/GP,
and so on) was already covered under different category names. Only the
genuinely new 68 cards got added: most slotted into the existing 7
categories (9 new food items, 8 new contacts/emergency actions, 6 new
family/role people, 6 new everyday objects, and so on), and two concepts
didn't fit anywhere existing, so they became new categories —
**Communication Support** (7 cards: "I can't talk right now", "Speak
slowly", "Write it down") and **About Me** (8 cards: "I'm autistic", "I
need routine", "Please don't rush me"). 334 cards, 9 categories, both
English and French kept in sync row-for-row. See `BUILD_NOTES.md` for
the full reasoning on why this was additive rather than a rebuild.

2. **[S] Screenshots.** Take real ones on an emulator, drop them in
   `docs/screenshots/`. Nothing currently references phantom images, so
   this is purely additive — there's no lie to fix, just a gap to fill.
   Still blocked: no `/dev/kvm`, no vmx/svm CPU flags in this environment
   — an emulator isn't just "not set up yet" here, it's not viable
   without different hardware.

3. **[M] UI/instrumented tests.** Everything so far runs on the JVM
   (plain JUnit or Robolectric) precisely because there's no emulator in
   the environment this got built in (see item 2 — same root cause). The
   actual on-screen behaviour of Edit mode, the speech bar, and the maths
   gate has never been watched running — only reasoned about and
   unit-tested at the logic layer. Espresso tests for the core
   tap-to-speak flow would close that gap, whenever this runs somewhere
   with real virtualisation.

## P3 — future, only after the above

~~**[L] Sentence strip mode.**~~ **Done.** Off by default, a Settings
toggle away. On: tapping a card in `CategoryActivity` appends it to an
in-memory strip instead of speaking it straight away; a dedicated strip
bar replaces the ordinary speech bar while the mode's on, showing the
sentence built so far, with a tap-to-speak and a separate clear control.
Off (the default): behaviour is unchanged from every other section of
this document. The join logic lives in `SentenceStrip`, a plain object
with no Context dependency, precisely so it could be unit-tested without
Robolectric — four tests cover the empty-strip, single-card, multi-card,
and label/speech-text-differ cases. The strip itself is deliberately not
persisted; it's a scratchpad for one sentence, not vocabulary data, and
doesn't need to survive the activity being recreated.

~~**[L] Multiple profiles.**~~ **Done, shared-vocabulary design.** A
`Profile` entity plus a `profileId` column on `Category` — `0L` means
shared/global (every seeded category defaults to it, visible to every
profile), any other value is a specific profile's own custom category,
visible only to that profile. Chose this over giving every profile a
full independent copy of the 334-card vocabulary specifically for
efficiency: no duplicated rows, no per-profile reseeding, and adding a
profile is instant rather than a fresh 334-row insert. The trade-off,
stated plainly: editing or deleting a *shared* category still affects
every profile, since there's only one copy of it. `ProfileActivity`
(switch/add/edit-mode-gated edit/delete) sits behind MathGate like
Settings and Import; deleting the last remaining profile is refused
outright, since the app must never open onto zero profiles. Deleting a
profile only removes categories it actually owns — the shared vocabulary
is untouched.
5. **[M] Home-screen widget** for the Needs category.
6. **[L] Switch-access scanning support.**

## Rejected / not doing

- Cloud sync and accounts — conflicts with the offline/privacy commitment
  outright, not a "maybe later"
- Analytics of any kind — same reason, and no, "just crash reports"
  doesn't get a pass either
- Gamification — this is a communication tool, not a game, and treating
  it like one would be a genuinely bad idea for the people using it
