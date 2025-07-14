package com.example.mydividendreminder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_keys")
data class ApiKey(
    @PrimaryKey val provider: String, // e.g., 'openai', 'gemini'
    val key: String
) 