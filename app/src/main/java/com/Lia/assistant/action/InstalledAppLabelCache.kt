package com.Lia.assistant.action

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object InstalledAppLabelCache {
    private const val TTL_MS = 15 * 60 * 1000L // 15 mins
    private var lastLoadedTime: Long = 0L
    private val map = mutableMapOf<String, String>() // lowercase label -> package
    private val mutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun prewarm(context: Context) {
        scope.launch {
            get(context)
        }
    }

    suspend fun get(context: Context): Map<String, String> = mutex.withLock {
        val now = System.currentTimeMillis()
        if (map.isNotEmpty() && (now - lastLoadedTime) < TTL_MS) {
            return@withLock map.toMap()
        }

        map.clear()
        try {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val apps = pm.queryIntentActivities(intent, 0)
            for (resolveInfo in apps) {
                val label = resolveInfo.loadLabel(pm).toString().trim().lowercase()
                val pkg = resolveInfo.activityInfo.packageName
                if (label.isNotBlank() && pkg.isNotBlank()) {
                    map[label] = pkg
                }
            }
            lastLoadedTime = now
        } catch (_: Exception) {}

        return@withLock map.toMap()
    }
}
