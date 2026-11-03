package com.Lia.assistant

import com.Lia.assistant.data.LanguagePreference
import com.Lia.assistant.data.Personality
import com.Lia.assistant.forge.ForgeHtml
import com.Lia.assistant.forge.ForgeIntent
import com.Lia.assistant.forge.ForgeStage
import com.Lia.assistant.forge.ForgeStageDetector
import com.Lia.assistant.license.AccessKeyRules
import com.Lia.assistant.voice.ConversationMemory
import com.Lia.assistant.voice.DetectedLanguage
import com.Lia.assistant.voice.LanguageDetector
import com.Lia.assistant.voice.MemoryRole
import com.Lia.assistant.voice.PersonalityPromptBuilder
import com.Lia.assistant.voice.ToolCallDeduplicator
import com.Lia.assistant.voice.VoiceLatencyTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LiaCoreTests {

    // 1. LanguageDetector Tests
    @Test
    fun testLanguageDetectionDevanagari() {
        val result = LanguageDetector.detect("नमस्ते, आप कैसे हैं?")
        assertEquals(DetectedLanguage.HINDI, result)
    }

    @Test
    fun testLanguageDetectionHinglish() {
        val result = LanguageDetector.detect("kya haal hai yaar, kaise ho?")
        assertEquals(DetectedLanguage.HINGLISH, result)
    }

    @Test
    fun testLanguageDetectionEnglish() {
        val result = LanguageDetector.detect("What is the current weather forecast?")
        assertEquals(DetectedLanguage.ENGLISH, result)
    }

    @Test
    fun testLanguageDetectionBlank() {
        val result = LanguageDetector.detect("   ")
        assertEquals(DetectedLanguage.UNKNOWN, result)
    }

    // 2. PersonalityPromptBuilder Tests
    @Test
    fun testPromptBuilderCombinations() {
        for (p in Personality.entries) {
            for (l in LanguagePreference.entries) {
                val prompt = PersonalityPromptBuilder.build(
                    personality = p,
                    languagePreference = l,
                    detectedLanguage = DetectedLanguage.ENGLISH,
                    recap = null,
                    assistantName = "Lia"
                )
                assertTrue(prompt.contains("Lia"))
                assertTrue(prompt.contains(p.systemInstruction))
                assertFalse(prompt.contains("This is a continuing conversation"))
            }
        }
    }

    @Test
    fun testPromptBuilderWithRecap() {
        val memory = ConversationMemory(12)
        memory.addTurn(MemoryRole.USER, "Hello Lia")
        memory.addTurn(MemoryRole.ASSISTANT, "Hello! How can I help?")

        val prompt = PersonalityPromptBuilder.build(
            personality = Personality.NORMAL,
            languagePreference = LanguagePreference.AUTO,
            detectedLanguage = DetectedLanguage.ENGLISH,
            recap = memory.recap(),
            assistantName = "Lia"
        )
        assertTrue(prompt.contains("This is a continuing conversation — do not greet again. Recap:"))
        assertTrue(prompt.contains("User: Hello Lia"))
    }

    // 3. ToolCallDeduplicator Tests
    @Test
    fun testToolCallDeduplication() {
        val dedup = ToolCallDeduplicator(5)
        assertFalse(dedup.isDuplicateAndRecord("call_1"))
        assertTrue(dedup.isDuplicateAndRecord("call_1"))
        assertFalse(dedup.isDuplicateAndRecord("call_2"))
    }

    // 4. VoiceLatencyTracker Tests
    @Test
    fun testLatencyTrackerInOrder() {
        val tracker = VoiceLatencyTracker()
        tracker.markTurnStart(1000L)
        tracker.markFirstServerAudio(1400L)
        tracker.markTurnEnd(2200L)

        assertEquals(400L, tracker.lastServerLatencyMs)
        assertEquals(1200L, tracker.lastTurnDurationMs)
    }

    // 5. AccessKeyRules Tests
    @Test
    fun testAccessKeyRulesLifetime() {
        val info = AccessKeyRules.evaluate(
            plan = "lifetime",
            expiresAt = 0L,
            blocked = false,
            hasKey = true
        )
        assertTrue(info.active)
        assertEquals("Lifetime", info.plan)
    }

    @Test
    fun testAccessKeyRulesExpired() {
        val past = System.currentTimeMillis() - 10000L
        val info = AccessKeyRules.evaluate(
            plan = "monthly",
            expiresAt = past,
            blocked = false,
            hasKey = true
        )
        assertFalse(info.active)
        assertEquals("Access key expired", info.statusText)
    }

    // 6. ForgeHtml & Intent Tests
    @Test
    fun testForgeIntentExtraction() {
        val intent = ForgeIntent.extract("Lia, ek cafe ki 3D website bana do")
        assertNotNull(intent)

        val nonIntent = ForgeIntent.extract("What is a website?")
        assertNull(nonIntent)
    }

    @Test
    fun testForgeHtmlFenceStripping() {
        val raw = "```html\n<!DOCTYPE html><html><body>Hi</body></html>\n```"
        val stripped = ForgeHtml.stripFences(raw)
        assertEquals("<!DOCTYPE html><html><body>Hi</body></html>", stripped)
    }

    @Test
    fun testForgeStageDetector() {
        val html = "<!DOCTYPE html><html><head><script src='three.js'></script></head><body><section id='hero'></section></body></html>"
        val stage = ForgeStageDetector.detect(html)
        assertEquals(ForgeStage.READY, stage)
    }
}
