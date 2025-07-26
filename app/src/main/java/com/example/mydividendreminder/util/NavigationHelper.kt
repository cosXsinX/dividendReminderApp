package com.example.mydividendreminder.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.mydividendreminder.*

/**
 * Utility class to centralize navigation functions across the app.
 * This eliminates the need to recreate the same navigation lambda functions in each activity.
 */
class NavigationHelper(private val context: Context) {

    fun navigateToMain() : () -> Unit = {
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
        finishCurrentActivity()
    }

    fun navigateToHelp(): () -> Unit = {
        val intent = Intent(context, HelpActivity::class.java)
        context.startActivity(intent)
        finishCurrentActivity()
    }
    
    fun navigateToProducts(): () -> Unit = {
        val intent = Intent(context, ProductListActivity::class.java)
        context.startActivity(intent)
        finishCurrentActivity()
    }
    
    fun navigateToSectors(): () -> Unit = {
        val intent = Intent(context, SectorActivity::class.java)
        context.startActivity(intent)
        finishCurrentActivity()
    }
    
    fun navigateToAddDividend(): () -> Unit = {
        val intent = Intent(context, AddDividendActivity::class.java)
        context.startActivity(intent)
        finishCurrentActivity()
    }
    
    fun navigateToApiKeys(): () -> Unit = {
        val intent = Intent(context, ApiKeyActivity::class.java)
        context.startActivity(intent)
        finishCurrentActivity()
    }
    
    fun navigateToPrompt(): () -> Unit = {
        val intent = Intent(context, PromptActivity::class.java)
        context.startActivity(intent)
        finishCurrentActivity()
    }
    
    /**
     * Creates a function that navigates to AddDividend with a specific product ID.
     * This is useful for the ProductListScreen where we need to pass a product ID.
     */
    fun navigateToAddDividendForProduct(): (Long) -> Unit = { productId ->
        val intent = Intent(context, AddDividendActivity::class.java)
        intent.putExtra("PRODUCT_ID", productId)
        context.startActivity(intent)
        finishCurrentActivity()
    }
    
    /**
     * Creates a function that finishes the current activity.
     * This is useful for closing the current screen and returning to the previous one.
     */
    fun finishCurrentActivity(): () -> Unit = {
        if (context is Activity) {
            context.finish()
        }
    }
    
    /**
     * Creates a function that exports dividends to CSV.
     * This requires the productsWithDividends data which varies by screen.
     */
    fun createExportDividendsFunction(productsWithDividends: List<com.example.mydividendreminder.data.entity.ProductWithDividends>): () -> Unit = {
        {
            DividendExportHelper.exportDividendsToCsv(context, productsWithDividends)
        }
    }
    
    /**
     * Creates a function that handles export functionality when no data is available.
     * This is used in screens that don't have access to dividend data.
     */
    fun createEmptyExportFunction(): () -> Unit = {
        // Handle export functionality when no data is available
        // Could show a toast or dialog indicating no data to export
    }
} 