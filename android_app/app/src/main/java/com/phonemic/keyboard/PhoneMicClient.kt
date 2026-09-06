package com.phonemic.keyboard

import android.content.Context
import android.util.Base64
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PhoneMicClient(private val context: Context) {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .pingInterval(15, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    var onStatusChange: ((ConnectionStatus) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onConnected: (() -> Unit)? = null

    private var _status: ConnectionStatus = ConnectionStatus.DISCONNECTED
    val status: ConnectionStatus get() = _status

    private fun setStatus(s: ConnectionStatus) {
        _status = s
        onStatusChange?.invoke(s)
    }

    fun connect(url: String) {
        if (_status != ConnectionStatus.DISCONNECTED) {
            Log.d("PhoneMic", "connect() skipped, status=$_status")
            return
        }
        Log.d("PhoneMic", "Connecting to $url ...")
        setStatus(ConnectionStatus.CONNECTING)

        val request = Request.Builder().url(url).build()
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d("PhoneMic", "WebSocket CONNECTED!")
                setStatus(ConnectionStatus.CONNECTED)
                onConnected?.invoke()
            }
            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    when (json.optString("type")) {
                        "error" -> onError?.invoke(json.optString("msg", "Unknown error"))
                    }
                } catch (_: Exception) { }
            }
            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                ws.close(code, reason)
            }
            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("PhoneMic", "WebSocket FAILED: ${t.message}", t)
                onError?.invoke(t.message ?: "Connection failed")
                setStatus(ConnectionStatus.DISCONNECTED)
            }
            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                setStatus(ConnectionStatus.DISCONNECTED)
            }
        })
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "Normal closure")
        } catch (_: Exception) { }
        webSocket = null
        setStatus(ConnectionStatus.DISCONNECTED)
    }

    fun sendKey(key: String, modifiers: List<String> = emptyList()) {
        Log.d("PhoneMic", "sendKey: key=$key modifiers=$modifiers status=$_status ws=${webSocket != null}")
        if (_status != ConnectionStatus.CONNECTED) {
            Log.e("PhoneMic", "sendKey SKIPPED: not connected")
            return
        }
        val json = JSONObject().apply {
            put("type", "key")
            put("key", key)
            put("modifiers", org.json.JSONArray(modifiers))
        }
        val sent = webSocket?.send(json.toString()) ?: false
        Log.d("PhoneMic", "sendKey sent=$sent json=$json")
    }

    fun sendKeyDown(key: String) {
        if (_status != ConnectionStatus.CONNECTED) return
        val json = JSONObject().apply {
            put("type", "key_down")
            put("key", key)
        }
        webSocket?.send(json.toString())
    }

    fun sendKeyUp(key: String) {
        if (_status != ConnectionStatus.CONNECTED) return
        val json = JSONObject().apply {
            put("type", "key_up")
            put("key", key)
        }
        webSocket?.send(json.toString())
    }

    fun sendButtonDown() {
        if (_status != ConnectionStatus.CONNECTED) return
        val json = JSONObject().apply {
            put("type", "button_down")
        }
        webSocket?.send(json.toString())
        Log.d("PhoneMic", "Sent button_down")
    }

    fun sendButtonUp() {
        if (_status != ConnectionStatus.CONNECTED) return
        val json = JSONObject().apply {
            put("type", "button_up")
        }
        webSocket?.send(json.toString())
        Log.d("PhoneMic", "Sent button_up")
    }

    fun sendAudioData(data: ByteArray) {
        if (_status != ConnectionStatus.CONNECTED) return
        val buffer = okio.Buffer()
        buffer.write(data)
        webSocket?.send(buffer.readByteString())
    }
}