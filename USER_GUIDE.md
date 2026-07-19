# FlashTalk AAC User Guide

## Table of Contents
1. [Getting Started](#getting-started)
2. [Daily Use](#daily-use)
3. [Customisation](#customisation)
4. [Importing Sets](#importing-sets)
5. [Settings](#settings)
6. [Tips for Caregivers](#tips-for-caregivers)
7. [Troubleshooting](#troubleshooting)

---

## Getting Started

### First Launch

When you first open FlashTalk AAC, you'll see:
- **8 colorful category cards** on the main screen
- Each category has an emoji icon and name
- A **+ button** in the bottom-right corner

### Understanding Categories

Categories organise flashcards by topic:
- **Food & Drink** 🍎: Hunger, thirst, meals
- **Feelings** 😊: Emotions and states
- **Activities** ⚽: Things to do
- **People** 👨‍👩‍👧: Family and relationships
- **Places** 🏠: Locations
- **Needs** 🆘: Urgent communication
- **Yes/No** ✅: Agreement and thanks
- **Time** 🕐: When things happen

---

## Daily Use

### Communicating with Flashcards

**Step 1**: Tap a category (e.g., "Food & Drink")

**Step 2**: Browse the flashcards
- Each card shows an emoji or photo, and text
- Cards are arranged in a grid

**Step 3**: Tap a card to speak
- The text is spoken aloud immediately
- A small popup shows the text
- The card animates when you tap it

**Example Communication Flow**:
1. User taps "Food & Drink"
2. User taps "Water"
3. App speaks "Water"
4. Caregiver provides water

### Quick Tips
- **Single tap** = Speak immediately
- **No need to build sentences** - just tap what you need
- **Visual feedback** confirms your tap
- **Return home** with the back button or arrow

---

## Customisation

### Adding a Custom Category

**Why**: Create categories for personal interests (e.g., "Toys", "Video Games", "Pets")

**How**:
1. From the main screen, tap the **+ button**
2. You'll see "New Category" at the top
3. Enter a category name (e.g., "My Toys")
4. (Optional) Enter an emoji as the category icon
5. Tap **Save**
6. Your new category appears on the main screen

### Adding Custom Cards to a Category

**Why**: Add personal items, favourite foods, specific people, or special requests

**How**:
1. Open any category
2. Tap the **+ button** in the bottom-right
3. Tap **"Select Image"**
4. Choose a photo from your gallery — the system photo/document picker
   opens; no in-app storage permission prompt is needed
   - Pick a clear, high-contrast image
5. Enter the text (what should be spoken)
6. Tap **Save**
7. Your new card appears in the category

### Best Practices for Custom Cards

**Photos**:
✅ Use clear, well-lit photos
✅ Focus on the main subject
✅ High contrast works best
❌ Avoid dark or blurry images
❌ Avoid busy backgrounds

**Text**:
✅ Keep it short and clear
✅ Use common words
✅ Be consistent (e.g., always "bathroom", not mixing with "toilet")
❌ Avoid long sentences
❌ Avoid ambiguous words

---

## Importing Sets

### When to Import

Import flashcard sets when you want to:
- Add many cards at once (faster than one-by-one)
- Use sets created by therapists or educators
- Share sets between family members
- Backup and restore your cards

### Import Methods

#### Method 1: JSON File (No Images)
**Best for**: Quick setup, text-only needs — cards fall back to an emoji

1. Get or create a `.json` file (see example_imports folder)
2. Transfer file to your Android device
3. In FlashTalk AAC: Menu (⋮) → **Import Set**
4. Tap **"Import JSON File"**
5. Navigate to and select your .json file
6. Wait for the import summary to appear
7. New category appears on main screen

#### Method 2: ZIP File (With Images)
**Best for**: Complete sets with custom images

1. Create or download a `.zip` file containing:
   - `manifest.json` (card definitions)
   - Image files (referenced in manifest)
2. Transfer ZIP to your Android device
3. In FlashTalk AAC: Menu (⋮) → **Import Set**
4. Tap **"Import ZIP File"**
5. Navigate to and select your .zip file
6. Wait for import to complete
7. New category appears with all cards and images (any card whose image
   filename didn't match a file in the ZIP falls back to an emoji, and is
   listed in the import summary rather than failing silently)

### Creating Your Own Import Files

See `example_imports/README.md` for detailed instructions and examples.

**Quick Template**:
```json
{
  "set_name": "My Set Name",
  "category_name": "Category Display Name",
  "category_icon": "🎯",
  "category_color": "#FF6B6B",
  "cards": [
    {
      "text": "First Card",
      "image_filename": "first.jpg"
    },
    {
      "text": "Second Card",
      "icon": "🎨"
    }
  ]
}
```

---

## Settings

Access settings via: Main screen → Menu (⋮) → **Settings**

### Speech Rate
**What it does**: Controls how fast the speech is

**Adjust**:
- Drag the slider left = slower
- Drag the slider right = faster
- Range: 0.5x to 2.0x
- Default: 0.9x

**When to adjust**:
- Too fast to understand → Slow down
- Feels sluggish → Speed up
- Processing time needed → Slow down

### Voice Pitch
**What it does**: Controls the voice tone

**Adjust**:
- Drag left = lower pitch
- Drag right = higher pitch
- Range: 0.5x to 2.0x
- Default: 1.0x

**When to adjust**:
- Voice sounds too robotic → Adjust slightly
- Preference for different tone → Customise
- Matching user's natural voice → Adjust accordingly

### Dark Mode
**What it does**: Changes app colors to darker theme

**Benefits**:
- Easier on eyes in low light
- Reduces screen glare
- Some users find it calming
- Better for photosensitivity

**Toggle**: Tap the switch on/off — takes effect immediately

### Large Text
**What it does**: Increases text size throughout app

**Benefits**:
- Easier to read
- Better for visual impairments
- Clearer from a distance

**Toggle**: Tap the switch on/off

---

## Tips for Caregivers

### Initial Setup

**Week 1: Learn the Basics**
- Explore all pre-loaded categories
- Practice tapping cards together
- Explain that tapping = speaking
- Respond immediately to requests

**Week 2: Personalise**
- Add 2-3 favourite foods
- Add photos of family members
- Create one custom category
- Add 5-10 most-used items

**Week 3+: Expand**
- Add more personal items
- Import additional sets
- Adjust settings to preference
- Establish communication routines

### Encouraging Use

**Do's**:
✅ Respond immediately when device is used
✅ Keep device accessible and charged
✅ Model using the app yourself
✅ Celebrate all communication attempts
✅ Add items the user requests
✅ Keep frequently-used cards easy to access

**Don'ts**:
❌ Force use - offer as an option
❌ Correct "wrong" choices - validate all communication
❌ Rush the user
❌ Use as reward/punishment
❌ Limit to only "needs" - include fun items
❌ Let the device battery die

### Building Communication Skills

**Start Simple**:
1. Begin with high-motivation items (favourite snacks)
2. Use 1-step communication (tap = get item)
3. Respond immediately and consistently

**Gradually Expand**:
1. Add more categories
2. Introduce choices (this snack or that snack?)
3. Practice in different settings
4. Extend to social communication (greetings, feelings)

**Maintenance**:
1. Regular review of cards (remove unused, add needed)
2. Update photos as interests change
3. Keep device charged and accessible
4. Involve therapists/educators in setup

### Working with Professionals

**Speech Therapist**:
- Share the app in sessions
- Ask for recommended card sets
- Get guidance on vocabulary selection
- Coordinate home and therapy use

**Teachers**:
- Create school-specific category
- Add classroom vocabulary
- Align with IEP goals
- Train classroom staff

**Family Members**:
- Train all caregivers on use
- Share custom categories
- Import sets between devices (export is on the roadmap — see BACKLOG.md)
- Maintain consistency

---

## Troubleshooting

### Sound Issues

**Problem**: No sound when tapping cards

**Solutions**:
1. Check device volume (turn up media volume)
2. Ensure device is not in silent mode
3. Verify text-to-speech is installed:
   - Settings → Accessibility → Text-to-Speech
   - Install "Google Text-to-Speech" if needed
4. Close and reopen the app
5. Restart your device

**Problem**: Voice sounds robotic or unclear

**Solutions**:
1. Install better TTS engine (Google Text-to-Speech recommended)
2. Adjust speech rate (Settings → Speech Rate)
3. Adjust pitch (Settings → Voice Pitch)
4. Try different TTS engine in Android settings

### Image Issues

**Problem**: Images not loading for custom cards

**Solutions**:
1. Re-select the photo — the app doesn't request a storage/media
   permission (it uses the system picker), so this is almost always an
   unsupported or corrupted file rather than a permission problem
2. Use smaller image files (under 5MB)
3. Try a different image format (.jpg recommended)

**Problem**: Import shows a card with an emoji instead of the expected photo

**Solutions**:
1. Check the import summary — it lists exactly which `image_filename`
   values didn't match a file in the ZIP
2. Verify images are included in the ZIP file
3. Check image filenames match manifest.json exactly (case-sensitive)
4. Ensure images are in same folder as manifest.json (not a subfolder)
5. Re-import with corrected file

### Import Issues

**Problem**: Import fails with error message

**Solutions**:
1. Validate JSON syntax at jsonlint.com
2. Check manifest.json is properly formatted
3. Ensure all image_filenames are correct
4. Verify ZIP contains manifest.json
5. Try smaller set first (fewer cards)
6. Check file size (under 50MB — the import will refuse anything larger)

### Performance Issues

**Problem**: App is slow or laggy

**Solutions**:
1. Clear app cache:
   - Settings → Apps → FlashTalk AAC → Storage → Clear Cache
2. Reduce number of cards (move some to separate categories)
3. Use smaller image files
4. Close other apps
5. Restart device
6. Ensure device has sufficient storage

### General Issues

**Problem**: App crashes or freezes

**Solutions**:
1. Force stop app:
   - Settings → Apps → FlashTalk AAC → Force Stop
2. Clear cache (not data!)
3. Reinstall app (will lose custom content — there's no export yet, so
   note down anything irreplaceable first)
4. Update Android OS
5. Report issue with details

**Problem**: Lost custom cards after update

**Prevention**:
1. There's no export/backup yet (see BACKLOG.md) — for anything
   irreplaceable, keep a copy of the original photos separately
2. Note: export is planned; see CHANGELOG.md [Unreleased]

---

## Quick Reference Guide

### Common Actions

| I want to... | How to do it |
|-------------|-------------|
| Communicate | Tap category → Tap card |
| Add a card | Open category → Tap + → Select image → Enter text → Save |
| Add category | Main screen → Tap + → Enter name → Save |
| Import set | Menu → Import Set → Choose file |
| Change speech speed | Menu → Settings → Adjust Speech Rate |
| Enable dark mode | Menu → Settings → Toggle Dark Mode |
| Make text bigger | Menu → Settings → Toggle Large Text |

### Keyboard Shortcuts

Currently, the app is touch-optimized. Keyboard shortcuts may be added in future versions.

### Accessibility Features

- ✅ Large touch targets
- ✅ High contrast design
- ✅ Text-to-speech
- ✅ Dark mode
- ✅ Adjustable text size
- ✅ Simple navigation
- ✅ TalkBack-correct tap feedback
- 🔜 Switch access (future)
- 🔜 Voice control (future)

---

## Contact & Support

For additional help:
- Check the main README.md
- Review example_imports for import guidance
- Consult with speech therapist
- Share feedback via app store review

---

**Remember**: Communication is a right, not a privilege. This app is a tool to support autonomy, choice, and self-expression. Every tap is communication. Every communication attempt is valuable.

Happy communicating! 💙
