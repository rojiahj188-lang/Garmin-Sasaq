package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CoordinateFormat
import com.example.model.UnitSystem
import com.example.sensor.LocationServiceManager
import com.example.ui.components.GarminCard
import com.example.ui.theme.DarkTacticalBackground
import com.example.ui.theme.DarkTacticalBorder
import com.example.ui.theme.DarkTacticalSurface
import com.example.ui.theme.GarminAmber
import com.example.ui.theme.GarminCyan
import com.example.ui.theme.GarminEmerald
import com.example.ui.theme.GarminOrange
import com.example.ui.theme.GarminRed
import com.example.ui.viewmodel.GarminViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarineScreen(
    viewModel: GarminViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val locationData by viewModel.locationData.collectAsStateWithLifecycle()
    val compassData by viewModel.compassData.collectAsStateWithLifecycle()
    val marineState by viewModel.marineState.collectAsStateWithLifecycle()

    val sogKnots = locationData.speedMps * 1.94384
    val sogKmh = locationData.speedMps * 3.6
    val dmsCoords = LocationServiceManager.formatCoordinates(
        locationData.latitude,
        locationData.longitude,
        CoordinateFormat.DEGREES_MINUTES_SECONDS
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "⛵ NAVIGASI PELAYARAN",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GarminCyan
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Marine Nav Header Card
            GarminCard(
                title = "GARMIN MARINE • INSTRUMEN NAUTIKA",
                badgeText = if (marineState.anchorLat != null) "JANGKAR TERPASANG" else "BERLAYAR",
                badgeColor = if (marineState.anchorLat != null) GarminAmber else GarminCyan
            ) {
                // SOG (Speed Over Ground in Knots)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "KECEPATAN KAPAL (SOG)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.1f", sogKnots),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = GarminCyan
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KNOTS (${String.format(Locale.US, "%.1f km/h", sogKmh)})",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "HALUAN (COG)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "${compassData.azimuthDegrees.toInt()}°",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = GarminOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Nautical Coordinates (DMS)
                Text(
                    text = "POSISI KOORDINAT MARITIM (DMS):",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A0F1D), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dmsCoords,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Marine GPS", dmsCoords))
                            Toast.makeText(context, "Koordinat Maritim disalin!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Salin", tint = GarminCyan, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Anchor Watch / Pengawas Jangkar
            GarminCard(
                title = "PENGAWAS JANGKAR (ANCHOR WATCH ALARM)",
                badgeText = if (marineState.isAnchorAlarmTriggered) "ALARM DRIFT!" else "STABIL",
                badgeColor = if (marineState.isAnchorAlarmTriggered) GarminRed else GarminEmerald
            ) {
                Text(
                    text = "Fitur keamanan maritim untuk memantau apakah kapal hanyut terbawa arus melebihi radius aman jangkar yang ditentukan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (marineState.anchorLat != null) {
                    // Active Anchor Watch HUD
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (marineState.isAnchorAlarmTriggered) GarminRed.copy(alpha = 0.2f) else Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (marineState.isAnchorAlarmTriggered) GarminRed else DarkTacticalBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "JARAK HANYUT (DRIFT)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = String.format(Locale.US, "%.1f m", marineState.currentDriftMeters),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (marineState.isAnchorAlarmTriggered) GarminRed else GarminEmerald
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "BATAS RADIUS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = "${marineState.anchorRadiusMeters.toInt()} m",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                }
                            }

                            if (marineState.isAnchorAlarmTriggered) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = GarminRed)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PERINGATAN: Kapal hanyut melebihi radius jangkar!",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = GarminRed
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.clearAnchorWatch() },
                        colors = ButtonDefaults.buttonColors(containerColor = GarminRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Angkat Jangkar / Matikan Watch", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Set Anchor Watch Buttons
                    Text(
                        text = "Pilih radius toleransi hanyut:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFCBD5E1)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(25.0, 50.0, 100.0).forEach { radius ->
                            Button(
                                onClick = { viewModel.setAnchorWatch(radius) },
                                colors = ButtonDefaults.buttonColors(containerColor = GarminCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Anchor, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${radius.toInt()}m", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Maritime Reference & Safety
            GarminCard(title = "PROTOKOL KOMUNIKASI & MARITIM") {
                Text(
                    text = "• Saluran Darurat Laut: VHF Channel 16 (156.800 MHz)\n" +
                            "• Navigasi Selat Lombok: Arus lintas rata-rata 2.5 - 4.0 knots arah selatan.\n" +
                            "• Koordinat Pelabuhan Lembar: 08° 43' 35\" S, 116° 04' 12\" E",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = Color(0xFFCBD5E1)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
