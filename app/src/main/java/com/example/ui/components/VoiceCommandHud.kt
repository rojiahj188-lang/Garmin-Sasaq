package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.sensor.VoiceCommandState
import com.example.ui.theme.DarkTacticalBorder
import com.example.ui.theme.DarkTacticalSurface
import com.example.ui.theme.GarminAmber
import com.example.ui.theme.GarminCyan
import com.example.ui.theme.GarminEmerald
import com.example.ui.theme.GarminOrange
import com.example.ui.theme.GarminRed

/**
 * Hands-Free Voice Command HUD for Waypoint Logging.
 * Enables users to mark landmarks, water sources, summits, or camp locations hands-free
 * via speech recognition while navigating or hiking.
 */
@Composable
fun VoiceCommandHud(
    voiceState: VoiceCommandState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onSimulateCommand: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            onStartListening()
        } else {
            Toast.makeText(
                context,
                "Izin mikrofon diperlukan untuk mencatat waypoint dengan suara",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Mic Pulsing Animation when active
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("voice_command_hud")
            .border(1.dp, if (voiceState.isListening) GarminEmerald else DarkTacticalBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkTacticalSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = if (voiceState.isListening) GarminEmerald else GarminCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "CATAT WAYPOINT VIA SUARA",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Navigasi Hands-Free saat mendaki / berkendara",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Main Mic Trigger Button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (voiceState.isListening)
                                GarminEmerald.copy(alpha = pulseAlpha)
                            else
                                Color(0xFF1E293B)
                        )
                        .border(
                            1.5.dp,
                            if (voiceState.isListening) GarminEmerald else GarminCyan,
                            CircleShape
                        )
                        .clickable {
                            if (voiceState.isListening) {
                                onStopListening()
                            } else {
                                if (hasPermission) {
                                    onStartListening()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                        .testTag("mic_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (voiceState.isListening) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Mikrofon",
                        tint = if (voiceState.isListening) Color.Black else GarminCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Real-Time Audio Soundwave Equalizer (Visible when listening)
            AnimatedVisibility(
                visible = voiceState.isListening,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dynamic waveform bars driven by soundLevelRms
                        val rms = voiceState.soundLevelRms
                        for (i in 0..15) {
                            val factor = ((i * 3 + rms * 2) % 10) / 10f
                            val barHeight = (6 + factor * 16).dp
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(GarminEmerald, GarminCyan)
                                        )
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (voiceState.spokenText.isNotBlank())
                            "\"${voiceState.spokenText}\""
                        else
                            "Silakan ucapkan: \"Simpan titik Basecamp\" atau \"Tandai Sumber Air\"",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = GarminAmber
                    )
                }
            }

            // Waypoint Successfully Logged Notification Banner
            AnimatedVisibility(
                visible = voiceState.lastLoggedWaypoint != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    color = GarminEmerald.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GarminEmerald.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GarminEmerald, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "WAYPOINT BERHASIL DICATAT!",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GarminEmerald
                            )
                            Text(
                                text = "Tersimpan: \"${voiceState.lastLoggedWaypoint}\"",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Error display if any
            if (voiceState.errorMessage != null && !voiceState.isListening) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = voiceState.errorMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = GarminAmber
                )
            }

            // Quick Example Chips for Simulation / Touch Fallback
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "CONTOH PERINTAH SUARA CEPAT:",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "Tandai Pos 2",
                    "Simpan Sumber Air",
                    "Waypoint Puncak"
                ).forEach { sample ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkTacticalBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onSimulateCommand?.invoke(sample)
                            }
                    ) {
                        Text(
                            text = sample,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = GarminCyan,
                            modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
