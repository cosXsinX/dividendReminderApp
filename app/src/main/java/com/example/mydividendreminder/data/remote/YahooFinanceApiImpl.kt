package com.example.mydividendreminder.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class YahooFinanceResponse(
    val symbol: String,
    val longName: String? = null,
    val shortName: String? = null,
    val regularMarketPrice: Double? = null,
    val isin: String? = null,
    val currency: String? = null,
    val exchange: String? = null
)

interface YahooFinanceApi {
    suspend fun fetchQuote(symbol: String): YahooFinanceResponse
}

class YahooFinanceApiImpl : YahooFinanceApi {
    
    override suspend fun fetchQuote(symbol: String): YahooFinanceResponse {
        return withContext(Dispatchers.IO) {
            try {
                // Validate input
                if (symbol.isBlank()) {
                    throw IllegalArgumentException("Symbol cannot be blank")
                }
                
                val cleanSymbol = symbol.trim().uppercase()
                
                // Create URL and connection
                val url = URL("https://query1.finance.yahoo.com/v8/finance/chart/$cleanSymbol")
                val connection = url.openConnection() as HttpURLConnection
                
                // Set request properties
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                // Check response code
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw RuntimeException("HTTP $responseCode: Failed to fetch stock data for $cleanSymbol")
                }
                
                // Read response
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val responseBody = reader.use { it.readText() }
                
                if (responseBody.isBlank()) {
                    throw RuntimeException("Empty response for $cleanSymbol")
                }
                
                // Parse JSON response using Android's built-in JSONObject
                val jsonObject = JSONObject(responseBody)
                val chart = jsonObject.getJSONObject("chart")
                
                // Check for API errors
                if (chart.has("error") && !chart.isNull("error")) {
                    val error = chart.getJSONObject("error")
                    val description = error.optString("description", "Unknown error")
                    throw RuntimeException("API Error: $description")
                }
                
                val resultArray = chart.getJSONArray("result")
                if (resultArray.length() == 0) {
                    throw RuntimeException("No data available for symbol: $cleanSymbol")
                }
                
                val result = resultArray.getJSONObject(0)
                val meta = result.getJSONObject("meta")
                val indicators = result.getJSONObject("indicators")
                val quoteArray = indicators.getJSONArray("quote")
                
                if (quoteArray.length() == 0) {
                    throw RuntimeException("No quote data available for symbol: $cleanSymbol")
                }
                
                val quote = quoteArray.getJSONObject(0)
                
                // Extract data from meta
                val longName = meta.optString("longName", null).takeIf { it.isNotBlank() }
                val shortName = meta.optString("shortName", null).takeIf { it.isNotBlank() }
                val currency = meta.optString("currency", null).takeIf { it.isNotBlank() }
                val exchange = meta.optString("exchangeName", null).takeIf { it.isNotBlank() }
                
                // Extract price from quote data - use the last close price
                val closeArray = quote.optJSONArray("close")
                val price = if (closeArray != null && closeArray.length() > 0) {
                    // Find the last non-null close price
                    var lastPrice: Double? = null
                    for (i in closeArray.length() - 1 downTo 0) {
                        if (!closeArray.isNull(i)) {
                            lastPrice = closeArray.getDouble(i)
                            break
                        }
                    }
                    lastPrice
                } else null
                
                // Use the best available name
                val stockName = shortName ?: longName ?: "Unknown"
                
                // Validate that we have at least a name or price
                if (stockName == "Unknown" && price == null) {
                    throw RuntimeException("No valid data available for symbol: $cleanSymbol")
                }
                
                YahooFinanceResponse(
                    symbol = cleanSymbol,
                    longName = longName,
                    shortName = shortName,
                    regularMarketPrice = price,
                    isin = null, // Yahoo Finance doesn't provide ISIN in this endpoint
                    currency = currency,
                    exchange = exchange
                )
            } catch (e: IllegalArgumentException) {
                throw e
            } catch (e: IOException) {
                throw RuntimeException("Network error while fetching stock information for $symbol: ${e.message}")
            } catch (e: RuntimeException) { 
                throw e
            } catch (e: Exception) {
                throw RuntimeException("Unexpected error fetching stock information for $symbol: ${e.message}")
            }
        }
    }
}