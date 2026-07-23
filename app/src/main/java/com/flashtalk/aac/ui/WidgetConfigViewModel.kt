package com.flashtalk.aac.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.flashtalk.aac.data.AppDatabase
import com.flashtalk.aac.data.AppRepository
import com.flashtalk.aac.data.Category

class WidgetConfigViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.categoryDao(), database.flashCardDao(), database.profileDao())
    }

    suspend fun getSharedCategories(): List<Category> = repository.getSharedCategoriesSync()
}

class WidgetConfigViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WidgetConfigViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WidgetConfigViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
