package com.example.mydividendreminder.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.example.mydividendreminder.data.entity.ApiKey

@Dao
interface ApiKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(apiKey: ApiKey)

    @Query("SELECT * FROM api_keys WHERE provider = :provider LIMIT 1")
    suspend fun getApiKey(provider: String): ApiKey?

    @Query("SELECT * FROM api_keys")
    suspend fun getAllApiKeys(): List<ApiKey>

    @Query("DELETE FROM api_keys WHERE provider = :provider")
    suspend fun deleteApiKey(provider: String)
} 