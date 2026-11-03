package com.Lia.assistant.agent.core

data class UiElement(
    val id: String,
    val text: String,
    val className: String,
    val isClickable: Boolean,
    val isEditable: Boolean,
    val bounds: String = ""
)

data class ScreenObservation(
    val observationId: String,
    val packageName: String,
    val windowClass: String,
    val elements: List<UiElement>,
    val screenSize: Pair<Int, Int>,
    val windowStamp: Long
)

data class CapturedScreen(
    val packageName: String,
    val windowClass: String,
    val elements: List<UiElement>,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ScreenDriverResult {
    OK,
    FAILED,
    STALE,
    NOT_FOUND,
    UNSUPPORTED
}

enum class ExecPhase {
    IDLE,
    PLANNING,
    ACTING,
    VERIFYING,
    DONE
}

enum class ActionMethod {
    ACCESSIBILITY_CLICK,
    GESTURE_ON_ELEMENT_BOUNDS,
    VISION_TAP,
    SET_TEXT
}

enum class Blocker {
    LOGIN,
    CAPTCHA,
    TWO_FACTOR,
    SECURITY_CHECK,
    PERMISSION_PROMPT
}

enum class FailureReason {
    ELEMENT_NOT_FOUND,
    ACTION_TIMEOUT,
    APP_CRASHED,
    BLOCKED_BY_SECURITY,
    STALE_OBSERVATION,
    UNKNOWN
}

sealed class ActionResult {
    object Success : ActionResult()
    data class Failed(val reason: FailureReason) : ActionResult()
    data class Blocked(val blocker: Blocker) : ActionResult()
}

interface ScreenDriver {
    suspend fun capture(): ScreenObservation
    suspend fun clickElement(obs: ScreenObservation, element: UiElement): ScreenDriverResult
    suspend fun gestureTap(x: Float, y: Float): ScreenDriverResult
    suspend fun setText(text: String): ScreenDriverResult
    suspend fun scroll(direction: String): ScreenDriverResult
    suspend fun back(): ScreenDriverResult
    suspend fun home(): ScreenDriverResult
}
