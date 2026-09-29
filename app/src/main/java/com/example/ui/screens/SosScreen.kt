package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CoordinateFormat
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosScreen(
    viewModel: GarminViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sosState by viewModel.sosState.collectAsStateWithLifecycle()
    val locationData by viewModel.locationData.collectAsStateWithLifecycle()
    val coordFormat by viewModel.coordinateFormat.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            // Keep running if user navigated back or user chooses to stop
        }
    }

    // Screen strobe background color during active emission
    val strobeBg by animateColorAsState(
        targetValue = when {
            !sosState.isActive -> DarkTacticalBackground
            sosState.isLightEmitting -> Color(0xFFDC2626) // Bright emergency red
            else -> Color(0xFF0F172A)
        },
        animationSpec = tween(durationMillis = 80),
        label = "strobe"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🚨 SINYAL DARURAT (SOS)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GarminRed
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
        containerColor = strobeBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Visual SOS Strobe Display
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (sosState.isLightEmitting) Color.White else DarkTacticalSurface,
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    if (sosState.isActive) GarminRed else DarkTacticalBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (sosState.isActive) {
                        Text(
                            text = "S • O • S",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 8.sp
                            ),
                            color = if (sosState.isLightEmitting) GarminRed else Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = when (sosState.currentLetter) {
                                "S" -> "HURUF S: [ • • • ] (3 KETUKAN PENDEK)"
                                "O" -> "HURUF O: [ — — — ] (3 KETUKAN PANJANG)"
                                else -> "JEDA KATA (MORSE)"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = if (sosState.isLightEmitting) Color.Black else GarminAmber
                        )
                        Text(
                            text = "Siklus Darurat #${sosState.cycleCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (sosState.isLightEmitting) Color.DarkGray else Color(0xFF94A3B8)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = GarminRed,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "STANDBY DARURAT SOS",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Tekan tombol darurat di bawah untuk memancarkan sinyal morse visual dan audio.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Big SOS Trigger Button
            Button(
                onClick = {
                    if (sosState.isActive) {
                        viewModel.stopSos()
                    } else {
                        viewModel.startSos(useFlashlight = true, useAudio = true)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (sosState.isActive) Color.White else GarminRed,
                    contentColor = if (sosState.isActive) GarminRed else Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = if (sosState.isActive) Icons.Default.FlashlightOff else Icons.Default.FlashlightOn,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (sosState.isActive) "HENTIKAN SINYAL DARURAT" else "AKTIFKAN SINYAL SOS (MORSE)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                )
            }

            // Toggles: Flashlight & Siren
            GarminCard(title = "KONTROL HARDWARE SINYAL SOS") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = GarminAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Senter Flashlight HP", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            Text("Kedip Morse otomatis", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        }
                    }
                    Switch(
                        checked = sosState.isFlashlightOn,
                        onCheckedChange = { viewModel.toggleSosFlashlight() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = GarminOrange)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (sosState.isAudioEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = null,
                            tint = GarminCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Sirene Audio / Nada Beep", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            Text("Frekuensi alarm desibel tinggi", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        }
                    }
                    Switch(
                        checked = sosState.isAudioEnabled,
                        onCheckedChange = { viewModel.toggleSosAudio() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = GarminCyan)
                    )
                }
            }

            // Broadcast GPS Distress Card
            val coordStr = LocationServiceManager.formatCoordinates(
                locationData.latitude,
                locationData.longitude,
                coordFormat
            )

            GarminCard(
                title = "KOORDINAT DISTRESS LAPANGAN (SAR)",
                badgeText = "SIAP KIRIM",
                badgeColor = GarminRed
            ) {
                Text(
                    text = "Posisi Presisi Saat Ini:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = coordStr,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Text(
                    text = "Elevasi: ${String.format(java.util.Locale.US, "%.0f mdpl", locationData.altitudeMeters)} • Akurasi GPS: ±${locationData.accuracyMeters.toInt()}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = GarminCyan
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Send via WhatsApp / SMS
                    Button(
                        onClick = {
                            val sosMsg = """
                                🚨 PESAN DARURAT (SOS) - GARMIN FINDER 🚨
                                Saya membutuhkan bantuan darurat di lokasi ini:
                                Koordinat: $coordStr
                                Lat/Lon Desimal: ${locationData.latitude}, ${locationData.longitude}
                                Elevasi: ${locationData.altitudeMeters.toInt()} mdpl
                                Akurasi: ±${locationData.accuracyMeters.toInt()}m
                                Waktu: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}
                                Peta: https://maps.google.com/?q=${locationData.latitude},${locationData.longitude}
                            """.trimIndent()

                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, sosMsg)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Kirim Sinyal SOS"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GarminEmerald),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bagikan SOS", fontWeight = FontWeight.Bold)
                    }

                    // Direct Emergency Call (112 or SAR / Basarnas 115)
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:115"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GarminRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hubungi SAR 115", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
