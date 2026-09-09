package com.phonemic.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

// ===== 设计 Token =====
object KeyboardTokens {
    const val KeyHeight = 56
    const val SmallKeyHeight = 44
    const val ArrowKeySize = 44
    const val Gap = 10
    const val RowGap = 14
    const val KeyRadius = 12

    val BgColor = Color(0xFFF5F5F5)
    val KeyBg = Color(0xFFE8E8E8)
    val KeyLetterBg = Color(0xFFFFFFFF)
    val KeyFnBg = Color(0xFFE0E0E0)
    val KeyArrowBg = Color(0xFFE0E0E0)
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

    val t = KeyboardTokens
    val KeyRadius = t.KeyRadius.dp
    val Gap = t.Gap.dp
    val RowGap = t.RowGap.dp

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = t.BgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(RowGap)
        ) {
            // ─── 连接栏 ───
            ConnectionBar(
                serverUrl = serverUrl,
                isConnected = isConnected,
                onUrlChange = { vm.serverUrl.value = it },
                onToggle = { if (isConnected) vm.disconnect() else vm.connect() },
                keyRadius = KeyRadius,
            )

            // ─── 连接提示 ───
            if (connectionHint.isNotEmpty()) {
                Text(
                    connectionHint,
                    fontSize = 11.sp,
                    color = if (connectionHint.contains("失败")) t.ErrorColor else t.AccentColor,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // ─── 修饰键 ───
            ModifierRow(vm = vm, keyRadius = KeyRadius, gap = Gap)

            // ─── 快捷键 ───
            ShortcutRow(vm = vm, keyRadius = KeyRadius, gap = Gap)

            // ─── 字母键盘 ───
            LetterKeyboard(vm = vm, keyRadius = KeyRadius, gap = Gap)

            // ─── 功能键 ───
            FunctionRow(vm = vm, keyRadius = KeyRadius, gap = Gap)

            // ─── 方向键 + Enter ───
            ArrowAndEnter(vm = vm, keyRadius = KeyRadius)

            Spacer(modifier = Modifier.weight(1f))

            // ─── 语音输入按钮 ───
            MicButton(
                isRecording = isRecording,
                onPress = { vm.startRecording() },
                onRelease = { vm.stopRecording() },
            )
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
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isConnected) t.ErrorColor else t.AccentColor
            ),
            modifier = Modifier.height(48.dp).defaultMinSize(minWidth = 72.dp),
            shape = RoundedCornerShape(keyRadius)
        ) {
            Text(
                if (isConnected) "断开" else "连接",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ModifierRow(vm: MainViewModel, keyRadius: androidx.compose.ui.unit.Dp, gap: androidx.compose.ui.unit.Dp) {
    val t = KeyboardTokens
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(gap)
    ) {
        listOf("ctrl", "alt", "shift", "win").forEach { mod ->
            val isActive = vm.isModifierActive(mod)
            val isShort = mod == "shift" || mod == "win"
            Button(
                onClick = { vm.toggleModifier(mod) },
                modifier = Modifier.weight(1f).height(t.SmallKeyHeight.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) t.AccentColor else t.KeyBg
                ),
                shape = RoundedCornerShape(keyRadius)
            ) {
                Text(
                    mod.replaceFirstChar { it.uppercase() },
                    fontSize = if (isShort) 11.sp else 14.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isActive) Color.White else t.TextDark
                )
            }
        }
    }
}

