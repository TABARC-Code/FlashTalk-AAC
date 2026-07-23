package com.flashtalk.aac.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.flashtalk.aac.data.AppDatabase
import com.flashtalk.aac.data.AppRepository
import com.flashtalk.aac.utils.ImageSetImporter
import kotlinx.coroutines.launch

sealed class ImportUiState {
    object Idle : ImportUiState()
    object Importing : ImportUiState()
    data class Done(val categoryName: String, val cardCount: Int, val warnings: List<String>) : ImportUiState()
    data class Failed(val message: String) : ImportUiState()
}

class ImportViewModel(application: Application, private val profileId: Long) : AndroidViewModel(application) {

    private val repository: AppRepository
    private val importer = ImageSetImporter(application)

    private val _state = MutableLiveData<ImportUiState>(ImportUiState.Idle)
    val state: LiveData<ImportUiState> = _state

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.categoryDao(), database.flashCardDao(), database.profileDao())
    }

    fun importZip(uri: Uri) = runImport { importer.importFromZip(uri) }

    fun importJson(uri: Uri) = runImport { importer.importFromJson(uri) }

    private fun runImport(block: suspend () -> ImageSetImporter.ImportResult) {
        _state.value = ImportUiState.Importing
        viewModelScope.launch {
            when (val result = block()) {
                is ImageSetImporter.ImportResult.Success -> {
                    val categoryId = repository.insertCategory(result.category.copy(profileId = profileId))
                    repository.insertCards(result.cards.map { it.copy(categoryId = categoryId) })
                    _state.value = ImportUiState.Done(result.category.name, result.cards.size, result.warnings)
                }
                is ImageSetImporter.ImportResult.Error -> {
                    _state.value = ImportUiState.Failed(result.message)
                }
            }
        }
    }
}

class ImportViewModelFactory(
    private val application: Application,
    private val profileId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImportViewModel(application, profileId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
