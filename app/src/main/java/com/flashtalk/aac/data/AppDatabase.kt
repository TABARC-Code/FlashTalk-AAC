package com.flashtalk.aac.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Category::class, FlashCard::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun flashCardDao(): FlashCardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flashtalk_database"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    populateDatabase(database.categoryDao(), database.flashCardDao())
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Seed cards use an emoji glyph instead of a bundled drawable: BACKLOG.md
        // item 1 was resolved by picking option (c) rather than committing to an
        // external symbol-set licence. Custom cards (user photos) are unaffected —
        // they still load from filesDir/custom_images via FlashCardAdapter.
        private suspend fun populateDatabase(categoryDao: CategoryDao, flashCardDao: FlashCardDao) {
            val categoryCards = linkedMapOf(
                Category(name = "Food & Drink", icon = "🍎", color = "#FF6B6B", order = 0) to listOf(
                    "Water" to "💧",
                    "Juice" to "🧃",
                    "Milk" to "🥛",
                    "Hungry" to "🍽️",
                    "Thirsty" to "🥤",
                    "Snack" to "🍪",
                    "Breakfast" to "🍳",
                    "Lunch" to "🥪",
                    "Dinner" to "🍝"
                ),
                Category(name = "Feelings", icon = "😊", color = "#4ECDC4", order = 1) to listOf(
                    "Happy" to "😀",
                    "Sad" to "😢",
                    "Angry" to "😠",
                    "Scared" to "😨",
                    "Excited" to "🤩",
                    "Tired" to "😴",
                    "Confused" to "😕",
                    "Love" to "❤️"
                ),
                Category(name = "Activities", icon = "⚽", color = "#95E1D3", order = 2) to listOf(
                    "Play" to "🧩",
                    "Read" to "📖",
                    "Watch TV" to "📺",
                    "Music" to "🎵",
                    "Outside" to "🌳",
                    "Walk" to "🚶",
                    "Bath" to "🛁",
                    "Sleep" to "🛌"
                ),
                Category(name = "People", icon = "👨‍👩‍👧", color = "#FFD93D", order = 3) to listOf(
                    "Mom" to "👩",
                    "Dad" to "👨",
                    "Brother" to "👦",
                    "Sister" to "👧",
                    "Friend" to "🧑‍🤝‍🧑",
                    "Teacher" to "🧑‍🏫",
                    "Doctor" to "🩺"
                ),
                Category(name = "Places", icon = "🏠", color = "#A8E6CF", order = 4) to listOf(
                    "Home" to "🏠",
                    "School" to "🏫",
                    "Park" to "🛝",
                    "Store" to "🏬",
                    "Bathroom" to "🚻",
                    "Bedroom" to "🛏️",
                    "Car" to "🚗"
                ),
                Category(name = "Needs", icon = "🆘", color = "#FF8B94", order = 5) to listOf(
                    "Help" to "🆘",
                    "Stop" to "✋",
                    "Wait" to "⏳",
                    "More" to "➕",
                    "All done" to "🏁",
                    "Hurt" to "🤕",
                    "Bathroom" to "🚻"
                ),
                Category(name = "Yes/No", icon = "✅", color = "#B4A7D6", order = 6) to listOf(
                    "Yes" to "✅",
                    "No" to "❌",
                    "Please" to "🙏",
                    "Thank you" to "💛"
                ),
                Category(name = "Time", icon = "🕐", color = "#F8B195", order = 7) to listOf(
                    "Now" to "⏰",
                    "Later" to "⏭️",
                    "Today" to "📅",
                    "Tomorrow" to "🌅",
                    "Morning" to "🌄",
                    "Night" to "🌙"
                )
            )

            categoryCards.forEach { (category, cards) ->
                val categoryId = categoryDao.insertCategory(category)
                val flashCards = cards.mapIndexed { index, (text, emoji) ->
                    FlashCard(categoryId = categoryId, text = text, emoji = emoji, order = index)
                }
                flashCardDao.insertCards(flashCards)
            }
        }
    }
}
