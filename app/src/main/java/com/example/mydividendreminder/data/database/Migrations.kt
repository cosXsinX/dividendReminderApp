package com.example.mydividendreminder.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create sectors table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS sectors (
                    name TEXT NOT NULL,
                    providerName TEXT NOT NULL,
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
                )
            """)
            
            // Create product_sector_cross_ref table (no foreign keys or indices)
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS product_sector_cross_ref (
                    productId INTEGER NOT NULL,
                    sectorId INTEGER NOT NULL,
                    PRIMARY KEY(productId, sectorId)
                )
            """)
        }
    }
} 