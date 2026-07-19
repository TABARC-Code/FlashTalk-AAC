# FlashTalk AAC

A free, open-source Android AAC app. Tap a card, it speaks. That's it, and
that's deliberate.

Most flashcard communication apps are either expensive, cloud-tethered, or
both. This one is neither. Everything lives on the device, there's no
account, no subscription, and no analytics quietly phoning home — the app
doesn't even request the INTERNET permission. The user taps a picture card
and the phone says the word. Fast enough to be useful in an actual
conversation, which is the bar most AAC apps quietly fail.

Built for autistic users of any age. Simple enough for a four-year-old,
customisable enough for an adult who wants their own vocabulary, their own
photos, and none of the cartoon nonsense.

## What it does

- Single-tap communication — card to speech with nothing blocking in
  between
- Eight seeded categories (food, feelings, activities, people, places,
  needs, yes/no, time), 56 starter cards
- Custom cards from your own photos, unlimited — picked via the system
  photo/document chooser, so no storage permission is ever requested
- Custom categories, unlimited
- Bulk import of whole card sets from ZIP or JSON — build a set on your
  PC, drop it onto the device
- TTS rate and pitch controls, dark mode, a working large-text mode
- Works entirely offline

## What it doesn't do (yet)

Honesty section. Seed cards render as an emoji glyph rather than a photo
— that's a deliberate choice, not a placeholder awaiting a decision: it
means the app never had to settle a symbol-set licence (ARASAAC, Mulberry,
etc.) to ship with recognisable cards. Your own photo cards work exactly
as you'd expect. There's no sentence building (single-tap is the design,
but a strip mode is on the backlog), no export yet, and no edit-in-place
for existing cards. See BACKLOG.md for the full confession, in priority
order.

## Building it

You need Android Studio (Hedgehog or later), JDK 17, and patience for the
first Gradle sync.

```bash
git clone <this repo>
cd FlashTalkAAC
./gradlew assembleDebug
```

Or open the folder in Android Studio and hit Run. Min SDK is 24
(Android 7.0), target is 34.

## Importing card sets

Zip up a `manifest.json` and your images, import from the app menu — or
skip the images entirely and import a JSON-only set (each card falls back
to an emoji). Format and worked examples live in `example_imports/`. The
short version:

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

`CLAUDE.md` gives an AI coding agent everything it needs to work on this
repo sensibly — architecture map, invariants, and the design rules that
actually matter (touch targets, contrast, no surprise animations).
`BACKLOG.md` is the prioritised work queue. `CONTRIBUTING.md` covers the
human formalities.

The one rule that outranks all others: nothing may slow down or
complicate the tap-to-speak path. Communication latency is the product.

## Licence

MIT. Use it, fork it, ship it. If it helps someone talk, it's done its
job.

---

Author: TABARC-Code
