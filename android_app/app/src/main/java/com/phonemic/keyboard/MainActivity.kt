package com.phonemic.keyboard

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val client = PhoneMicClient(application)
    private val prefs = application.getSharedPreferences("phone_mic", android.content.Context.MODE_PRIVATE)

    val status = mutableStateOf(ConnectionStatus.DISCONNECTED)
    val error = mutableStateOf<String?>(null)
    val serverUrl = mutableStateOf(prefs.getString("server_url", "localhost:8765") ?: "localhost:8765")
    val isRecording = mutableStateOf(false)
    val connectionHint = mutableStateOf("")
    private val _activeModifiers = mutableStateListOf<String>()
    val activeModifiers: List<String> get() = _activeModifiers

    private var audioRecord: AudioRecord? = null
    private var recordingJob: kotlinx.coroutines.Job? = null
    private var autoReconnectJob: kotlinx.coroutines.Job? = null
    private var userDisconnected = false

    init {
        client.onStatusChange = { s ->
            status.value = s
            if (s == ConnectionStatus.CONNECTED) {
                connectionHint.value = ""
                userDisconnected = false
                autoReconnectJob?.cancel()
            } else if (s == ConnectionStatus.DISCONNECTED && !userDisconnected) {
                startAutoReconnect()
            }
        }
        client.onError = { error.value = it }
        // 启动时自动连接
        viewModelScope.launch(Dispatchers.IO) {
            delay(500)
            autoConnect()
        }
    }

    private fun autoConnect() {
        val url = serverUrl.value.trim()
        if (url.isEmpty()) return
        connectionHint.value = "正在连接..."
        Log.d("PhoneMic", "Auto-connecting to: $url")
        val wsUrl = if (url.startsWith("ws://") || url.startsWith("wss://")) url else "ws://$url"
        Log.d("PhoneMic", "WebSocket URL: $wsUrl")
        client.connect(wsUrl)
    }

    private fun startAutoReconnect() {
        autoReconnectJob?.cancel()
        autoReconnectJob = viewModelScope.launch(Dispatchers.IO) {
            var retryCount = 0
            while (isActive && retryCount < 30) {
                delay(3000)
                retryCount++
                connectionHint.value = "连接断开，正在重连...($retryCount)"
                Log.d("PhoneMic", "Auto-reconnect attempt $retryCount")
                autoConnect()
                // 等待一下看是否连上了
                delay(1500)
                if (status.value == ConnectionStatus.CONNECTED) break
            }
            if (status.value != ConnectionStatus.CONNECTED) {
                connectionHint.value = "重连失败，请检查服务器是否运行"
            }
        }
    }

    fun connect() {
        val url = serverUrl.value.trim()
        if (url.isEmpty()) { error.value = "请输入服务器地址"; return }
        userDisconnected = false
        // 保存地址
        prefs.edit().putString("server_url", url).apply()
        serverUrl.value = url
        autoConnect()
    }

    fun disconnect() {
        userDisconnected = true
        autoReconnectJob?.cancel()
        connectionHint.value = ""
        stopRecording()
        client.disconnect()
    }

    fun toggleModifier(mod: String) {
        if (_activeModifiers.contains(mod)) _activeModifiers.remove(mod) else _activeModifiers.add(mod)
    }

    fun isModifierActive(mod: String): Boolean = _activeModifiers.contains(mod)

    fun sendKey(key: String) {
        client.sendKey(key, _activeModifiers.toList())
    }

    fun sendKey(key: String, modifiers: List<String>) {
        client.sendKey(key, modifiers)
    }

    fun startRecording() {
        if (status.value != ConnectionStatus.CONNECTED) return
        if (isRecording.value) return
        // 长按 Ctrl+Win 触发微信输入法语音输入
        client.sendKeyDown("ctrl")
        client.sendKeyDown("win")

        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            error.value = "音频初始化失败"
            return
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate, channelConfig, audioFormat, bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                error.value = "无法启动录音"
                audioRecord?.release()
                audioRecord = null
                return
            }

            isRecording.value = true
            client.sendButtonDown()

            recordingJob = viewModelScope.launch(Dispatchers.IO) {
                audioRecord?.startRecording()
                val buffer = ByteArray(bufferSize)
                while (isActive && isRecording.value) {
                    val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (bytesRead > 0) {
                        val data = buffer.copyOf(bytesRead)
                        client.sendAudioData(data)
                    }
                }
            }
        } catch (e: SecurityException) {
            error.value = "无录音权限"
        } catch (e: Exception) {
            error.value = "录音错误: ${e.message}"
        }
    }

    fun stopRecording() {
        if (!isRecording.value) return
        isRecording.value = false
        recordingJob?.cancel()
        recordingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
        client.sendButtonUp()
        // 释放 Ctrl+Win
        client.sendKeyUp("win")
        client.sendKeyUp("ctrl")
    }

    fun clearError() { error.value = null }
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            viewModel.error.value = "需要录音权限才能使用语音输入"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermission.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    background = Color(0xFFF5F5F5),
                    surface = Color(0xFFFFFFFF),
                    primary = Color(0xFF1A9E8F),
                    secondary = Color(0xFF3B82F6),
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    KeyboardScreen(viewModel)
                }
            }
        }
    }
}

