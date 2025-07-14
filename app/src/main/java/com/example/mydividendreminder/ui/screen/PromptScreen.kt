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

@Composable
fun PromptScreen(
    viewModel: PromptViewModel,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit
) {
    val prompt by viewModel.prompt.collectAsState()
    val provider by viewModel.provider.collectAsState()
    val result by viewModel.result.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(start = 4.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Prompt Playground",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = prompt,
                onValueChange = { viewModel.setPrompt(it) },
                label = { Text("Prompt") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Provider:", modifier = Modifier.padding(end = 8.dp))
                RadioButton(
                    selected = provider == PromptProvider.GEMINI,
                    onClick = { viewModel.setProvider(PromptProvider.GEMINI) }
                )
                Text("Gemini", modifier = Modifier.padding(end = 16.dp))
                RadioButton(
                    selected = provider == PromptProvider.OPENAI,
                    onClick = { viewModel.setProvider(PromptProvider.OPENAI) }
                )
                Text("OpenAI")
            }
            Button(
                onClick = { viewModel.sendPrompt() },
                enabled = !loading && prompt.isNotBlank(),
                modifier = Modifier.align(Alignment.End)
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Send")
                }
            }
            if (error != null) {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }
            if (result.isNotBlank()) {
                Text("Result:", style = MaterialTheme.typography.titleMedium)
                Text(result, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
} 