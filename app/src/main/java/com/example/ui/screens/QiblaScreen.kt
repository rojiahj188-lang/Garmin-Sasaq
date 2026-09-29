package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CoordinateFormat
import com.example.sensor.CompassSensorManager
import com.example.sensor.LocationServiceManager
import com.example.ui.components.CompassDial
import com.example.ui.components.GarminCard
import com.example.ui.theme.DarkTacticalBackground
import com.example.ui.theme.DarkTacticalBorder
import com.example.ui.theme.DarkTacticalSurface
import com.example.ui.theme.GarminAmber
import com.example.ui.theme.GarminCyan
import com.example.ui.theme.GarminEmerald
import com.example.ui.theme.GarminOrange
import com.example.ui.viewmodel.GarminViewModel
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    viewModel: GarminViewModel,
    onBack: () -> Unit
) {
    val compassData by viewModel.compassData.collectAsStateWithLifecycle()
    val locationData by viewModel.locationData.collectAsStateWithLifecycle()
    val qiblaBearing by viewModel.qiblaBearing.collectAsStateWithLifecycle()
    val qiblaDistanceKm by viewModel.qiblaDistanceKm.collectAsStateWithLifecycle()
    val coordFormat by viewModel.coordinateFormat.collectAsStateWithLifecycle()

    val deviation = ((qiblaBearing - compassData.azimuthDegrees + 360f) % 360f)
    val isAligned = deviation <= 3f || deviation >= 357f

    LaunchedEffect(deviation) {
        viewModel.checkAndTriggerQiblaHaptic(deviation)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🧭 KOMPAS ARAH KIBLAT",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GarminOrange
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("qibla_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkTacticalBackground)
            )
        },
        containerColor = DarkTacticalBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Alignment Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isAligned) GarminEmerald.copy(alpha = 0.2f) else DarkTacticalSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isAligned) GarminEmerald else DarkTacticalBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isAligned) GarminEmerald else GarminOrange.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAligned) Icons.Default.CheckCircle else Icons.Default.Mosque,
                            contentDescription = null,
                            tint = if (isAligned) Color.White else GarminOrange
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (isAligned) "TEPAT MENGHADAP KIBLAT (KA'BAH)" else "ARAHKAN HP HINGGA JARUM HIJAU SELARAS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isAligned) GarminEmerald else GarminOrange
                        )
                        Text(
                            text = if (isAligned) "Getaran aktif • Posisi presisi" else "Azimuth Kiblat: ${qiblaBearing.toInt()}° (Deviasi: ${deviation.toInt()}°)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Compass Dial with Qibla alignment
            CompassDial(
                azimuthDegrees = compassData.azimuthDegrees,
                qiblaBearing = qiblaBearing,
                isQiblaAligned = isAligned,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(310.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Telemetry Cards
            GarminCard(title = "TELEMETRI KOORDINAT KA'BAH") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "JARAK KE MEKKAH",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = String.format(Locale.US, "%,.1f km", qiblaDistanceKm),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GarminEmerald
                        )
                    }
                    Column {
                        Text(
                            text = "DERAJAT KIBLAT",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f° BL", qiblaBearing),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GarminOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Posisi GPS Pengguna: ${LocationServiceManager.formatCoordinates(locationData.latitude, locationData.longitude, coordFormat)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "Koordinat Ka'bah: 21.4225° N, 39.8262° E (Masjidil Haram)",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calibration Note
            GarminCard(title = "PETUNJUK KALIBRASI KOMPAS SENSOR") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Sync, contentDescription = null, tint = GarminAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gerakkan perangkat membentuk angka delapan (∞) di udara untuk meningkatkan akurasi sensor geomagnetik bila jarum tidak stabil.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
