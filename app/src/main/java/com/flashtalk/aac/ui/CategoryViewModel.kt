package com.flashtalk.aac.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.flashtalk.aac.data.AppDatabase
import com.flashtalk.aac.data.AppRepository
import com.flashtalk.aac.data.FlashCard

class CategoryViewModel(application: Application, categoryId: Long) : AndroidViewModel(application) {

    private val repository: AppRepository
    val flashCards: LiveData<List<FlashCard>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.categoryDao(), database.flashCardDao())
        flashCards = repository.getCardsByCategory(categoryId)
    }
}

class CategoryViewModelFactory(
    private val application: Application,
    private val categoryId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(application, categoryId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
