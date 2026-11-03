package com.Lia.assistant.forge

import android.content.Context
import com.Lia.assistant.data.ApiKeyStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

enum class ForgePhase {
    IDLE,
    BUILDING,
    DONE,
    FAILED,
    CANCELLED
}

data class ForgeMetrics(
    val chars: Int = 0,
    val sections: Int = 0,
    val elapsedSec: Long = 0L,
    val tokensPerSec: Float = 0f
)

data class ForgeUiState(
    val phase: ForgePhase = ForgePhase.IDLE,
    val prompt: String = "",
    val isEdit: Boolean = false,
    val stage: ForgeStage = ForgeStage.INITIALIZING,
    val progress: Float = 0f,
    val metrics: ForgeMetrics = ForgeMetrics(),
    val sections: List<String> = emptyList(),
    val log: List<String> = emptyList(),
    val logSeq: Int = 0,
    val buildId: String = "",
    val file: File? = null,
    val error: String? = null
)

object ForgeController {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var streamingJob: Job? = null

    private val _uiState = MutableStateFlow(ForgeUiState())
    val uiState: StateFlow<ForgeUiState> = _uiState.asStateFlow()

    private val _pendingOpen = MutableStateFlow(false)
    val pendingOpen: StateFlow<Boolean> = _pendingOpen.asStateFlow()

    private val buffer = StringBuilder()

    fun requestOpen() {
        _pendingOpen.value = true
    }

    fun consumePendingOpen() {
        _pendingOpen.value = false
    }

    fun partialHtml(): String = buffer.toString()

    fun start(context: Context, prompt: String, isEdit: Boolean = false): String {
        val apiKey = ApiKeyStore.getKey(context)
        if (apiKey.isBlank()) {
            return "Please enter your Gemini API key in Settings first."
        }

        if (_uiState.value.phase == ForgePhase.BUILDING) {
            return "A website is already being forged right now."
        }

        streamingJob?.cancel()
        buffer.clear()

        val buildId = "forge_${System.currentTimeMillis()}"
        _uiState.value = ForgeUiState(
            phase = ForgePhase.BUILDING,
            prompt = prompt,
            isEdit = isEdit,
            stage = ForgeStage.INITIALIZING,
            progress = 0.05f,
            buildId = buildId,
            log = listOf("Igniting Website Forge core...", "Connecting to Gemini 3D Engine...")
        )
        requestOpen()

        val startTime = System.currentTimeMillis()

        streamingJob = scope.launch(Dispatchers.IO) {
            try {
                var lastEmitTime = 0L
                var tokenCount = 0

                WebsiteForgeClient.streamWebsite(apiKey, prompt, isEdit).collect { event ->
                    when (event) {
                        is ForgeStreamEvent.Chunk -> {
                            buffer.append(event.text)
                            tokenCount++
                            val now = System.currentTimeMillis()

                            if (now - lastEmitTime >= 120) {
                                lastEmitTime = now
                                val currentHtml = buffer.toString()
                                val stage = ForgeStageDetector.detect(currentHtml)
                                val sections = ForgeStageDetector.extractSections(currentHtml)
                                val elapsedSec = (now - startTime) / 1000
                                val tps = if (elapsedSec > 0) tokenCount.toFloat() / elapsedSec else 0f

                                val updatedLogs = _uiState.value.log.toMutableList()
                                if (updatedLogs.size < 5 + sections.size) {
                                    val lastSec = sections.lastOrNull()
                                    if (lastSec != null && !updatedLogs.contains("Constructed section: $lastSec")) {
                                        updatedLogs.add("Constructed section: $lastSec")
                                    }
                                }

                                _uiState.value = _uiState.value.copy(
                                    stage = stage,
                                    progress = stage.progress.coerceAtLeast(_uiState.value.progress),
                                    sections = sections,
                                    log = updatedLogs,
                                    metrics = ForgeMetrics(
                                        chars = currentHtml.length,
                                        sections = sections.size,
                                        elapsedSec = elapsedSec,
                                        tokensPerSec = tps
                                    )
                                )
                            }
                        }

                        is ForgeStreamEvent.Finish -> {
                            // Finish event
                        }

                        is ForgeStreamEvent.Error -> {
                            _uiState.value = _uiState.value.copy(
                                phase = ForgePhase.FAILED,
                                error = event.message
                            )
                        }
                    }
                }

                // Finalize build
                val rawHtml = buffer.toString()
                if (rawHtml.isNotBlank()) {
                    val stripped = ForgeHtml.stripFences(rawHtml)
                    val repaired = ForgeHtml.repairTruncated(stripped)
                    val finalHtml = ForgeHtml.withSafetyNet(repaired)

                    val sitesDir = File(context.filesDir, "Websites").apply { mkdirs() }
                    val siteFile = File(sitesDir, "website_${System.currentTimeMillis()}.html")
                    siteFile.writeText(finalHtml)

                    _uiState.value = _uiState.value.copy(
                        phase = ForgePhase.DONE,
                        stage = ForgeStage.READY,
                        progress = 1.0f,
                        file = siteFile,
                        log = _uiState.value.log + "Website compiled and ready!"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        phase = ForgePhase.FAILED,
                        error = "No HTML received from the forge engine."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    phase = ForgePhase.FAILED,
                    error = e.localizedMessage ?: "Unexpected Forge failure"
                )
            }
        }

        return "Starting Website Forge for $prompt"
    }

    fun edit(context: Context, change: String) {
        val current = _uiState.value.file?.readText().orEmpty().ifBlank { buffer.toString() }
        start(context, change, isEdit = true)
    }

    fun retry(context: Context) {
        val p = _uiState.value.prompt
        if (p.isNotBlank()) {
            start(context, p, _uiState.value.isEdit)
        }
    }

    fun cancel() {
        streamingJob?.cancel()
        streamingJob = null
        _uiState.value = _uiState.value.copy(
            phase = ForgePhase.CANCELLED,
            log = _uiState.value.log + "Build cancelled by user."
        )
    }

    fun reset() {
        streamingJob?.cancel()
        streamingJob = null
        buffer.clear()
        _uiState.value = ForgeUiState()
    }

    fun openSaved(file: File) {
        if (!file.exists()) return
        buffer.clear()
        buffer.append(file.readText())
        _uiState.value = ForgeUiState(
            phase = ForgePhase.DONE,
            prompt = file.nameWithoutExtension,
            stage = ForgeStage.READY,
            progress = 1f,
            file = file
        )
        requestOpen()
    }
}
