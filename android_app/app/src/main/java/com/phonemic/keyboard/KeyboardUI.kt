package com.phonemic.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object KeyboardTokens {
    const val KeyHeight = 52
    const val SmallKeyHeight = 42
    const val ArrowKeySize = 44
    const val Gap = 8
    const val RowGap = 10
    const val KeyRadius = 12

    val BgColor = Color(0xFFF5F5F5)
    val KeyBg = Color(0xFFE0E0E0)
    val AccentColor = Color(0xFF1A9E8F)
    val TextDark = Color(0xFF333333)
    val TextDim = Color(0xFF888888)
    val ErrorColor = Color(0xFFDC2626)
}

@Composable
fun KeyboardScreen(vm: MainViewModel) {
    val status by vm.status
    val error by vm.error
    val serverUrl by vm.serverUrl
    val isConnected = status == ConnectionStatus.CONNECTED
    val isRecording by vm.isRecording
    val connectionHint by vm.connectionHint
    val mouseSettings by vm.mouseSettings

    val t = KeyboardTokens
    val KeyRadius = t.KeyRadius.dp
    val Gap = t.Gap.dp

    var showSettings by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = t.BgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(t.RowGap.dp)
        ) {
            // 连接栏 + 设置按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(t.Gap.dp)
            ) {
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { vm.serverUrl.value = it },
                    modifier = Modifier.weight(1f).height(48.dp),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                    enabled = !isConnected,
                    shape = RoundedCornerShape(KeyRadius),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = t.AccentColor,
                        unfocusedBorderColor = Color(0xFFCCCCCC),
                        focusedTextColor = t.TextDark,
                        unfocusedTextColor = t.TextDark,
                        disabledTextColor = t.TextDim
                    )
                )
                Button(
                    onClick = { if (isConnected) vm.disconnect() else vm.connect() },
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConnected) t.ErrorColor else t.AccentColor
                    ),
                    shape = RoundedCornerShape(KeyRadius)
                ) {
                    Text(
                        if (isConnected) "断开" else "连接",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        maxLines = 1
                    )
                }
                IconButton(
                    onClick = { showSettings = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "设置",
                        tint = t.TextDim
                    )
                }
            }

            if (connectionHint.isNotEmpty()) {
                Text(
                    connectionHint,
                    fontSize = 11.sp,
                    color = if (connectionHint.contains("失败")) t.ErrorColor else t.AccentColor,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // 键盘主界面
            // 修饰键
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Gap)
            ) {
                listOf("ctrl" to "Ctrl", "alt" to "Alt", "shift" to "Shift", "win" to "Win").forEach { (key, label) ->
                    val isActive = vm.isModifierActive(key)
                    Button(
                        onClick = { vm.toggleModifier(key) },
                        modifier = Modifier.weight(1f).height(t.SmallKeyHeight.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) t.AccentColor else t.KeyBg
                        ),
                        shape = RoundedCornerShape(t.KeyRadius.dp)
                    ) {
                        Text(
                            label,
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isActive) Color.White else t.TextDark
                        )
                    }
                }
            }

            // 常用功能键
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Gap)
            ) {
                listOf("Esc" to "escape", "Tab" to "tab", "Bksp" to "backspace", "Del" to "delete").forEach { (label, key) ->
                    Button(
                        onClick = { vm.sendKey(key) },
                        modifier = Modifier.weight(1f).height(t.SmallKeyHeight.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = t.KeyBg),
                        shape = RoundedCornerShape(t.KeyRadius.dp)
                    ) {
                        Text(label, fontSize = 12.sp, color = t.TextDark)
                    }
                }
            }

            // 方向键
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(Gap)) {
                    Button(
                        onClick = { vm.sendKey("up") },
                        modifier = Modifier.size(t.ArrowKeySize.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = t.KeyBg),
                        shape = RoundedCornerShape(t.KeyRadius.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("↑", fontSize = 16.sp, color = t.TextDark) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Gap)) {
                    listOf("←" to "left", "↓" to "down", "→" to "right").forEach { (label, key) ->
                        Button(
                            onClick = { vm.sendKey(key) },
                            modifier = Modifier.size(t.ArrowKeySize.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = t.KeyBg),
                            shape = RoundedCornerShape(t.KeyRadius.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) { Text(label, fontSize = 14.sp, color = t.TextDark) }
                    }
                }
            }

            // Enter
            Button(
                onClick = { vm.sendKey("enter") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = t.AccentColor),
                shape = RoundedCornerShape(t.KeyRadius.dp)
            ) {
                Text("Enter", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            // 鼠标按键（如果启用）
            if (mouseSettings.enabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Gap)
                ) {
                    Button(
                        onClick = { vm.sendMouseClick("left") },
                        modifier = Modifier.weight(1f).height(t.SmallKeyHeight.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = t.KeyBg),
                        shape = RoundedCornerShape(t.KeyRadius.dp)
                    ) {
                        Text("左键", fontSize = 12.sp, color = t.TextDark)
                    }
                    Button(
                        onClick = { vm.sendMouseClick("right") },
                        modifier = Modifier.weight(1f).height(t.SmallKeyHeight.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = t.KeyBg),
                        shape = RoundedCornerShape(t.KeyRadius.dp)
                    ) {
                        Text("右键", fontSize = 12.sp, color = t.TextDark)
                    }
                }
            }

            // 触控板
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                TouchPadMode(
                    onMove = { dx, dy -> vm.sendMouseMove(dx, dy) },
                    onClick = { vm.sendMouseClick("left") },
                    onRightClick = { vm.sendMouseClick("right") },
                    onScroll = { dy -> vm.sendMouseScroll(dy) },
                    enabled = true
                )
            }

            // 语音按钮
            MicButton(
                isRecording = isRecording,
                onPress = { vm.startRecording() },
                onRelease = { vm.stopRecording() },
            )

            // 错误提示
            error?.let { err ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(err, color = t.ErrorColor, modifier = Modifier.weight(1f))
                        TextButton(onClick = { vm.clearError() }) {
                            Text("关闭")
                        }
                    }
                }
            }
        }
    }

    // 设置对话框
    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("设置") },
            text = {
                SettingsContent(
                    settings = mouseSettings,
                    onSettingsChange = { vm.updateMouseSettings(it) }
                )
            },
            confirmButton = {
                TextButton(onClick = { showSettings = false }) {
                    Text("完成")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    settings: MouseSettings,
    onSettingsChange: (MouseSettings) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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

@Composable
fun AirMouseMode(
    enabled: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onPause: () -> Unit,
    paused: Boolean,
    onLeftClick: () -> Unit = {},
    onRightClick: () -> Unit = {}
) {
    val t = KeyboardTokens
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!enabled) {
            Text("空中鼠标", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = t.TextDark)
            Spacer(modifier = Modifier.height(8.dp))
            Text("倾斜手机移动光标，触摸屏幕点击", color = t.TextDim)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = t.AccentColor),
                shape = RoundedCornerShape(t.KeyRadius.dp)
            ) {
                Text("启动", color = Color.White)
            }
        } else {
            Text(
                text = if (paused) "已暂停" else "移动中",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (paused) Color(0xFFF59E0B) else Color(0xFF22C55E)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("倾斜手机控制光标", color = t.TextDim)
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onPause,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (paused) Color(0xFF22C55E) else Color(0xFFF59E0B)
                )
            ) {
                Text(
                    text = if (paused) "继续" else "暂停",
                    fontSize = 20.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onStop,
                shape = RoundedCornerShape(t.KeyRadius.dp)
            ) {
                Text("停止")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 鼠标按键
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onLeftClick() },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = t.KeyBg),
                    shape = RoundedCornerShape(t.KeyRadius.dp)
                ) {
                    Text("左键", fontSize = 14.sp, color = t.TextDark)
                }
                Button(
                    onClick = { onRightClick() },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = t.KeyBg),
                    shape = RoundedCornerShape(t.KeyRadius.dp)
                ) {
                    Text("右键", fontSize = 14.sp, color = t.TextDark)
                }
            }
        }
    }
}

