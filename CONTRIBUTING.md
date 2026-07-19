# Contributing to FlashTalk AAC

Thanks for even considering it. This project's aim is free, accessible
communication tools for autistic people and anyone else who needs them —
if that's why you're here, you're in the right place.

## How to Contribute

### Reporting Bugs

Open an issue with:
- A clear description of the bug
- Steps to reproduce it
- What you expected vs. what actually happened
- Android version and device
- Screenshots, if they'd help — they usually do

### Suggesting Features

Feature suggestions are genuinely welcome, but read `BACKLOG.md` first —
there's a decent chance it's already been thought about and either
queued or deliberately rejected (see the "Rejected / not doing" section;
cloud sync and analytics aren't oversights). If it's not there, open an
issue with:
- A clear description of the feature
- The use case — why it's actually needed, not just neat
- How you'd expect it to work
- Examples from other apps, if any come to mind

### Code Contributions

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes**
   - Follow standard Kotlin conventions
   - Keep the MVVM architecture — don't smuggle logic into Activities
   - Comment where the *why* isn't obvious, not the *what*
   - Update docs if behaviour changes; stale docs cost more than they
     look like they save

4. **Test your changes**
   - Multiple Android versions if you can manage it
   - Accessibility features still need to work — don't just eyeball this
   - TTS still needs to function correctly

5. **Commit your changes**
   ```bash
   git commit -m "Add feature: description"
   ```

6. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

7. **Open a Pull Request**
   - Describe what changed and why
   - Link related issues
   - Screenshots for UI changes, please

## Code Standards

### Kotlin Style
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Meaningful names over clever ones
- Small, focused functions
- Data classes for models

### Architecture
- MVVM, kept that way
- Repository pattern for data access
- UI logic stays in ViewModels
- LiveData for reactive updates
- Coroutines for async work

### Accessibility
- High colour contrast (WCAG AA)
- Large touch targets (48dp minimum, and that's a floor, not a target)
- Test with TalkBack actually enabled
- Content descriptions on images
- Support dynamic text sizing

### Documentation
- README.md for new features
- USER_GUIDE.md for anything user-facing
- Inline comments only where the reasoning genuinely isn't obvious
- New import formats need an example in `example_imports/`

## Areas for Contribution

`BACKLOG.md` is the actively-maintained, prioritised queue — that's the
real answer. Short version, highest priority first:

### High Priority
- [ ] Edit existing cards/categories (with an entry point that's hard to
  trigger by accident — see BACKLOG.md item 1 for why that constraint
  matters more than it sounds)
- [ ] Delete UI for cards/categories (repository support already exists)
- [ ] Settings/import lock, caregiver-facing
- [ ] Export functionality

### Medium Priority
- [ ] kapt → KSP migration
- [ ] Automated tests, data layer first
- [ ] Reorder cards
- [ ] Locale-aware seed vocabulary
- [ ] Real screenshots for the README

### Low Priority (P3 in BACKLOG.md)
- [ ] Sentence builder mode (PECS-style, opt-in)
- [ ] Multiple user profiles
- [ ] Home-screen widget
- [ ] Switch-access scanning support

## Flashcard Set Contributions

Card sets are genuinely welcome. To contribute one:

1. Build a well-organised set — see `example_imports/README.md`
2. Include a proper `manifest.json`
3. Use clear images, or the per-card `icon` emoji fallback if you'd
   rather ship a set with no images at all
4. Actually test the import before submitting it
5. Submit via pull request to `example_imports/`

### What makes a good set
- Age-appropriate vocabulary
- Clear, simple images
- Culturally inclusive
- Focused on things people actually need to say
- Sensibly organised categories

## Questions?

Check existing issues first. Then open a new one. Be respectful — this
app exists because communication matters to the people using it, and
that's worth keeping in mind even in a GitHub thread about button
placement.

## Code of Conduct

### Our Pledge
A welcoming, inclusive environment for all contributors, regardless of:
- Age, body size, disability, ethnicity
- Gender identity and expression
- Level of experience
- Nationality, personal appearance, race
- Religion, sexual identity and orientation

### Our Standards
**Positive behaviour includes:**
- Welcoming, inclusive language
- Respecting differing viewpoints
- Accepting constructive criticism gracefully
- Focusing on what's best for the community
- Basic empathy

**Unacceptable behaviour includes:**
- Harassment, trolling, insults
- Personal or political attacks
- Public or private harassment
- Publishing someone else's private information
- Generally being unprofessional about it

### Enforcement
1. Warning
2. Temporary ban
3. Permanent ban

Report issues to the maintainers.

## Recognition

Contributors get credited in:
- The GitHub contributors list
- App credits, for anything significant
- Release notes

## Thank You

Every contribution helps, however small — this genuinely is a tool
people rely on to communicate, so thanks for taking it seriously.

---

**Questions?** Open an issue.
