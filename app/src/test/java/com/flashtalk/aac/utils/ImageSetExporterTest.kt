package com.flashtalk.aac.utils

import com.flashtalk.aac.data.Category
import com.flashtalk.aac.data.FlashCard
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Covers ImageSetExporter.buildManifest and the JSON round-trip through it —
 * export -> import must read back exactly what was written, since that's
 * the whole point of matching ImageSetImporter's own manifest format
 * (BACKLOG.md P1 item 4). Doesn't touch file I/O (no Context needed): that
 * part is a straightforward zip write, this is the part worth pinning down.
 */
class ImageSetExporterTest {

    @Test
    fun `emoji-only card maps to icon, not image_filename`() {
        val category = Category(id = 1, name = "Animals", icon = "🐾", color = "#FF9F43")
        val cards = listOf(
            FlashCard(id = 10, categoryId = 1, text = "Dog", emoji = "🐶", isCustom = false)
        )

        val manifest = ImageSetExporter.buildManifest(category, cards)

        assertEquals("Animals", manifest.categoryName)
        assertEquals("🐾", manifest.categoryIcon)
        assertEquals(1, manifest.cards.size)
        assertEquals("🐶", manifest.cards[0].icon)
        assertEquals("", manifest.cards[0].imageFilename)
    }

    @Test
    fun `custom photo card maps to image_filename, not icon`() {
        val category = Category(id = 2, name = "Family", icon = "👨‍👩‍👧", color = "#FFD93D")
        val cards = listOf(
            FlashCard(
                id = 42,
                categoryId = 2,
                text = "Mum",
                imagePath = "/data/user/0/com.flashtalk.aac/files/custom_images/mum.jpg",
                isCustom = true
            )
        )

        val manifest = ImageSetExporter.buildManifest(category, cards)

        assertEquals("card_42.jpg", manifest.cards[0].imageFilename)
        assertEquals("", manifest.cards[0].icon)
    }

    @Test
    fun `manifest survives a JSON round-trip unchanged`() {
        val category = Category(id = 3, name = "Colours", icon = "🎨", color = "#845EC2")
        val cards = listOf(
            FlashCard(id = 1, categoryId = 3, text = "Red", emoji = "🔴", isCustom = false),
            FlashCard(
                id = 2,
                categoryId = 3,
                text = "Blue",
                imagePath = "/tmp/blue.png",
                isCustom = true
            )
        )

        val manifest = ImageSetExporter.buildManifest(category, cards)
        val json = Gson().toJson(manifest)
        val roundTripped = Gson().fromJson(json, ImageSetImporter.ImportManifest::class.java)

        assertEquals(manifest, roundTripped)
    }
}
