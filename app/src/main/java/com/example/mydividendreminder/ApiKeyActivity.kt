package com.example.mydividendreminder

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import com.example.mydividendreminder.data.database.AppDatabase
import com.example.mydividendreminder.data.repository.ApiKeyRepository
import com.example.mydividendreminder.ui.screen.ApiKeyScreen
import com.example.mydividendreminder.ui.theme.MyDividendReminderTheme
import com.example.mydividendreminder.ui.viewmodel.ApiKeyViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mydividendreminder.R
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding

@OptIn(ExperimentalMaterial3Api::class)
class ApiKeyActivity : FragmentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyDividendReminderTheme {
                Scaffold(
                ) { innerPadding ->
                    val database = AppDatabase.getDatabase(this)
                    val repository = ApiKeyRepository(database.apiKeyDao())
                    val apiKeyViewModel: ApiKeyViewModel = viewModel(
                        factory = ApiKeyViewModel.Factory(repository)
                    )
                    ApiKeyScreen(
                        viewModel = apiKeyViewModel,
                        modifier = Modifier.padding(innerPadding),
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }
} 