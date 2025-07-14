package com.example.mydividendreminder.data.repository

import com.example.mydividendreminder.data.dao.ApiKeyDao
import com.example.mydividendreminder.data.entity.ApiKey

class ApiKeyRepository(private val apiKeyDao: ApiKeyDao) {
    suspend fun upsertApiKey(apiKey: ApiKey) = apiKeyDao.upsert(apiKey)
    suspend fun getApiKey(provider: String): ApiKey? = apiKeyDao.getApiKey(provider)
    suspend fun getAllApiKeys(): List<ApiKey> = apiKeyDao.getAllApiKeys()
    suspend fun deleteApiKey(provider: String) = apiKeyDao.deleteApiKey(provider)

    suspend fun getOpenAiModel(): String =
        apiKeyDao.getApiKey("openai_model")?.key ?: "text-davinci-003"

    suspend fun setOpenAiModel(model: String) =
        apiKeyDao.upsert(ApiKey("openai_model", model))
} 