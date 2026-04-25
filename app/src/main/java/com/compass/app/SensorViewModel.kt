package com.compass.app

import android.app.Application
import android.content.Context
import android.hardware.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SensorViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _azimuth = MutableLiveData(0f)
    val azimuth: LiveData<Float> = _azimuth

    private var rotationVectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var magnetometerSensor: Sensor? = null

    private var currentAzimuth = 0f
    private val accReading = FloatArray(3)
    private val magReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var hasAcc = false
    private var hasMag = false

    init {
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationVectorSensor == null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            magnetometerSensor  = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        }
    }

    fun registerSensors() {
        rotationVectorSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        } ?: run {
            accelerometerSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
            magnetometerSensor?.let  { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        }
    }

    fun unregisterSensors() = sensorManager.unregisterListener(this)

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val raw = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                smoothUpdate(((raw % 360) + 360) % 360)
            }
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accReading, 0, 3)
                hasAcc = true
                tryAccMag()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, magReading, 0, 3)
                hasMag = true
                tryAccMag()
            }
        }
    }

    private fun tryAccMag() {
        if (!hasAcc || !hasMag) return
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accReading, magReading)) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val raw = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            smoothUpdate(((raw % 360) + 360) % 360)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    private fun smoothUpdate(newAzimuth: Float) {
        var delta = newAzimuth - currentAzimuth
        if (delta > 180) delta -= 360
        if (delta < -180) delta += 360
        currentAzimuth = ((currentAzimuth + delta * 0.25f) % 360 + 360) % 360
        _azimuth.postValue(currentAzimuth)
    }

    override fun onCleared() {
        super.onCleared()
        unregisterSensors()
    }
}
