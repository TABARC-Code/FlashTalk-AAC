# FlashTalk AAC

**v1.0 — single-tap AAC communication**

FlashTalk AAC is a cross-platform (iOS / Android / web) Augmentative and
Alternative Communication app. Tap a tile, it speaks — no menus, no
confirmation dialogs, no delay.

## v1.0 feature set

- Single-tap tiles: tapping a tile immediately speaks its phrase aloud via
  on-device text-to-speech (works offline, no network required).
- Five starter vocabulary categories: **Needs**, **Feelings**, **People**,
  **Social**, **Actions** — 30 core words/phrases total.
- Large, high-contrast touch targets sized for motor-accessibility.
- New speech requests interrupt any speech in progress, so the app never
  feels laggy under repeated taps.

## Tech stack

- [Expo](https://expo.dev) (React Native + TypeScript)
- [`expo-speech`](https://docs.expo.dev/versions/latest/sdk/speech/) for
  on-device text-to-speech

## Getting started

```bash
npm install
npx expo start
```

Then open the app in Expo Go (iOS/Android), or press `w` to run it in a
browser.

## Project structure

```
App.tsx                  # App entry point
src/
  data/
    categories.ts         # Category definitions (id, label, color)
    tiles.ts               # Vocabulary tiles (label, spoken phrase, emoji, category)
  components/
    Tile.tsx                # Single tappable AAC tile
    CategoryTabs.tsx        # Category switcher
  screens/
    HomeScreen.tsx           # Main screen: category tabs + tile grid
  hooks/
    useSpeech.ts              # Text-to-speech wrapper (expo-speech)
  theme.ts                    # Color palette & spacing
  types.ts                    # Shared TypeScript types
```

## Roadmap ideas (post-v1.0)

- Editable/custom vocabulary boards
- Adjustable speech rate/pitch/voice in a settings screen
- Multi-tile sentence building
- Symbol sets beyond emoji (e.g. PCS/ARASAAC-style boards)

## License

MIT — see [LICENSE](./LICENSE).
