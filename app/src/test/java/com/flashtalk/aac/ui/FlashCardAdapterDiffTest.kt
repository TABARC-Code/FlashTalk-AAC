package com.flashtalk.aac.ui

import com.flashtalk.aac.data.FlashCard
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FlashCardAdapterDiffTest {

    private val callback = FlashCardAdapter.FlashCardDiffCallback()
    private val card = FlashCard(id = 1, categoryId = 1, text = "Dog", emoji = "🐶")

    @Test
    fun `same id is the same item even if the text changes`() {
        assertTrue(callback.areItemsTheSame(card, card.copy(text = "Doggo")))
    }

    @Test
    fun `different id is a different item`() {
        assertFalse(callback.areItemsTheSame(card, card.copy(id = 2)))
    }

    @Test
    fun `identical cards have the same contents`() {
        assertTrue(callback.areContentsTheSame(card, card.copy()))
    }

    @Test
    fun `changing priority to urgent is a content change`() {
        assertFalse(callback.areContentsTheSame(card, card.copy(priority = "urgent")))
    }

    @Test
    fun `changing enabled is a content change`() {
        assertFalse(callback.areContentsTheSame(card, card.copy(enabled = false)))
    }
}
