package com.Lia.assistant.voice

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VoiceLatencyTracker {
    private val _reconnectCount = MutableStateFlow(0)
    val reconnectCount: StateFlow<Int> = _reconnectCount.asStateFlow()

    private var turnStartTime: Long = 0L
    private var firstServerAudioTime: Long = 0L
    private var toolStartTime: Long = 0L
    private var toolEndTime: Long = 0L

    var lastTurnDurationMs: Long = 0L
        private set
    var lastServerLatencyMs: Long = 0L
        private set
    var lastToolExecutionMs: Long = 0L
        private set

    @Synchronized
    fun markTurnStart(now: Long = System.currentTimeMillis()) {
        turnStartTime = now
        firstServerAudioTime = 0L
    }

    @Synchronized
    fun markFirstServerAudio(now: Long = System.currentTimeMillis()) {
        if (turnStartTime > 0 && now >= turnStartTime && firstServerAudioTime == 0L) {
            firstServerAudioTime = now
            lastServerLatencyMs = now - turnStartTime
        }
    }

    @Synchronized
    fun markTurnEnd(now: Long = System.currentTimeMillis()) {
        if (turnStartTime > 0 && now >= turnStartTime) {
            lastTurnDurationMs = now - turnStartTime
        }
    }

    @Synchronized
    fun markToolStart(now: Long = System.currentTimeMillis()) {
        toolStartTime = now
        toolEndTime = 0L
    }

    @Synchronized
    fun markToolEnd(now: Long = System.currentTimeMillis()) {
        if (toolStartTime > 0 && now >= toolStartTime) {
            toolEndTime = now
            lastToolExecutionMs = now - toolStartTime
        }
    }

    fun incrementReconnect() {
        _reconnectCount.value += 1
    }

    fun resetReconnect() {
        _reconnectCount.value = 0
    }

    fun reset() {
        turnStartTime = 0L
        firstServerAudioTime = 0L
        toolStartTime = 0L
        toolEndTime = 0L
        lastTurnDurationMs = 0L
        lastServerLatencyMs = 0L
        lastToolExecutionMs = 0L
        resetReconnect()
    }
}
