package com.phonemic.keyboard

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class AppSettings(
    val mouseEnabled: Boolean = false,
    val mouseMode: String = "air_mouse",
    val sensitivity: Float = 50f,
    val twoFingerScroll: Boolean = true,
    val deadZone: Float = 0.03f,
    val voiceTriggerKeys: List<String> = listOf("ctrl", "win")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("设置", style = MaterialTheme.typography.headlineMedium)

        // 语音输入触发键设置
        Text("语音输入触发键", style = MaterialTheme.typography.titleMedium)
        Text(
            "点击添加或移除按键组合，当前按键会在按住语音按钮时发送",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val availableKeys = listOf("ctrl", "alt", "shift", "win", "v", "space")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableKeys) { key ->
                val isSelected = key in settings.voiceTriggerKeys
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newKeys = if (isSelected) {
                            settings.voiceTriggerKeys - key
                        } else {
                            settings.voiceTriggerKeys + key
                        }
                        onSettingsChange(settings.copy(voiceTriggerKeys = newKeys))
                    },
                    label = { Text(key.uppercase()) }
                )
            }
        }

        Divider()

        // 鼠标功能设置
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("启用鼠标功能")
            Switch(
                checked = settings.mouseEnabled,
                onCheckedChange = { onSettingsChange(settings.copy(mouseEnabled = it)) }
            )
        }

        if (settings.mouseEnabled) {
            Text("鼠标模式", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = settings.mouseMode == "air_mouse",
                    onClick = { onSettingsChange(settings.copy(mouseMode = "air_mouse")) },
                    label = { Text("空中鼠标") }
                )
                FilterChip(
                    selected = settings.mouseMode == "touchpad",
                    onClick = { onSettingsChange(settings.copy(mouseMode = "touchpad")) },
                    label = { Text("触控板") }
                )
            }

            Text("灵敏度: ${settings.sensitivity.toInt()}%")
            Slider(
                value = settings.sensitivity,
                onValueChange = { onSettingsChange(settings.copy(sensitivity = it)) },
                valueRange = 10f..100f
            )

            Text("死区阈值: ${String.format("%.2f", settings.deadZone)}")
            Slider(
                value = settings.deadZone,
                onValueChange = { onSettingsChange(settings.copy(deadZone = it)) },
                valueRange = 0.01f..0.1f
            )

            if (settings.mouseMode == "touchpad") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("双指滚轮")
                    Switch(
                        checked = settings.twoFingerScroll,
                        onCheckedChange = { onSettingsChange(settings.copy(twoFingerScroll = it)) }
                    )
                }
            }
        }
    }
}

fun saveAppSettings(context: Context, settings: AppSettings) {
    context.getSharedPreferences("phone_mic", Context.MODE_PRIVATE).edit()
        .putBoolean("mouse_enabled", settings.mouseEnabled)
        .putString("mouse_mode", settings.mouseMode)
        .putFloat("mouse_sensitivity", settings.sensitivity)
        .putBoolean("mouse_two_finger_scroll", settings.twoFingerScroll)
        .putFloat("mouse_dead_zone", settings.deadZone)
        .putStringSet("voice_trigger_keys", settings.voiceTriggerKeys.toSet())
        .apply()
}

fun loadAppSettings(context: Context): AppSettings {
    val prefs = context.getSharedPreferences("phone_mic", Context.MODE_PRIVATE)
    return AppSettings(
        mouseEnabled = prefs.getBoolean("mouse_enabled", false),
        mouseMode = prefs.getString("mouse_mode", "air_mouse") ?: "air_mouse",
        sensitivity = prefs.getFloat("mouse_sensitivity", 50f),
        twoFingerScroll = prefs.getBoolean("mouse_two_finger_scroll", true),
        deadZone = prefs.getFloat("mouse_dead_zone", 0.03f),
        voiceTriggerKeys = prefs.getStringSet("voice_trigger_keys", setOf("ctrl", "win"))?.toList() ?: listOf("ctrl", "win")
    )
}

// 保持向后兼容
typealias MouseSettings = AppSettings
