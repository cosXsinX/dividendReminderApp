package com.example.mydividendreminder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sectors")
data class Sector(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val providerName: String
) 