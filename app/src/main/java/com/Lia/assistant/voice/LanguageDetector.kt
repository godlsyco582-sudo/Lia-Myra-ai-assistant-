package com.Lia.assistant.voice

enum class DetectedLanguage {
    HINDI,
    HINGLISH,
    ENGLISH,
    UNKNOWN
}

object LanguageDetector {
    private val HINGLISH_MARKERS = setOf(
        "hai", "hain", "ho", "kya", "kyun", "kaise", "nahi", "haan",
        "mujhe", "tumhe", "aap", "mera", "yaar", "bhai", "accha", "theek",
        "matlab", "karna", "karo", "raha", "chahiye", "batao", "suno",
        "dekho", "abhi", "phir", "lekin", "bahut", "thoda", "kuch", "sab",
        "wala", "hum", "yeh", "woh", "karein", "bolo", "meri", "tere",
        "kahan", "kaun", "kisi", "kitna", "khol", "kholo", "chalo", "banao"
    )

    fun detect(text: String): DetectedLanguage {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return DetectedLanguage.UNKNOWN

        // Check for Devanagari script (U+0900..U+097F)
        for (ch in trimmed) {
            if (ch in '\u0900'..'\u097F') {
                return DetectedLanguage.HINDI
            }
        }

        // Tokenize latin words
        val words = trimmed.lowercase()
            .split(Regex("[^a-zA-Z']+"))
            .filter { it.isNotBlank() }

        if (words.isEmpty()) return DetectedLanguage.UNKNOWN

        var hits = 0
        for (word in words) {
            if (HINGLISH_MARKERS.contains(word)) {
                hits++
            }
        }

        val count = words.size
        return if (count <= 4) {
            if (hits >= 1) DetectedLanguage.HINGLISH else DetectedLanguage.ENGLISH
        } else {
            val ratio = hits.toFloat() / count.toFloat()
            if (hits >= 2 || ratio >= 0.15f) DetectedLanguage.HINGLISH else DetectedLanguage.ENGLISH
        }
    }
}
