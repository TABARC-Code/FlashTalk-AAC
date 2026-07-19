package com.flashtalk.aac.data

import androidx.lifecycle.LiveData
import java.io.File

class AppRepository(
    private val categoryDao: CategoryDao,
    private val flashCardDao: FlashCardDao
) {

    // Categories
    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()

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
