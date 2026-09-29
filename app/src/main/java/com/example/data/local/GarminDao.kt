package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GarminDao {
    @Query("SELECT * FROM saved_locations ORDER BY timestamp DESC")
    fun getAllLocations(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM saved_locations WHERE category = :category ORDER BY timestamp DESC")
    fun getLocationsByCategory(category: String): Flow<List<LocationEntity>>

    @Query("SELECT * FROM saved_locations WHERE id = :id LIMIT 1")
    fun getLocationById(id: Long): Flow<LocationEntity?>

    @Query("SELECT * FROM saved_locations WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteLocations(): Flow<List<LocationEntity>>

    @Query("UPDATE saved_locations SET isFavorite = :isFavorite, favoriteLabel = :favoriteLabel WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean, favoriteLabel: String = "")

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>)

    @Update
    suspend fun updateLocation(location: LocationEntity)

    @Delete
    suspend fun deleteLocation(location: LocationEntity)

    @Query("DELETE FROM saved_locations WHERE id = :id")
    suspend fun deleteLocationById(id: Long)

    @Query("SELECT * FROM saved_tracks ORDER BY startTime DESC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity): Long

    @Delete
    suspend fun deleteTrack(track: TrackEntity)

    @Query("DELETE FROM saved_tracks WHERE id = :id")
    suspend fun deleteTrackById(id: Long)

    @Query("SELECT * FROM offline_map_tiles ORDER BY cachedTimestamp DESC")
    fun getAllOfflineTiles(): Flow<List<OfflineMapTileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflineTile(tile: OfflineMapTileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflineTiles(tiles: List<OfflineMapTileEntity>)

    @Delete
    suspend fun deleteOfflineTile(tile: OfflineMapTileEntity)

    @Query("DELETE FROM offline_map_tiles WHERE id = :id")
    suspend fun deleteOfflineTileById(id: Long)

    // Cached Map Tiles schema queries
    @Query("SELECT * FROM cached_map_tiles WHERE zoom = :zoom AND tileX = :tileX AND tileY = :tileY AND layerType = :layerType LIMIT 1")
    fun getCachedTile(zoom: Int, tileX: Int, tileY: Int, layerType: String): Flow<CachedMapTileEntity?>

    @Query("SELECT * FROM cached_map_tiles WHERE tileKey = :tileKey AND layerType = :layerType LIMIT 1")
    suspend fun getDirectTile(tileKey: String, layerType: String): CachedMapTileEntity?

    @Query("SELECT * FROM cached_map_tiles WHERE zoom = :zoom AND layerType = :layerType AND minLatitude <= :maxLat AND maxLatitude >= :minLat AND minLongitude <= :maxLon AND maxLongitude >= :minLon")
    fun getTilesForBoundingBox(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double, zoom: Int, layerType: String): Flow<List<CachedMapTileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedTile(tile: CachedMapTileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedTiles(tiles: List<CachedMapTileEntity>)

    @Query("UPDATE cached_map_tiles SET lastAccessedTimestamp = :timestamp WHERE tileKey = :tileKey")
    suspend fun updateTileAccess(tileKey: String, timestamp: Long)

    @Delete
    suspend fun deleteCachedTile(tile: CachedMapTileEntity)

    @Query("DELETE FROM cached_map_tiles WHERE regionTag = :regionTag")
    suspend fun deleteTilesByRegion(regionTag: String)

    @Query("DELETE FROM cached_map_tiles")
    suspend fun clearAllCachedTiles()

    @Query("SELECT COUNT(*) FROM cached_map_tiles")
    fun getCachedTilesCount(): Flow<Int>

    @Query("SELECT SUM(dataSizeBytes) FROM cached_map_tiles")
    fun getTotalTileCacheSizeBytes(): Flow<Long?>

    // Emergency Contacts queries
    @Query("SELECT * FROM emergency_contacts ORDER BY isPrimary DESC, name ASC")
    fun getAllEmergencyContacts(): Flow<List<EmergencyContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyContact(contact: EmergencyContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyContacts(contacts: List<EmergencyContactEntity>)

    @Delete
    suspend fun deleteEmergencyContact(contact: EmergencyContactEntity)
}
