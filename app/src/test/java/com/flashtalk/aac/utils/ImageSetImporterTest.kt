package com.flashtalk.aac.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers ImageSetImporter.parseManifest and findMissingImageWarnings — both
 * pure and Context-free, so no need for Robolectric here. The actual
 * file/zip I/O in importFromJson/importFromZip is straightforward enough
 * not to need its own test; this is the part with real edge cases
 * (BACKLOG.md P2 item 6).
 */
class ImageSetImporterTest {

    @Test
    fun `valid manifest parses with all fields`() {
        val json = """
            {
              "set_name": "Animals",
              "category_name": "Animals",
              "category_icon": "🐶",
              "category_color": "#FF9F43",
              "cards": [
                { "text": "Dog", "icon": "🐶" },
                { "text": "Cat", "image_filename": "cat.jpg" }
              ]
            }
        """.trimIndent()

        val manifest = ImageSetImporter.parseManifest(json)

        assertNotNull(manifest)
        assertEquals("Animals", manifest!!.categoryName)
        assertEquals(2, manifest.cards.size)
        assertEquals("cat.jpg", manifest.cards[1].imageFilename)
    }

    @Test
    fun `manifest with only required fields uses defaults`() {
        val json = """{ "category_name": "Colours", "cards": [{ "text": "Red" }] }"""

        val manifest = ImageSetImporter.parseManifest(json)

        assertNotNull(manifest)
        assertEquals("📦", manifest!!.categoryIcon)
        assertEquals("#95E1D3", manifest.categoryColor)
    }

    @Test
    fun `malformed JSON returns null rather than throwing`() {
        val manifest = ImageSetImporter.parseManifest("{ not valid json at all")
        assertNull(manifest)
    }

    @Test
    fun `manifest missing category_name returns null`() {
        val json = """{ "cards": [{ "text": "Dog" }] }"""
        assertNull(ImageSetImporter.parseManifest(json))
    }

    @Test
    fun `manifest missing cards field returns null`() {
        val json = """{ "category_name": "Animals" }"""
        assertNull(ImageSetImporter.parseManifest(json))
    }

    @Test
    fun `missing image produces one warning naming the card and filename`() {
        val json = """
            {
              "category_name": "Animals",
              "cards": [
                { "text": "Dog", "image_filename": "dog.jpg" },
                { "text": "Cat", "image_filename": "cat.jpg" }
              ]
            }
        """.trimIndent()
        val manifest = ImageSetImporter.parseManifest(json)!!

        val warnings = ImageSetImporter.findMissingImageWarnings(manifest, availableFilenames = setOf("dog.jpg"))

        assertEquals(1, warnings.size)
        assertTrue(warnings[0].contains("Cat"))
        assertTrue(warnings[0].contains("cat.jpg"))
    }

    @Test
    fun `emoji-only cards with no image_filename never produce a warning`() {
        val json = """{ "category_name": "Animals", "cards": [{ "text": "Dog", "icon": "🐶" }] }"""
        val manifest = ImageSetImporter.parseManifest(json)!!

        val warnings = ImageSetImporter.findMissingImageWarnings(manifest, availableFilenames = emptySet())

        assertEquals(emptyList<String>(), warnings)
    }
}
