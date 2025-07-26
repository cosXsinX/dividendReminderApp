package com.example.mydividendreminder.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.mydividendreminder.ui.viewmodel.PromptViewModel
import com.example.mydividendreminder.ui.viewmodel.PromptProvider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.example.mydividendreminder.ui.theme.DefaultMainAppBar

@Composable
fun PromptScreen(
    viewModel: PromptViewModel,
    navigationHelper: com.example.mydividendreminder.util.NavigationHelper,
    modifier: Modifier = Modifier
) {
    val prompt by viewModel.prompt.collectAsState()
    val provider by viewModel.provider.collectAsState()
    val result by viewModel.result.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DefaultMainAppBar(
            navigationHelper = navigationHelper
        )
        
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Provider Selection
            ProviderSelection(
                selectedProvider = provider,
                onProviderSelected = { viewModel.setProvider(it) }
            )
            
            // Prompt Input
            PromptInput(
                prompt = prompt,
                onPromptChange = { viewModel.setPrompt(it) }
            )
            
            // Submit Button
            Button(
                onClick = { viewModel.sendPrompt() },
                enabled = prompt.isNotBlank() && !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Submit")
            }
            
            // Error Display
            val errorMessage = error
            if (errorMessage != null && errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Result Display
            if (result.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = result,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
} 

@Composable
fun ProviderSelection(
    selectedProvider: PromptProvider,
    onProviderSelected: (PromptProvider) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Provider:", modifier = Modifier.padding(end = 8.dp))
        RadioButton(
            selected = selectedProvider == PromptProvider.GEMINI,
            onClick = { onProviderSelected(PromptProvider.GEMINI) }
        )
        Text("Gemini", modifier = Modifier.padding(end = 16.dp))
        RadioButton(
            selected = selectedProvider == PromptProvider.OPENAI,
            onClick = { onProviderSelected(PromptProvider.OPENAI) }
        )
        Text("OpenAI")
    }
}

@Composable
fun PromptInput(
    prompt: String,
    onPromptChange: (String) -> Unit
) {
    OutlinedTextField(
        value = prompt,
        onValueChange = onPromptChange,
        label = { Text("Prompt") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 8,
        singleLine = false
    )
} 