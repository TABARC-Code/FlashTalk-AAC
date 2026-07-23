package com.flashtalk.aac.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

@Database(entities = [Category::class, FlashCard::class, Profile::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun flashCardDao(): FlashCardDao
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // The seed vocabulary lives in assets/vocabulary/, not hardcoded here —
        // 266 cards is too much to hand-maintain as Kotlin literals, and the
        // CSV is the actual source of truth a non-developer could sensibly
        // update. Category icon is the one thing the CSV doesn't carry, so
        // it's assigned here per category_id.
        //
        // Locale picks a variant file (communicards_vocabulary_v1_<lang>.csv)
        // if one's bundled, falling back to the English default otherwise —
        // see vocabularyAssetNameFor. Non-English translations are a first
        // pass, not clinically reviewed; see BACKLOG.md item 1.
        private const val VOCABULARY_DIR = "vocabulary"
        private const val DEFAULT_VOCABULARY_ASSET = "communicards_vocabulary_v1.csv"

        private val CATEGORY_ICONS = mapOf(
            "core_social" to "💬",
            "actions_requests" to "⚡",
            "physical_self_care" to "🧼",
            "health_feelings_emergency" to "🩺",
            "sensory_comfort" to "🧘",
            "objects_leisure" to "🧸",
            "places_time_sequence" to "🗺️",
            "communication_support" to "🗣️",
            "about_me" to "🧩"
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
                                    populateDatabase(
                                        context,
                                        database.categoryDao(),
                                        database.flashCardDao(),
                                        database.profileDao()
                                    )
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
            flashCardDao: FlashCardDao,
            profileDao: ProfileDao
        ) {
            // One profile always exists from first run — Multiple profiles
            // (BACKLOG P3 item 5) lets caregivers add more later, but the app
            // must never open onto zero profiles. Seed categories default to
            // profileId = 0L (shared), so they need no profile id at all.
            profileDao.insertProfile(Profile(name = "Default"))

            val availableAssets = context.assets.list(VOCABULARY_DIR)?.toSet() ?: emptySet()
            val assetName = vocabularyAssetNameFor(Locale.getDefault().language, availableAssets)
            val rows = context.assets.open("$VOCABULARY_DIR/$assetName")
                .bufferedReader(Charsets.UTF_8).use { it.readLines() }
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

        // Pure — no Context/asset access — so AppDatabaseCsvTest can check
        // the selection logic without needing Robolectric. `languageTag` is
        // Locale.getDefault().language (e.g. "fr", "en"); `availableAssets`
        // is whatever's actually bundled, from context.assets.list(...).
        internal fun vocabularyAssetNameFor(languageTag: String, availableAssets: Set<String>): String {
            val localised = "communicards_vocabulary_v1_${languageTag.lowercase()}.csv"
            return if (localised in availableAssets) localised else DEFAULT_VOCABULARY_ASSET
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
