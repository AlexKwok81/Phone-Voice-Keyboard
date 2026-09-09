package com.phonemic.keyboard

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

class AudioRecorder(
    private val scope: CoroutineScope,
    private val onAudioData: (ByteArray) -> Unit,
    private val onError: (String) -> Unit,
) {
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null

    val isRecording: Boolean get() = recordingJob?.isActive == true

    fun start() {
        if (isRecording) return

        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            onError("音频初始化失败")
            return
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate, channelConfig, audioFormat, bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                onError("无法启动录音")
                audioRecord?.release()
                audioRecord = null
                return
            }

            recordingJob = scope.launch(Dispatchers.IO) {
                audioRecord?.startRecording()
                val buffer = ByteArray(bufferSize)
                while (isActive) {
                    val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (bytesRead > 0) {
                        onAudioData(buffer.copyOf(bytesRead))
                    }
                }
            }
        } catch (e: SecurityException) {
            onError("无录音权限")
        } catch (e: Exception) {
            onError("录音错误: ")
        }
    }

    fun stop() {
        recordingJob?.cancel()
        recordingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
    }
}
