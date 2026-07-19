package com.flashtalk.aac.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String, // Emoji, rendered directly — no drawable asset required
    val color: String, // Hex color code
    val isCustom: Boolean = false,
    val order: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
