package com.example.mydividendreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mydividendreminder.data.repository.ApiKeyRepository
import com.example.mydividendreminder.data.entity.ApiKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ApiKeyViewModel(private val repository: ApiKeyRepository) : ViewModel() {
    private val _openAiKey = MutableStateFlow("")
    val openAiKey: StateFlow<String> = _openAiKey

    private val _geminiKey = MutableStateFlow("")
    val geminiKey: StateFlow<String> = _geminiKey

    private val _openAiModel = MutableStateFlow("text-davinci-003")
    val openAiModel: StateFlow<String> = _openAiModel

    init {
        loadKeys()
        loadOpenAiModel()
    }

    fun loadKeys() {
        viewModelScope.launch {
            _openAiKey.value = repository.getApiKey("openai")?.key ?: ""
            _geminiKey.value = repository.getApiKey("gemini")?.key ?: ""
        }
    }

    fun saveOpenAiKey(key: String) {
        viewModelScope.launch {
            repository.upsertApiKey(ApiKey("openai", key))
            _openAiKey.value = key
        }
    }

    fun saveGeminiKey(key: String) {
        viewModelScope.launch {
            repository.upsertApiKey(ApiKey("gemini", key))
            _geminiKey.value = key
        }
    }

    fun loadOpenAiModel() {
        viewModelScope.launch {
            _openAiModel.value = repository.getOpenAiModel()
        }
    }

    fun saveOpenAiModel(model: String) {
        viewModelScope.launch {
            repository.setOpenAiModel(model)
            _openAiModel.value = model
        }
    }

    class Factory(private val repository: ApiKeyRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ApiKeyViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ApiKeyViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 