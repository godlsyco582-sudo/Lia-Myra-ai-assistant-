package com.Lia.assistant.license

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AccessKeyManager {
    private const val PREFS_NAME = "lia_access_key"
    private const val KEY_ACCESS_KEY = "access_key"
    private const val KEY_PLAN = "plan"
    private const val KEY_EXPIRES_AT = "expires_at"
    private const val KEY_BLOCKED = "blocked"
    private const val KEY_LAST_CHECK = "last_remote_check"

    private val _info = MutableStateFlow(
        LicenseInfo(
            active = true,
            plan = "Direct Pro",
            expiresAt = 0L,
            statusText = "Active (Lifetime Auto-Activated)",
            blocked = false,
            hasRememberedKey = true
        )
    )
    val info: StateFlow<LicenseInfo> = _info.asStateFlow()

    fun refreshInfo(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = prefs.getString(KEY_ACCESS_KEY, "AUTO-PRO-LIFETIME") ?: "AUTO-PRO-LIFETIME"
        val plan = prefs.getString(KEY_PLAN, "Direct Pro") ?: "Direct Pro"
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        val blocked = prefs.getBoolean(KEY_BLOCKED, false)

        val license = AccessKeyRules.evaluate(
            plan = plan,
            expiresAt = expiresAt,
            blocked = blocked,
            hasKey = true
        )
        _info.value = license
    }

    suspend fun activate(context: Context, key: String): Boolean {
        val clean = key.trim().ifBlank { "AUTO-PRO-LIFETIME" }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_ACCESS_KEY, clean)
            .putString(KEY_PLAN, "Direct Pro")
            .putLong(KEY_EXPIRES_AT, 0L)
            .putBoolean(KEY_BLOCKED, false)
            .putLong(KEY_LAST_CHECK, System.currentTimeMillis())
            .apply()

        refreshInfo(context)
        return true
    }

    suspend fun autoActivatePro(context: Context): Boolean {
        return activate(context, "AUTO-PRO-LIFETIME")
    }

    suspend fun liveCheck(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCheck = prefs.getLong(KEY_LAST_CHECK, 0L)
        val now = System.currentTimeMillis()
        if (now - lastCheck < 3 * 3600 * 1000L) {
            return
        }

        prefs.edit().putLong(KEY_LAST_CHECK, now).apply()
        refreshInfo(context)
    }

    fun isLicensed(context: Context): Boolean {
        refreshInfo(context)
        return _info.value.active
    }
}

@Composable
fun AccessKeyBanner(onActivateClick: () -> Unit) {
    val info by AccessKeyManager.info.collectAsState()
    if (!info.active) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(Color(0xFF2C161D), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Access Key Required", color = Color(0xFFFFB4AB), fontWeight = FontWeight.Bold)
                Text(info.statusText, color = Color(0xFFE0C4C8), fontSize = 12.sp)
            }
            Button(
                onClick = onActivateClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB547))
            ) {
                Text("Activate", color = Color(0xFF15112E))
            }
        }
    }
}

@Composable
fun AccessKeySection(onActivate: (String) -> Unit) {
    val info by AccessKeyManager.info.collectAsState()
    var inputKey by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFFFB547).copy(alpha = 0.4f))
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF241D4E),
                        Color(0xFF17143A)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                1.5.dp,
                Brush.horizontalGradient(listOf(Color(0xFFFFB547), Color(0xFF3FE0D0))),
                RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFFFFB547).copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFFFFB547),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Direct Pro Edition",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Lifetime License",
                        color = Color(0xFFA7A0CC),
                        fontSize = 12.sp
                    )
                }
            }

            // Glowing Auto-Activated Chip
            Box(
                modifier = Modifier
                    .background(Color(0xFF3FE0D0).copy(alpha = 0.2f), CircleShape)
                    .border(1.dp, Color(0xFF3FE0D0), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(Color(0xFF3FE0D0), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "AUTO-ACTIVATED",
                        color = Color(0xFF3FE0D0),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Status row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF15113A), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("STATUS", fontSize = 10.sp, color = Color(0xFFA7A0CC), fontWeight = FontWeight.Bold)
                Text(
                    text = info.statusText,
                    color = Color(0xFF3FE0D0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF3FE0D0),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Unlocked Pro features list
        Text(
            text = "PRO FEATURES UNLOCKED",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFB547),
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        val perks = listOf(
            "Autonomous Screen Control & Gesture Automation",
            "3D Website Forge Engine (Full Single-File Generation)",
            "Full-Duplex Hindi, Hinglish & English Real-Time Voice",
            "Automated Calling, Contact Resolution & WhatsApp Messaging",
            "Unlimited Multi-turn Conversation Memory"
        )

        for (perk in perks) {
            Row(
                modifier = Modifier.padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✓", color = Color(0xFF3FE0D0), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(perk, color = Color(0xFFEEE9FF), fontSize = 12.5.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Toggle custom key
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCustomInput = !showCustomInput }
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (showCustomInput) "Hide Custom Key Input" else "Enter Custom Key (Optional)",
                color = Color(0xFFA7A0CC),
                fontSize = 12.sp
            )
            Icon(
                imageVector = if (showCustomInput) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = Color(0xFFA7A0CC),
                modifier = Modifier.size(18.dp)
            )
        }

        AnimatedVisibility(visible = showCustomInput) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it },
                    placeholder = { Text("e.g. LIA-PRO-ENTERPRISE", color = Color(0xFF7A73A6)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFB547),
                        unfocusedBorderColor = Color(0xFF33EEE9FF),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        onActivate(inputKey)
                        inputKey = ""
                        showCustomInput = false
                    },
                    enabled = inputKey.isNotBlank(),
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB547))
                ) {
                    Text("Apply Key", color = Color(0xFF15112E), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
