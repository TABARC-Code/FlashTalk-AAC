# Example import sets

Three ready-made sets — Animals, Colours, Transportation — each a single
`manifest.json`. JSON-only, no bundled photos, so every card falls back
to an emoji glyph. Didn't see the point in shipping placeholder images
when the emoji fallback already exists and looks fine.

## Trying them

In the app: **Menu (⋮) → Import Set → Import JSON File**, then pick one
of these `manifest.json` files. Each import creates one new category
with all of its cards.

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
  in this folder use.

## Two ways to package a set

**JSON only** (what's here): just `manifest.json`, imported directly.
Fastest to put together, no photos required.

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
the ZIP that isn't referenced by a card gets ignored, not an error. If
`image_filename` doesn't match a file in the ZIP, that one card falls
back to `icon` / `category_icon` rather than failing the whole import —
you'll see exactly which ones didn't match in the import summary, not a
generic "something went wrong".

## Limits

- ZIP imports are capped at 50MB total.
- `category_name` and `cards` are required. Everything else has a
  sensible default, so don't feel obliged to fill in every field.
