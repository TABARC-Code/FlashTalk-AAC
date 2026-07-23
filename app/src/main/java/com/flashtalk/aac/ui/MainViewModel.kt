package com.flashtalk.aac.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.flashtalk.aac.data.AppDatabase
import com.flashtalk.aac.data.AppRepository
import com.flashtalk.aac.data.Category
import com.flashtalk.aac.data.Profile
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    private val currentProfileId = MutableLiveData<Long>()
    val categories: LiveData<List<Category>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.categoryDao(), database.flashCardDao(), database.profileDao())
        categories = currentProfileId.switchMap { profileId -> repository.getCategoriesForProfile(profileId) }
    }

    // Called from MainActivity.onResume() every time the active profile
    // might have changed (Settings-style "read prefs on resume" pattern —
    // see applyEditModeState/applyStripModeState in other activities).
    fun setCurrentProfile(profileId: Long) {
        if (currentProfileId.value != profileId) {
            currentProfileId.value = profileId
        }
    }

    suspend fun getAllProfilesSync(): List<Profile> = repository.getAllProfilesSync()

    fun addCategory(name: String, icon: String, color: String, profileId: Long) {
        viewModelScope.launch {
            repository.insertCategory(
                Category(name = name, icon = icon, color = color, isCustom = true, profileId = profileId)
            )
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    suspend fun cardCountFor(categoryId: Long): Int = repository.getCardsByCategorySync(categoryId).size

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
