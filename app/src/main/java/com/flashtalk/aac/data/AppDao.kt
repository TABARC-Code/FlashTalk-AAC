package com.flashtalk.aac.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY `order` ASC, name ASC")
    fun getAllCategories(): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Long): Category?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("DELETE FROM categories WHERE isCustom = 1")
    suspend fun deleteAllCustomCategories()
}

@Dao
interface FlashCardDao {
    @Query("SELECT * FROM flashcards WHERE categoryId = :categoryId ORDER BY `order` ASC, text ASC")
    fun getCardsByCategory(categoryId: Long): LiveData<List<FlashCard>>

    @Query("SELECT * FROM flashcards WHERE categoryId = :categoryId ORDER BY `order` ASC, text ASC")
    suspend fun getCardsByCategorySync(categoryId: Long): List<FlashCard>

    @Query("SELECT * FROM flashcards WHERE id = :cardId")
    suspend fun getCardById(cardId: Long): FlashCard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: FlashCard): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<FlashCard>)

    @Update
    suspend fun updateCard(card: FlashCard)

    @Delete
    suspend fun deleteCard(card: FlashCard)

    @Query("DELETE FROM flashcards WHERE categoryId = :categoryId")
    suspend fun deleteCardsByCategory(categoryId: Long)

    @Query("DELETE FROM flashcards WHERE isCustom = 1")
    suspend fun deleteAllCustomCards()
}
