# CSV Export Feature

## Overview

The CSV export feature allows users to export all their dividend data to a CSV file for external analysis, record keeping, or sharing with other applications.

## Features

### Export Button Location
- **Main Dashboard**: The "Export Dividends" button is located in the main activity's navigation card
- **Access**: Available from the main screen alongside other navigation buttons

### CSV Content
The exported CSV file contains the following columns:
1. **Product Name**: The full name of the company/product
2. **Product Ticker**: The stock ticker symbol
3. **Dividend Date**: The date when the dividend is paid (YYYY-MM-DD format)
4. **Dividend Amount (€)**: The dividend amount in euros with 2 decimal places

### Data Sorting
- All dividends are sorted by date from earliest to latest
- This provides a chronological view of all dividend payments

### File Format
- **Format**: Standard CSV (Comma-Separated Values)
- **Encoding**: UTF-8
- **Delimiter**: Comma (,)
- **Quote Handling**: Properly escaped quotes and commas in text fields

## Usage

### How to Export
1. Open the main dashboard of the app
2. Tap the "Export Dividends" button in the navigation card
3. Choose your preferred sharing method (email, cloud storage, etc.)
4. The CSV file will be shared with the selected application

### Error Handling
The app provides user-friendly error messages for:
- **No dividends**: When there are no dividends to export
- **Export failures**: When the CSV file cannot be created
- **General errors**: When unexpected errors occur during export

## Technical Implementation

### Files Added/Modified

#### New Files
- `app/src/main/java/com/example/mydividendreminder/util/CsvExportUtil.kt`
- `app/src/main/res/xml/file_paths.xml`

#### Modified Files
- `app/src/main/AndroidManifest.xml` - Added FileProvider configuration
- `app/src/main/java/com/example/mydividendreminder/MainActivity.kt` - Added export functionality
- `app/src/main/java/com/example/mydividendreminder/ui/screen/MainDashboardScreen.kt` - Added export button
- `app/src/main/res/values/strings.xml` - Added export-related strings
- `app/src/main/res/values-fr/strings.xml` - Added French translations

### Key Components

#### CsvExportUtil
- **Purpose**: Handles CSV file creation and sharing
- **Features**:
  - Builds CSV content with proper formatting
  - Handles CSV value escaping
  - Creates shareable file URIs
  - Generates share intents

#### FileProvider Configuration
- **Authority**: `com.example.mydividendreminder.fileprovider`
- **Path**: Cache directory for temporary file storage
- **Security**: Properly configured for secure file sharing

#### User Interface
- **Button Location**: Main dashboard navigation card
- **User Feedback**: Toast messages for success/error states
- **Accessibility**: Proper string resources for internationalization

## Security Considerations

### File Access
- Files are stored in the app's cache directory
- FileProvider ensures secure sharing without exposing internal app structure
- Temporary files are automatically cleaned up by the system

### Data Privacy
- Only dividend data is exported (no sensitive user information)
- CSV format is standard and widely supported
- No data is transmitted to external servers

## Future Enhancements

Potential improvements for the CSV export feature:
1. **Filtering Options**: Export dividends by date range or product
2. **Additional Columns**: Include sector information, dividend yield, etc.
3. **Export Formats**: Support for other formats (Excel, JSON, etc.)
4. **Scheduled Exports**: Automatic periodic exports
5. **Custom Filenames**: User-defined file names with timestamps

## Testing

To test the CSV export feature:
1. Add some products and dividends to the app
2. Navigate to the main dashboard
3. Tap "Export Dividends"
4. Verify the CSV file contains correct data
5. Test sharing to different applications (email, cloud storage, etc.)
6. Verify error handling with empty dividend lists 