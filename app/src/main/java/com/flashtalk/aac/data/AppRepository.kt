package com.flashtalk.aac.data

import androidx.lifecycle.LiveData
import java.io.File

class AppRepository(
    private val categoryDao: CategoryDao,
    private val flashCardDao: FlashCardDao,
    private val profileDao: ProfileDao
) {

    // Profiles
    val allProfiles: LiveData<List<Profile>> = profileDao.getAllProfiles()

    suspend fun getAllProfilesSync(): List<Profile> = profileDao.getAllProfilesSync()

    suspend fun insertProfile(profile: Profile): Long = profileDao.insertProfile(profile)

    suspend fun updateProfile(profile: Profile) = profileDao.updateProfile(profile)

    suspend fun getCategoriesByProfileSync(profileId: Long): List<Category> =
        categoryDao.getCategoriesByProfileSync(profileId)

    suspend fun getSharedCategoriesSync(): List<Category> = categoryDao.getSharedCategoriesSync()

    // Deleting a profile only removes the categories/cards it owns
    // (profileId == profile.id) — the shared vocabulary (profileId == 0L,
    // seeded from the CSV) is never touched by a profile delete. Reuses
    // deleteCategory per owned category so custom photo files get cleaned
    // up the same way an ordinary category delete already does.
    suspend fun deleteProfile(profile: Profile) {
        categoryDao.getCategoriesByProfileSync(profile.id).forEach { deleteCategory(it) }
        profileDao.deleteProfile(profile)
    }

    // Categories
    fun getCategoriesForProfile(profileId: Long): LiveData<List<Category>> {
        return categoryDao.getCategoriesForProfile(profileId)
    }

    suspend fun getCategoryById(id: Long): Category? {
        return categoryDao.getCategoryById(id)
    }

    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun insertCategories(categories: List<Category>) {
        categoryDao.insertCategories(categories)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    // Deleting a category must not orphan its cards' custom photo files
    // (CLAUDE.md invariant 3) — clean those up before the cards themselves go.
    suspend fun deleteCategory(category: Category) {
        val cards = flashCardDao.getCardsByCategorySync(category.id)
        cards.forEach { deleteCardImageFile(it) }
        categoryDao.deleteCategory(category)
        flashCardDao.deleteCardsByCategory(category.id)
    }

    // FlashCards
    fun getCardsByCategory(categoryId: Long): LiveData<List<FlashCard>> {
        return flashCardDao.getCardsByCategory(categoryId)
    }

    suspend fun getCardsByCategorySync(categoryId: Long): List<FlashCard> {
        return flashCardDao.getCardsByCategorySync(categoryId)
    }

    suspend fun getEnabledCardsByCategorySync(categoryId: Long): List<FlashCard> {
        return flashCardDao.getEnabledCardsByCategorySync(categoryId)
    }

    suspend fun getCardById(id: Long): FlashCard? {
        return flashCardDao.getCardById(id)
    }

    suspend fun insertCard(card: FlashCard): Long {
        return flashCardDao.insertCard(card)
    }

    suspend fun insertCards(cards: List<FlashCard>) {
        flashCardDao.insertCards(cards)
    }

    suspend fun updateCard(card: FlashCard) {
        flashCardDao.updateCard(card)
    }

    suspend fun deleteCard(card: FlashCard) {
        deleteCardImageFile(card)
        flashCardDao.deleteCard(card)
    }

    private fun deleteCardImageFile(card: FlashCard) {
        if (card.isCustom && card.imagePath.isNotBlank()) {
            File(card.imagePath).delete()
        }
    }
}
