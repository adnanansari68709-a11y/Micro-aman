package com.example.engine

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class SensorTriggerManager(
    context: Context,
    private val onShakeDetected: () -> Unit,
    private val onFlipFaceDown: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastShakeTime: Long = 0
    private var lastFlipTime: Long = 0
    private var isFaceDown: Boolean = false

    // Shake threshold parameters
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var lastUpdate: Long = 0

    fun startListening() {
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val curTime = System.currentTimeMillis()
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // 1. Shake Detection
        if ((curTime - lastUpdate) > 100) {
            val diffTime = curTime - lastUpdate
            lastUpdate = curTime

            val speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000

            // Shake speed threshold ~ 800 - 1200
            if (speed > 950) {
                if (curTime - lastShakeTime > 1500) { // 1.5 second debounce
                    lastShakeTime = curTime
                    onShakeDetected()
                }
            }

            lastX = x
            lastY = y
            lastZ = z
        }

        // 2. Face Down Flip Detection
        // When face down on table, Z is approximately -9.8 m/s^2
        if (z < -7.5f) {
            if (!isFaceDown && (curTime - lastFlipTime > 2000)) {
                isFaceDown = true
                lastFlipTime = curTime
                onFlipFaceDown()
            }
        } else if (z > 2.0f) {
            isFaceDown = false
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
