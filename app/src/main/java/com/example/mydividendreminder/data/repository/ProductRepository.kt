package com.example.mydividendreminder.data.repository

import com.example.mydividendreminder.data.dao.ProductDao
import com.example.mydividendreminder.data.entity.Product
import com.example.mydividendreminder.data.entity.ProductWithSectors
import com.example.mydividendreminder.data.entity.ProductWithDividends
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {
    
    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()
    
    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)
    
    suspend fun getProductByTicker(ticker: String): Product? = productDao.getProductByTicker(ticker)
    
    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)
    
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)
    
    suspend fun deleteProductById(id: Long) = productDao.deleteProductById(id)
    
    // Relationship queries
    fun getAllProductsWithSectors(): Flow<List<ProductWithSectors>> = 
        productDao.getAllProductsWithSectors()
    
    suspend fun getProductWithSectorsById(id: Long): ProductWithSectors? = 
        productDao.getProductWithSectorsById(id)
    
    fun getAllProductsWithDividends(): Flow<List<ProductWithDividends>> = 
        productDao.getAllProductsWithDividends()
    
    suspend fun getProductWithDividendsById(id: Long): ProductWithDividends? = 
        productDao.getProductWithDividendsById(id)
} 