package com.phonemic.keyboard

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class MouseSettings(
    val enabled: Boolean = false,
    val mode: String = "air_mouse",
    val sensitivity: Float = 50f,
    val twoFingerScroll: Boolean = true,
    val deadZone: Float = 0.03f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: MouseSettings,
    onSettingsChange: (MouseSettings) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("设置", style = MaterialTheme.typography.headlineMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("启用鼠标功能")
            Switch(
                checked = settings.enabled,
                onCheckedChange = { onSettingsChange(settings.copy(enabled = it)) }
            )
        }

        if (settings.enabled) {
            Divider()

            Text("鼠标模式", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = settings.mode == "air_mouse",
                    onClick = { onSettingsChange(settings.copy(mode = "air_mouse")) },
                    label = { Text("空中鼠标") }
                )
                FilterChip(
                    selected = settings.mode == "touchpad",
                    onClick = { onSettingsChange(settings.copy(mode = "touchpad")) },
                    label = { Text("触控板") }
                )
            }

            Text("灵敏度: %")
            Slider(
                value = settings.sensitivity,
                onValueChange = { onSettingsChange(settings.copy(sensitivity = it)) },
                valueRange = 10f..100f
            )

            Text("死区阈值: ")
            Slider(
                value = settings.deadZone,
                onValueChange = { onSettingsChange(settings.copy(deadZone = it)) },
                valueRange = 0.01f..0.1f
            )

            if (settings.mode == "touchpad") {
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

fun saveMouseSettings(context: Context, settings: MouseSettings) {
    context.getSharedPreferences("phone_mic", Context.MODE_PRIVATE).edit()
        .putBoolean("mouse_enabled", settings.enabled)
        .putString("mouse_mode", settings.mode)
        .putFloat("mouse_sensitivity", settings.sensitivity)
        .putBoolean("mouse_two_finger_scroll", settings.twoFingerScroll)
        .putFloat("mouse_dead_zone", settings.deadZone)
        .apply()
}

fun loadMouseSettings(context: Context): MouseSettings {
    val prefs = context.getSharedPreferences("phone_mic", Context.MODE_PRIVATE)
    return MouseSettings(
        enabled = prefs.getBoolean("mouse_enabled", false),
        mode = prefs.getString("mouse_mode", "air_mouse") ?: "air_mouse",
        sensitivity = prefs.getFloat("mouse_sensitivity", 50f),
        twoFingerScroll = prefs.getBoolean("mouse_two_finger_scroll", true),
        deadZone = prefs.getFloat("mouse_dead_zone", 0.03f)
    )
}
