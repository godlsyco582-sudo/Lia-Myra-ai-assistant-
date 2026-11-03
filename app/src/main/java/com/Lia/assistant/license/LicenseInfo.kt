package com.Lia.assistant.license

data class LicenseInfo(
    val active: Boolean,
    val plan: String,
    val expiresAt: Long,
    val statusText: String,
    val blocked: Boolean,
    val hasRememberedKey: Boolean
)
