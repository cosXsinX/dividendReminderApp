package com.example.mydividendreminder.data.remote.repository

interface OpenAiApi {
    suspend fun fetchCompletion(prompt: String, model: String): String
} 