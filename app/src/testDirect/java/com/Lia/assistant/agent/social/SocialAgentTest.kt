package com.Lia.assistant.agent.social

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SocialAgentTest {

    @Test
    fun testLegalStateTransitions() {
        val sm = TaskStateMachine(TaskMode.PUBLISH)
        assertEquals(TaskStatus.IDLE, sm.currentState)

        sm.moveTo(TaskStatus.OPENING_APP)
        assertEquals(TaskStatus.OPENING_APP, sm.currentState)

        sm.moveTo(TaskStatus.OBSERVING)
        assertEquals(TaskStatus.OBSERVING, sm.currentState)

        sm.moveTo(TaskStatus.RESOLVING_TARGET)
        assertEquals(TaskStatus.RESOLVING_TARGET, sm.currentState)

        sm.moveTo(TaskStatus.VALIDATING_TARGET)
        assertEquals(TaskStatus.VALIDATING_TARGET, sm.currentState)

        sm.moveTo(TaskStatus.EXECUTING_ACTION)
        assertEquals(TaskStatus.EXECUTING_ACTION, sm.currentState)

        sm.moveTo(TaskStatus.WAITING_FOR_CONFIRMATION)
        assertEquals(TaskStatus.WAITING_FOR_CONFIRMATION, sm.currentState)
    }

    @Test
    fun testDraftNeverPublishes() {
        val sm = TaskStateMachine(TaskMode.DRAFT)
        sm.moveTo(TaskStatus.OPENING_APP)
        sm.moveTo(TaskStatus.OBSERVING)
        sm.moveTo(TaskStatus.RESOLVING_TARGET)
        sm.moveTo(TaskStatus.VALIDATING_TARGET)
        sm.moveTo(TaskStatus.EXECUTING_ACTION)

        // Attempting to move to WAITING_FOR_CONFIRMATION on DRAFT mode must throw
        assertThrows(IllegalStateException::class.java) {
            sm.moveTo(TaskStatus.WAITING_FOR_CONFIRMATION)
        }
    }

    @Test
    fun testTerminalStatesCannotExit() {
        val sm = TaskStateMachine()
        sm.moveTo(TaskStatus.CANCELLED)

        assertThrows(IllegalStateException::class.java) {
            sm.moveTo(TaskStatus.IDLE)
        }
    }
}
