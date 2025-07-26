package com.example.mydividendreminder.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.mydividendreminder.R
import com.example.mydividendreminder.data.entity.ProductWithDividends

class DividendExportHelper {
    
    companion object {
        fun exportDividendsToCsv(
            context: Context,
            productsWithDividends: List<ProductWithDividends>
        ) {
            try {
                if (productsWithDividends.isEmpty()) {
                    Toast.makeText(context, context.getString(R.string.no_dividends_to_export), Toast.LENGTH_SHORT).show()
                    return
                }
                
                val csvUri = CsvExportUtil.exportDividendsToCsv(context, productsWithDividends)
                if (csvUri != null) {
                    val shareIntent = CsvExportUtil.createShareIntent(context, csvUri)
                    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.export_dividends)))
                } else {
                    Toast.makeText(context, context.getString(R.string.failed_to_create_csv), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, context.getString(R.string.error_exporting_dividends, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }
} 