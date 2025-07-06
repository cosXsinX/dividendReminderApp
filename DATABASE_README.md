# Dividend Reminder App - SQLite Database Implementation

This document describes the SQLite database implementation for the Dividend Reminder Android app using Room persistence library.

## Overview

The app now includes a complete SQLite database implementation with the following components:

### 1. Product Entity
Located in `app/src/main/java/com/example/mydividendreminder/data/entity/Product.kt`

**Properties:**
- `id` (Long): Primary key, auto-generated
- `ticker` (String): Stock ticker symbol
- `name` (String): Company name
- `isin` (String): International Securities Identification Number
- `dividendDate` (LocalDate): Date when dividend is paid
- `dividendAmount` (Double): Amount of dividend per share

### 2. Database Components

#### Type Converters
- **File**: `app/src/main/java/com/example/mydividendreminder/data/converter/Converters.kt`
- **Purpose**: Converts LocalDate to/from String for Room database storage

#### Data Access Object (DAO)
- **File**: `app/src/main/java/com/example/mydividendreminder/data/dao/ProductDao.kt`
- **Features**:
  - Get all products
  - Get product by ID or ticker
  - Insert, update, delete products
  - Query products by dividend date

#### Database
- **File**: `app/src/main/java/com/example/mydividendreminder/data/database/AppDatabase.kt`
- **Database Name**: `dividend_reminder_database`
- **Version**: 1

#### Repository
- **File**: `app/src/main/java/com/example/mydividendreminder/data/repository/ProductRepository.kt`
- **Purpose**: Abstraction layer between ViewModel and DAO

### 3. UI Components

#### ViewModel
- **File**: `app/src/main/java/com/example/mydividendreminder/ui/viewmodel/ProductViewModel.kt`
- **Features**:
  - Observable product list
  - Loading state management
  - CRUD operations for products

#### UI Screen
- **File**: `app/src/main/java/com/example/mydividendreminder/ui/screen/ProductListScreen.kt`
- **Features**:
  - Display list of products
  - Add new products via dialog
  - Delete products
  - Material Design 3 UI

## Dependencies Added

The following dependencies were added to support Room database:

```kotlin
// Room dependencies
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
kapt(libs.androidx.room.compiler)
```

## Usage Examples

### Adding a Product
```kotlin
val product = Product(
    ticker = "AAPL",
    name = "Apple Inc.",
    isin = "US0378331005",
    dividendDate = LocalDate.of(2024, 2, 15),
    dividendAmount = 0.24
)
repository.insertProduct(product)
```

### Getting All Products
```kotlin
repository.getAllProducts().collect { products ->
    // Handle product list
}
```

### Querying by Dividend Date
```kotlin
val startDate = LocalDate.now()
repository.getProductsByDividendDate(startDate).collect { products ->
    // Handle filtered products
}
```

## Database Schema

The database creates a single table called `products` with the following structure:

```sql
CREATE TABLE products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ticker TEXT NOT NULL,
    name TEXT NOT NULL,
    isin TEXT NOT NULL,
    dividendDate TEXT NOT NULL,
    dividendAmount REAL NOT NULL
);
```

## Features

1. **CRUD Operations**: Complete Create, Read, Update, Delete functionality
2. **Reactive UI**: Uses Kotlin Flow for reactive data updates
3. **Type Safety**: Full Kotlin type safety with Room annotations
4. **Date Handling**: Proper LocalDate conversion for database storage
5. **Material Design**: Modern UI with Material Design 3 components
6. **Error Handling**: Basic error handling for invalid input

## Next Steps

Potential improvements for the database implementation:

1. **Data Validation**: Add input validation for ticker, ISIN, and amounts
2. **Search Functionality**: Add search by ticker or company name
3. **Sorting Options**: Add sorting by dividend date, amount, or ticker
4. **Notifications**: Integrate with Android notifications for dividend reminders
5. **Data Import/Export**: Add CSV import/export functionality
6. **Backup/Restore**: Implement database backup and restore features 