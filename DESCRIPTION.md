# FlashTalk AAC — Description

**One line:** free, offline, single-tap flashcard communication app for
Android, built for autistic users.

**Longer:** the phone or tablet becomes a picture-card communication
device. Tap a card, it speaks the word. That's genuinely the whole
mechanism, and I'd rather defend that as a feature than apologise for it.
No sentence-building ceremony, no account creation, no cloud, no
subscription. Seven seeded categories, 266 cards, cover a genuinely
comprehensive daily-needs vocabulary out of the box — not a starter
list — and caregivers can extend it further with photos from the
gallery or a bulk-imported card set built on a PC.

Who it's actually for: autistic children and adults with limited or
unreliable speech, the people around them, and the SLTs and teachers
trying to make it work in practice. Also, frankly, anyone who's tired of
communication apps that cost money and phone home for no reason.

**Design position:** closer to Leeloo's quick-tap communication than
PECS-style sentence assembly. Narrow on purpose — it does one interaction
(tap → speak) and says no to anything that would slow that down, which is
a harder discipline to hold onto than it sounds when every feature
request looks reasonable in isolation.

**Stack:** Kotlin, MVVM, Room, Coroutines, Glide, Material Components.
Min SDK 24. One module, no DI framework, and no network stack at all —
not even an INTERNET permission in the manifest.

**Status:** v1.0, and it actually builds — I ran it through a real SDK
before calling it done, not just assumed. Seed cards render as emoji
glyphs rather than photos, which sidesteps a symbol-set licensing
decision entirely rather than kicking it down the road. Edit/delete UI
and export are the honest gaps left; see BACKLOG.md, in order, no
skipping ahead.

Author: TABARC-Code
