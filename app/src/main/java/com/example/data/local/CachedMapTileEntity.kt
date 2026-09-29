package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room database entity representing an individual cached map tile for offline navigation.
 * Standard Web Mercator / Slippy Map tile schema (Z/X/Y) with geographic bounding box.
 */
@Entity(
    tableName = "cached_map_tiles",
    indices = [
        Index(value = ["tileKey", "layerType"], unique = true),
        Index(value = ["zoom", "tileX", "tileY"]),
        Index(value = ["minLatitude", "maxLatitude", "minLongitude", "maxLongitude"]),
        Index(value = ["regionTag"])
    ]
)
data class CachedMapTileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "tileKey")
    val tileKey: String, // e.g. "15/26984/17231"

    @ColumnInfo(name = "zoom")
    val zoom: Int,

    @ColumnInfo(name = "tileX")
    val tileX: Int,

    @ColumnInfo(name = "tileY")
    val tileY: Int,

    @ColumnInfo(name = "layerType")
    val layerType: String = "TOPO", // TOPO, SATELLITE, TERRAIN, NAUTICAL

    @ColumnInfo(name = "tileData", typeAffinity = ColumnInfo.BLOB)
    val tileData: ByteArray,

    @ColumnInfo(name = "minLatitude")
    val minLatitude: Double,

    @ColumnInfo(name = "maxLatitude")
    val maxLatitude: Double,

    @ColumnInfo(name = "minLongitude")
    val minLongitude: Double,

    @ColumnInfo(name = "maxLongitude")
    val maxLongitude: Double,

    @ColumnInfo(name = "dataSizeBytes")
    val dataSizeBytes: Int,

    @ColumnInfo(name = "cachedTimestamp")
    val cachedTimestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "lastAccessedTimestamp")
    val lastAccessedTimestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "regionTag")
    val regionTag: String = "DEFAULT"
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CachedMapTileEntity

        if (id != other.id) return false
        if (tileKey != other.tileKey) return false
        if (zoom != other.zoom) return false
        if (tileX != other.tileX) return false
        if (tileY != other.tileY) return false
        if (layerType != other.layerType) return false
        if (!tileData.contentEquals(other.tileData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + tileKey.hashCode()
        result = 31 * result + zoom
        result = 31 * result + tileX
        result = 31 * result + tileY
        result = 31 * result + layerType.hashCode()
        result = 31 * result + tileData.contentHashCode()
        return result
    }
}
