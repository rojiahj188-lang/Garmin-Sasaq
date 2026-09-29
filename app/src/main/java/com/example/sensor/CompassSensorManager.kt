package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class CompassData(
    val azimuthDegrees: Float = 0f,
    val pitchDegrees: Float = 0f,
    val rollDegrees: Float = 0f,
    val accuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH
)

class CompassSensorManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometerSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val pressureSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    private val _compassData = MutableStateFlow(CompassData())
    val compassData: StateFlow<CompassData> = _compassData.asStateFlow()

    private val _barometerHpa = MutableStateFlow<Float?>(null)
    val barometerHpa: StateFlow<Float?> = _barometerHpa.asStateFlow()

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Exponential smoothing
    private var smoothAzimuth = 0f
    private val alpha = 0.15f

    fun start() {
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelerometerSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            magneticSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
        pressureSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val rawAzimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                val normalizedAzimuth = (rawAzimuth + 360f) % 360f
                val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
                val roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

                smoothAzimuth = smoothHeading(smoothAzimuth, normalizedAzimuth, alpha)
                _compassData.value = CompassData(
                    azimuthDegrees = smoothAzimuth,
                    pitchDegrees = pitch,
                    rollDegrees = roll,
                    accuracy = event.accuracy
                )
            }

            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, gravity, 0, 3)
                hasGravity = true
                computeOrientationFallback(event.accuracy)
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                hasGeomagnetic = true
                computeOrientationFallback(event.accuracy)
            }

            Sensor.TYPE_PRESSURE -> {
                _barometerHpa.value = event.values[0]
            }
        }
    }

    private fun computeOrientationFallback(accuracy: Int) {
        if (hasGravity && hasGeomagnetic) {
            val r = FloatArray(9)
            val i = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                val rawAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                val normalizedAzimuth = (rawAzimuth + 360f) % 360f
                smoothAzimuth = smoothHeading(smoothAzimuth, normalizedAzimuth, alpha)
                _compassData.value = CompassData(
                    azimuthDegrees = smoothAzimuth,
                    pitchDegrees = Math.toDegrees(orientation[1].toDouble()).toFloat(),
                    rollDegrees = Math.toDegrees(orientation[2].toDouble()).toFloat(),
                    accuracy = accuracy
                )
            }
        }
    }

    private fun smoothHeading(oldAngle: Float, newAngle: Float, factor: Float): Float {
        var diff = (newAngle - oldAngle) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return (oldAngle + factor * diff + 360f) % 360f
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handled in onSensorChanged
    }

    companion object {
        const val KAABA_LATITUDE = 21.422487
        const val KAABA_LONGITUDE = 39.826206

        /**
         * Calculate Great Circle initial bearing from current Lat/Lon to Target Lat/Lon
         */
        fun calculateBearing(fromLat: Double, fromLon: Double, toLat: Double, toLon: Double): Float {
            val phi1 = Math.toRadians(fromLat)
            val phi2 = Math.toRadians(toLat)
            val deltaLambda = Math.toRadians(toLon - fromLon)

            val y = sin(deltaLambda) * cos(phi2)
            val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
            val theta = atan2(y, x)
            val bearing = Math.toDegrees(theta)
            return ((bearing + 360.0) % 360.0).toFloat()
        }

        /**
         * Calculate Great Circle distance between two points in meters
         */
        fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val earthRadius = 6371000.0 // meters
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
            return earthRadius * c
        }
    }
}
