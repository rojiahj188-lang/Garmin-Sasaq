package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LocationEntity
import com.example.model.LocationCategory
import com.example.model.PriorityLevel
import com.example.model.UnitSystem
import com.example.sensor.CompassSensorManager
import com.example.sensor.LocationServiceManager
import com.example.ui.theme.DarkTacticalBorder
import com.example.ui.theme.DarkTacticalSurface
import com.example.ui.theme.GarminAmber
import com.example.ui.theme.GarminCyan
import com.example.ui.theme.GarminEmerald
import com.example.ui.theme.GarminOrange
import com.example.ui.theme.GarminRed
import java.util.Locale
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
fun GarminCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    badgeText: String? = null,
    badgeColor: Color = GarminOrange,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkTacticalBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkTacticalSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (title != null || badgeText != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (title != null) {
                        Text(
                            text = title.uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = GarminOrange
                        )
                    }
                    if (badgeText != null) {
                        Surface(
                            color = badgeColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            content()
        }
    }
}

@Composable
fun CompassDial(
    azimuthDegrees: Float,
    targetBearing: Float? = null,
    qiblaBearing: Float? = null,
    isQiblaAligned: Boolean = false,
    modifier: Modifier = Modifier
) {
    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuthDegrees,
        animationSpec = tween(durationMillis = 200),
        label = "azimuth"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - 16.dp.toPx()

            // Outer dark bezel with gradient ring
            drawCircle(
                color = Color(0xFF0F172A),
                radius = radius + 12.dp.toPx(),
                center = center
            )
            drawCircle(
                color = if (isQiblaAligned) GarminEmerald else GarminOrange.copy(alpha = 0.6f),
                radius = radius + 8.dp.toPx(),
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF1E293B),
                radius = radius,
                center = center
            )

            // Rotate compass dial against device heading
            rotate(degrees = -animatedAzimuth, pivot = center) {
                // Draw 360 ticks
                for (angle in 0 until 360 step 5) {
                    val angleRad = Math.toRadians(angle.toDouble())
                    val isMajor = angle % 30 == 0
                    val isCardinal = angle % 90 == 0
                    val tickLen = when {
                        isCardinal -> 14.dp.toPx()
                        isMajor -> 10.dp.toPx()
                        else -> 5.dp.toPx()
                    }
                    val strokeW = when {
                        isCardinal -> 2.5f
                        isMajor -> 1.8f
                        else -> 1f
                    }
                    val tickColor = when {
                        angle == 0 -> GarminRed
                        isCardinal -> Color.White
                        isMajor -> GarminAmber.copy(alpha = 0.8f)
                        else -> Color(0xFF64748B)
                    }

                    val startX = (center.x + (radius - tickLen) * sin(angleRad)).toFloat()
                    val startY = (center.y - (radius - tickLen) * cos(angleRad)).toFloat()
                    val endX = (center.x + radius * sin(angleRad)).toFloat()
                    val endY = (center.y - radius * cos(angleRad)).toFloat()

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }

                // If Qibla bearing exists, draw Kaaba Marker on dial
                if (qiblaBearing != null) {
                    val qRad = Math.toRadians(qiblaBearing.toDouble())
                    val qDist = radius - 24.dp.toPx()
                    val qX = (center.x + qDist * sin(qRad)).toFloat()
                    val qY = (center.y - qDist * cos(qRad)).toFloat()

                    drawCircle(
                        color = GarminEmerald,
                        radius = 8.dp.toPx(),
                        center = Offset(qX, qY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(qX, qY)
                    )
                }

                // If generic Navigation Target exists, draw Cyan Marker on dial
                if (targetBearing != null) {
                    val tRad = Math.toRadians(targetBearing.toDouble())
                    val tDist = radius - 24.dp.toPx()
                    val tX = (center.x + tDist * sin(tRad)).toFloat()
                    val tY = (center.y - tDist * cos(tRad)).toFloat()

                    drawCircle(
                        color = GarminCyan,
                        radius = 7.dp.toPx(),
                        center = Offset(tX, tY)
                    )
                }
            }

            // Fixed Center Crosshair & Device Heading Line
            drawLine(
                color = if (isQiblaAligned) GarminEmerald else GarminOrange,
                start = Offset(center.x, center.y - radius - 6.dp.toPx()),
                end = Offset(center.x, center.y - radius + 18.dp.toPx()),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Center needle pointer
            val needlePath = Path().apply {
                moveTo(center.x, center.y - radius * 0.65f)
                lineTo(center.x + 8.dp.toPx(), center.y)
                lineTo(center.x - 8.dp.toPx(), center.y)
                close()
            }
            drawPath(
                path = needlePath,
                brush = Brush.verticalGradient(listOf(GarminRed, GarminOrange))
            )

            val southNeedle = Path().apply {
                moveTo(center.x, center.y + radius * 0.65f)
                lineTo(center.x + 8.dp.toPx(), center.y)
                lineTo(center.x - 8.dp.toPx(), center.y)
                close()
            }
            drawPath(
                path = southNeedle,
                brush = Brush.verticalGradient(listOf(Color(0xFF64748B), Color(0xFFE2E8F0)))
            )

            // Center pivot
            drawCircle(color = Color(0xFF0F172A), radius = 10.dp.toPx(), center = center)
            drawCircle(
                color = if (isQiblaAligned) GarminEmerald else GarminOrange,
                radius = 5.dp.toPx(),
                center = center
            )
        }

        // Digital Heading Display in Center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 110.dp)
        ) {
            val cardinal = getCardinalDirection(animatedAzimuth)
            Text(
                text = "${animatedAzimuth.toInt()}°",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                ),
                color = if (isQiblaAligned) GarminEmerald else Color.White
            )
            Text(
                text = cardinal,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (isQiblaAligned) GarminEmerald else GarminOrange
            )
        }
    }
}