@Composable
private fun ConnectionBar(
    serverUrl: String,
    isConnected: Boolean,
    onUrlChange: (String) -> Unit,
    onToggle: () -> Unit,
    keyRadius: androidx.compose.ui.unit.Dp,
) {
    val t = KeyboardTokens
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(t.Gap.dp)
    ) {
        OutlinedTextField(
            value = serverUrl,
            onValueChange = onUrlChange,
            modifier = Modifier.weight(1f).height(48.dp),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
            enabled = !isConnected,
            shape = RoundedCornerShape(keyRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = t.AccentColor,
                unfocusedBorderColor = Color(0xFFCCCCCC),
                focusedTextColor = t.TextDark,
                unfocusedTextColor = t.TextDark,
                disabledTextColor = t.TextDim
            )
        )
        Button(
            onClick = onToggle,
            modifier = Modifier.height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isConnected) t.ErrorColor else t.AccentColor
            ),
            shape = RoundedCornerShape(keyRadius)
        ) {
            Text(
                if (isConnected) "断开" else "连接",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MicButton(
    isRecording: Boolean,
    onPress: () -> Unit,
    onRelease: () -> Unit,
) {
    val t = KeyboardTokens
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        val micColor = if (isRecording) t.ErrorColor else t.AccentColor
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(micColor)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onPress()
                            tryAwaitRelease()
                            onRelease()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text("🎤", fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            if (isRecording) "正在录音..." else "按住语音输入",
            color = if (isRecording) t.ErrorColor else t.TextDim,
            fontSize = 11.sp
        )
    }
}
