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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LocationEntity
import com.example.ui.theme.DarkTacticalBackground
import com.example.ui.theme.DarkTacticalBorder
import com.example.ui.theme.DarkTacticalSurface
import com.example.ui.theme.GarminAmber
import com.example.ui.theme.GarminCyan
import com.example.ui.theme.GarminEmerald
import com.example.ui.theme.GarminOrange
import com.example.ui.theme.GarminRed

data class LegendItemInfo(
    val categoryKey: String,
    val title: String,
    val iconEmoji: String,
    val color: Color,
    val description: String
)

/**
 * Toggleable Map Legend Overlay that explains and interprets all navigation symbols,
 * icons, and terrain layers (historical sites, mineral deposits, waypoints, favorite stars,
 * and weather radar precipitation).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MapLegendOverlay(
    locations: List<LocationEntity>,
    selectedFilterCategory: String?,
    onCategoryFilterSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val legendItems = remember {
        listOf(
            LegendItemInfo(
                categoryKey = "HERITAGE",
                title = "Situs Bersejarah & Pusaka",
                iconEmoji = "🏺",
                color = Color(0xFFD946EF),
                description = "Artefak purbakala, makam bersejarah, cagar budaya Lombok & peninggalan era kerajaan."
            ),
            LegendItemInfo(
                categoryKey = "MINERAL",
                title = "Deposit Mineral Bumi",
                iconEmoji = "💎",
                color = GarminCyan,
                description = "Urat kuarsa emas, pasir besi pesisir, singkapan batuan mulia & mineral geologi."
            ),
            LegendItemInfo(
                categoryKey = "TREASURE",
                title = "Harta Karun & Logam Mulia",
                iconEmoji = "💰",
                color = GarminAmber,
                description = "Anomali logam tersembunyi, peninggalan emas aluvial & titik target ekspedisi."
            ),
            LegendItemInfo(
                categoryKey = "PRIORITY_ZONE",
                title = "Zona Prioritas Tinggi",
                iconEmoji = "📍",
                color = GarminRed,
                description = "Wilayah target khusus SAR atau zona eksplorasi aktif berprioritas utama."
            ),
            LegendItemInfo(
                categoryKey = "HIKING",
                title = "Jalur & Pos Pendakian",
                iconEmoji = "🥾",
                color = GarminEmerald,
                description = "Pos pendakian gunung (Rinjani), shelter darurat, mata air & jalur tapak alam."
            ),
            LegendItemInfo(
                categoryKey = "MARINE",
                title = "Pelayaran & Titik Bahari",
                iconEmoji = "⛵",
                color = Color(0xFF38BDF8),
                description = "Area labuh jangkar kapal, terumbu karang dangkal & rute perairan Selat Lombok."
            ),
            LegendItemInfo(
                categoryKey = "WAYPOINT",
                title = "Titik Acuan Navigasi",
                iconEmoji = "🚩",
                color = GarminOrange,
                description = "Titik koordinat referensi umum untuk navigasi darat dan pos kompas."
            ),
            LegendItemInfo(
                categoryKey = "FAVORITE",
                title = "Lokasi Favorit (Quick Nav)",
                iconEmoji = "⭐",
                color = GarminAmber,
                description = "Titik penting berulang (Basecamp, Sumber Air) dengan cincin emas berpijar."
            )
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Toggle Button Bar
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isExpanded) DarkTacticalSurface else Color(0xFF131D2E),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isExpanded) GarminOrange else DarkTacticalBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .testTag("map_legend_toggle")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = if (isExpanded) GarminOrange else GarminCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LEGENDA SIMBOL & ARTI IKON PETA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedFilterCategory != null) {
                        Surface(
                            color = GarminOrange.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "Filter Aktif: $selectedFilterCategory",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = GarminOrange,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isExpanded) "TUTUP ▲" else "BUKA ▼",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = if (isExpanded) GarminOrange else GarminCyan
                    )
                }
            }
        }

        // Expanded Legend Content Panel
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("map_legend_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkTacticalSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkTacticalBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "PANDUAN INTERPRETASI SIMBOL PETA & RADAR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GarminCyan,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "Ketuk ikon untuk memfilter tampilan titik di radar peta navigasi:",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter Reset Chip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selectedFilterCategory == null) GarminEmerald.copy(alpha = 0.3f) else Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedFilterCategory == null) GarminEmerald else DarkTacticalBorder
                            ),
                            modifier = Modifier
                                .clickable { onCategoryFilterSelected(null) }
                        ) {
                            Text(
                                text = "Semua Titik (${locations.size})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (selectedFilterCategory == null) GarminEmerald else Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Legend Entries Grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        legendItems.forEach { item ->
                            val count = when (item.categoryKey) {
                                "FAVORITE" -> locations.count { it.isFavorite }
                                else -> locations.count { it.category == item.categoryKey }
                            }
                            val isSelected = selectedFilterCategory == item.categoryKey

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) item.color.copy(alpha = 0.2f) else Color(0xFF0F172A),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) item.color else DarkTacticalBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (item.categoryKey != "FAVORITE") {
                                            onCategoryFilterSelected(if (isSelected) null else item.categoryKey)
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Symbol Pin Icon
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(item.color.copy(alpha = 0.2f))
                                            .border(1.5.dp, item.color, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = item.iconEmoji, fontSize = 13.sp)
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = item.color
                                            )
                                            Text(
                                                text = "$count titik",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                        Text(
                                            text = item.description,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                            color = Color(0xFF94A3B8),
                                            lineHeight = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Weather Radar Explanation Item
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkTacticalBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(GarminCyan.copy(alpha = 0.2f))
                                        .border(1.5.dp, GarminCyan, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🌧️", fontSize = 13.sp)
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Lapisan Awan Presipitasi Radar",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = GarminCyan
                                    )
                                    Text(
                                        text = "Sel radar awan hujan biru-cyan transparan yang dihitung dari curah hujan API satelit cuaca real-time.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = Color(0xFF94A3B8),
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
