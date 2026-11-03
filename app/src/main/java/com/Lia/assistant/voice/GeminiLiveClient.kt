package com.Lia.assistant.voice

import android.util.Base64
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class GeminiLiveClient(private val listener: Listener) {

    interface Listener {
        fun onSetupComplete()
        fun onAudioChunk(bytes: ByteArray)
        fun onInputTranscript(text: String)
        fun onOutputTranscript(text: String)
        fun onToolCall(id: String, name: String, args: JSONObject)
        fun onTurnComplete()
        fun onInterrupted()
        fun onError(message: String)
        fun onClosed()
    }

    companion object {
        const val TAG = "GeminiLiveClient"

        // Candidate real-time live voice models in priority order
        val CANDIDATE_LIVE_MODELS = listOf(
            "models/gemini-3.1-flash-live-preview",
            "models/gemini-2.5-flash-native-audio-preview-12-2025",
            "models/gemini-3.8-flash",
            "models/gemini-2.0-flash-realtime-exp"
        )

        private const val WS_BASE = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent"
    }

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .connectTimeout(25, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val isConnected = AtomicBoolean(false)
    private var setupSucceeded = false
    val deduplicator = ToolCallDeduplicator(100)

    private var activeApiKey: String = ""
    private var activeSystemInstruction: String = ""
    private var activeVoiceName: String = "Aoede"
    private var currentModelIndex = 0
    private var activeCandidateModels: List<String> = CANDIDATE_LIVE_MODELS

    fun connect(
        apiKey: String,
        systemInstruction: String,
        voiceName: String = "Aoede",
        preferredModel: String? = null
    ) {
        disconnect()
        deduplicator.clear()

        if (apiKey.isBlank()) {
            listener.onError("Missing Gemini API Key. Please configure it in Settings.")
            return
        }

        activeApiKey = apiKey
        activeSystemInstruction = systemInstruction
        activeVoiceName = voiceName
        setupSucceeded = false

        activeCandidateModels = if (!preferredModel.isNullOrBlank()) {
            listOf(preferredModel) + CANDIDATE_LIVE_MODELS.filter { it != preferredModel }
        } else {
            CANDIDATE_LIVE_MODELS
        }
        currentModelIndex = 0

        connectToModel(activeCandidateModels[currentModelIndex])
    }

    private fun connectToModel(modelName: String) {
        val url = "$WS_BASE?key=$activeApiKey"
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                isConnected.set(true)
                sendSetup(ws, modelName, activeSystemInstruction, activeVoiceName)
            }

            override fun onMessage(ws: WebSocket, text: String) {
                handleServerMessage(text)
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                handleServerMessage(bytes.utf8())
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                isConnected.set(false)
                val code = response?.code ?: 0
                val body = try { response?.body?.string() } catch (_: Exception) { null }
                val errorDetails = if (!body.isNullOrBlank()) " ($code: $body)" else " (${t.localizedMessage ?: "error $code"})"
                Log.w(TAG, "WebSocket failure on model $modelName: $errorDetails")

                if (!setupSucceeded && tryNextModel()) {
                    return
                }

                listener.onError("Live Voice Connection: ${t.localizedMessage ?: "Failed with code $code"}")
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                isConnected.set(false)
                if (!setupSucceeded && tryNextModel()) {
                    return
                }
                listener.onClosed()
            }
        })
    }

    private fun tryNextModel(): Boolean {
        currentModelIndex++
        if (currentModelIndex < activeCandidateModels.size) {
            val nextModel = activeCandidateModels[currentModelIndex]
            Log.i(TAG, "Switching to next candidate live model: $nextModel")
            webSocket?.close(1000, "Switching model")
            webSocket = null
            connectToModel(nextModel)
            return true
        }
        return false
    }

    private fun sendSetup(ws: WebSocket, modelName: String, systemInstruction: String, voiceName: String) {
        try {
            val setupObj = JSONObject()
            val setupInner = JSONObject()
            setupInner.put("model", modelName)

            val genConfig = JSONObject()
            val responseModalities = JSONArray()
            responseModalities.put("AUDIO")
            genConfig.put("responseModalities", responseModalities)

            val speechConfig = JSONObject()
            val voiceConfig = JSONObject()
            val prebuilt = JSONObject()
            prebuilt.put("voiceName", voiceName)
            voiceConfig.put("prebuiltVoiceConfig", prebuilt)
            speechConfig.put("voiceConfig", voiceConfig)
            genConfig.put("speechConfig", speechConfig)

            setupInner.put("generationConfig", genConfig)

            val sysInstObj = JSONObject()
            val partsArray = JSONArray()
            val partText = JSONObject()
            partText.put("text", systemInstruction)
            partsArray.put(partText)
            sysInstObj.put("parts", partsArray)
            setupInner.put("systemInstruction", sysInstObj)

            // Function declarations from catalog
            val toolsArray = JSONArray()
            val toolDecl = JSONObject()
            toolDecl.put("functionDeclarations", LiaToolCatalog.declarations())
            toolsArray.put(toolDecl)
            setupInner.put("tools", toolsArray)

            setupObj.put("setup", setupInner)

            ws.send(setupObj.toString())
        } catch (e: Exception) {
            listener.onError("Setup configuration failed: ${e.message}")
        }
    }

    fun sendAudio(pcmBytes: ByteArray) {
        val ws = webSocket ?: return
        if (!isConnected.get()) return
        try {
            val base64 = Base64.encodeToString(pcmBytes, Base64.NO_WRAP)
            val root = JSONObject()
            val realtimeInput = JSONObject()
            val mediaChunks = JSONArray()
            val chunk = JSONObject()
            chunk.put("mimeType", "audio/pcm;rate=16000")
            chunk.put("data", base64)
            mediaChunks.put(chunk)

            realtimeInput.put("mediaChunks", mediaChunks)
            root.put("realtimeInput", realtimeInput)

            ws.send(root.toString())
        } catch (e: Exception) {
            Log.w(TAG, "Audio send failed: ${e.message}")
        }
    }

    // Sends turnComplete signal so Gemini immediately begins speaking back
    fun commitTurn() {
        val ws = webSocket ?: return
        if (!isConnected.get()) return
        try {
            val root = JSONObject()
            val clientContent = JSONObject()
            clientContent.put("turnComplete", true)
            root.put("clientContent", clientContent)
            ws.send(root.toString())
            Log.d(TAG, "Sent turnComplete commit signal to Gemini Live API")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to commit turn: ${e.message}")
        }
    }

    fun sendToolResponse(id: String, name: String, resultJson: JSONObject) {
        val ws = webSocket ?: return
        if (!isConnected.get()) return
        try {
            val root = JSONObject()
            val toolResponse = JSONObject()
            val functionResponses = JSONArray()
            val resp = JSONObject()
            resp.put("id", id)
            resp.put("name", name)
            resp.put("response", resultJson)
            functionResponses.put(resp)
            toolResponse.put("functionResponses", functionResponses)
            root.put("toolResponse", toolResponse)
            ws.send(root.toString())
        } catch (_: Exception) {
        }
    }

    fun disconnect() {
        try {
            isConnected.set(false)
            setupSucceeded = false
            webSocket?.close(1000, "Normal closure")
            webSocket = null
        } catch (_: Exception) {
        }
    }

    private fun handleServerMessage(jsonText: String) {
        try {
            val json = JSONObject(jsonText)

            // Handle server errors
            if (json.has("error")) {
                val errObj = json.getJSONObject("error")
                val errMsg = errObj.optString("message", "Live API Error")
                Log.w(TAG, "Server error frame: $errMsg")
                if (!setupSucceeded && tryNextModel()) {
                    return
                }
                listener.onError("Live API: $errMsg")
                return
            }

            if (json.has("setupComplete")) {
                setupSucceeded = true
                Log.i(TAG, "Live session established with model ${activeCandidateModels[currentModelIndex]}")
                listener.onSetupComplete()
                return
            }

            if (json.has("toolCall")) {
                val toolCall = json.getJSONObject("toolCall")
                if (toolCall.has("functionCalls")) {
                    val calls = toolCall.getJSONArray("functionCalls")
                    for (i in 0 until calls.length()) {
                        val call = calls.getJSONObject(i)
                        val id = call.optString("id", "")
                        val name = call.optString("name", "")
                        val args = call.optJSONObject("args") ?: JSONObject()
                        if (!deduplicator.isDuplicateAndRecord(id)) {
                            listener.onToolCall(id, name, args)
                        }
                    }
                }
            }

            if (json.has("serverContent")) {
                val serverContent = json.getJSONObject("serverContent")

                if (serverContent.optBoolean("interrupted", false)) {
                    listener.onInterrupted()
                }

                if (serverContent.has("modelTurn")) {
                    val modelTurn = serverContent.getJSONObject("modelTurn")
                    if (modelTurn.has("parts")) {
                        val parts = modelTurn.getJSONArray("parts")
                        for (i in 0 until parts.length()) {
                            val part = parts.getJSONObject(i)
                            if (part.has("inlineData")) {
                                val inlineData = part.getJSONObject("inlineData")
                                val data = inlineData.optString("data", "")
                                if (data.isNotEmpty()) {
                                    val bytes = Base64.decode(data, Base64.DEFAULT)
                                    listener.onAudioChunk(bytes)
                                }
                            }
                            val textPart = part.optString("text", "")
                            if (textPart.isNotBlank()) {
                                listener.onOutputTranscript(textPart)
                            }
                        }
                    }
                }

                if (serverContent.has("inputTranscription")) {
                    val text = serverContent.getJSONObject("inputTranscription").optString("text", "")
                    if (text.isNotBlank()) {
                        listener.onInputTranscript(text)
                    }
                }

                if (serverContent.has("outputTranscription")) {
                    val text = serverContent.getJSONObject("outputTranscription").optString("text", "")
                    if (text.isNotBlank()) {
                        listener.onOutputTranscript(text)
                    }
                }

                if (serverContent.optBoolean("turnComplete", false)) {
                    listener.onTurnComplete()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling server message", e)
        }
    }
}
