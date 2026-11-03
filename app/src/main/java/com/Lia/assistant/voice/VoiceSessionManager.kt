package com.Lia.assistant.voice

import android.content.Context
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import android.util.Log
import com.Lia.assistant.action.ActionExecutor
import com.Lia.assistant.action.InstalledAppLabelCache
import com.Lia.assistant.data.ApiKeyStore
import com.Lia.assistant.data.LanguagePreference
import com.Lia.assistant.data.NovaPreferences
import com.Lia.assistant.data.Personality
import com.Lia.assistant.data.PersonalityRepository
import com.Lia.assistant.forge.ForgeController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

object VoiceSessionManager {
    private const val TAG = "VoiceSessionManager"
    private val scope = CoroutineScope(Dispatchers.Main)
    private val connectMutex = Mutex()
    private val sessionEpoch = AtomicInteger(0)

    private val _state = MutableStateFlow(NovaOrbState.IDLE)
    val state: StateFlow<NovaOrbState> = _state.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    private val _muted = MutableStateFlow(false)
    val muted: StateFlow<Boolean> = _muted.asStateFlow()

    private val _statusText = MutableStateFlow("Ready")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _currentPersonality = MutableStateFlow(Personality.NORMAL)
    val currentPersonality: StateFlow<Personality> = _currentPersonality.asStateFlow()

    private val _currentLanguage = MutableStateFlow(LanguagePreference.AUTO)
    val currentLanguage: StateFlow<LanguagePreference> = _currentLanguage.asStateFlow()

    private val _detectedLanguage = MutableStateFlow(DetectedLanguage.UNKNOWN)
    val detectedLanguage: StateFlow<DetectedLanguage> = _detectedLanguage.asStateFlow()

    private val _vadActive = MutableStateFlow(false)
    val vadActive: StateFlow<Boolean> = _vadActive.asStateFlow()

    val memory = ConversationMemory(12)
    val latencyTracker = VoiceLatencyTracker()

    private var audioIO: LiveAudioIO? = null
    private var liveClient: GeminiLiveClient? = null
    private var appContext: Context? = null
    private var tts: TextToSpeech? = null
    private var receivedAudioChunkInTurn = false

    // VAD speech & commit tracking
    @Volatile private var userWasSpeaking = false
    @Volatile private var silenceStartTime = 0L
    private var vadJob: Job? = null

    private var reconnectJob: Job? = null
    private val reconnectAttempts = AtomicInteger(0)
    private var playbackWatchdogJob: Job? = null