@Composable
private fun ShortcutRow(vm: MainViewModel, keyRadius: androidx.compose.ui.unit.Dp, gap: androidx.compose.ui.unit.Dp) {
    val t = KeyboardTokens
    val shortcuts = listOf(
        Triple("A", "a", "全选"),
        Triple("C", "c", "复制"),
        Triple("V", "v", "粘贴"),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(gap)
    ) {
        shortcuts.forEach { (label, key, _) ->
            Button(
                onClick = { vm.sendKey(key, listOf("ctrl")) },
                modifier = Modifier.weight(1f).height(t.KeyHeight.dp),
                colors = ButtonDefaults.buttonColors(containerColor = t.AccentColor),
                shape = RoundedCornerShape(keyRadius),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    "Ctrl+",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun LetterKeyboard(vm: MainViewModel, keyRadius: androidx.compose.ui.unit.Dp, gap: androidx.compose.ui.unit.Dp) {
    val t = KeyboardTokens
    val rows = listOf(
        listOf("q","w","e","r","t","y","u","i","o","p"),
        listOf("a","s","d","f","g","h","j","k","l"),
        listOf("z","x","c","v","b","n","m"),
    )
    rows.forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            row.forEach { letter ->
                Button(
                    onClick = { vm.sendKey(letter) },
                    modifier = Modifier.weight(1f).height(t.KeyHeight.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = t.KeyLetterBg),
                    shape = RoundedCornerShape(keyRadius),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    Text(
                        letter.uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = t.TextDark
                    )
                }
            }
        }
    }
}

@Composable
private fun FunctionRow(vm: MainViewModel, keyRadius: androidx.compose.ui.unit.Dp, gap: androidx.compose.ui.unit.Dp) {
    val t = KeyboardTokens
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(gap)
    ) {
        listOf(
            Triple("Tab", "tab", 1f),
            Triple("Esc", "escape", 1f),
            Triple("Bksp", "backspace", 1f)
        ).forEach { (label, key, weight) ->
            Button(
                onClick = { vm.sendKey(key) },
                modifier = Modifier.weight(weight).height(t.SmallKeyHeight.dp),
                colors = ButtonDefaults.buttonColors(containerColor = t.KeyFnBg),
                shape = RoundedCornerShape(keyRadius)
            ) {
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = t.TextDark)
            }
        }
    }
}

@Composable
private fun ArrowAndEnter(vm: MainViewModel, keyRadius: androidx.compose.ui.unit.Dp) {
    val t = KeyboardTokens
    val arrowSize = t.ArrowKeySize.dp
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { vm.sendKey("up") },
                modifier = Modifier.size(arrowSize),
                colors = ButtonDefaults.buttonColors(containerColor = t.KeyArrowBg),
                shape = RoundedCornerShape(keyRadius),
                contentPadding = PaddingValues(0.dp)
            ) { Text("↑", fontSize = 18.sp, color = t.TextDark) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { vm.sendKey("left") },
                modifier = Modifier.size(arrowSize),
                colors = ButtonDefaults.buttonColors(containerColor = t.KeyArrowBg),
                shape = RoundedCornerShape(keyRadius),
                contentPadding = PaddingValues(0.dp)
            ) { Text("←", fontSize = 14.sp, color = t.TextDark) }
            Button(
                onClick = { vm.sendKey("down") },
                modifier = Modifier.size(arrowSize),
                colors = ButtonDefaults.buttonColors(containerColor = t.KeyArrowBg),
                shape = RoundedCornerShape(keyRadius),
                contentPadding = PaddingValues(0.dp)
            ) { Text("↓", fontSize = 18.sp, color = t.TextDark) }
            Button(
                onClick = { vm.sendKey("right") },
                modifier = Modifier.size(arrowSize),
                colors = ButtonDefaults.buttonColors(containerColor = t.KeyArrowBg),
                shape = RoundedCornerShape(keyRadius),
                contentPadding = PaddingValues(0.dp)
            ) { Text("→", fontSize = 14.sp, color = t.TextDark) }
        }
        Button(
            onClick = { vm.sendKey("enter") },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = t.AccentColor),
            shape = RoundedCornerShape(keyRadius)
        ) {
            Text("Enter", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
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
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        val micColor = if (isRecording) t.ErrorColor else t.AccentColor
        Box(
            modifier = Modifier
                .size(80.dp)
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
            Text("🎤", fontSize = 26.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            if (isRecording) "正在录音..." else "按住语音输入",
            color = if (isRecording) t.ErrorColor else t.TextDim,
            fontSize = 12.sp
        )
    }
}
