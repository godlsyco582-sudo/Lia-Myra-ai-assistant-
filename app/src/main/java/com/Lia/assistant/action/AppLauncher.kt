package com.Lia.assistant.action

import android.content.Context
import android.content.Intent
import kotlin.math.abs

object AppLauncher {

    suspend fun findApp(context: Context, rawQuery: String): String? {
        val query = rawQuery.trim().lowercase()
        if (query.isBlank()) return null

        val appMap = InstalledAppLabelCache.get(context)

        // 1. Exact match
        if (appMap.containsKey(query)) {
            return appMap[query]
        }

        // 2. Filter candidates
        val queryWords = query.split(Regex("\\s+")).filter { it.isNotBlank() }
        val candidates = mutableListOf<Pair<String, String>>() // label to pkg

        for ((label, pkg) in appMap) {
            if (label.length < 3 && label != query) continue

            val match = if (label.startsWith(query) || query.startsWith(label)) {
                true
            } else if (queryWords.isNotEmpty()) {
                val labelWords = label.split(Regex("\\s+")).filter { it.isNotBlank() }
                queryWords.all { qw ->
                    labelWords.any { lw -> lw.startsWith(qw) }
                }
            } else {
                false
            }

            if (match) {
                candidates.add(label to pkg)
            }
        }

        if (candidates.isEmpty()) return null

        // Pick closest label length
        val best = candidates.minByOrNull { abs(it.first.length - query.length) }
        return best?.second
    }

    fun launch(context: Context, packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