@Composable
private fun KeyboardScreen(vm: MainViewModel) {
    val status by vm.status
    val error by vm.error
    val serverUrl by vm.serverUrl
    val isConnected = status == ConnectionStatus.CONNECTED
    val isRecording by vm.isRecording
    val connectionHint by vm.connectionHint

    // === 设计系统 ===
    val KeyHeight      = 56.dp
    val SmallKeyHeight = 44.dp
    val ArrowKeySize   = 44.dp
    val Gap            = 10.dp
    val RowGap         = 14.dp
    val KeyRadius      = 12.dp

    // 浅色主题颜色
    val BgColor        = Color(0xFFF5F5F5)
    val KeyBg          = Color(0xFFE8E8E8)
    val KeyLetterBg    = Color(0xFFFFFFFF)
    val KeyFnBg        = Color(0xFFE0E0E0)
    val KeyArrowBg     = Color(0xFFE0E0E0)
    val AccentColor    = Color(0xFF1A9E8F) // 青绿色
    val TextDark       = Color(0xFF333333)
    val TextDim        = Color(0xFF888888)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(RowGap)
        ) {
            // ─── 连接栏 ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Gap)
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
                        focusedBorderColor = AccentColor,
                        unfocusedBorderColor = Color(0xFFCCCCCC),
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        disabledTextColor = TextDim
                    )
                )
                Button(
                    onClick = { if (isConnected) vm.disconnect() else vm.connect() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConnected) Color(0xFFDC2626) else AccentColor
                    ),
                    modifier = Modifier.height(48.dp).defaultMinSize(minWidth = 72.dp),
                    shape = RoundedCornerShape(KeyRadius)
                ) {
                    Text(
                        if (isConnected) "断开" else "连接",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            // ─── 连接提示 ───
            val hint by vm.connectionHint
            if (hint.isNotEmpty()) {
                Text(
                    hint,
                    fontSize = 11.sp,
                    color = if (hint.contains("失败")) Color(0xFFDC2626) else AccentColor,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // ─── 修饰键 (Ctrl / Alt / Shift / Win) ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Gap)
            ) {
                listOf("ctrl", "alt", "shift", "win").forEach { mod ->
                    val isActive = vm.isModifierActive(mod)
                    val isShort = mod == "shift" || mod == "win"
                    Button(
                        onClick = { vm.toggleModifier(mod) },
                        modifier = Modifier.weight(1f).height(SmallKeyHeight),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) AccentColor else KeyBg
                        ),
                        shape = RoundedCornerShape(KeyRadius)
                    ) {
                        Text(
                            mod.replaceFirstChar { it.uppercase() },
                            fontSize = if (isShort) 11.sp else 14.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isActive) Color.White else TextDark
                        )
                    }
                }
            }

            // ─── 快捷键 (Ctrl+A / Ctrl+C / Ctrl+V) ───
            val shortcuts = listOf(
                Triple("A", "a", "全选"),
                Triple("C", "c", "复制"),
                Triple("V", "v", "粘贴"),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Gap)
            ) {
                shortcuts.forEach { (label, key, desc) ->
                    Button(
                        onClick = { vm.sendKey(key, listOf("ctrl")) },
                        modifier = Modifier.weight(1f).height(KeyHeight),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                        shape = RoundedCornerShape(KeyRadius),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            "Ctrl+$label",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // ─── 功能键 (Tab / Esc / Bksp) ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Gap)
            ) {
                listOf(
                    Triple("Tab", "tab", 1f),
                    Triple("Esc", "escape", 1f),
                    Triple("Bksp", "backspace", 1f)
                ).forEach { (label, key, weight) ->
                    Button(
                        onClick = { vm.sendKey(key) },
                        modifier = Modifier.weight(weight).height(SmallKeyHeight),
                        colors = ButtonDefaults.buttonColors(containerColor = KeyFnBg),
                        shape = RoundedCornerShape(KeyRadius)
                    ) {
                        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextDark)
                    }
                }
            }

            // ─── 方向键 + Enter ───
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 十字方向键
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // ↑
                    Button(
                        onClick = { vm.sendKey("up") },
                        modifier = Modifier.size(ArrowKeySize),
                        colors = ButtonDefaults.buttonColors(containerColor = KeyArrowBg),
                        shape = RoundedCornerShape(KeyRadius),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("↑", fontSize = 18.sp, color = TextDark) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { vm.sendKey("left") },
                        modifier = Modifier.size(ArrowKeySize),
                        colors = ButtonDefaults.buttonColors(containerColor = KeyArrowBg),
                        shape = RoundedCornerShape(KeyRadius),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("←", fontSize = 14.sp, color = TextDark) }
                    Button(
                        onClick = { vm.sendKey("down") },
                        modifier = Modifier.size(ArrowKeySize),
                        colors = ButtonDefaults.buttonColors(containerColor = KeyArrowBg),
                        shape = RoundedCornerShape(KeyRadius),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("↓", fontSize = 18.sp, color = TextDark) }
                    Button(
                        onClick = { vm.sendKey("right") },
                        modifier = Modifier.size(ArrowKeySize),
                        colors = ButtonDefaults.buttonColors(containerColor = KeyArrowBg),
                        shape = RoundedCornerShape(KeyRadius),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("→", fontSize = 14.sp, color = TextDark) }
                }
                // Enter（全宽）
                Button(
                    onClick = { vm.sendKey("enter") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                    shape = RoundedCornerShape(KeyRadius)
                ) {
                    Text("Enter", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ─── 语音输入按钮 ───
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                val micColor = if (isRecording) Color(0xFFDC2626) else AccentColor
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(micColor)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    vm.startRecording()
                                    tryAwaitRelease()
                                    vm.stopRecording()
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎤", fontSize = 26.dp.value.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    if (isRecording) "正在录音..." else "按住语音输入",
                    color = if (isRecording) Color(0xFFDC2626) else TextDim,
                    fontSize = 12.sp
                )
            }
        }
    }
}