fun getCardinalDirection(degrees: Float): String {
    val deg = (degrees % 360 + 360) % 360
    return when {
        deg >= 337.5 || deg < 22.5 -> "U (Utara)"
        deg < 67.5 -> "TL (Timur Laut)"
        deg < 112.5 -> "T (Timur)"
        deg < 157.5 -> "TG (Tenggara)"
        deg < 202.5 -> "S (Selatan)"
        deg < 247.5 -> "BD (Barat Daya)"
        deg < 292.5 -> "B (Barat)"
        else -> "BL (Barat Laut)"
    }
}

@Composable
fun RadarMapCanvas(
    userLat: Double,
    userLon: Double,
    headingDegrees: Float,
    searchRadiusMeters: Double,
    locations: List<LocationEntity>,
    selectedLocation: LocationEntity?,
    onLocationSelected: (LocationEntity) -> Unit,
    weatherOverlay: com.example.data.api.LiveWeatherOverlayData? = null,
    showWeatherOverlay: Boolean = true,
    onToggleWeather: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.15f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF070B12))
            .border(1.dp, GarminOrange.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(locations, userLat, userLon, searchRadiusMeters) {
                    detectTapGestures { tapOffset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val maxRadarPx = min(size.width, size.height) * 0.44f

                        // Find closest location to tap within 28dp tolerance
                        var closestLoc: LocationEntity? = null
                        var minDistancePx = 32.dp.toPx()

                        locations.forEach { loc ->
                            val distMeters = CompassSensorManager.calculateDistanceMeters(
                                userLat, userLon, loc.latitude, loc.longitude
                            )
                            if (distMeters <= searchRadiusMeters * 1.3) {
                                val bearing = CompassSensorManager.calculateBearing(
                                    userLat, userLon, loc.latitude, loc.longitude
                                )
                                val angleRad = Math.toRadians((bearing - headingDegrees).toDouble())
                                val distPx = (distMeters / searchRadiusMeters) * maxRadarPx
                                val pinX = (center.x + distPx * sin(angleRad)).toFloat()
                                val pinY = (center.y - distPx * cos(angleRad)).toFloat()

                                val tapDist = (Offset(pinX, pinY) - tapOffset).getDistance()
                                if (tapDist < minDistancePx) {
                                    minDistancePx = tapDist
                                    closestLoc = loc
                                }
                            }
                        }

                        closestLoc?.let { onLocationSelected(it) }
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadarPx = min(size.width, size.height) * 0.44f

            // Radar background rings (25%, 50%, 75%, 100% radius)
            val ringColors = Color(0xFF1E293B)
            for (step in 1..4) {
                val r = (step / 4f) * maxRadarPx
                drawCircle(
                    color = ringColors,
                    radius = r,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Real-Time Weather Precipitation Radar Overlay
            if (showWeatherOverlay && weatherOverlay != null) {
                val prec = weatherOverlay.precipitationMm
                val isRaining = weatherOverlay.isRaining || prec > 0.05
                if (isRaining) {
                    // Rain radar cloud contour circles
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x4038BDF8),
                                Color(0x200284C7),
                                Color.Transparent
                            ),
                            center = Offset(center.x + maxRadarPx * 0.3f, center.y - maxRadarPx * 0.25f),
                            radius = maxRadarPx * 0.65f
                        ),
                        radius = maxRadarPx * 0.65f,
                        center = Offset(center.x + maxRadarPx * 0.3f, center.y - maxRadarPx * 0.25f)
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x350EA5E9),
                                Color.Transparent
                            ),
                            center = Offset(center.x - maxRadarPx * 0.2f, center.y + maxRadarPx * 0.3f),
                            radius = maxRadarPx * 0.5f
                        ),
                        radius = maxRadarPx * 0.5f,
                        center = Offset(center.x - maxRadarPx * 0.2f, center.y + maxRadarPx * 0.3f)
                    )
                }
            }

            // Radar Axis Lines
            drawLine(
                color = Color(0xFF1E293B),
                start = Offset(center.x - maxRadarPx, center.y),
                end = Offset(center.x + maxRadarPx, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color(0xFF1E293B),
                start = Offset(center.x, center.y - maxRadarPx),
                end = Offset(center.x, center.y + maxRadarPx),
                strokeWidth = 1.dp.toPx()
            )

            // North pointer on top of radar
            drawLine(
                color = GarminRed.copy(alpha = 0.7f),
                start = Offset(center.x, center.y - maxRadarPx),
                end = Offset(center.x, center.y - maxRadarPx + 12.dp.toPx()),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Plot waypoints
            locations.forEach { loc ->
                val distMeters = CompassSensorManager.calculateDistanceMeters(
                    userLat, userLon, loc.latitude, loc.longitude
                )

                if (distMeters <= searchRadiusMeters * 1.25) {
                    val bearing = CompassSensorManager.calculateBearing(
                        userLat, userLon, loc.latitude, loc.longitude
                    )
                    val angleRad = Math.toRadians((bearing - headingDegrees).toDouble())
                    val distPx = (distMeters / searchRadiusMeters) * maxRadarPx
                    val pinX = (center.x + distPx * sin(angleRad)).toFloat()
                    val pinY = (center.y - distPx * cos(angleRad)).toFloat()

                    val isSelected = selectedLocation?.id == loc.id
                    val pinColor = when (loc.category) {
                        "TREASURE" -> GarminAmber
                        "MINERAL" -> GarminCyan
                        "HERITAGE" -> Color(0xFFD946EF)
                        "PRIORITY_ZONE" -> GarminRed
                        "HIKING" -> GarminEmerald
                        "MARINE" -> Color(0xFF38BDF8)
                        else -> GarminOrange
                    }

                    // Special Halo for Favorite Recurring Locations
                    if (loc.isFavorite) {
                        drawCircle(
                            color = GarminAmber.copy(alpha = 0.45f),
                            radius = if (isSelected) 20.dp.toPx() else 14.dp.toPx(),
                            center = Offset(pinX, pinY),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        // Star accent tick above the favorite pin
                        drawLine(
                            color = GarminAmber,
                            start = Offset(pinX, pinY - 10.dp.toPx()),
                            end = Offset(pinX, pinY - 14.dp.toPx()),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    if (isSelected) {
                        drawCircle(
                            color = pinColor.copy(alpha = 0.35f),
                            radius = 16.dp.toPx(),
                            center = Offset(pinX, pinY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 10.dp.toPx(),
                            center = Offset(pinX, pinY),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }

                    drawCircle(
                        color = if (loc.isFavorite) GarminAmber else pinColor,
                        radius = if (isSelected) 7.dp.toPx() else 5.dp.toPx(),
                        center = Offset(pinX, pinY)
                    )
                }
            }

            // User Position at center (Bright Blue/Cyan with GPS heading arrow)
            drawCircle(
                color = GarminCyan.copy(alpha = 0.25f),
                radius = 14.dp.toPx(),
                center = center
            )
            drawCircle(
                color = GarminCyan,
                radius = 6.dp.toPx(),
                center = center
            )
        }

        // Top Status HUD in Radar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xCC0F172A),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "RADAR: ${LocationServiceManager.formatDistance(searchRadiusMeters, UnitSystem.METRIC)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GarminOrange,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Real-Time Weather Overlay Map Chip
            if (weatherOverlay != null) {
                Surface(
                    color = if (weatherOverlay.isRaining) Color(0xD90284C7) else Color(0xCC0F172A),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (weatherOverlay.isRaining) GarminCyan else Color(0xFF334155)
                    ),
                    modifier = Modifier.clickable { onToggleWeather?.invoke() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = weatherOverlay.weatherEmoji,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${String.format(Locale.US, "%.1f°C", weatherOverlay.temperatureC)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        if (weatherOverlay.precipitationMm > 0.0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "🌧️ ${String.format(Locale.US, "%.1fmm", weatherOverlay.precipitationMm)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GarminCyan
                            )
                        }
                    }
                }
            } else {
                Surface(
                    color = Color(0xCC0F172A),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${locations.size} TITIK TERDETEKSI",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ElevationProfileChart(
    elevationPoints: List<Pair<Long, Double>>,
    currentElevation: Double,
    minElevation: Double,
    maxElevation: Double,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(Color(0xFF0A0F1D), RoundedCornerShape(12.dp))
            .border(1.dp, DarkTacticalBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (elevationPoints.size < 2) {
                // Flat line if not enough points
                val baseline = size.height * 0.6f
                drawLine(
                    color = GarminCyan,
                    start = Offset(0f, baseline),
                    end = Offset(size.width, baseline),
                    strokeWidth = 2.dp.toPx()
                )
                return@Canvas
            }

            val range = max(10.0, maxElevation - minElevation)
            val stepX = size.width / (elevationPoints.size - 1)

            val path = Path()
            val fillPath = Path()

            fillPath.moveTo(0f, size.height)

            elevationPoints.forEachIndexed { i, pt ->
                val x = i * stepX
                val normalizedY = 1.0 - ((pt.second - minElevation) / range)
                val y = (normalizedY * (size.height - 20.dp.toPx()) + 10.dp.toPx()).toFloat()

                if (i == 0) {
                    path.moveTo(x, y)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            fillPath.lineTo(size.width, size.height)
            fillPath.close()

            // Draw elevation fill gradient
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GarminOrange.copy(alpha = 0.4f),
                        GarminOrange.copy(alpha = 0.05f)
                    )
                )
            )

            // Draw elevation line
            drawPath(
                path = path,
                color = GarminOrange,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Min & Max altitude overlay
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "MAX: ${String.format(Locale.US, "%.0f m", maxElevation)}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = GarminEmerald
            )
            Text(
                text = "SAAT INI: ${String.format(Locale.US, "%.0f m", currentElevation)}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = GarminOrange
            )
            Text(
                text = "MIN: ${String.format(Locale.US, "%.0f m", minElevation)}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = GarminCyan
            )
        }
    }
}

@Composable
fun StatusBadge(
    priority: String,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (priority) {
        "TINGGI" -> Pair(GarminRed, "PRIORITAS TINGGI")
        "SEDANG" -> Pair(GarminAmber, "PRIORITAS SEDANG")
        else -> Pair(GarminEmerald, "PRIORITAS RENDAH")
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            ),
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
