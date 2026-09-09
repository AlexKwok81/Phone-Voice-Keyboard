package com.phonemic.keyboard

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val conn = ConnectionManager(
        application = application,
        scope = viewModelScope,
        onStatusChange = { s ->
            status.value = s
            if (s == ConnectionStatus.CONNECTED) connectionHint.value = ""
        },
        onError = { error.value = it },
    )

    private val recorder = AudioRecorder(
        scope = viewModelScope,
        onAudioData = { conn.sendAudioData(it) },
        onError = { error.value = it },
    )

    val status = mutableStateOf(ConnectionStatus.DISCONNECTED)
    val error = mutableStateOf<String?>(null)
    val serverUrl = mutableStateOf(conn.savedUrl)
    val isRecording = mutableStateOf(false)
    val connectionHint = mutableStateOf("")
    private val _activeModifiers = mutableStateListOf<String>()
    val activeModifiers: List<String> get() = _activeModifiers

    init {
        viewModelScope.launch(Dispatchers.IO) {
            delay(500)
            conn.autoConnect { connectionHint.value = it }
        }
    }

    fun connect() {
        val url = serverUrl.value.trim()
        if (url.isEmpty()) { error.value = "请输入服务器地址"; return }
        conn.connect(url) { connectionHint.value = it }
    }

    fun disconnect() {
        stopRecording()
        conn.disconnect()
        connectionHint.value = ""
    }

    fun toggleModifier(mod: String) {
        if (_activeModifiers.contains(mod)) _activeModifiers.remove(mod) else _activeModifiers.add(mod)
    }

    fun isModifierActive(mod: String): Boolean = _activeModifiers.contains(mod)

    fun sendKey(key: String) = conn.sendKey(key, _activeModifiers.toList())

    fun sendKey(key: String, modifiers: List<String>) = conn.sendKey(key, modifiers)

    fun startRecording() {
        if (status.value != ConnectionStatus.CONNECTED) return
        if (isRecording.value) return
        conn.sendKeyDown("ctrl")
        conn.sendKeyDown("win")
        conn.sendButtonDown()
        recorder.start()
        isRecording.value = true
    }

    fun stopRecording() {
        if (!isRecording.value) return
        recorder.stop()
        isRecording.value = false
        conn.sendButtonUp()
        conn.sendKeyUp("win")
        conn.sendKeyUp("ctrl")
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
                KeyboardScreen(viewModel)
            }
        }
    }
}
