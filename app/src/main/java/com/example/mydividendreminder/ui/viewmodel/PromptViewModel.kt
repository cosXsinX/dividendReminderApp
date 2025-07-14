package com.example.mydividendreminder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mydividendreminder.data.repository.ApiKeyRepository
import com.example.mydividendreminder.data.remote.repository.GeminiApiImpl
import com.example.mydividendreminder.data.remote.repository.OpenAiApiImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class PromptProvider { GEMINI, OPENAI }

class PromptViewModel(private val apiKeyRepository: ApiKeyRepository) : ViewModel() {
    val prompt = MutableStateFlow("")
    val provider = MutableStateFlow(PromptProvider.GEMINI)
    val result = MutableStateFlow("")
    val loading = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    private var openAiKey: String? = null
    private var geminiKey: String? = null
    private var openAiModel: String = "text-davinci-003"

    init {
        viewModelScope.launch {
            openAiKey = apiKeyRepository.getApiKey("openai")?.key
            geminiKey = apiKeyRepository.getApiKey("gemini")?.key
            openAiModel = apiKeyRepository.getOpenAiModel()
        }
    }

    fun setPrompt(value: String) { prompt.value = value }
    fun setProvider(value: PromptProvider) { provider.value = value }

    fun sendPrompt() {
        val promptText = prompt.value
        val selectedProvider = provider.value
        result.value = ""
        error.value = null
        loading.value = true
        viewModelScope.launch {
            try {
                val completion = when (selectedProvider) {
                    PromptProvider.GEMINI -> {
                        val key = geminiKey ?: throw Exception("Gemini API key not set")
                        GeminiApiImpl(key).fetchCompletion(promptText)
                    }
                    PromptProvider.OPENAI -> {
                        val key = openAiKey ?: throw Exception("OpenAI API key not set")
                        OpenAiApiImpl(key).fetchCompletion(promptText, openAiModel)
                    }
                }
                result.value = completion
            } catch (e: Exception) {
                error.value = e.message
            } finally {
                loading.value = false
            }
        }
    }

    class Factory(private val apiKeyRepository: ApiKeyRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PromptViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PromptViewModel(apiKeyRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 