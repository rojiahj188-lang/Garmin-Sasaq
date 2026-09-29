package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.LiveWeatherOverlayData
import com.example.ui.theme.DarkTacticalBorder
import com.example.ui.theme.DarkTacticalSurface
import com.example.ui.theme.GarminAmber
import com.example.ui.theme.GarminCyan
import com.example.ui.theme.GarminEmerald
import com.example.ui.theme.GarminOrange
import com.example.ui.theme.GarminRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dedicated Weather Information Layer overlay for the map interface.
 * Displays real-time meteorology data fetched from live weather APIs based on device GPS coordinates:
 * - Temperature & feels like
 * - Precipitation rate & status
 * - Wind velocity & direction
 * - Atmospheric pressure & relative humidity
 * - Real-time weather radar overlay switch
 */
@Composable
fun WeatherInformationLayer(
    weather: LiveWeatherOverlayData,
    currentLat: Double,
    currentLon: Double,
    currentAlt: Double,
    isLayerVisible: Boolean,
    onToggleLayer: () -> Unit,
    onRefreshWeather: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Main Toggleable Header Bar
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (weather.isRaining) Color(0xFF0C243B) else Color(0xFF131D2E),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isLayerVisible) (if (weather.isRaining) GarminCyan else GarminAmber) else DarkTacticalBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("weather_info_layer_bar")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Weather icon + current temp + condition
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isExpanded = !isExpanded }
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (weather.isRaining) GarminCyan.copy(alpha = 0.2f)
                                else GarminAmber.copy(alpha = 0.2f)
                            )
                            .border(
                                1.dp,
                                if (weather.isRaining) GarminCyan else GarminAmber,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = weather.weatherEmoji, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${String.format(Locale.US, "%.1f°C", weather.temperatureC)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = weather.weatherCondition,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (weather.isRaining) GarminCyan else GarminAmber
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Presipitasi: ${String.format(Locale.US, "%.1f mm/h", weather.precipitationMm)} • Angin: ${weather.windSpeedKmh.toInt()} km/j",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isExpanded) "▲" else "▼",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = GarminCyan
                            )
                        }
                    }
                }

                // Controls: Refresh + Layer Activation Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onRefreshWeather,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Perbarui Data Cuaca",
                            tint = GarminCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isLayerVisible) GarminEmerald.copy(alpha = 0.25f) else Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isLayerVisible) GarminEmerald else DarkTacticalBorder
                        ),
                        modifier = Modifier.clickable { onToggleLayer() }
                    ) {
                        Text(
                            text = if (isLayerVisible) "LAYER ON" else "LAYER OFF",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isLayerVisible) GarminEmerald else Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Expanded Weather Telemetry Details Panel
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("weather_telemetry_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkTacticalSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkTacticalBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LAPISAN TELEMETRI METEOROLOGI REAL-TIME",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GarminCyan,
                                letterSpacing = 1.sp
                            )
                        )

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (weather.isLiveFromApi) GarminEmerald.copy(alpha = 0.2f) else GarminAmber.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (weather.isLiveFromApi) "OPEN-METEO LIVE" else "SENSOR TERTANAM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (weather.isLiveFromApi) GarminEmerald else GarminAmber,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Coordinates & Location Context
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Koordinat Lokasi: ${String.format(Locale.US, "%.4f, %.4f", currentLat, currentLon)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "${currentAlt.toInt()} mdpl",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GarminOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4-Quadrant Meteorological Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WeatherMetricTile(
                            icon = Icons.Default.WaterDrop,
                            label = "PRESIPITASI",
                            value = "${String.format(Locale.US, "%.1f", weather.precipitationMm)} mm",
                            subtext = if (weather.isRaining) "Hujan Aktif di Peta" else "Kondisi Kering",
                            accentColor = GarminCyan,
                            modifier = Modifier.weight(1f)
                        )

                        WeatherMetricTile(
                            icon = Icons.Default.Air,
                            label = "KECEPATAN ANGIN",
                            value = "${weather.windSpeedKmh.toInt()} km/j",
                            subtext = "Arah ${weather.windDirectionDegrees.toInt()}°",
                            accentColor = GarminEmerald,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WeatherMetricTile(
                            icon = Icons.Default.Compress,
                            label = "TEKANAN UDARA",
                            value = "${weather.pressureHpa} hPa",
                            subtext = if (weather.pressureHpa > 1013) "Tekanan Tinggi" else "Tekanan Rendah",
                            accentColor = GarminAmber,
                            modifier = Modifier.weight(1f)
                        )

                        WeatherMetricTile(
                            icon = Icons.Default.Cloud,
                            label = "KELEMBAPAN UDARA",
                            value = "${weather.humidityPercent}%",
                            subtext = "Titik Embun Tropis",
                            accentColor = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val timeFormatter = SimpleDateFormat("dd MMM, HH:mm:ss", Locale.getDefault())
                    val updateTimeStr = timeFormatter.format(Date(weather.lastUpdated))
                    Text(
                        text = "Data cuaca diperbarui: $updateTimeStr • Berbasis lokasi GPS perangkat",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherMetricTile(
    icon: ImageVector,
    label: String,
    value: String,
    subtext: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkTacticalBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF94A3B8)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = accentColor
            )
        }
    }
}
