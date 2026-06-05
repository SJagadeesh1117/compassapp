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

    private val _pitch = MutableLiveData(0f)
    val pitch: LiveData<Float> = _pitch

    private val _roll = MutableLiveData(0f)
    val roll: LiveData<Float> = _roll

    private val _accuracy = MutableLiveData(SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
    val accuracy: LiveData<Int> = _accuracy

    private val _useTrueNorth = MutableLiveData(false)
    val useTrueNorth: LiveData<Boolean> = _useTrueNorth

    private var declination = 0f
    private var rawSmoothedAzimuth = 0f

    private var rotationVectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var magnetometerSensor: Sensor? = null

    private var currentPitch = 0f
    private var currentRoll = 0f

    private val accReading = FloatArray(3)
    private val magReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var hasAcc = false
    private var hasMag = false

    init {
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometerSensor  = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    fun registerSensors() {
        rotationVectorSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        } ?: run {
            accelerometerSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        }
        // Always register magnetic field sensor to monitor its accuracy/calibration status
        magnetometerSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    fun unregisterSensors() = sensorManager.unregisterListener(this)

    fun setUseTrueNorth(use: Boolean) {
        _useTrueNorth.value = use
        postUpdatedAzimuth()
    }

    fun setLocation(latitude: Double, longitude: Double, altitude: Double) {
        val geomag = GeomagneticField(
            latitude.toFloat(),
            longitude.toFloat(),
            altitude.toFloat(),
            System.currentTimeMillis()
        )
        declination = geomag.declination
        postUpdatedAzimuth()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            _accuracy.postValue(event.accuracy)
        }
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val rawAzimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                val rawPitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
                val rawRoll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
                smoothUpdate(((rawAzimuth % 360) + 360) % 360, rawPitch, rawRoll)
            }
            Sensor.TYPE_ACCELEROMETER -> {
                if (rotationVectorSensor == null) {
                    System.arraycopy(event.values, 0, accReading, 0, 3)
                    hasAcc = true
                    tryAccMag()
                }
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                if (rotationVectorSensor == null) {
                    System.arraycopy(event.values, 0, magReading, 0, 3)
                    hasMag = true
                    tryAccMag()
                }
            }
        }
    }

    private fun tryAccMag() {
        if (!hasAcc || !hasMag) return
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accReading, magReading)) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val rawAzimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            val rawPitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
            val rawRoll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
            smoothUpdate(((rawAzimuth % 360) + 360) % 360, rawPitch, rawRoll)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            _accuracy.postValue(accuracy)
        }
    }

    private fun smoothUpdate(newAzimuth: Float, newPitch: Float, newRoll: Float) {
        // Azimuth smoothing
        var delta = newAzimuth - rawSmoothedAzimuth
        if (delta > 180) delta -= 360
        if (delta < -180) delta += 360
        rawSmoothedAzimuth = ((rawSmoothedAzimuth + delta * 0.25f) % 360 + 360) % 360
        postUpdatedAzimuth()

        // Pitch & Roll smoothing
        currentPitch = currentPitch + (newPitch - currentPitch) * 0.25f
        currentRoll = currentRoll + (newRoll - currentRoll) * 0.25f
        _pitch.postValue(currentPitch)
        _roll.postValue(currentRoll)
    }

    private fun postUpdatedAzimuth() {
        val finalAzimuth = if (_useTrueNorth.value == true) {
            ((rawSmoothedAzimuth + declination) % 360 + 360) % 360
        } else {
            rawSmoothedAzimuth
        }
        _azimuth.postValue(finalAzimuth)
    }

    override fun onCleared() {
        super.onCleared()
        unregisterSensors()
    }
}
