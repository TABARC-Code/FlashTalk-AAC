# FlashTalk AAC

A free, open-source Android AAC app. Tap a card, it speaks. That's it,
and that's deliberate — I'd rather defend that decision than apologise
for it.

Most flashcard communication apps are expensive, cloud-tethered, or both.
This one's neither. Everything lives on the device. No account, no
subscription, no analytics quietly phoning home — the app doesn't even
request the INTERNET permission, because it genuinely has nothing to say
to a server. Tap a picture card, the phone says the word. Fast enough to
be useful in an actual conversation, which is the bar most AAC apps
quietly fail while their marketing copy claims otherwise.

Built for autistic users of any age. Simple enough for a four-year-old,
customisable enough for an adult who wants their own vocabulary, their
own photos, and none of the cartoon nonsense some of these apps assume
everyone wants.

## What it does

- Single-tap communication — card to speech, nothing blocking in between
- Nine seeded categories (Core & Social, Actions & Requests, Physical
  Needs & Self-Care, Health/Feelings/Emergencies, Sensory & Comfort,
  Objects & Leisure, Places/Time/Sequence, Communication Support, About
  Me), 334 starter cards — a real vocabulary set, not a starter list
- Seed vocabulary follows the device's locale — French is bundled
  alongside English, and adding another language is just translating a
  CSV, no code changes needed. (The French file is a single first-pass
  translation, not a native-speaker or SLT-reviewed one — see
  `BACKLOG.md` item 1 before trusting the emergency-related cards.)
- Custom cards from your own photos, unlimited — picked via the system
  photo/document chooser, so no storage permission is ever requested
- Custom categories, unlimited
- Bulk import of whole card sets from ZIP or JSON — build a set on your
  PC, drop it onto the device — and export any category back out again
  in the same format
- Edit or delete any card or category, behind an Edit mode toggle that's
  off by default — long-press does nothing until you've deliberately
  turned it on in Settings
- A persistent speech bar shows the last-spoken word with a repeat
  button, instead of a Toast that's gone before anyone slower than
  instant can read it
- A maths-question gate on Settings and Import — not security, a
  caregiver-facing speed bump against wandering in mid-session
- TTS rate and pitch controls, dark mode, and a large-text mode that
  actually does something
- Sentence strip mode, off by default — tap cards to build a short
  sentence instead of speaking each one immediately, then tap the strip
  to speak the whole thing. PECS-adjacent, opt-in only, and the ordinary
  tap-once behaviour is completely unchanged when it's off
- Works entirely offline, always

## What it doesn't do (yet)

Honesty section, because I'd rather write this than have someone else
discover it the hard way. Seed cards render as an emoji glyph rather than
a photo — that's a decision, not a placeholder waiting on one. It means
the app never had to settle a symbol-set licence before it could ship
with cards that look like something. Your own photo cards work exactly
as you'd expect, no caveats there.
There's no card reordering yet. Sentence strip mode exists now but is
opt-in and off by default — single-tap is still the default design, and
that's deliberate, not a placeholder. Nothing here has been run on an
actual device or emulator either — compiled, linted, and unit-tested (38
tests), but not watched running, and that's not a "hasn't gotten to it
yet": this environment has no `/dev/kvm` and no CPU virtualisation flags
at all, so an Android emulator genuinely isn't something that runs here.
`BACKLOG.md`
has the full confession, in priority order — I'd rather it be too honest
than flattering.

## Building it

You'll need Android Studio (Hedgehog or later), JDK 17, and enough
patience for the first Gradle sync, which is never as fast as you
remember.

```bash
git clone <this repo>
cd FlashTalkAAC
./gradlew assembleDebug
```

Or open the folder in Android Studio and hit Run. Min SDK is 24
(Android 7.0), target's 34.

## Importing and exporting card sets

Zip up a `manifest.json` with your images and import it from the app
menu — or skip the images entirely and import a JSON-only set instead,
where each card falls back to an emoji. Export works the same way in
reverse: pick a category's menu → Export, and it writes out to the exact
same ZIP+manifest format, ready to hand to someone else or re-import
later. Format and worked examples live in `example_imports/`. Short
version:

```json
{
  "set_name": "Animals",
  "category_name": "Animals",
  "category_icon": "🐶",
  "category_color": "#FF9F43",
  "cards": [
    { "text": "Dog", "icon": "🐶" }
  ]
}
```

## For contributors (human or otherwise)

`CLAUDE.md` gives an AI coding agent what it needs to work on this repo
without breaking it — architecture map, invariants, the design rules
that actually matter (touch targets, contrast, no surprise animations).
`BACKLOG.md` is the prioritised queue. `CONTRIBUTING.md` covers the human
formalities.

The one rule that outranks everything else: nothing may slow down or
complicate the tap-to-speak path. Communication latency is the product,
not an implementation detail.

## Licence

MIT. Use it, fork it, ship it. If it helps someone talk, it's done its
job — everything else is secondary to that.

---

Author: TABARC-Code
