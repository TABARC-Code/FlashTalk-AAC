# Contributing to FlashTalk AAC

Thank you for your interest in contributing to FlashTalk AAC! This project aims to provide free, accessible communication tools for autistic individuals and others with communication needs.

## How to Contribute

### Reporting Bugs

If you find a bug, please create an issue with:
- Clear description of the bug
- Steps to reproduce
- Expected vs actual behavior
- Android version and device info
- Screenshots if applicable

### Suggesting Features

We welcome feature suggestions! Please create an issue with:
- Clear description of the feature
- Use case / why it's needed
- How it would work
- Any examples from other apps

### Code Contributions

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes**
   - Follow Kotlin coding conventions
   - Maintain MVVM architecture
   - Add comments for complex logic
   - Update documentation as needed

4. **Test your changes**
   - Test on multiple Android versions if possible
   - Ensure accessibility features still work
   - Check that TTS functions correctly

5. **Commit your changes**
   ```bash
   git commit -m "Add feature: description"
   ```

6. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

7. **Create a Pull Request**
   - Describe your changes
   - Reference any related issues
   - Include screenshots if UI changes

## Code Standards

### Kotlin Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Keep functions small and focused
- Use data classes for models

### Architecture
- Maintain MVVM pattern
- Use Repository pattern for data access
- Keep UI logic in ViewModels
- Use LiveData for reactive updates
- Handle async with Coroutines

### Accessibility
- Ensure high color contrast (WCAG AA)
- Maintain large touch targets (48dp minimum)
- Test with TalkBack enabled
- Provide content descriptions for images
- Support dynamic text sizing

### Documentation
- Update README.md for new features
- Update USER_GUIDE.md for user-facing changes
- Add inline comments for complex code
- Create example imports for new import formats

## Areas for Contribution

See `BACKLOG.md` for the prioritised, actively-maintained work queue.
The short version, highest priority first:

### High Priority
- [ ] Edit existing cards/categories (with a hard-to-trigger-by-accident entry point)
- [ ] Delete UI for cards/categories (repository support already exists)
- [ ] Settings/import lock (caregiver-facing)
- [ ] Export functionality (backup categories)

### Medium Priority
- [ ] kapt → KSP migration
- [ ] Automated tests (data layer first)
- [ ] Reorder cards
- [ ] Locale-aware seed vocabulary
- [ ] Real screenshots for the README

### Low Priority (P3 in BACKLOG.md)
- [ ] Sentence builder mode (PECS-style, opt-in)
- [ ] Multiple user profiles
- [ ] Home-screen widget
- [ ] Switch-access scanning support

## Flashcard Set Contributions

We welcome contributions of flashcard sets! To contribute:

1. Create a well-organized set (see `example_imports/README.md`)
2. Include manifest.json with metadata
3. Use clear, high-quality images (or the per-card `icon` emoji fallback
   for a JSON-only set that needs no images at all)
4. Test the import process
5. Submit via pull request to `example_imports/`

### Good Flashcard Sets
- Age-appropriate vocabulary
- Clear, simple images
- Culturally inclusive
- Focused on common needs
- Well-organized categories

## Questions?

- Check existing issues first
- Create a new issue for questions
- Be respectful and constructive
- Remember this app serves a vital purpose

## Code of Conduct

### Our Pledge
We are committed to providing a welcoming and inclusive environment for all contributors, regardless of:
- Age, body size, disability, ethnicity
- Gender identity and expression
- Level of experience
- Nationality, personal appearance, race
- Religion, sexual identity and orientation

### Our Standards
**Positive behavior includes:**
- Using welcoming and inclusive language
- Being respectful of differing viewpoints
- Accepting constructive criticism gracefully
- Focusing on what's best for the community
- Showing empathy towards others

**Unacceptable behavior includes:**
- Harassment, trolling, or insulting comments
- Personal or political attacks
- Public or private harassment
- Publishing others' private information
- Unprofessional conduct

### Enforcement
Violations may result in:
1. Warning
2. Temporary ban
3. Permanent ban

Report issues to project maintainers.

## Recognition

Contributors will be recognized in:
- GitHub contributors list
- App credits (for significant contributions)
- Release notes

## Thank You!

Every contribution, no matter how small, helps make communication more accessible. Thank you for being part of this project!

---

**Questions?** Open an issue or contact the maintainers.
