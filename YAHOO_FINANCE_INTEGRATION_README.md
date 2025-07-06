# Yahoo Finance API Integration

This document describes the integration of the Yahoo Finance API into the Dividend Reminder App to automatically fetch stock information when adding products.

## Overview

The integration allows users to automatically populate product name and ISIN fields when entering a stock ticker symbol. This reduces manual data entry and ensures data accuracy.

## Features

- **Automatic Stock Information Fetching**: When a user enters a ticker symbol, the app automatically fetches stock information from Yahoo Finance API
- **Auto-fill Fields**: The stock name and ISIN are automatically populated in the form
- **Loading States**: Visual feedback during API calls
- **Error Handling**: Clear error messages when API calls fail
- **Debounced Input**: API calls are debounced to avoid excessive requests while typing

## Implementation Details

### 1. Data Models

- `YahooFinanceResponse`: Data class for API response
- `Stock`: Domain model containing stock information
- Updated to include ISIN field

### 2. API Layer

- `YahooFinanceApi`: Interface for API calls
- `YahooFinanceApiImpl`: Implementation using OkHttp HTTP client
- `StockRepository`: Repository pattern for stock data
- `GetStockInfoUseCase`: Use case for fetching stock information

### 3. UI Integration

- Updated `ProductViewModel` to include stock fetching functionality
- Enhanced `AddProductDialog` with:
  - Real-time stock information fetching
  - Loading indicators
  - Error handling
  - Auto-fill functionality
  - Debounced input (500ms delay)

## Setup Instructions

### 1. Dependencies

No additional dependencies are required. The implementation uses Android's built-in networking and JSON parsing capabilities.

### 2. No API Key Required

The implementation uses Yahoo Finance's public API endpoint, which doesn't require an API key.

## Usage

1. Navigate to the Product List screen
2. Tap "Add Product" button
3. Enter a stock ticker symbol (e.g., "AAPL", "MSFT", "GOOGL")
4. The app will automatically:
   - Show a loading indicator
   - Fetch stock information from Yahoo Finance
   - Display a preview card with stock details
   - Auto-fill the name and ISIN fields
5. Select sectors and tap "Add" to save the product

## Error Handling

The integration includes comprehensive error handling:

- Network errors are displayed to the user
- Invalid ticker symbols show appropriate error messages
- Loading states prevent multiple simultaneous requests
- Form validation ensures required fields are filled

## API Details

The integration uses Yahoo Finance's public API:
- **Endpoint**: `https://query1.finance.yahoo.com/v8/finance/chart/{symbol}`
- **Method**: GET
- **No API Key Required**: Free to use
- **Rate Limits**: Subject to Yahoo Finance's terms of service

## Response Format

The API returns stock information including:
- Symbol
- Stock name (shortName or longName)
- Current price (close price)
- Currency
- Exchange name
- Note: ISIN is not directly available through this endpoint

## Technical Implementation

### HTTP Client
- Uses Android's built-in HttpURLConnection
- Proper User-Agent header to avoid blocking
- JSON parsing with Android's built-in JSONObject
- Connection timeouts for reliability

### Error Handling
- Network error detection
- JSON parsing error handling
- Invalid symbol validation
- Empty response handling
- API error response handling

### Performance
- Network operations on IO dispatcher
- Debounced API calls (500ms)
- Efficient JSON parsing with built-in Android libraries

## Limitations

- Requires internet connection
- Subject to Yahoo Finance's rate limits and terms of service
- ISIN information is not directly available (set to null)
- Some international stocks may not be supported
- Network operations are performed on IO dispatcher to avoid blocking UI

## Advantages

- **No API Key Required**: No need to manage API keys
- **Free to Use**: No usage costs
- **Zero External Dependencies**: Uses only Android's built-in libraries
- **Reliable**: Direct access to Yahoo Finance data
- **Simple Setup**: No additional dependencies required
- **Compatible**: Works with all Android versions
- **Lightweight**: No additional library overhead

## Future Enhancements

Potential improvements could include:
- Caching of stock information
- Offline support
- Additional stock data (market cap, P/E ratio, etc.)
- Support for multiple data providers
- Historical price data
- ISIN lookup through alternative sources
- Retry mechanism for failed requests 