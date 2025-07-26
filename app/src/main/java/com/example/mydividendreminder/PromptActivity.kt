package com.example.mydividendreminder

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.example.mydividendreminder.data.database.AppDatabase
import com.example.mydividendreminder.data.repository.ApiKeyRepository
import com.example.mydividendreminder.ui.screen.PromptScreen
import com.example.mydividendreminder.ui.theme.MyDividendReminderTheme
import com.example.mydividendreminder.util.NavigationHelper
import com.example.mydividendreminder.ui.viewmodel.PromptViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Intent

class PromptActivity : FragmentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyDividendReminderTheme {
                val database = AppDatabase.getDatabase(this)
                val repository = ApiKeyRepository(database.apiKeyDao())
                val promptViewModel: PromptViewModel = viewModel(
                    factory = PromptViewModel.Factory(repository)
                )
                
                // Initialize navigation helper
                val navigationHelper = NavigationHelper(this@PromptActivity)
                
                PromptScreen(
                    viewModel = promptViewModel,
                    navigationHelper = navigationHelper
                )
            }
        }
    }
} 