package com.phonemic.keyboard

import android.app.Application
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ConnectionManager(
    private val application: Application,
    private val scope: CoroutineScope,
    private val onStatusChange: (ConnectionStatus) -> Unit,
    private val onError: (String) -> Unit,
) {
    private val client = PhoneMicClient(application)
    private val prefs = application.getSharedPreferences("phone_mic", Context.MODE_PRIVATE)

    val status: ConnectionStatus get() = client.status
    val savedUrl: String get() = prefs.getString("server_url", "localhost:8765") ?: "localhost:8765"

    private var autoReconnectJob: Job? = null
    private var userDisconnected = false

    init {
        client.onStatusChange = { s ->
            onStatusChange(s)
            if (s == ConnectionStatus.CONNECTED) {
                userDisconnected = false
                autoReconnectJob?.cancel()
            } else if (s == ConnectionStatus.DISCONNECTED && !userDisconnected) {
                // autoReconnect will be triggered by ViewModel
            }
        }
        client.onError = onError
    }

    fun autoConnect(onHint: (String) -> Unit = {}) {
        val url = savedUrl.trim()
        if (url.isEmpty()) return
        onHint("正在连接...")
        Log.d("PhoneMic", "Auto-connecting to: ")
        val wsUrl = if (url.startsWith("ws://") || url.startsWith("wss://")) url else "ws://"
        client.connect(wsUrl)
        startAutoReconnect(onHint)
    }

    fun connect(url: String, onHint: (String) -> Unit = {}) {
        if (url.isEmpty()) return
        userDisconnected = false
        prefs.edit().putString("server_url", url).apply()
        onHint("正在连接...")
        val wsUrl = if (url.startsWith("ws://") || url.startsWith("wss://")) url else "ws://"
        client.connect(wsUrl)
        startAutoReconnect(onHint)
    }

    fun disconnect() {
        userDisconnected = true
        autoReconnectJob?.cancel()
        client.disconnect()
    }

    private fun startAutoReconnect(onHint: (String) -> Unit) {
        autoReconnectJob?.cancel()
        autoReconnectJob = scope.launch(Dispatchers.IO) {
            var retryCount = 0
            while (isActive && retryCount < 30) {
                delay(3000)
                retryCount++
                onHint("连接断开，正在重连...()")
                Log.d("PhoneMic", "Auto-reconnect attempt ")
                autoConnect(onHint)
                delay(1500)
                if (client.status == ConnectionStatus.CONNECTED) break
            }
            if (client.status != ConnectionStatus.CONNECTED) {
                onHint("重连失败，请检查服务器是否运行")
            }
        }
    }

    fun sendKey(key: String, modifiers: List<String> = emptyList()) = client.sendKey(key, modifiers)
    fun sendKeyDown(key: String) = client.sendKeyDown(key)
    fun sendKeyUp(key: String) = client.sendKeyUp(key)
    fun sendButtonDown() = client.sendButtonDown()
    fun sendButtonUp() = client.sendButtonUp()
    fun sendAudioData(data: ByteArray) = client.sendAudioData(data)
}
