package com.example.mydividendreminder.data.remote.repository

interface GeminiApi {
    suspend fun fetchCompletion(prompt: String): String
} 