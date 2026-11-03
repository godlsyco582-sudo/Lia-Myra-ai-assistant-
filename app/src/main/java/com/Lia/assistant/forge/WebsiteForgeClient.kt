package com.Lia.assistant.forge

import com.Lia.assistant.voice.GeminiTextClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

sealed class ForgeStreamEvent {
    data class Chunk(val text: String) : ForgeStreamEvent()
    data class Finish(val finishReason: String) : ForgeStreamEvent()
    data class Error(val message: String) : ForgeStreamEvent()
}

object WebsiteForgeClient {

    fun streamWebsite(
        apiKey: String,
        prompt: String,
        isEdit: Boolean = false,
        currentHtml: String = ""
    ): Flow<ForgeStreamEvent> = flow {
        if (apiKey.isBlank()) {
            emit(ForgeStreamEvent.Error("API key is missing."))
            return@flow
        }

        val model = GeminiTextClient.resolveModel(apiKey)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent?alt=sse&key=$apiKey"

        val root = JSONObject()

        // System prompt
        val systemText = if (isEdit) {
            ForgeSystemPrompt.forEdit(currentHtml, prompt)
        } else {
            ForgeSystemPrompt.forNewSite(prompt)
        }

        val sysInstObj = JSONObject()
        val sysParts = JSONArray()
        sysParts.put(JSONObject().put("text", systemText))
        sysInstObj.put("parts", sysParts)
        root.put("systemInstruction", sysInstObj)

        val contents = JSONArray()
        val contentObj = JSONObject()
        contentObj.put("role", "user")
        val parts = JSONArray()
        parts.put(JSONObject().put("text", prompt))
        contentObj.put("parts", parts)
        contents.put(contentObj)
        root.put("contents", contents)

        val genConfig = JSONObject()
        if (model.contains("2.5")) {
            val thinkingConfig = JSONObject()
            thinkingConfig.put("thinkingBudget", 0)
            genConfig.put("thinkingConfig", thinkingConfig)
        }
        root.put("generationConfig", genConfig)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = root.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .addHeader("x-goog-api-key", apiKey)
            .post(requestBody)
            .build()

        try {
            val response = GeminiTextClient.okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                emit(ForgeStreamEvent.Error("HTTP ${response.code}: ${response.message}"))
                return@flow
            }

            val body = response.body
            if (body == null) {
                emit(ForgeStreamEvent.Error("Empty response stream"))
                return@flow
            }

            val reader = BufferedReader(InputStreamReader(body.byteStream()))
            var line: String? = reader.readLine()

            while (line != null) {
                val trimmed = line.trim()
                if (trimmed.startsWith("data:")) {
                    val dataJson = trimmed.removePrefix("data:").trim()
                    if (dataJson.isNotEmpty() && dataJson != "[DONE]") {
                        try {
                            val json = JSONObject(dataJson)
                            val candidates = json.optJSONArray("candidates")
                            if (candidates != null && candidates.length() > 0) {
                                val cand = candidates.getJSONObject(0)
                                val content = cand.optJSONObject("content")
                                val cParts = content?.optJSONArray("parts")
                                if (cParts != null && cParts.length() > 0) {
                                    val chunkText = cParts.getJSONObject(0).optString("text", "")
                                    if (chunkText.isNotEmpty()) {
                                        emit(ForgeStreamEvent.Chunk(chunkText))
                                    }
                                }
                                val finishReason = cand.optString("finishReason", "")
                                if (finishReason.isNotEmpty() && finishReason != "STOP") {
                                    emit(ForgeStreamEvent.Finish(finishReason))
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
                line = reader.readLine()
            }
        } catch (e: Exception) {
            emit(ForgeStreamEvent.Error(e.localizedMessage ?: "Streaming interrupted"))
        }
    }.flowOn(Dispatchers.IO)
}
