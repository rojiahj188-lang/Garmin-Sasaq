package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocationCategory
import com.example.ui.theme.DarkTacticalBackground
import com.example.ui.theme.DarkTacticalBorder
import com.example.ui.theme.DarkTacticalSurface
import com.example.ui.theme.GarminAmber
import com.example.ui.theme.GarminCyan
import com.example.ui.theme.GarminEmerald
import com.example.ui.theme.GarminOrange
import java.util.Locale

@Composable
fun SaveFavoriteDialog(
    currentLat: Double,
    currentLon: Double,
    currentAlt: Double,
    onDismiss: () -> Unit,
    onSaveFavorite: (name: String, label: String, lat: Double, lon: Double, alt: Double, category: LocationCategory) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedLabel by remember { mutableStateOf("BASECAMP") }
    var customLabel by remember { mutableStateOf("") }
    var latText by remember { mutableStateOf(String.format(Locale.US, "%.6f", currentLat)) }
    var lonText by remember { mutableStateOf(String.format(Locale.US, "%.6f", currentLon)) }
    var selectedCategory by remember { mutableStateOf(LocationCategory.WAYPOINT) }

    val presetLabels = listOf("BASECAMP", "SUMBER AIR", "SHELTER", "TITIK JEMPUT", "POS DARURAT", "DERMAGA")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = GarminAmber, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "SIMPAN TITIK FAVORIT",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Tandai dan beri label koordinat penting untuk navigasi cepat (Quick Nav) ke lokasi berulang.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Lokasi Favorit") },
                    placeholder = { Text("Misal: Basecamp Danau Segara Anak") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fav_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GarminAmber,
                        unfocusedBorderColor = DarkTacticalBorder
                    ),
                    singleLine = true
                )

                Text(
                    text = "PILIH LABEL CEPAT:",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = GarminCyan
                )

                // Label Chips Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetLabels.take(3).forEach { label ->
                        val isSelected = selectedLabel == label
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) GarminAmber.copy(alpha = 0.25f) else Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) GarminAmber else DarkTacticalBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedLabel = label }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) GarminAmber else Color.White,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetLabels.drop(3).forEach { label ->
                        val isSelected = selectedLabel == label
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) GarminAmber.copy(alpha = 0.25f) else Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) GarminAmber else DarkTacticalBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedLabel = label }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) GarminAmber else Color.White,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Coordinates Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GarminCyan,
                            unfocusedBorderColor = DarkTacticalBorder
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = lonText,
                        onValueChange = { lonText = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GarminCyan,
                            unfocusedBorderColor = DarkTacticalBorder
                        ),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.ifBlank { "Favorit $selectedLabel" }
                    val finalLabel = customLabel.ifBlank { selectedLabel }
                    val lat = latText.toDoubleOrNull() ?: currentLat
                    val lon = lonText.toDoubleOrNull() ?: currentLon
                    onSaveFavorite(finalTitle, finalLabel, lat, lon, currentAlt, selectedCategory)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GarminAmber),
                modifier = Modifier.testTag("save_favorite_confirm_button")
            ) {
                Text("Simpan Favorit", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal", color = Color.White)
            }
        },
        containerColor = DarkTacticalSurface,
        shape = RoundedCornerShape(16.dp)
    )
}
