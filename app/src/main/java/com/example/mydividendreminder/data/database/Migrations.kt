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

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create dividends table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS dividends (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    productId INTEGER NOT NULL,
                    dividendDate TEXT NOT NULL,
                    dividendAmount REAL NOT NULL,
                    FOREIGN KEY(productId) REFERENCES products(id) ON DELETE CASCADE
                )
            """)
            
            // Migrate existing dividend data from products table to dividends table
            database.execSQL("""
                INSERT INTO dividends (productId, dividendDate, dividendAmount)
                SELECT id, dividendDate, dividendAmount FROM products
                WHERE dividendDate IS NOT NULL AND dividendAmount IS NOT NULL
            """)
            
            // Create temporary table with new schema
            database.execSQL("""
                CREATE TABLE products_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    ticker TEXT NOT NULL,
                    name TEXT NOT NULL,
                    isin TEXT NOT NULL
                )
            """)
            
            // Copy data to new table
            database.execSQL("""
                INSERT INTO products_new (id, ticker, name, isin)
                SELECT id, ticker, name, isin FROM products
            """)
            
            // Drop old table and rename new table
            database.execSQL("DROP TABLE products")
            database.execSQL("ALTER TABLE products_new RENAME TO products")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS api_keys (
                    provider TEXT NOT NULL PRIMARY KEY,
                    key TEXT NOT NULL
                )
            """)
        }
    }
} 