    fun start(context: Context) {
        if (_isActive.value) return

        appContext = context.applicationContext
        _isActive.value = true
        _lastError.value = null
        _statusText.value = "Connecting to Gemini Voice..."
        _state.value = NovaOrbState.CONNECTING

        memory.clear()
        latencyTracker.reset()
        EchoGuard.reset()
        reconnectAttempts.set(0)
        receivedAudioChunkInTurn = false
        userWasSpeaking = false
        silenceStartTime = 0L

        InstalledAppLabelCache.prewarm(context)

        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                try {
                    val langName = NovaPreferences.getString(context, NovaPreferences.Keys.LANGUAGE, LanguagePreference.AUTO.name)
                    val langPref = LanguagePreference.fromName(langName)
                    val loc = if (langPref == LanguagePreference.ENGLISH) Locale.US else Locale("hi", "IN")
                    val result = tts?.setLanguage(loc)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.language = Locale.getDefault()
                    }
                    val attrs = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                    tts?.setAudioAttributes(attrs)
                } catch (e: Exception) {
                    Log.w(TAG, "TTS init error: ${e.message}")
                }
            }
        }

        audioIO = LiveAudioIO(
            context = context.applicationContext,
            onMicChunk = { pcmBytes ->
                // Mic is always sent when active unless user explicitly pressed mute button or assistant is actively speaking
                if (_isActive.value && !_muted.value && _state.value != NovaOrbState.SPEAKING) {
                    liveClient?.sendAudio(pcmBytes)
                }
            },
            onMicAmplitude = { amp ->
                if (_state.value == NovaOrbState.LISTENING) {
                    _amplitude.value = amp
                    val isSpeech = amp > 0.045f
                    _vadActive.value = isSpeech

                    if (isSpeech) {
                        userWasSpeaking = true
                        silenceStartTime = 0L
                    } else if (userWasSpeaking) {
                        val now = System.currentTimeMillis()
                        if (silenceStartTime == 0L) {
                            silenceStartTime = now
                        } else if (now - silenceStartTime > 550) {
                            // User finished speaking, send turnComplete commit signal!
                            userWasSpeaking = false
                            silenceStartTime = 0L
                            scope.launch {
                                _state.value = NovaOrbState.THINKING
                                val name = PersonalityRepository.getAssistantName(context)
                                _statusText.value = "$name is thinking..."
                                liveClient?.commitTurn()
                            }
                        }
                    }
                }
            }
        )

        scope.launch(Dispatchers.IO) {
            connectSession(context, isResuming = false)
        }
    }

    private suspend fun connectSession(context: Context, isResuming: Boolean) = connectMutex.withLock {
        val currentEpoch = sessionEpoch.incrementAndGet()

        liveClient?.disconnect()
        liveClient = null

        val apiKey = ApiKeyStore.getKey(context)
        if (apiKey.isBlank()) {
            _state.value = NovaOrbState.ERROR
            _lastError.value = "Gemini API Key missing in Settings"
            _statusText.value = "API Key Missing"
            return@withLock
        }

        val personality = PersonalityRepository.getPersonality(context)
        val languagePref = PersonalityRepository.getLanguage(context)
        val assistantName = PersonalityRepository.getAssistantName(context)
        val voiceName = PersonalityRepository.getVoiceName(context)
        val liveModel = NovaPreferences.getString(
            context,
            NovaPreferences.Keys.LIVE_MODEL,
            "models/gemini-3.1-flash-live-preview"
        )

        _currentPersonality.value = personality
        _currentLanguage.value = languagePref

        val recap = if (isResuming) memory.recap() else null
        val prompt = PersonalityPromptBuilder.build(
            personality = personality,
            languagePreference = languagePref,
            detectedLanguage = _detectedLanguage.value,
            recap = recap,
            assistantName = assistantName
        )

        val client = GeminiLiveClient(object : GeminiLiveClient.Listener {
            override fun onSetupComplete() {
                if (sessionEpoch.get() != currentEpoch) return
                scope.launch {
                    _state.value = NovaOrbState.LISTENING
                    _statusText.value = "$assistantName is listening..."
                    _lastError.value = null
                    reconnectAttempts.set(0)
                    userWasSpeaking = false
                    silenceStartTime = 0L
                    audioIO?.startRecording()
                }
            }

            override fun onAudioChunk(bytes: ByteArray) {
                if (sessionEpoch.get() != currentEpoch) return
                scope.launch {
                    receivedAudioChunkInTurn = true
                    if (_state.value != NovaOrbState.SPEAKING) {
                        EchoGuard.notifyPlaybackStarted()
                        _state.value = NovaOrbState.SPEAKING
                        _statusText.value = "$assistantName is speaking..."
                        latencyTracker.markFirstServerAudio()
                    }

                    val amp = audioIO?.playChunk(bytes) ?: 0f
                    _amplitude.value = amp

                    // Watchdog for end of utterance
                    playbackWatchdogJob?.cancel()
                    playbackWatchdogJob = scope.launch {
                        delay(400)
                        if (_state.value == NovaOrbState.SPEAKING) {
                            EchoGuard.notifyPlaybackStopped()
                            _state.value = NovaOrbState.LISTENING
                            _statusText.value = "$assistantName is listening..."
                            _amplitude.value = 0f
                            userWasSpeaking = false
                            silenceStartTime = 0L
                        }
                    }
                }
            }

            override fun onInputTranscript(text: String) {
                if (sessionEpoch.get() != currentEpoch) return
                scope.launch {
                    memory.addTurn(MemoryRole.USER, text)
                    val lang = LanguageDetector.detect(text)
                    if (lang != DetectedLanguage.UNKNOWN) {
                        _detectedLanguage.value = lang
                    }
                    latencyTracker.markTurnStart()
                    _state.value = NovaOrbState.THINKING
                    _statusText.value = "$assistantName is thinking..."
                }
            }

            override fun onOutputTranscript(text: String) {
                if (sessionEpoch.get() != currentEpoch) return
                scope.launch {
                    memory.addTurn(MemoryRole.ASSISTANT, text)

                    // If raw PCM audio was not received, speak via Android TTS immediately
                    if (!receivedAudioChunkInTurn && text.isNotBlank()) {
                        EchoGuard.notifyPlaybackStarted()
                        _state.value = NovaOrbState.SPEAKING
                        _statusText.value = "$assistantName is speaking..."
                        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "lia_voice_turn")

                        playbackWatchdogJob?.cancel()
                        playbackWatchdogJob = scope.launch {
                            delay(2500)
                            if (_state.value == NovaOrbState.SPEAKING) {
                                EchoGuard.notifyPlaybackStopped()
                                _state.value = NovaOrbState.LISTENING
                                _statusText.value = "$assistantName is listening..."
                                _amplitude.value = 0f
                            }
                        }
                    }
                }
            }

            override fun onToolCall(id: String, name: String, args: JSONObject) {
                if (sessionEpoch.get() != currentEpoch) return
                scope.launch {
                    latencyTracker.markToolStart()
                    _state.value = NovaOrbState.THINKING
                    _statusText.value = "Executing $name..."

                    if (name == "build_website") {
                        val sitePrompt = args.optString("prompt", "personal website")
                        appContext?.let { ForgeController.start(it, sitePrompt) }
                        val immediateResp = JSONObject().apply {
                            put("result", "forge_started")
                            put("status", "Website Forge opened and is generating the 3D site")
                        }
                        liveClient?.sendToolResponse(id, name, immediateResp)
                        latencyTracker.markToolEnd()
                        return@launch
                    }

                    val result = appContext?.let { ctx ->
                        ActionExecutor.execute(ctx, name, args)
                    } ?: JSONObject().put("error", "No context")

                    latencyTracker.markToolEnd()
                    if (sessionEpoch.get() == currentEpoch) {
                        liveClient?.sendToolResponse(id, name, result)
                    }
                }
            }

            override fun onTurnComplete() {
                if (sessionEpoch.get() != currentEpoch) return
                scope.launch {
                    playbackWatchdogJob?.cancel()
                    EchoGuard.notifyPlaybackStopped()
                    _state.value = NovaOrbState.LISTENING
                    _statusText.value = "$assistantName is listening..."
                    _amplitude.value = 0f
                    receivedAudioChunkInTurn = false
                    userWasSpeaking = false
                    silenceStartTime = 0L
                    latencyTracker.markTurnEnd()
                }
            }

            override fun onInterrupted() {
                if (sessionEpoch.get() != currentEpoch) return
                scope.launch {
                    tts?.stop()
                    audioIO?.flushPlayback()
                    EchoGuard.notifyPlaybackStopped()
                    _state.value = NovaOrbState.LISTENING
                    _statusText.value = "$assistantName is listening..."
                    _amplitude.value = 0f
                    receivedAudioChunkInTurn = false
                    userWasSpeaking = false
                    silenceStartTime = 0L
                }
            }

            override fun onError(message: String) {
                if (sessionEpoch.get() != currentEpoch) return
                scope.launch {
                    Log.w(TAG, "LiveClient Error: $message")
                    _lastError.value = message
                    _statusText.value = message
                    if (_isActive.value) {
                        scheduleReconnect(context)
                    }
                }
            }

            override fun onClosed() {
                if (sessionEpoch.get() != currentEpoch) return
                scope.launch {
                    if (_isActive.value) {
                        scheduleReconnect(context)
                    }
                }
            }
        })

        liveClient = client
        client.connect(
            apiKey = apiKey,
            systemInstruction = prompt,
            voiceName = voiceName,
            preferredModel = liveModel
        )
    }

    private fun scheduleReconnect(context: Context) {
        if (reconnectJob?.isActive == true) return

        val attempts = reconnectAttempts.incrementAndGet()
        latencyTracker.incrementReconnect()

        if (attempts > 5) {
            _state.value = NovaOrbState.ERROR
            val lastErr = _lastError.value ?: "Connection lost"
            _lastError.value = "$lastErr — tap Stop and reopen Voice Mode"
            _statusText.value = "Voice Error: $lastErr"
            return
        }

        _state.value = NovaOrbState.CONNECTING
        _statusText.value = "Connecting (${attempts}/5)..."

        reconnectJob = scope.launch(Dispatchers.IO) {
            delay(1000)
            connectSession(context, isResuming = true)
        }
    }

    fun refreshInstructions(context: Context) {
        if (!_isActive.value) return
        scope.launch(Dispatchers.IO) {
            connectSession(context, isResuming = true)
        }
    }

    fun toggleMute() {
        _muted.value = !_muted.value
    }

    fun stop() {
        sessionEpoch.incrementAndGet()
        reconnectJob?.cancel()
        reconnectJob = null
        playbackWatchdogJob?.cancel()
        playbackWatchdogJob = null
        vadJob?.cancel()
        vadJob = null

        liveClient?.disconnect()
        liveClient = null

        tts?.stop()
        tts?.shutdown()
        tts = null

        audioIO?.release()
        audioIO = null

        EchoGuard.reset()
        memory.clear()

        _isActive.value = false
        _state.value = NovaOrbState.IDLE
        _statusText.value = "Ready"
        _amplitude.value = 0f
        _vadActive.value = false
        receivedAudioChunkInTurn = false
        userWasSpeaking = false
        silenceStartTime = 0L
    }
}
