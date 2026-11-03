package com.Lia.assistant.voice

object LiaCapabilityPrompts {
    fun toolInstruction(assistantName: String): String {
        return """
Capabilities ($assistantName):
- You have tools to open apps (open_app), initiate calls (call_contact), and open message composers (message_contact).
- You can build custom websites using build_website tool.
- Google Play Policy Notice: You cannot read or control the screen, and cannot post to social apps. You open message drafts in the official messaging app so the user can tap Send.
""".trimIndent()
    }
}
