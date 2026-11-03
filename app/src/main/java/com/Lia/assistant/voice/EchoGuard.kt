package com.Lia.assistant.voice

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

object EchoGuard {
    private const val ECHO_TAIL_MS = 450L
    private const val WATCHDOG_TIMEOUT_MS = 60_000L

    private val playbackCounter = AtomicInteger(0)
    private val isMuted = AtomicBoolean(false)
    private var unmuteJob: Job? = null
    private var watchdogJob: Job? = null
    private val guardScope = CoroutineScope(Dispatchers.Default)

    @Synchronized
    fun notifyPlaybackStarted() {
        unmuteJob?.cancel()
        unmuteJob = null

        val count = playbackCounter.incrementAndGet()
        isMuted.set(true)

        // Arm watchdog
        if (watchdogJob == null) {
            watchdogJob = guardScope.launch {
                delay(WATCHDOG_TIMEOUT_MS)
                forceUnmute()
            }
        }
    }

    @Synchronized
    fun notifyPlaybackStopped() {
        val remaining = playbackCounter.decrementAndGet()
        if (remaining <= 0) {
            playbackCounter.set(0)
            watchdogJob?.cancel()
            watchdogJob = null

            unmuteJob?.cancel()
            unmuteJob = guardScope.launch {
                delay(ECHO_TAIL_MS)
                isMuted.set(false)
            }
        }
    }

    fun isMicMuted(): Boolean {
        return isMuted.get()
    }

    @Synchronized
    fun reset() {
        playbackCounter.set(0)
        isMuted.set(false)
        unmuteJob?.cancel()
        unmuteJob = null
        watchdogJob?.cancel()
        watchdogJob = null
    }

    @Synchronized
    private fun forceUnmute() {
        playbackCounter.set(0)
        isMuted.set(false)
        watchdogJob = null
    }
}
