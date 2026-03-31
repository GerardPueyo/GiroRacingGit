package com.example.giroracing.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class AccelerometerHandler(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    fun start(onTiltChanged: (Float) -> Unit): SensorEventListener {
        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    val tiltX = event.values[0]
                    onTiltChanged(tiltX)
                }
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        return sensorListener
    }

    fun stop(listener: SensorEventListener) {
        sensorManager.unregisterListener(listener)
    }
}