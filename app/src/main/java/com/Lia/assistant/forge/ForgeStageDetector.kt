package com.Lia.assistant.forge

object ForgeStageDetector {

    fun detect(partialHtml: String): ForgeStage {
        val len = partialHtml.length
        if (len < 50) return ForgeStage.INITIALIZING

        val lower = partialHtml.lowercase()

        return when {
            lower.contains("</html>") -> ForgeStage.READY
            lower.contains("</script>") && len > 12000 -> ForgeStage.FINALIZING
            lower.contains("<script") || lower.contains("three") || lower.contains("gsap") -> ForgeStage.INTERACTION
            lower.contains("class=") || lower.contains("<style") || lower.contains("tailwind") -> ForgeStage.STYLING
            lower.contains("<body") || lower.contains("<section") || lower.contains("<div") -> ForgeStage.STRUCTURE
            lower.contains("<html") || lower.contains("<head") -> ForgeStage.CONCEPT
            else -> ForgeStage.INITIALIZING
        }
    }

    fun extractSections(partialHtml: String): List<String> {
        val sections = mutableListOf<String>()
        val regex = Regex("""<section[^>]*id=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        val matches = regex.findAll(partialHtml)
        for (m in matches) {
            val id = m.groupValues[1]
            val title = id.replace("-", " ").replace("_", " ").split(" ")
                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            if (!sections.contains(title)) {
                sections.add(title)
            }
        }
        return sections
    }
}
