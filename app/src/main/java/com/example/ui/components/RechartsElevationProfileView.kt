package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkTacticalBorder
import com.example.ui.theme.DarkTacticalSurface
import com.example.ui.theme.GarminAmber
import com.example.ui.theme.GarminCyan
import com.example.ui.theme.GarminEmerald
import com.example.ui.theme.GarminOrange
import com.example.ui.theme.GarminRed
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

data class ElevationWaypointMarker(
    val normalizedPosition: Float, // 0.0 to 1.0 along route
    val label: String,
    val elevationMeters: Double
)

/**
 * Recharts-inspired Data Visualization Component for recorded hiking trails and active paths.
 * Renders an interactive, responsive elevation profile area graph with:
 * - Cartesian gridlines and X/Y-axis ticks
 * - Interactive scrubbing crosshair tooltip
 * - Elevation gain / loss (D+/D-) telemetry
 * - Slope grade calculations and waypoint pins
 */
@Composable
fun RechartsElevationProfileView(
    elevationData: List<Pair<Double, Double>>, // Pair(distanceMeters, elevationMeters)
    title: String = "PROFIL ELEVASI JALUR PENDAKIAN",
    totalDistanceMeters: Double = 0.0,
    elevationGainMeters: Double = 0.0,
    elevationLossMeters: Double = 0.0,
    minElevationMeters: Double = 0.0,
    maxElevationMeters: Double = 0.0,
    waypoints: List<ElevationWaypointMarker> = emptyList(),
    modifier: Modifier = Modifier
) {
    var scrubNormalizedX by remember { mutableFloatStateOf(-1f) }
    var isScrubbing by remember { mutableStateOf(false) }

    // Normalize or fallback data points if empty
    val effectivePoints = remember(elevationData, minElevationMeters, maxElevationMeters, totalDistanceMeters) {
        if (elevationData.size >= 2) {
            elevationData
        } else {
            // Generate standard hiking topography curve based on min/max/distance
            val baseMin = if (minElevationMeters > 0) minElevationMeters else 1150.0
            val baseMax = if (maxElevationMeters > 0) maxElevationMeters else 3726.0
            val dist = if (totalDistanceMeters > 0) totalDistanceMeters else 8500.0
            listOf(
                Pair(0.0, baseMin),
                Pair(dist * 0.15, baseMin + (baseMax - baseMin) * 0.18),
                Pair(dist * 0.35, baseMin + (baseMax - baseMin) * 0.32),
                Pair(dist * 0.50, baseMin + (baseMax - baseMin) * 0.48),
                Pair(dist * 0.70, baseMin + (baseMax - baseMin) * 0.76),
                Pair(dist * 0.88, baseMin + (baseMax - baseMin) * 0.94),
                Pair(dist, baseMax)
            )
        }
    }

    val actualMin = effectivePoints.minOfOrNull { it.second } ?: minElevationMeters
    val actualMax = effectivePoints.maxOfOrNull { it.second } ?: maxElevationMeters
    val actualDist = effectivePoints.maxOfOrNull { it.first } ?: totalDistanceMeters
    val elevRange = max(25.0, actualMax - actualMin)

    // Scrubbed point calculations
    val scrubbedPoint = if (isScrubbing && scrubNormalizedX in 0f..1f && effectivePoints.isNotEmpty()) {
        val targetIdx = (scrubNormalizedX * (effectivePoints.size - 1)).toInt().coerceIn(0, effectivePoints.size - 1)
        effectivePoints[targetIdx]
    } else null

    // Slope calculation at scrubbed point
    val currentSlopePercent = remember(scrubbedPoint, effectivePoints) {
        if (scrubbedPoint != null && effectivePoints.size > 2) {
            val idx = effectivePoints.indexOf(scrubbedPoint).coerceIn(0, effectivePoints.size - 1)
            val prev = if (idx > 0) effectivePoints[idx - 1] else effectivePoints[idx]
            val next = if (idx < effectivePoints.size - 1) effectivePoints[idx + 1] else effectivePoints[idx]
            val dDist = max(1.0, next.first - prev.first)
            val dElev = next.second - prev.second
            (dElev / dDist) * 100.0
        } else {
            if (actualDist > 0) ((actualMax - actualMin) / actualDist) * 100.0 else 12.0
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_elevation_profile_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkTacticalSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkTacticalBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Title & Recharts Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = GarminEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                }

                Surface(
                    color = GarminEmerald.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GarminEmerald.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "RECHARTS AREA GRAPH",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = GarminEmerald,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Real-Time Scrubbing Tooltip Banner (Recharts-style Crosshair Tooltip)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isScrubbing) GarminAmber else DarkTacticalBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isScrubbing) "INSPEKSI TITIK JALUR (SCRUB)" else "ELEVASI SAAT INI",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isScrubbing) GarminAmber else GarminCyan
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.0f", scrubbedPoint?.second ?: actualMax),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "mdpl",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GarminOrange
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "JARAK DARI AWAL",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color(0xFF94A3B8)
                        )
                        val distKm = (scrubbedPoint?.first ?: actualDist) / 1000.0
                        Text(
                            text = String.format(Locale.US, "%.2f km", distKm),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GarminCyan
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "KEMIRINGAN LERENG",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color(0xFF94A3B8)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (currentSlopePercent >= 0) Icons.Default.TrendingUp else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (currentSlopePercent > 15) GarminRed else GarminEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${String.format(Locale.US, "%+.1f", currentSlopePercent)}%",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (currentSlopePercent > 15) GarminRed else GarminEmerald
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Recharts Area Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFF070B14), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .pointerInput(effectivePoints) {
                        detectTapGestures(
                            onPress = { offset ->
                                isScrubbing = true
                                scrubNormalizedX = (offset.x / size.width).coerceIn(0f, 1f)
                                tryAwaitRelease()
                                isScrubbing = false
                            }
                        )
                    }
                    .pointerInput(effectivePoints) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isScrubbing = true
                                scrubNormalizedX = (offset.x / size.width).coerceIn(0f, 1f)
                            },
                            onDragEnd = { isScrubbing = false },
                            onDragCancel = { isScrubbing = false },
                            onDrag = { change, _ ->
                                change.consume()
                                scrubNormalizedX = (change.position.x / size.width).coerceIn(0f, 1f)
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val paddingBottom = 22.dp.toPx()
                    val paddingTop = 16.dp.toPx()
                    val graphH = h - paddingBottom - paddingTop

                    // 1. Cartesian Grid Lines (Horizontal altitude bands & Vertical distance ticks)
                    val gridColor = Color(0xFF1E293B)
                    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                    for (i in 0..4) {
                        val y = paddingTop + (i / 4f) * graphH
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashedEffect
                        )
                    }

                    for (j in 1..4) {
                        val x = (j / 4f) * w
                        drawLine(
                            color = gridColor,
                            start = Offset(x, paddingTop),
                            end = Offset(x, h - paddingBottom),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashedEffect
                        )
                    }

                    // 2. Build Area Chart Path & Bezier Smoothing Curve
                    val linePath = Path()
                    val areaPath = Path()

                    areaPath.moveTo(0f, h - paddingBottom)

                    val pointsPx = effectivePoints.mapIndexed { idx, pt ->
                        val x = (idx.toFloat() / (effectivePoints.size - 1)) * w
                        val normY = 1f - ((pt.second - actualMin) / elevRange).toFloat().coerceIn(0f, 1f)
                        val y = paddingTop + normY * graphH
                        Offset(x, y)
                    }

                    if (pointsPx.isNotEmpty()) {
                        linePath.moveTo(pointsPx[0].x, pointsPx[0].y)
                        areaPath.lineTo(pointsPx[0].x, pointsPx[0].y)

                        for (i in 0 until pointsPx.size - 1) {
                            val p0 = pointsPx[i]
                            val p1 = pointsPx[i + 1]
                            val midX = (p0.x + p1.x) / 2f
                            linePath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                            areaPath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                        }

                        areaPath.lineTo(w, h - paddingBottom)
                        areaPath.close()

                        // 3. Render Recharts Area Gradient Fill
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    GarminEmerald.copy(alpha = 0.45f),
                                    GarminCyan.copy(alpha = 0.20f),
                                    Color.Transparent
                                ),
                                startY = paddingTop,
                                endY = h - paddingBottom
                            )
                        )

                        // 4. Render Stroke Line
                        drawPath(
                            path = linePath,
                            brush = Brush.horizontalGradient(
                                colors = listOf(GarminCyan, GarminEmerald, GarminAmber, GarminOrange)
                            ),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // 5. Draw Waypoints along graph
                        waypoints.forEach { wp ->
                            val wpX = wp.normalizedPosition.coerceIn(0f, 1f) * w
                            val normY = 1f - ((wp.elevationMeters - actualMin) / elevRange).toFloat().coerceIn(0f, 1f)
                            val wpY = paddingTop + normY * graphH

                            drawCircle(
                                color = Color.White,
                                radius = 5.dp.toPx(),
                                center = Offset(wpX, wpY)
                            )
                            drawCircle(
                                color = GarminAmber,
                                radius = 3.5.dp.toPx(),
                                center = Offset(wpX, wpY)
                            )
                        }

                        // 6. Draw Peak Point Marker (Star/Ring)
                        val peakIdx = effectivePoints.indices.maxByOrNull { effectivePoints[it].second } ?: 0
                        val peakPt = pointsPx[peakIdx]
                        drawCircle(
                            color = GarminOrange.copy(alpha = 0.35f),
                            radius = 9.dp.toPx(),
                            center = peakPt
                        )
                        drawCircle(
                            color = GarminOrange,
                            radius = 4.dp.toPx(),
                            center = peakPt
                        )

                        // 7. Interactive Scrubber Crosshair (Vertical Guide line & Scrub Circle)
                        if (isScrubbing && scrubNormalizedX in 0f..1f) {
                            val scrubX = scrubNormalizedX * w
                            val scrubIdx = (scrubNormalizedX * (pointsPx.size - 1)).toInt().coerceIn(0, pointsPx.size - 1)
                            val scrubPt = pointsPx[scrubIdx]

                            drawLine(
                                color = GarminAmber,
                                start = Offset(scrubX, paddingTop),
                                end = Offset(scrubX, h - paddingBottom),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                            )

                            drawCircle(
                                color = GarminAmber,
                                radius = 7.dp.toPx(),
                                center = Offset(scrubX, scrubPt.y)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.5.dp.toPx(),
                                center = Offset(scrubX, scrubPt.y)
                            )
                        }
                    }
                }

                // Axis Labels inside Canvas Container
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "0 km (Start)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", (actualDist * 0.5) / 1000.0)} km",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f km", actualDist / 1000.0)} (Puncak)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                        color = GarminOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4-Card Topographic Summary Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TopoMetricCard(
                    icon = Icons.Default.Terrain,
                    label = "PUNCAK MAKSIMUM",
                    value = "${actualMax.toInt()} m",
                    accentColor = GarminOrange,
                    modifier = Modifier.weight(1f)
                )

                TopoMetricCard(
                    icon = Icons.Default.ArrowUpward,
                    label = "GAIN ELEVASI (D+)",
                    value = "+${elevationGainMeters.toInt().coerceAtLeast((actualMax - actualMin).toInt())} m",
                    accentColor = GarminEmerald,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TopoMetricCard(
                    icon = Icons.Default.ArrowDownward,
                    label = "ELEVASI TERENDAH",
                    value = "${actualMin.toInt()} m",
                    accentColor = GarminCyan,
                    modifier = Modifier.weight(1f)
                )

                TopoMetricCard(
                    icon = Icons.Default.FitnessCenter,
                    label = "ESTIMASI KALORI",
                    value = "${((actualDist / 1000.0) * 85 + elevationGainMeters * 0.45).toInt()} kkal",
                    accentColor = GarminAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TopoMetricCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkTacticalBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        }
    }
}
