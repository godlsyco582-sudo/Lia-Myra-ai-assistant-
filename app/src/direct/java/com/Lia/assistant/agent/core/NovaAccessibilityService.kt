package com.Lia.assistant.agent.core

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.delay
import java.util.LinkedList

class NovaAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: NovaAccessibilityService? = null
            private set

        fun isEnabled(context: Context): Boolean {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val expected = "${context.packageName}/${NovaAccessibilityService::class.java.canonicalName}"
            return enabledServices.contains(context.packageName) &&
                    (enabledServices.contains(expected) || enabledServices.contains(NovaAccessibilityService::class.java.simpleName))
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Events monitored for screen state
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        if (instance == this) {
            instance = null
        }
        super.onDestroy()
    }

    fun readScreen(maxElements: Int = 40, maxLabelLength: Int = 40): List<String> {
        val root = rootInActiveWindow ?: return listOf("Screen root is empty or inaccessible")
        val lines = mutableListOf<String>()
        val queue = LinkedList<AccessibilityNodeInfo>()
        queue.add(root)

        while (queue.isNotEmpty() && lines.size < maxElements) {
            val node = queue.poll() ?: continue
            val text = (node.text ?: node.contentDescription)?.toString()?.trim()
            val className = node.className?.toString()?.substringAfterLast('.') ?: "element"

            if (!text.isNullOrBlank()) {
                val truncated = if (text.length > maxLabelLength) text.take(maxLabelLength) + "..." else text
                val type = when {
                    node.isEditable -> "input"
                    node.isClickable -> "button"
                    else -> "text"
                }
                val line = "[$type] $truncated"
                if (!lines.contains(line)) {
                    lines.add(line)
                }
            }

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return if (lines.isEmpty()) listOf("No readable text found on screen") else lines
    }

    fun tapText(query: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val lowerQuery = query.lowercase().trim()

        val matchingNodes = mutableListOf<AccessibilityNodeInfo>()
        val queue = LinkedList<AccessibilityNodeInfo>()
        queue.add(root)

        while (queue.isNotEmpty()) {
            val node = queue.poll() ?: continue
            val text = (node.text ?: node.contentDescription)?.toString()?.lowercase()?.trim()
            if (text != null && (text == lowerQuery || text.contains(lowerQuery))) {
                matchingNodes.add(node)
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }

        if (matchingNodes.isEmpty()) return false

        // Best match: exact match first, then clickable node or ancestor
        val targetNode = matchingNodes.firstOrNull {
            (it.text ?: it.contentDescription)?.toString()?.trim().equals(query, ignoreCase = true)
        } ?: matchingNodes.first()

        var current: AccessibilityNodeInfo? = targetNode
        while (current != null) {
            if (current.isClickable) {
                return current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            current = current.parent
        }

        return targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    fun typeText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)

        val target = if (focused != null && focused.isEditable) {
            focused
        } else {
            // Find first editable node
            var foundEditable: AccessibilityNodeInfo? = null
            val queue = LinkedList<AccessibilityNodeInfo>()
            queue.add(root)
            while (queue.isNotEmpty()) {
                val node = queue.poll() ?: continue
                if (node.isEditable) {
                    foundEditable = node
                    break
                }
                for (i in 0 until node.childCount) {
                    node.getChild(i)?.let { queue.add(it) }
                }
            }
            foundEditable
        } ?: return false

        val args = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
            )
        }
        return target.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    fun scroll(direction: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val action = if (direction.equals("up", ignoreCase = true) || direction.equals("backward", ignoreCase = true)) {
            AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        } else {
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        }

        val queue = LinkedList<AccessibilityNodeInfo>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val node = queue.poll() ?: continue
            if (node.isScrollable) {
                return node.performAction(action)
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return false
    }

    fun goBack(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun goHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }

    suspend fun autoSendWhatsAppMessage(): Boolean {
        for (attempt in 1..12) {
            delay(400)
            val service = instance ?: return false

            // Check if "Continue to chat" prompt appeared
            service.tapText("Continue to chat")

            // Attempt to tap send button
            if (service.tapText("Send")) {
                return true
            }
        }
        return false
    }
}
