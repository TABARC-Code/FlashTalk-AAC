package com.flashtalk.aac.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.flashtalk.aac.data.AppDatabase
import com.flashtalk.aac.data.AppRepository
import com.flashtalk.aac.data.Category
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository
    val allCategories: LiveData<List<Category>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.categoryDao(), database.flashCardDao())
        allCategories = repository.allCategories
    }

    fun addCategory(name: String, icon: String, color: String) {
        viewModelScope.launch {
            repository.insertCategory(Category(name = name, icon = icon, color = color, isCustom = true))
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
