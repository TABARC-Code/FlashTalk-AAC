package com.flashtalk.aac.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class FlashCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long,
    val text: String, // Displayed label — may include an alt term, e.g. "Bathroom / Toilet"
    val speechText: String = text, // What's actually spoken; usually shorter than the label
    val emoji: String = "", // Seed-card placeholder glyph (BACKLOG.md item 1, option c) — no drawable licence needed
    val imagePath: String = "", // Absolute file path in filesDir/custom_images; set only for custom photo cards
    val isCustom: Boolean = false,
    val priority: String = "standard", // "standard" | "urgent" — urgent cards get a visual accent, see FlashCardAdapter
    val enabled: Boolean = true,
    val order: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
