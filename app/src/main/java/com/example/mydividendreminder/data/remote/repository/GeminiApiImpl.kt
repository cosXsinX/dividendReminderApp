package com.example.mydividendreminder.data.remote.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GeminiApiImpl(private val apiKey: String) : GeminiApi {
    override suspend fun fetchCompletion(prompt: String): String = withContext(Dispatchers.IO) {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey"
        val url = URL(endpoint)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        val requestBody = """
            { "contents": [{ "parts": [{ "text": "$prompt" }] }] }
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
            throw RuntimeException("Gemini API error: $response")
        }
        val json = JSONObject(response)
        val candidates = json.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return@withContext parts.getJSONObject(0).optString("text", "")
            }
        }
        throw RuntimeException("No completion found in Gemini API response.")
    }
} 