package com.example.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkTacticalBackground
import com.example.ui.theme.DarkTacticalBorder
import com.example.ui.theme.DarkTacticalSurface
import com.example.ui.theme.GarminAmber
import com.example.ui.theme.GarminCyan
import com.example.ui.theme.GarminEmerald
import com.example.ui.theme.GarminOrange
import com.example.ui.theme.GarminRed
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Custom composable that reads sensor data from the device to display a real-time digital compass for orientation.
 *
 * Automatically registers and unregisters hardware sensors (Rotation Vector, Accelerometer, Magnetometer)
 * using [DisposableEffect], applies low-pass smoothing, and renders high-precision orientation visuals.
 */
@Composable
fun RealtimeDigitalCompass(
    modifier: Modifier = Modifier,
    targetBearing: Float? = null,
    showTelemetryBadge: Boolean = true,
    showLevelBubble: Boolean = true,
    onHeadingChanged: ((Float) -> Unit)? = null
) {
    val context = LocalContext.current

    // Real-time sensor state directly managed inside the composable
    var currentHeading by remember { mutableFloatStateOf(0f) }
    var currentPitch by remember { mutableFloatStateOf(0f) }
    var currentRoll by remember { mutableFloatStateOf(0f) }
    var sensorAccuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasGeomagnetic = false
        var smoothedAzimuth = 0f
        val smoothingFactor = 0.18f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientationAngles)
                        val rawDeg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                        val normalized = (rawDeg + 360f) % 360f

                        // Shortest angular distance smoothing
                        var diff = (normalized - smoothedAzimuth) % 360f
                        if (diff > 180f) diff -= 360f
                        if (diff < -180f) diff += 360f
                        smoothedAzimuth = (smoothedAzimuth + smoothingFactor * diff + 360f) % 360f

                        currentHeading = smoothedAzimuth
                        currentPitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
                        currentRoll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
                        sensorAccuracy = event.accuracy

                        onHeadingChanged?.invoke(smoothedAzimuth)
                    }

                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(event.values, 0, gravity, 0, 3)
                        hasGravity = true
                        calculateFallback(event.accuracy)
                    }

                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                        hasGeomagnetic = true
                        calculateFallback(event.accuracy)
                    }
                }
            }

            private fun calculateFallback(accuracy: Int) {
                if (hasGravity && hasGeomagnetic) {
                    val r = FloatArray(9)
                    val i = FloatArray(9)
                    if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                        SensorManager.getOrientation(r, orientationAngles)
                        val rawDeg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                        val normalized = (rawDeg + 360f) % 360f

                        var diff = (normalized - smoothedAzimuth) % 360f
                        if (diff > 180f) diff -= 360f
                        if (diff < -180f) diff += 360f
                        smoothedAzimuth = (smoothedAzimuth + smoothingFactor * diff + 360f) % 360f

                        currentHeading = smoothedAzimuth
                        currentPitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
                        currentRoll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()
                        sensorAccuracy = accuracy
                        onHeadingChanged?.invoke(smoothedAzimuth)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                sensorAccuracy = accuracy
            }
        }

        if (rotationVectorSensor != null) {
            sensorManager.registerListener(listener, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME)
        } else {
            accelSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
            magnetSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Smooth visual animation
    val animatedHeading by animateFloatAsState(
        targetValue = currentHeading,
        animationSpec = spring(stiffness = 600f),
        label = "animatedHeading"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("realtime_digital_compass"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Digital Heading Header
        val cardinalName = getDetailedCardinalDirection(animatedHeading)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${animatedHeading.toInt()}°",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = cardinalName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GarminOrange
                    )
                }
            }

            // Accuracy badge
            val (accColor, accText) = when (sensorAccuracy) {
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> Pair(GarminEmerald, "AKURAT (TINGGI)")
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> Pair(GarminAmber, "AKURASI SEDANG")
                else -> Pair(GarminRed, "KALIBRASI DIPERLUKAN")
            }

            Surface(
                color = accColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, accColor.copy(alpha = 0.6f))
            ) {
                Text(
                    text = accText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = accColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Main Animated Compass Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .aspectRatio(1f)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = min(size.width, size.height) / 2f - 16.dp.toPx()

                // Outer Bezel Layer
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1E293B), Color(0xFF0B132B)),
                        center = center,
                        radius = radius + 14.dp.toPx()
                    ),
                    radius = radius + 12.dp.toPx(),
                    center = center
                )

                // Bezel outer ring
                drawCircle(
                    color = GarminOrange.copy(alpha = 0.8f),
                    radius = radius + 10.dp.toPx(),
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // Sub-bezel ring
                drawCircle(
                    color = Color(0xFF334155),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Rotating Dial Rose (Rotates in reverse of device heading)
                rotate(degrees = -animatedHeading, pivot = center) {
                    // Draw 360 degree ticks
                    for (deg in 0 until 360 step 5) {
                        val rad = Math.toRadians(deg.toDouble())
                        val isCardinal = deg % 90 == 0
                        val isIntercardinal = deg % 45 == 0 && !isCardinal
                        val isMajor = deg % 30 == 0

                        val tickLength = when {
                            isCardinal -> 16.dp.toPx()
                            isIntercardinal -> 12.dp.toPx()
                            isMajor -> 9.dp.toPx()
                            else -> 5.dp.toPx()
                        }

                        val strokeWidth = when {
                            isCardinal -> 3f
                            isIntercardinal -> 2f
                            isMajor -> 1.5f
                            else -> 1f
                        }

                        val tickColor = when {
                            deg == 0 -> GarminRed
                            isCardinal -> Color.White
                            isIntercardinal -> GarminAmber
                            isMajor -> Color(0xFFCBD5E1)
                            else -> Color(0xFF475569)
                        }

                        val startX = (center.x + (radius - tickLength) * sin(rad)).toFloat()
                        val startY = (center.y - (radius - tickLength) * cos(rad)).toFloat()
                        val endX = (center.x + radius * sin(rad)).toFloat()
                        val endY = (center.y - radius * cos(rad)).toFloat()

                        drawLine(
                            color = tickColor,
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }

                    // Draw Target Bearing Marker on dial if set
                    if (targetBearing != null) {
                        val tRad = Math.toRadians(targetBearing.toDouble())
                        val tDist = radius - 26.dp.toPx()
                        val tX = (center.x + tDist * sin(tRad)).toFloat()
                        val tY = (center.y - tDist * cos(tRad)).toFloat()

                        drawCircle(
                            color = GarminCyan,
                            radius = 7.dp.toPx(),
                            center = Offset(tX, tY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = Offset(tX, tY)
                        )
                    }
                }

                // Fixed Center Crosshairs
                drawLine(
                    color = Color(0x33FFFFFF),
                    start = Offset(center.x - radius * 0.75f, center.y),
                    end = Offset(center.x + radius * 0.75f, center.y),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color(0x33FFFFFF),
                    start = Offset(center.x, center.y - radius * 0.75f),
                    end = Offset(center.x, center.y + radius * 0.75f),
                    strokeWidth = 1.dp.toPx()
                )

                // Top Fixed Heading Pointer
                val topPointer = Path().apply {
                    moveTo(center.x, center.y - radius - 10.dp.toPx())
                    lineTo(center.x + 8.dp.toPx(), center.y - radius + 4.dp.toPx())
                    lineTo(center.x - 8.dp.toPx(), center.y - radius + 4.dp.toPx())
                    close()
                }
                drawPath(path = topPointer, color = GarminOrange)

                // North Arrow
                val northNeedle = Path().apply {
                    moveTo(center.x, center.y - radius * 0.65f)
                    lineTo(center.x + 7.dp.toPx(), center.y)
                    lineTo(center.x - 7.dp.toPx(), center.y)
                    close()
                }
                drawPath(
                    path = northNeedle,
                    brush = Brush.verticalGradient(listOf(GarminRed, GarminOrange))
                )

                // South Needle
                val southNeedle = Path().apply {
                    moveTo(center.x, center.y + radius * 0.65f)
                    lineTo(center.x + 7.dp.toPx(), center.y)
                    lineTo(center.x - 7.dp.toPx(), center.y)
                    close()
                }
                drawPath(
                    path = southNeedle,
                    brush = Brush.verticalGradient(listOf(Color(0xFF64748B), Color(0xFFF8FAFC)))
                )

                // Center Pin & Pivot
                drawCircle(color = Color(0xFF0F172A), radius = 12.dp.toPx(), center = center)
                drawCircle(color = GarminOrange, radius = 6.dp.toPx(), center = center)
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = center)

                // Inclinometer / Level Bubble in center if enabled
                if (showLevelBubble) {
                    val maxOffset = 22.dp.toPx()
                    val bubbleX = (center.x + (currentRoll / 45f).coerceIn(-1f, 1f) * maxOffset)
                    val bubbleY = (center.y + (currentPitch / 45f).coerceIn(-1f, 1f) * maxOffset)

                    drawCircle(
                        color = Color(0x3300E5FF),
                        radius = 24.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = GarminCyan.copy(alpha = 0.5f),
                        radius = 4.dp.toPx(),
                        center = Offset(bubbleX, bubbleY)
                    )
                }
            }
        }

        // Secondary Telemetry Bar: Pitch & Roll Inclination
        if (showTelemetryBadge) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkTacticalBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PITCH (INKLINASI)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "${currentPitch.toInt()}°",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (kotlin.math.abs(currentPitch) <= 5) GarminEmerald else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkTacticalBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ROLL (KEMIRINGAN)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "${currentRoll.toInt()}°",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (kotlin.math.abs(currentRoll) <= 5) GarminEmerald else Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun getDetailedCardinalDirection(degrees: Float): String {
    val deg = (degrees % 360 + 360) % 360
    return when {
        deg >= 348.75 || deg < 11.25 -> "U (Utara)"
        deg < 33.75 -> "Utara Timur Laut (NNE)"
        deg < 56.25 -> "Timur Laut (NE)"
        deg < 78.75 -> "Timur Timur Laut (ENE)"
        deg < 101.25 -> "T (Timur)"
        deg < 123.75 -> "Timur Menenggara (ESE)"
        deg < 146.25 -> "Tenggara (SE)"
        deg < 168.75 -> "Selatan Menenggara (SSE)"
        deg < 191.25 -> "S (Selatan)"
        deg < 213.75 -> "Selatan Barat Daya (SSW)"
        deg < 236.25 -> "Barat Daya (SW)"
        deg < 258.75 -> "Barat Barat Daya (WSW)"
        deg < 281.25 -> "B (Barat)"
        deg < 303.75 -> "Barat Barat Laut (WNW)"
        deg < 326.25 -> "Barat Laut (NW)"
        else -> "Utara Barat Laut (NNW)"
    }
}
