package com.Lia.assistant.voice

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class ChatTurn(
    val isUser: Boolean,
    val text: String
)

sealed class ChatReplyResult {
    data class Success(val text: String) : ChatReplyResult()
    data class Error(val message: String) : ChatReplyResult()
}

object GeminiTextClient {
    internal val okHttpClient = OkHttpClient.Builder()
        .readTimeout(180, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    @Volatile
    internal var cachedModel: String? = null

    // Modern supported models in strict priority order
    val SUPPORTED_CHAT_MODELS = listOf(
        "gemini-3.8-flash",
        "gemini-3.6-flash",
        "gemini-3.5-flash",
        "gemini-3.1-flash-lite-preview",
        "gemini-flash-latest"
    )

    suspend fun resolveModel(apiKey: String, preferredModel: String? = null): String = withContext(Dispatchers.IO) {
        if (!preferredModel.isNullOrBlank() && preferredModel in SUPPORTED_CHAT_MODELS) {
            cachedModel = preferredModel
            return@withContext preferredModel
        }

        cachedModel?.let { return@withContext it }

        if (apiKey.isBlank()) {
            return@withContext "gemini-3.8-flash"
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
            val request = Request.Builder().url(url).get().build()
            val response = okHttpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)
                val models = json.optJSONArray("models") ?: JSONArray()

                val availableNames = mutableSetOf<String>()
                for (i in 0 until models.length()) {
                    val m = models.getJSONObject(i)
                    val name = m.optString("name", "").removePrefix("models/")
                    val methods = m.optJSONArray("supportedGenerationMethods") ?: JSONArray()
                    var canGenerate = false
                    for (j in 0 until methods.length()) {
                        if (methods.optString(j) == "generateContent") {
                            canGenerate = true
                            break
                        }
                    }
                    if (canGenerate) {
                        availableNames.add(name)
                    }
                }

                // Pick highest priority supported model available
                for (candidate in SUPPORTED_CHAT_MODELS) {
                    if (availableNames.contains(candidate)) {
                        cachedModel = candidate
                        return@withContext candidate
                    }
                }

                // If specific names not found, filter out obsolete models
                for (name in availableNames) {
                    val lower = name.lowercase()
                    if (lower.contains("flash") &&
                        !lower.contains("2.5-flash") &&
                        !lower.contains("2.0") &&
                        !lower.contains("1.5") &&
                        !lower.contains("image") &&
                        !lower.contains("tts") &&
                        !lower.contains("live")
                    ) {
                        cachedModel = name
                        return@withContext name
                    }
                }
            }
        } catch (_: Exception) {}

        val fallback = "gemini-3.8-flash"
        cachedModel = fallback
        return@withContext fallback
    }

    suspend fun reply(
        apiKey: String,
        systemInstruction: String,
        history: List<ChatTurn>,
        configuredModel: String? = null
    ): ChatReplyResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext ChatReplyResult.Error("Gemini API key is missing. Please configure it in Settings.")
        }

        if (history.isEmpty()) {
            return@withContext ChatReplyResult.Error("Cannot send an empty message history.")
        }

        val primaryModel = configuredModel ?: resolveModel(apiKey)
        val candidateModels = listOf(primaryModel) + SUPPORTED_CHAT_MODELS.filter { it != primaryModel }

        var lastErrorMsg = "Unknown error"

        for (model in candidateModels) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

                val root = JSONObject()

                if (systemInstruction.isNotBlank()) {
                    val sysInstObj = JSONObject()
                    val parts = JSONArray()
                    val part = JSONObject()
                    part.put("text", systemInstruction)
                    parts.put(part)
                    sysInstObj.put("parts", parts)
                    root.put("systemInstruction", sysInstObj)
                }

                val contents = JSONArray()
                for (turn in history) {
                    val contentObj = JSONObject()
                    contentObj.put("role", if (turn.isUser) "user" else "model")
                    val parts = JSONArray()
                    val part = JSONObject()
                    part.put("text", turn.text)
                    parts.put(part)
                    contentObj.put("parts", parts)
                    contents.put(contentObj)
                }
                root.put("contents", contents)

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = root.toString().toRequestBody(mediaType)
                val request = Request.Builder().url(url).post(requestBody).build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    val errorMsg = try {
                        val errorJson = JSONObject(responseBody).optJSONObject("error")
                        errorJson?.optString("message") ?: "HTTP ${response.code}"
                    } catch (_: Exception) {
                        "HTTP ${response.code}"
                    }
                    lastErrorMsg = errorMsg
                    // If model is discontinued or not found, try next candidate model!
                    if (errorMsg.contains("no longer available") ||
                        errorMsg.contains("not found") ||
                        response.code == 404
                    ) {
                        continue
                    }
                    return@withContext ChatReplyResult.Error("Gemini Error: $errorMsg")
                }

                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    lastErrorMsg = "No response candidate returned by Gemini."
                    continue
                }

                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text", "").orEmpty()

                if (text.isNotBlank()) {
                    cachedModel = model
                    return@withContext ChatReplyResult.Success(text)
                }
            } catch (e: IOException) {
                lastErrorMsg = "Couldn't reach Gemini. Please check your internet connection."
            } catch (e: Exception) {
                lastErrorMsg = e.localizedMessage ?: "Unexpected error"
            }
        }

        return@withContext ChatReplyResult.Error("Gemini Error: $lastErrorMsg")
    }
}
