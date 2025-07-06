package com.example.mydividendreminder.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mydividendreminder.data.converter.Converters
import com.example.mydividendreminder.data.dao.ProductDao
import com.example.mydividendreminder.data.entity.Product

@Database(
    entities = [Product::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dividend_reminder_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 