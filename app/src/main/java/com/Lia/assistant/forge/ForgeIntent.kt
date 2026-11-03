package com.Lia.assistant.forge

object ForgeIntent {
    private val SUBJECT_KEYWORDS = listOf(
        "website", "web site", "web page", "webpage", "landing page",
        "web app", "site", "portfolio", "store page"
    )

    private val BUILD_VERBS = listOf(
        "build", "create", "make", "bana", "banao", "generate",
        "design", "taiyaar", "develop", "kod"
    )

    fun extract(text: String): String? {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return null
        val lower = trimmed.lowercase()

        // Ignore mere informational questions
        if (lower.startsWith("what is") || lower.startsWith("how to") || lower.startsWith("kya hota")) {
            return null
        }

        val hasSubject = SUBJECT_KEYWORDS.any { lower.contains(it) }
        val hasVerb = BUILD_VERBS.any { lower.contains(it) }

        if (hasSubject && hasVerb) {
            return trimmed
        }

        return null
    }
}
