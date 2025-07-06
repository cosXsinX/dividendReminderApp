package com.example.mydividendreminder.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.mydividendreminder.data.entity.ProductWithDividends
import java.io.File
import java.io.FileWriter
import java.time.format.DateTimeFormatter

class CsvExportUtil {
    
    companion object {
        private const val CSV_FILENAME = "dividend_export.csv"
        private const val FILE_PROVIDER_AUTHORITY = "com.example.mydividendreminder.fileprovider"
        
        fun exportDividendsToCsv(
            context: Context,
            productsWithDividends: List<ProductWithDividends>
        ): Uri? {
            try {
                // Create CSV content
                val csvContent = buildCsvContent(productsWithDividends)
                
                // Write to file
                val file = File(context.cacheDir, CSV_FILENAME)
                FileWriter(file).use { writer ->
                    writer.write(csvContent)
                }
                
                // Return content URI for sharing
                return FileProvider.getUriForFile(
                    context,
                    FILE_PROVIDER_AUTHORITY,
                    file
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
        
        private fun buildCsvContent(productsWithDividends: List<ProductWithDividends>): String {
            val csvBuilder = StringBuilder()
            
            // Add CSV header
            csvBuilder.append("Product Name,Product Ticker,Dividend Date,Dividend Amount (€)\n")
            
            // Collect all dividends with product info and sort by date
            val allDividends = mutableListOf<Triple<String, String, com.example.mydividendreminder.data.entity.Dividend>>()
            
            productsWithDividends.forEach { productWithDividends ->
                val product = productWithDividends.product
                productWithDividends.dividends.forEach { dividend ->
                    allDividends.add(Triple(product.name, product.ticker, dividend))
                }
            }
            
            // Sort by dividend date (earliest to latest)
            allDividends.sortBy { it.third.dividendDate }
            
            // Add dividend data
            allDividends.forEach { (productName, productTicker, dividend) ->
                val date = dividend.dividendDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val amount = String.format("%.2f", dividend.dividendAmount)
                
                // Escape CSV values (handle commas and quotes)
                val escapedName = escapeCsvValue(productName)
                val escapedTicker = escapeCsvValue(productTicker)
                
                csvBuilder.append("$escapedName,$escapedTicker,$date,$amount\n")
            }
            
            return csvBuilder.toString()
        }
        
        private fun escapeCsvValue(value: String): String {
            return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                "\"${value.replace("\"", "\"\"")}\""
            } else {
                value
            }
        }
        
        fun createShareIntent(context: Context, csvUri: Uri): Intent {
            return Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, csvUri)
                putExtra(Intent.EXTRA_SUBJECT, "Dividend Export")
                putExtra(Intent.EXTRA_TEXT, "Dividend data export from MyDividendReminder")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }
} 