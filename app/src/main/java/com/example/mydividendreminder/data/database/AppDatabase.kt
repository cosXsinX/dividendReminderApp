package com.example.mydividendreminder.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mydividendreminder.data.converter.Converters
import com.example.mydividendreminder.data.dao.ProductDao
import com.example.mydividendreminder.data.dao.SectorDao
import com.example.mydividendreminder.data.dao.DividendDao
import com.example.mydividendreminder.data.entity.Product
import com.example.mydividendreminder.data.entity.Sector
import com.example.mydividendreminder.data.entity.Dividend
import com.example.mydividendreminder.data.entity.ProductSectorCrossRef
import com.example.mydividendreminder.data.database.Migrations

@Database(
    entities = [Product::class, Sector::class, Dividend::class, ProductSectorCrossRef::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun sectorDao(): SectorDao
    abstract fun dividendDao(): DividendDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dividend_reminder_database"
                )
                .addMigrations(Migrations.MIGRATION_1_2, Migrations.MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 