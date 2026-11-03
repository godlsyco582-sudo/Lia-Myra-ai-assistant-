package com.Lia.assistant.license

import android.content.Context
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AccessKeyManager {
    private val _info = MutableStateFlow(
        LicenseInfo(
            active = true,
            plan = "Google Play Edition",
            expiresAt = 0L,
            statusText = "Play Store Standard Edition",
            blocked = false,
            hasRememberedKey = true
        )
    )
    val info: StateFlow<LicenseInfo> = _info.asStateFlow()

    fun refreshInfo(context: Context) {}

    suspend fun activate(context: Context, key: String): Boolean = true

    suspend fun liveCheck(context: Context) {}

    fun isLicensed(context: Context): Boolean = true
}

@Composable
fun AccessKeyBanner(onActivateClick: () -> Unit) {
    // No-op for Google Play builds
}

@Composable
fun AccessKeySection(onActivate: (String) -> Unit) {
    // No-op for Google Play builds
}
