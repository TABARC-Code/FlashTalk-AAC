# Backlog

Prioritised. Work top-down. Each item sized roughly: S (< 1 hr), M (half
day), L (day+). Move completed items to CHANGELOG.md.

## P0 — the app is dishonest without these

_All clear as of this build — see CHANGELOG.md for what was resolved
(seed card images, "Large text" wiring, delete-cleans-up-images)._

## P1 — core UX gaps

1. **[M] Edit and delete for cards and categories.** Long-press → context
   menu (edit text, change image, delete with confirm). The category
   "Edit" menu item is currently a TODO toast. Design constraint: entry
   to edit mode must be hard to trigger accidentally — a child mid-
   communication must not land in a delete dialog. Consider a settings-
   gated "edit mode" toggle instead of raw long-press.

2. **[M] Speech bar instead of Toasts.** Toasts are the wrong idiom for
   AAC. Replace with a persistent bar at the top of CategoryActivity
   showing the last-spoken word, with a repeat button. Established
   pattern in every serious AAC app.

3. **[S] Settings lock.** A caregiver-facing concern: users shouldn't
   wander into Settings/Import mid-session. Simple maths-question gate
   (standard in kids' apps) on Settings, Import, and edit mode.

4. **[M] Export.** Import exists and is fully implemented (ZIP + JSON);
   export doesn't. Write a category to the ZIP+manifest format
   ImageSetImporter already reads. Round-trip test: export → wipe →
   import → identical.

## P2 — engineering hygiene

5. **[S] Migrate kapt → KSP.** kapt is deprecated. Room and Glide both
   support KSP. Also bump AGP/Gradle/dependencies to current stable.

6. **[M] Tests.** Start with: ImageSetImporter manifest parsing (happy
   path + malformed JSON + missing images — the zip-slip and 50MB-cap
   guards are exactly the kind of thing that quietly breaks under
   refactor without a test), Repository CRUD with in-memory Room,
   DiffUtil callbacks. No UI tests yet — foundation first.

7. **[S] Locale-aware seed data.** Seed vocabulary is hardcoded English.
   Move to string resources so translations become possible. (TTS
   already follows device locale via `Locale.getDefault()`; the card
   text should be able to as well.)

8. **[S] Screenshots.** Take real ones on an emulator, put them in
   `docs/screenshots/`. Nothing references phantom images yet, so this
   is additive, not a fix.

## P3 — future, only after the above

9. **[L] Sentence strip mode** (optional, off by default — tap cards to
   build a strip, tap strip to speak whole phrase). PECS-adjacent.
10. **[L] Multiple profiles.**
11. **[M] Home-screen widget** for the Needs category.
12. **[L] Switch-access scanning support.**

## Rejected / not doing

- Cloud sync and accounts — conflicts with the offline/privacy commitment
- Analytics of any kind — same reason
- Gamification — this is a communication tool, not a game
