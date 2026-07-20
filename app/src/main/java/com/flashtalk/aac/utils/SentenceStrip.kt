package com.flashtalk.aac.utils

import com.flashtalk.aac.data.FlashCard

/**
 * Pure sentence-building for sentence strip mode (BACKLOG P3 item 4): cards
 * tapped in strip mode accumulate here instead of speaking immediately.
 * Display uses each card's on-screen label; speech uses speechText, same
 * label/speech split CategoryActivity.speakCard already relies on.
 */
object SentenceStrip {
    fun displayText(cards: List<FlashCard>): String = cards.joinToString(" ") { it.text }
    fun speechText(cards: List<FlashCard>): String = cards.joinToString(" ") { it.speechText }
}
