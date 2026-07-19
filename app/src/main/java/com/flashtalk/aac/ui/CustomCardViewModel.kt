package com.flashtalk.aac.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.flashtalk.aac.data.AppDatabase
import com.flashtalk.aac.data.AppRepository
import com.flashtalk.aac.data.FlashCard
import kotlinx.coroutines.launch

class CustomCardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.categoryDao(), database.flashCardDao())
    }

    fun addCard(categoryId: Long, text: String, imagePath: String) {
        viewModelScope.launch {
            repository.insertCard(
                FlashCard(categoryId = categoryId, text = text, imagePath = imagePath, isCustom = true)
            )
        }
    }
}

class CustomCardViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomCardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CustomCardViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
