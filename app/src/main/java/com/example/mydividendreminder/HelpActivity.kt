package com.example.mydividendreminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mydividendreminder.ui.theme.MyDividendReminderTheme
import com.example.mydividendreminder.util.NavigationHelper
import com.example.mydividendreminder.ui.theme.DefaultMainAppBar
import android.content.Intent

class HelpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyDividendReminderTheme {
                // Initialize navigation helper
                val navigationHelper = NavigationHelper(this@HelpActivity)
                
                HelpScreen(
                    navigationHelper = navigationHelper
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    navigationHelper: com.example.mydividendreminder.util.NavigationHelper
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DefaultMainAppBar(
            navigationHelper = navigationHelper
        )
        
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Navigation", style = MaterialTheme.typography.titleLarge)
            HelpItem(icon = Icons.Default.Help, label = "Help", description = "Open this help screen to learn about the app's features and navigation.")
            HelpItem(icon = Icons.Filled.List, label = "Products", description = "Navigate to the Products screen to view, add, or edit your investment products.")
            HelpItem(icon = Icons.Filled.Category, label = "Sectors", description = "Go to the Sectors screen to organize your products by sector.")
            Spacer(modifier = Modifier.height(16.dp))
            Text("Data Management", style = MaterialTheme.typography.titleLarge)
            HelpItem(icon = Icons.Filled.Add, label = "Add Dividend", description = "Quickly add a new dividend entry for any of your products.")
        }
    }
}

@Composable
fun HelpItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, description: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
} 