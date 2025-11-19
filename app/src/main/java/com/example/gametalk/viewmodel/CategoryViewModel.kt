package com.example.gametalk.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gametalk.model.data.config.AppDatabase
import com.example.gametalk.model.data.repository.CategoryRepository
import com.example.gametalk.model.domain.CategoryUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val categoryRepository: CategoryRepository

    init {
        val categoryDao = AppDatabase.getDatabase(application).categoryDao()
        categoryRepository = CategoryRepository(categoryDao)
        
        // Inicializar categorías por defecto
        viewModelScope.launch {
            categoryRepository.initializeDefaultCategories()
        }
        
        // Observar cambios en categorías
        loadCategories()
    }

    private val _uiState = MutableStateFlow(CategoryUIState())
    val uiState: StateFlow<CategoryUIState> = _uiState.asStateFlow()

    private fun loadCategories() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            categoryRepository.allCategories
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar categorías: ${exception.message}"
                        )
                    }
                }
                .collect { categories ->
                    _uiState.update {
                        it.copy(
                            categories = categories,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun refreshCategories() {
        loadCategories()
    }
}
