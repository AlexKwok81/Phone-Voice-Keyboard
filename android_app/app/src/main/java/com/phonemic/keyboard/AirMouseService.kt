package com.phonemic.keyboard

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs

class AirMouseService(
    context: Context,
    private val onMove: (Float, Float) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    var enabled = false
        private set
    var paused = false
    var sensitivity = 50
    var deadZone = 0.1f

    private var filteredX = 0f
    private var filteredY = 0f
    private val alpha = 0.3f

    private var calibrationOffsetX = 0f
    private var calibrationOffsetY = 0f
    private var calibrationSamples = mutableListOf<Pair<Float, Float>>()
    private var calibrating = false
    private var calibrationStartTime = 0L

    fun start() {
        if (enabled || gyroscope == null) return
        enabled = true
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME)
        startCalibration()
    }

    fun stop() {
        if (!enabled) return
        enabled = false
        sensorManager.unregisterListener(this)
        filteredX = 0f
        filteredY = 0f
    }

    private fun startCalibration() {
        calibrating = true
        calibrationSamples.clear()
        calibrationStartTime = System.currentTimeMillis()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!enabled || paused) return

        var dx = event.values[0]
        var dy = event.values[1]

        if (calibrating) {
            calibrationSamples.add(Pair(dx, dy))
            if (System.currentTimeMillis() - calibrationStartTime > 1000 && calibrationSamples.size >= 30) {
                calibrationOffsetX = calibrationSamples.map { it.first }.average().toFloat()
                calibrationOffsetY = calibrationSamples.map { it.second }.average().toFloat()
                calibrating = false
            }
            return
        }

        dx -= calibrationOffsetX
        dy -= calibrationOffsetY

        if (abs(dx) < deadZone && abs(dy) < deadZone) return

        // 顺时针旋转90度: new_dx = dy, new_dy = -dx
        val rotDx = dy
        val rotDy = dx
        filteredX = alpha * rotDx + (1 - alpha) * filteredX
        filteredY = alpha * rotDy + (1 - alpha) * filteredY

        val factor = 0.1f + sensitivity / 600.0f
        onMove(filteredX * factor, filteredY * factor)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
