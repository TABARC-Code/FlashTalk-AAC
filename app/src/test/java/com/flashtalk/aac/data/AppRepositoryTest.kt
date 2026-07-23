package com.flashtalk.aac.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * Repository CRUD against a real (in-memory) Room database — Robolectric,
 * not a real device, since there's no emulator in this environment. Built
 * via Room.inMemoryDatabaseBuilder directly rather than
 * AppDatabase.getDatabase(), which deliberately skips the CSV-seeding
 * callback: these tests want an empty database they control, not the
 * production seed data (BACKLOG.md P2 item 6).
 */
@RunWith(RobolectricTestRunner::class)
class AppRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: AppRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AppRepository(db.categoryDao(), db.flashCardDao(), db.profileDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `inserting a category makes it fetchable by id`() = runTest {
        val id = repository.insertCategory(Category(name = "Test", icon = "🎯", color = "#FFFFFF"))

        assertTrue(id > 0)
        assertEquals("Test", repository.getCategoryById(id)?.name)
    }

    @Test
    fun `inserting cards makes them fetchable by category`() = runTest {
        val categoryId = repository.insertCategory(Category(name = "Test", icon = "🎯", color = "#FFFFFF"))
        repository.insertCard(FlashCard(categoryId = categoryId, text = "Hello", emoji = "👋"))
        repository.insertCard(FlashCard(categoryId = categoryId, text = "Bye", emoji = "👋"))

        assertEquals(2, repository.getCardsByCategorySync(categoryId).size)
    }

    @Test
    fun `deleting a category cascades to its cards`() = runTest {
        val categoryId = repository.insertCategory(Category(name = "Test", icon = "🎯", color = "#FFFFFF"))
        repository.insertCard(FlashCard(categoryId = categoryId, text = "Hello", emoji = "👋"))
        val category = repository.getCategoryById(categoryId)!!

        repository.deleteCategory(category)

        assertNull(repository.getCategoryById(categoryId))
        assertTrue(repository.getCardsByCategorySync(categoryId).isEmpty())
    }

    @Test
    fun `deleting a custom card removes its image file`() = runTest {
        val categoryId = repository.insertCategory(Category(name = "Test", icon = "🎯", color = "#FFFFFF"))
        val imageFile = File.createTempFile("card", ".jpg")
        val cardId = repository.insertCard(
            FlashCard(categoryId = categoryId, text = "Photo", imagePath = imageFile.absolutePath, isCustom = true)
        )
        assertTrue(imageFile.exists())

        repository.deleteCard(repository.getCardById(cardId)!!)

        assertFalse(imageFile.exists())
    }

    @Test
    fun `deleting a category removes every custom card's image file`() = runTest {
        val categoryId = repository.insertCategory(Category(name = "Test", icon = "🎯", color = "#FFFFFF"))
        val imageFile = File.createTempFile("card", ".jpg")
        repository.insertCard(
            FlashCard(categoryId = categoryId, text = "Photo", imagePath = imageFile.absolutePath, isCustom = true)
        )
        val category = repository.getCategoryById(categoryId)!!

        repository.deleteCategory(category)

        assertFalse(imageFile.exists())
    }

    @Test
    fun `updating a category persists the change`() = runTest {
        val categoryId = repository.insertCategory(Category(name = "Old", icon = "🎯", color = "#FFFFFF"))
        val category = repository.getCategoryById(categoryId)!!

        repository.updateCategory(category.copy(name = "New"))

        assertEquals("New", repository.getCategoryById(categoryId)?.name)
    }

    @Test
    fun `a profile's category list includes shared vocabulary and its own categories, not another profile's`() = runTest {
        val profileA = repository.insertProfile(Profile(name = "A"))
        val profileB = repository.insertProfile(Profile(name = "B"))
        repository.insertCategory(Category(name = "Shared", icon = "🌍", color = "#FFFFFF")) // profileId defaults to 0L
        repository.insertCategory(Category(name = "A's own", icon = "🅰️", color = "#FFFFFF", profileId = profileA))
        repository.insertCategory(Category(name = "B's own", icon = "🅱️", color = "#FFFFFF", profileId = profileB))

        val forA = repository.getCategoriesByProfileSync(profileA)
        val forB = repository.getCategoriesByProfileSync(profileB)

        assertEquals(1, forA.size)
        assertEquals("A's own", forA[0].name)
        assertEquals(1, forB.size)
        assertEquals("B's own", forB[0].name)
    }

    @Test
    fun `deleting a profile removes only its own categories, not shared vocabulary or another profile's`() = runTest {
        val profileA = repository.insertProfile(Profile(name = "A"))
        val sharedId = repository.insertCategory(Category(name = "Shared", icon = "🌍", color = "#FFFFFF"))
        val ownId = repository.insertCategory(Category(name = "A's own", icon = "🅰️", color = "#FFFFFF", profileId = profileA))

        repository.deleteProfile(repository.getAllProfilesSync().first { it.id == profileA })

        assertNull(repository.getCategoryById(ownId))
        assertEquals("Shared", repository.getCategoryById(sharedId)?.name)
    }

    @Test
    fun `shared categories are only the ones with the default profileId`() = runTest {
        val profileA = repository.insertProfile(Profile(name = "A"))
        repository.insertCategory(Category(name = "Shared", icon = "🌍", color = "#FFFFFF"))
        repository.insertCategory(Category(name = "A's own", icon = "🅰️", color = "#FFFFFF", profileId = profileA))

        val shared = repository.getSharedCategoriesSync()

        assertEquals(1, shared.size)
        assertEquals("Shared", shared[0].name)
    }

    @Test
    fun `getEnabledCardsByCategorySync excludes disabled cards`() = runTest {
        val categoryId = repository.insertCategory(Category(name = "Test", icon = "🎯", color = "#FFFFFF"))
        repository.insertCard(FlashCard(categoryId = categoryId, text = "On", emoji = "✅", enabled = true))
        repository.insertCard(FlashCard(categoryId = categoryId, text = "Off", emoji = "❌", enabled = false))

        val enabled = repository.getEnabledCardsByCategorySync(categoryId)

        assertEquals(1, enabled.size)
        assertEquals("On", enabled[0].text)
    }

    @Test
    fun `deleting a profile cleans up its owned categories' custom card images`() = runTest {
        val profileA = repository.insertProfile(Profile(name = "A"))
        val categoryId = repository.insertCategory(Category(name = "A's own", icon = "🅰️", color = "#FFFFFF", profileId = profileA))
        val imageFile = File.createTempFile("card", ".jpg")
        repository.insertCard(
            FlashCard(categoryId = categoryId, text = "Photo", imagePath = imageFile.absolutePath, isCustom = true)
        )

        repository.deleteProfile(repository.getAllProfilesSync().first { it.id == profileA })

        assertFalse(imageFile.exists())
    }
}
