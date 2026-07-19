# FlashTalk AAC — Description

**One line:** Free, offline, single-tap flashcard communication app for
Android, built for autistic users.

**Longer:** FlashTalk AAC turns an Android phone or tablet into a
picture-card communication device. The user taps a card — a symbol with
a word under it — and the device speaks the word aloud. No
sentence-building ceremony, no accounts, no cloud, no cost. Eight seeded
vocabulary categories cover daily needs out of the box; caregivers extend
it with photos from the device gallery or bulk-import whole card sets
built on a PC.

**Who it's for:** Autistic children and adults with limited or unreliable
speech, their families, SLTs, and teachers. Also anyone with a
communication disability who wants a tool that respects their privacy and
their wallet.

**Design position:** Leeloo-style quick communication rather than
PECS-style sentence assembly. Deliberately narrow — the app optimises one
interaction (tap → speak) and refuses features that would slow it down.

**Stack:** Kotlin, MVVM, Room, Coroutines, Glide, Material Components.
Min SDK 24. Single module, no DI framework, no network stack (no INTERNET
permission at all).

**Status:** v1.0 working build. Core loop is solid; seed cards render as
emoji glyphs rather than photos, which sidesteps a symbol-set licensing
decision entirely rather than deferring it. Edit/delete UI and export are
the headline gaps — tracked honestly in BACKLOG.md.

Author: TABARC-Code
