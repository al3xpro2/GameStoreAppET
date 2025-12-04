package com.example.gamestoreapp.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.pow
import kotlin.math.sqrt

class ShakeDetector(context: Context, private val onShake: () -> Unit) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? = null

    private var lastShakeTime: Long = 0
    private var lastX: Float = 0.0f
    private var lastY: Float = 0.0f
    private var lastZ: Float = 0.0f

    companion object {
        private const val SHAKE_THRESHOLD = 800
        private const val SHAKE_TIMEOUT = 500
    }

    init {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            if ((currentTime - lastShakeTime) > SHAKE_TIMEOUT) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val speed = sqrt((x - lastX).toDouble().pow(2) + (y - lastY).toDouble().pow(2) + (z - lastZ).toDouble().pow(2)) / (currentTime - lastShakeTime) * 10000

                if (speed > SHAKE_THRESHOLD) {
                    lastShakeTime = currentTime
                    onShake()
                }

                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}