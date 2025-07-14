package com.example.mydividendreminder.data.remote.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class OpenAiApiImpl(private val apiKey: String) : OpenAiApi {
    override suspend fun fetchCompletion(prompt: String, model: String): String = withContext(Dispatchers.IO) {
        val endpoint = "https://api.openai.com/v1/completions"
        val url = URL(endpoint)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        connection.doOutput = true
        val requestBody = """
            { "model": "$model", "prompt": "$prompt", "max_tokens": 100 }
        """.trimIndent()
        connection.outputStream.use { os ->
            BufferedOutputStream(os).use { bos ->
                bos.write(requestBody.toByteArray())
                bos.flush()
            }
        }
        val responseCode = connection.responseCode
        val response = if (responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream.bufferedReader().use(BufferedReader::readText)
        } else {
            connection.errorStream?.bufferedReader()?.use(BufferedReader::readText) ?: ""
        }
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw RuntimeException("OpenAI API error: $response")
        }
        val json = JSONObject(response)
        val choices = json.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
            return@withContext choices.getJSONObject(0).optString("text", "")
        }
        throw RuntimeException("No completion found in OpenAI API response.")
    }
} 