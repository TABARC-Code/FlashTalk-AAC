package com.flashtalk.aac.utils

import com.flashtalk.aac.data.FlashCard
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Covers the pure sentence-building behind sentence strip mode
 * (BACKLOG.md P3 item 4): display uses each card's on-screen label,
 * speech uses speechText — same label/speech split CategoryActivity's
 * ordinary tap-to-speak path already relies on.
 */
class SentenceStripTest {

    private fun card(text: String, speechText: String = text) =
        FlashCard(categoryId = 1, text = text, speechText = speechText)

    @Test
    fun `empty strip produces an empty string`() {
        assertEquals("", SentenceStrip.displayText(emptyList()))
        assertEquals("", SentenceStrip.speechText(emptyList()))
    }

    @Test
    fun `single card produces its own text`() {
        val cards = listOf(card("Want"))
        assertEquals("Want", SentenceStrip.displayText(cards))
        assertEquals("Want", SentenceStrip.speechText(cards))
    }

    @Test
    fun `multiple cards join in tap order with a single space`() {
        val cards = listOf(card("I"), card("Want"), card("More"))
        assertEquals("I Want More", SentenceStrip.displayText(cards))
        assertEquals("I Want More", SentenceStrip.speechText(cards))
    }

    @Test
    fun `display and speech text can differ per card`() {
        val cards = listOf(card("Bathroom / Toilet", speechText = "Bathroom"), card("Please"))
        assertEquals("Bathroom / Toilet Please", SentenceStrip.displayText(cards))
        assertEquals("Bathroom Please", SentenceStrip.speechText(cards))
    }
}
