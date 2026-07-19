# Example import sets

Three ready-to-use sets — Animals, Colors, Transportation — each a single
`manifest.json`. They're JSON-only (no bundled photos), so every card
falls back to an emoji glyph rather than a missing image.

## Trying them

In the app: **Menu (⋮) → Import Set → Import JSON File**, then pick one of
these `manifest.json` files. Each import creates one new category with all
of its cards.

## The manifest format

```json
{
  "set_name": "Display name for the set (informational only)",
  "category_name": "Name shown on the category card",
  "category_icon": "🎯",
  "category_color": "#FF6B6B",
  "cards": [
    { "text": "Card text (spoken aloud)", "image_filename": "photo.jpg" },
    { "text": "Card text (spoken aloud)", "icon": "🎨" }
  ]
}
```

- `category_icon` / `category_color`: shown on the category tile on the
  main screen.
- `cards[].image_filename`: only meaningful in a **ZIP** import (see
  below) — the name of an image file bundled alongside `manifest.json`.
- `cards[].icon`: an optional per-card emoji, used when there's no image.
  Falls back to `category_icon` if omitted. This is what the three sets
  in this folder use — no images needed.

## Two ways to package a set

**JSON only** (what's in this folder): just `manifest.json`, imported
directly. Fastest to create, no photos required — cards render with
`icon` (or `category_icon` as a fallback).

**ZIP with photos**: `manifest.json` plus the image files it references,
zipped together:

```
my_set.zip
├── manifest.json
├── dog.jpg
└── cat.jpg
```

Reference each file by name in `image_filename`. The importer copies
referenced images into the app's private storage on import; anything in
the ZIP that isn't referenced by a card is ignored. If `image_filename`
doesn't match any file in the ZIP, that card falls back to `icon` /
`category_icon` instead of failing the whole import — you'll see which
ones didn't match in the import summary.

## Limits

- ZIP imports are capped at 50MB total.
- `category_name` and `cards` are required; everything else has a
  sensible default.
