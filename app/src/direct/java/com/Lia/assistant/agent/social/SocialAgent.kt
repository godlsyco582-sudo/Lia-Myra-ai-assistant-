package com.Lia.assistant.agent.social

enum class Platform {
    INSTAGRAM,
    FACEBOOK
}

enum class SocialAction {
    CREATE_POST,
    CREATE_REEL,
    CREATE_STORY,
    TEXT_POST
}

enum class TaskMode {
    PUBLISH,
    DRAFT
}

enum class TaskStatus {
    IDLE,
    OPENING_APP,
    OBSERVING,
    RESOLVING_TARGET,
    VALIDATING_TARGET,
    EXECUTING_ACTION,
    WAITING_FOR_UI,
    VERIFYING,
    NEXT_STEP,
    RETRY,
    REOBSERVE,
    WAITING_FOR_CONFIRMATION,
    WAITING_FOR_USER,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class ControlCommand {
    CONFIRM,
    REJECT,
    RESUME,
    CANCEL,
    STATUS
}

data class SocialTaskRequest(
    val platform: Platform,
    val action: SocialAction,
    val mediaUri: String? = null,
    val caption: String = "",
    val mode: TaskMode = TaskMode.DRAFT,
    val targetAccount: String? = null
) {
    companion object {
        fun parse(
            platformStr: String,
            actionStr: String,
            mediaUri: String? = null,
            caption: String = "",
            modeStr: String = "draft",
            targetAccount: String? = null
        ): SocialTaskRequest {
            val platform = if (platformStr.equals("facebook", ignoreCase = true)) Platform.FACEBOOK else Platform.INSTAGRAM
            val action = when (actionStr.lowercase()) {
                "create_reel" -> SocialAction.CREATE_REEL
                "create_story" -> SocialAction.CREATE_STORY
                "text_post" -> SocialAction.TEXT_POST
                else -> SocialAction.CREATE_POST
            }
            val mode = if (modeStr.equals("publish", ignoreCase = true)) TaskMode.PUBLISH else TaskMode.DRAFT
            return SocialTaskRequest(platform, action, mediaUri, caption, mode, targetAccount)
        }
    }
}

class TaskStateMachine(val initialMode: TaskMode = TaskMode.DRAFT) {
    var currentState: TaskStatus = TaskStatus.IDLE
        private set

    val history = mutableListOf<TaskStatus>(TaskStatus.IDLE)

    fun moveTo(next: TaskStatus) {
        // Terminal states never exit
        if (currentState == TaskStatus.COMPLETED || currentState == TaskStatus.FAILED || currentState == TaskStatus.CANCELLED) {
            throw IllegalStateException("Cannot transition from terminal state $currentState to $next")
        }

        // FAILED and CANCELLED are reachable from any non-terminal state
        if (next == TaskStatus.FAILED || next == TaskStatus.CANCELLED) {
            currentState = next
            history.add(next)
            return
        }

        // Validation rule: DRAFT mode can never transition to WAITING_FOR_CONFIRMATION for publishing
        if (initialMode == TaskMode.DRAFT && next == TaskStatus.WAITING_FOR_CONFIRMATION) {
            throw IllegalStateException("DRAFT tasks cannot publish or request publishing confirmation.")
        }

        // Normal sequential transition check
        val isLegal = when (currentState) {
            TaskStatus.IDLE -> next == TaskStatus.OPENING_APP
            TaskStatus.OPENING_APP -> next == TaskStatus.OBSERVING || next == TaskStatus.WAITING_FOR_USER
            TaskStatus.OBSERVING -> next == TaskStatus.RESOLVING_TARGET || next == TaskStatus.WAITING_FOR_USER
            TaskStatus.RESOLVING_TARGET -> next == TaskStatus.VALIDATING_TARGET || next == TaskStatus.REOBSERVE
            TaskStatus.VALIDATING_TARGET -> next == TaskStatus.EXECUTING_ACTION || next == TaskStatus.RETRY
            TaskStatus.EXECUTING_ACTION -> next == TaskStatus.WAITING_FOR_UI || next == TaskStatus.WAITING_FOR_CONFIRMATION
            TaskStatus.WAITING_FOR_UI -> next == TaskStatus.VERIFYING
            TaskStatus.VERIFYING -> next == TaskStatus.NEXT_STEP || next == TaskStatus.COMPLETED || next == TaskStatus.RETRY
            TaskStatus.NEXT_STEP -> next == TaskStatus.OBSERVING
            TaskStatus.RETRY -> next == TaskStatus.OBSERVING
            TaskStatus.REOBSERVE -> next == TaskStatus.OBSERVING
            TaskStatus.WAITING_FOR_CONFIRMATION -> next == TaskStatus.EXECUTING_ACTION || next == TaskStatus.CANCELLED
            TaskStatus.WAITING_FOR_USER -> next == TaskStatus.OBSERVING || next == TaskStatus.CANCELLED
            else -> false
        }

        if (!isLegal) {
            throw IllegalStateException("Illegal state transition from $currentState to $next")
        }

        currentState = next
        history.add(next)
    }
}
