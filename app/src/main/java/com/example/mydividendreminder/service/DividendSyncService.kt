package com.example.mydividendreminder.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

data class DividendData(
    val ticker: String,
    val company: String,
    val isin: String,
    val exDate: LocalDate,
    val paymentDate: LocalDate,
    val amount: Double,
    val type: String
)

interface DividendSyncService {
    suspend fun fetchDividendsFromUrl(url: String): List<DividendData>
}

class DividendSyncServiceImpl : DividendSyncService {
    
    override suspend fun fetchDividendsFromUrl(url: String): List<DividendData> {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch HTML content
                val htmlContent = fetchHtmlContent(url)
                
                // Try to extract JSON from script tag first
                val jsonData = extractJsonFromScript(htmlContent)
                
                if (jsonData != null) {
                    parseJsonDividends(jsonData)
                } else {
                    // Fallback to parsing HTML table
                    parseHtmlTable(htmlContent)
                }
            } catch (e: Exception) {
                throw RuntimeException("Failed to fetch dividends: ${e.message}", e)
            }
        }
    }
    
    private fun fetchHtmlContent(urlString: String): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        
        val responseCode = connection.responseCode
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw RuntimeException("HTTP $responseCode: Failed to fetch page")
        }
        
        val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
        val responseBody = reader.use { it.readText() }
        
        if (responseBody.isBlank()) {
            throw RuntimeException("Empty response from server")
        }
        
        return responseBody
    }
    
    private fun extractJsonFromScript(html: String): JSONObject? {
        try {
            // Look for script tag with id="dividend-data"
            val pattern = Pattern.compile(
                "<script[^>]*id\\s*=\\s*[\"']dividend-data[\"'][^>]*>(.*?)</script>",
                Pattern.CASE_INSENSITIVE or Pattern.DOTALL
            )
            val matcher = pattern.matcher(html)
            
            if (matcher.find()) {
                val jsonContent = matcher.group(1).trim()
                // Check if it's a JSON object (starts with {) or array (starts with [)
                return when {
                    jsonContent.startsWith("{") -> JSONObject(jsonContent)
                    jsonContent.startsWith("[") -> {
                        // Wrap array in an object for compatibility
                        JSONObject().put("tickers", JSONArray(jsonContent))
                    }
                    else -> null
                }
            }
            
            // Alternative: look for any script tag containing JSON object with tickers
            val jsonObjectPattern = Pattern.compile(
                "\\{\\s*\"[^\"]*\"\\s*:\\s*\\[\\s*\\{[^}]*\"(?:symbol|ticker)\"[^}]*\\}[^\\]]*\\]",
                Pattern.CASE_INSENSITIVE or Pattern.DOTALL
            )
            val jsonObjectMatcher = jsonObjectPattern.matcher(html)
            
            if (jsonObjectMatcher.find()) {
                val jsonContent = jsonObjectMatcher.group(0)
                return JSONObject(jsonContent)
            }
            
            // Alternative: look for any script tag containing JSON array with dividend data
            val jsonArrayPattern = Pattern.compile(
                "\\[\\s*\\{[^}]*\"ticker\"[^}]*\\}[^\\]]*\\]",
                Pattern.CASE_INSENSITIVE or Pattern.DOTALL
            )
            val jsonArrayMatcher = jsonArrayPattern.matcher(html)
            
            if (jsonArrayMatcher.find()) {
                val jsonContent = jsonArrayMatcher.group(0)
                // Wrap array in an object for compatibility
                return JSONObject().put("tickers", JSONArray(jsonContent))
            }
        } catch (e: Exception) {
            // If JSON parsing fails, fall back to HTML parsing
        }
        
        return null
    }
    
    private fun parseJsonDividends(jsonObject: JSONObject): List<DividendData> {
        val dividends = mutableListOf<DividendData>()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        
        try {
            // Extract tickers array from the JSON object
            val tickersArray = jsonObject.optJSONArray("tickers")
            
            if (tickersArray != null) {
                // New structure: { "tickers": [ { "symbol": "...", "company": "...", "dividends": [...] } ] }
                for (i in 0 until tickersArray.length()) {
                    try {
                        val tickerObj = tickersArray.getJSONObject(i)
                        val ticker = tickerObj.optString("symbol", tickerObj.optString("ticker", ""))
                        val company = tickerObj.optString("company", "")
                        val isin = tickerObj.optString("isin", "")
                        val dividendsArray = tickerObj.optJSONArray("dividends")
                        
                        if (dividendsArray != null) {
                            // Process each dividend in the ticker's dividends array
                            for (j in 0 until dividendsArray.length()) {
                                try {
                                    val dividendObj = dividendsArray.getJSONObject(j)
                                    val exDateStr = dividendObj.getString("exDate")
                                    val paymentDateStr = dividendObj.getString("paymentDate")
                                    
                                    // Amount can be a number or string
                                    val amount = when {
                                        dividendObj.has("amount") && !dividendObj.isNull("amount") -> {
                                            if (dividendObj.get("amount") is Number) {
                                                dividendObj.getDouble("amount")
                                            } else {
                                                parseAmount(dividendObj.getString("amount"))
                                            }
                                        }
                                        else -> throw IllegalArgumentException("Missing amount field")
                                    }
                                    
                                    val type = dividendObj.optString("type", "final")
                                    
                                    // Parse dates
                                    val exDate = LocalDate.parse(exDateStr, dateFormatter)
                                    val paymentDate = LocalDate.parse(paymentDateStr, dateFormatter)
                                    
                                    dividends.add(
                                        DividendData(
                                            ticker = ticker,
                                            company = company,
                                            isin = isin,
                                            exDate = exDate,
                                            paymentDate = paymentDate,
                                            amount = amount,
                                            type = type
                                        )
                                    )
                                } catch (e: Exception) {
                                    // Skip invalid dividend entries
                                    continue
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Skip invalid ticker entries
                        continue
                    }
                }
            } else {
                // Fallback: try to parse as flat array structure (old format)
                // Check if the object itself is structured as an array-like object
                val keys = jsonObject.keys()
                var foundArray = false
                
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = jsonObject.get(key)
                    if (value is JSONArray) {
                        foundArray = true
                        // Treat as array of dividend objects
                        for (i in 0 until value.length()) {
                            try {
                                val item = value.getJSONObject(i)
                                val ticker = item.optString("ticker", item.optString("symbol", ""))
                                val company = item.optString("company", "")
                                val isin = item.optString("isin", "")
                                val exDateStr = item.getString("exDate")
                                val paymentDateStr = item.getString("paymentDate")
                                
                                val amount = when {
                                    item.has("amount") && !item.isNull("amount") -> {
                                        if (item.get("amount") is Number) {
                                            item.getDouble("amount")
                                        } else {
                                            parseAmount(item.getString("amount"))
                                        }
                                    }
                                    else -> throw IllegalArgumentException("Missing amount field")
                                }
                                
                                val type = item.optString("type", "final")
                                
                                val exDate = LocalDate.parse(exDateStr, dateFormatter)
                                val paymentDate = LocalDate.parse(paymentDateStr, dateFormatter)
                                
                                dividends.add(
                                    DividendData(
                                        ticker = ticker,
                                        company = company,
                                        isin = isin,
                                        exDate = exDate,
                                        paymentDate = paymentDate,
                                        amount = amount,
                                        type = type
                                    )
                                )
                            } catch (e: Exception) {
                                continue
                            }
                        }
                        break
                    }
                }
                
                if (!foundArray) {
                    // If no array found, the structure might be different
                    throw IllegalArgumentException("Unable to parse JSON structure: no 'tickers' array found")
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to parse JSON dividends: ${e.message}", e)
        }
        
        return dividends
    }
    
    private fun parseHtmlTable(html: String): List<DividendData> {
        val dividends = mutableListOf<DividendData>()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        
        // Pattern to match table rows with data attributes or table cells
        // Look for rows with data-ticker attribute or standard table structure
        val rowPattern = Pattern.compile(
            "<tr[^>]*data-ticker=\"([^\"]+)\"[^>]*>.*?</tr>",
            Pattern.CASE_INSENSITIVE or Pattern.DOTALL
        )
        val matcher = rowPattern.matcher(html)
        
        while (matcher.find()) {
            try {
                val rowHtml = matcher.group(0)
                val ticker = matcher.group(1)
                
                // Extract data attributes
                val exDateStr = extractAttribute(rowHtml, "data-ex-date") 
                    ?: extractTableCell(rowHtml, 2) // Ex-Date column
                val paymentDateStr = extractAttribute(rowHtml, "data-payment-date")
                    ?: extractTableCell(rowHtml, 3) // Payment Date column
                val amountStr = extractAttribute(rowHtml, "data-amount")
                    ?: extractTableCell(rowHtml, 4) // Amount column
                val company = extractAttribute(rowHtml, "data-company")
                    ?: extractTableCell(rowHtml, 1) // Company column
                val isin = extractAttribute(rowHtml, "data-isin")
                    ?: extractTableCell(rowHtml, 1) // Try to extract from company cell
                
                if (exDateStr != null && paymentDateStr != null && amountStr != null) {
                    val amount = parseAmount(amountStr)
                    val exDate = LocalDate.parse(exDateStr.trim(), dateFormatter)
                    val paymentDate = LocalDate.parse(paymentDateStr.trim(), dateFormatter)
                    
                    dividends.add(
                        DividendData(
                            ticker = ticker,
                            company = company ?: "",
                            isin = isin ?: "",
                            exDate = exDate,
                            paymentDate = paymentDate,
                            amount = amount,
                            type = "final" // Default type
                        )
                    )
                }
            } catch (e: Exception) {
                // Skip invalid rows
                continue
            }
        }
        
        // If no data-ticker rows found, try parsing standard HTML table
        if (dividends.isEmpty()) {
            return parseStandardHtmlTable(html, dateFormatter)
        }
        
        return dividends
    }
    
    private fun parseStandardHtmlTable(html: String, dateFormatter: DateTimeFormatter): List<DividendData> {
        val dividends = mutableListOf<DividendData>()
        
        // Pattern to match table rows (skip header row)
        val rowPattern = Pattern.compile(
            "<tr[^>]*>.*?</tr>",
            Pattern.CASE_INSENSITIVE or Pattern.DOTALL
        )
        val matcher = rowPattern.matcher(html)
        
        var isFirstRow = true
        while (matcher.find()) {
            if (isFirstRow) {
                isFirstRow = false
                continue // Skip header row
            }
            
            try {
                val rowHtml = matcher.group(0)
                val cells = extractTableCells(rowHtml)
                
                if (cells.size >= 5) {
                    val ticker = cells[0].trim()
                    val companyOrIsin = cells[1].trim()
                    val exDateStr = cells[2].trim()
                    val paymentDateStr = cells[3].trim()
                    val amountStr = cells[4].trim()
                    
                    if (ticker.isNotBlank() && exDateStr.isNotBlank() && 
                        paymentDateStr.isNotBlank() && amountStr.isNotBlank()) {
                        val amount = parseAmount(amountStr)
                        val exDate = LocalDate.parse(exDateStr, dateFormatter)
                        val paymentDate = LocalDate.parse(paymentDateStr, dateFormatter)
                        
                        // Check if company field is actually an ISIN (format: 2 letters + 10 digits)
                        val isIsin = companyOrIsin.matches(Regex("^[A-Z]{2}[0-9]{10}$"))
                        val isin = if (isIsin) companyOrIsin else ""
                        val company = if (isIsin) "" else companyOrIsin
                        
                        dividends.add(
                            DividendData(
                                ticker = ticker,
                                company = company,
                                isin = isin,
                                exDate = exDate,
                                paymentDate = paymentDate,
                                amount = amount,
                                type = if (cells.size > 5) cells[5].trim() else "final"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Skip invalid rows
                continue
            }
        }
        
        return dividends
    }
    
    private fun extractAttribute(html: String, attributeName: String): String? {
        val pattern = Pattern.compile(
            "$attributeName\\s*=\\s*[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = pattern.matcher(html)
        return if (matcher.find()) matcher.group(1) else null
    }
    
    private fun extractTableCell(html: String, index: Int): String? {
        val cells = extractTableCells(html)
        return if (index < cells.size) cells[index] else null
    }
    
    private fun extractTableCells(rowHtml: String): List<String> {
        val cells = mutableListOf<String>()
        val cellPattern = Pattern.compile(
            "<t[dh][^>]*>(.*?)</t[dh]>",
            Pattern.CASE_INSENSITIVE or Pattern.DOTALL
        )
        val matcher = cellPattern.matcher(rowHtml)
        
        while (matcher.find()) {
            val cellContent = matcher.group(1)
                .replace(Regex("<[^>]+>"), "") // Remove HTML tags
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .trim()
            cells.add(cellContent)
        }
        
        return cells
    }
    
    private fun parseAmount(amountStr: String): Double {
        // Remove currency symbols, spaces, and parse number
        val cleaned = amountStr
            .replace(Regex("[€$£,\\s]"), "")
            .replace(",", ".")
            .trim()
        
        return try {
            cleaned.toDouble()
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid amount format: $amountStr")
        }
    }
}

