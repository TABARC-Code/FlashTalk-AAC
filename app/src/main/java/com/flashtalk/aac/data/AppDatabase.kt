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

        // The seed vocabulary lives in assets/vocabulary/communicards_vocabulary_v1.csv,
        // not hardcoded here — 266 cards is too much to hand-maintain as Kotlin
        // literals, and the CSV is the actual source of truth a non-developer
        // could sensibly update. Category icon is the one thing the CSV doesn't
        // carry, so it's assigned here per category_id.
        private const val VOCABULARY_ASSET = "vocabulary/communicards_vocabulary_v1.csv"

        private val CATEGORY_ICONS = mapOf(
            "core_social" to "💬",
            "actions_requests" to "⚡",
            "physical_self_care" to "🧼",
            "health_feelings_emergency" to "🩺",
            "sensory_comfort" to "🧘",
            "objects_leisure" to "🧸",
            "places_time_sequence" to "🗺️"
        )

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
                                    populateDatabase(context, database.categoryDao(), database.flashCardDao())
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDatabase(
            context: Context,
            categoryDao: CategoryDao,
            flashCardDao: FlashCardDao
        ) {
            val rows = context.assets.open(VOCABULARY_ASSET).bufferedReader(Charsets.UTF_8).use { it.readLines() }
            if (rows.isEmpty()) return

            val header = parseCsvLine(rows.first())
            val col = header.withIndex().associate { (i, name) -> name to i }

            // LinkedHashMap so categories seed in first-seen order, which is
            // sort_order order in the CSV — no need to sort separately.
            val categoriesByKey = LinkedHashMap<String, Category>()
            val cardsByCategory = LinkedHashMap<String, MutableList<FlashCard>>()

            rows.drop(1).forEach { line ->
                if (line.isBlank()) return@forEach
                val fields = parseCsvLine(line)
                fun field(name: String) = fields.getOrElse(col.getValue(name)) { "" }

                val categoryKey = field("category_id")
                categoriesByKey.getOrPut(categoryKey) {
                    Category(
                        name = field("category"),
                        icon = CATEGORY_ICONS[categoryKey] ?: "🗂️",
                        color = field("category_hex"),
                        order = categoriesByKey.size
                    )
                }

                val cards = cardsByCategory.getOrPut(categoryKey) { mutableListOf() }
                cards.add(
                    FlashCard(
                        categoryId = 0, // resolved to the real id once the category's inserted, below
                        text = field("label"),
                        speechText = field("speech_text").ifBlank { field("label") },
                        emoji = field("emoji"),
                        priority = field("priority").ifBlank { "standard" },
                        enabled = field("enabled").equals("True", ignoreCase = true),
                        order = field("sort_order").toIntOrNull() ?: cards.size
                    )
                )
            }

            categoriesByKey.forEach { (key, category) ->
                val categoryId = categoryDao.insertCategory(category)
                val cards = cardsByCategory[key].orEmpty().map { it.copy(categoryId = categoryId) }
                flashCardDao.insertCards(cards)
            }
        }

        // Quote-aware CSV split — needed because category names like
        // "Health, Feelings & Emergencies" contain a comma inside quotes.
        // internal, not private: AppDatabaseCsvTest exercises this directly.
        internal fun parseCsvLine(line: String): List<String> {
            val fields = mutableListOf<String>()
            val current = StringBuilder()
            var inQuotes = false
            var i = 0
            while (i < line.length) {
                val c = line[i]
                when {
                    c == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                        current.append('"')
                        i++
                    }
                    c == '"' -> inQuotes = !inQuotes
                    c == ',' && !inQuotes -> {
                        fields.add(current.toString())
                        current.clear()
                    }
                    else -> current.append(c)
                }
                i++
            }
            fields.add(current.toString())
            return fields
        }
    }
}
