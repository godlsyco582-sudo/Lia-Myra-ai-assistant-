package com.Lia.assistant.voice

object LiaCapabilityPrompts {
    fun toolInstruction(assistantName: String): String {
        return """
Capabilities ($assistantName):
- You have tools to launch apps (open_app), initiate calls (call_contact), and prepare messages (message_contact).
- You can build custom websites using build_website tool.
- Direct distribution features: You can inspect the screen, tap text, type into fields, scroll, and navigate back/home via accessibility tools.
- Never take destructive or irreversible actions without explicit confirmation.
""".trimIndent()
    }
}
