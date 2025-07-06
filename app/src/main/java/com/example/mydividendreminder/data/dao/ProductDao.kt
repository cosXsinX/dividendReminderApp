package com.example.mydividendreminder.data.dao

import androidx.room.*
import com.example.mydividendreminder.data.entity.Product
import com.example.mydividendreminder.data.entity.ProductWithSectors
import com.example.mydividendreminder.data.entity.ProductWithDividends
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY ticker ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE ticker = :ticker")
    suspend fun getProductByTicker(ticker: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)
    
    // Relationship queries
    @Transaction
    @Query("SELECT * FROM products ORDER BY ticker ASC")
    fun getAllProductsWithSectors(): Flow<List<ProductWithSectors>>
    
    @Transaction
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductWithSectorsById(id: Long): ProductWithSectors?

    @Transaction
    @Query("SELECT * FROM products ORDER BY ticker ASC")
    fun getAllProductsWithDividends(): Flow<List<ProductWithDividends>>
    
    @Transaction
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductWithDividendsById(id: Long): ProductWithDividends?
} 