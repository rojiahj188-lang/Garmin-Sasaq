package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.LocationCategory
import com.example.model.PriorityLevel
import com.example.ui.theme.DarkTacticalBorder
import com.example.ui.theme.DarkTacticalSurface
import com.example.ui.theme.GarminOrange
import java.util.Locale

@Composable
fun AddLocationDialog(
    currentLat: Double,
    currentLon: Double,
    currentAlt: Double,
    defaultCategory: LocationCategory = LocationCategory.TREASURE,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        category: LocationCategory,
        lat: Double,
        lon: Double,
        alt: Double,
        desc: String,
        details: String,
        priority: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(defaultCategory) }
    var latText by remember { mutableStateOf(String.format(Locale.US, "%.5f", currentLat)) }
    var lonText by remember { mutableStateOf(String.format(Locale.US, "%.5f", currentLon)) }
    var altText by remember { mutableStateOf(String.format(Locale.US, "%.1f", currentAlt)) }
    var description by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(PriorityLevel.SEDANG) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = DarkTacticalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, GarminOrange.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CATAT TITIK LOKASI BARU",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GarminOrange
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category selector
                Text(
                    text = "Kategori Modul:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        LocationCategory.TREASURE,
                        LocationCategory.MINERAL,
                        LocationCategory.HERITAGE,
                        LocationCategory.PRIORITY_ZONE
                    ).forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) GarminOrange else Color(0xFF1E293B),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedCategory = cat }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = cat.icon, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = when (cat) {
                                        LocationCategory.TREASURE -> "Harta"
                                        LocationCategory.MINERAL -> "Mineral"
                                        LocationCategory.HERITAGE -> "Pusaka"
                                        else -> "Prioritas"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Titik / Target") },
                    placeholder = { Text("Contoh: Celah Batu Pusaka") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GarminOrange,
                        unfocusedBorderColor = DarkTacticalBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Coordinates Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GarminOrange,
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
                            focusedBorderColor = GarminOrange,
                            unfocusedBorderColor = DarkTacticalBorder
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Use current GPS button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            latText = String.format(Locale.US, "%.5f", currentLat)
                            lonText = String.format(Locale.US, "%.5f", currentLon)
                            altText = String.format(Locale.US, "%.1f", currentAlt)
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = GarminOrange)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Gunakan GPS Saat Ini (${String.format(Locale.US, "%.4f, %.4f", currentLat, currentLon)})",
                        style = MaterialTheme.typography.labelSmall,
                        color = GarminOrange
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Specific Details Field based on Category
                val detailLabel = when (selectedCategory) {
                    LocationCategory.TREASURE -> "Petunjuk / Clue Harta Karun"
                    LocationCategory.MINERAL -> "Kadar / Formasi Batuan Mineral"
                    LocationCategory.HERITAGE -> "Era Sejarah / Ciri Cagar Budaya"
                    LocationCategory.PRIORITY_ZONE -> "Jenis Fasilitas / Zona Wilayah"
                    else -> "Informasi Tambahan"
                }

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text(detailLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GarminOrange,
                        unfocusedBorderColor = DarkTacticalBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Catatan Lapangan & Deskripsi Lokasi") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GarminOrange,
                        unfocusedBorderColor = DarkTacticalBorder
                    ),
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Priority Selector
                Text(
                    text = "Tingkat Prioritas:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PriorityLevel.values().forEach { prio ->
                        val isSel = selectedPriority == prio
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) GarminOrange.copy(alpha = 0.3f) else Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) GarminOrange else DarkTacticalBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedPriority = prio }
                        ) {
                            Text(
                                text = prio.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSel) GarminOrange else Color(0xFF94A3B8),
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Button
                Button(
                    onClick = {
                        val latVal = latText.toDoubleOrNull() ?: currentLat
                        val lonVal = lonText.toDoubleOrNull() ?: currentLon
                        val altVal = altText.toDoubleOrNull() ?: currentAlt
                        val finalTitle = title.ifBlank { "${selectedCategory.label} Baru" }
                        onSave(
                            finalTitle,
                            selectedCategory,
                            latVal,
                            lonVal,
                            altVal,
                            description,
                            details,
                            selectedPriority.name
                        )
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GarminOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "SIMPAN KOORDINAT KE GARMIN FINDER",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}
