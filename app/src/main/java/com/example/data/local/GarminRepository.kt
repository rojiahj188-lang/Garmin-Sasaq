package com.example.data.local

import kotlinx.coroutines.flow.Flow

class GarminRepository(private val dao: GarminDao) {
    val allLocations: Flow<List<LocationEntity>> = dao.getAllLocations()
    val favoriteLocations: Flow<List<LocationEntity>> = dao.getFavoriteLocations()
    val allTracks: Flow<List<TrackEntity>> = dao.getAllTracks()

    fun getLocationsByCategory(category: String): Flow<List<LocationEntity>> =
        dao.getLocationsByCategory(category)

    fun getLocationById(id: Long): Flow<LocationEntity?> =
        dao.getLocationById(id)

    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean, label: String = "") =
        dao.updateFavoriteStatus(id, isFavorite, label)

    suspend fun insertLocation(location: LocationEntity): Long =
        dao.insertLocation(location)

    suspend fun updateLocation(location: LocationEntity) =
        dao.updateLocation(location)

    suspend fun deleteLocation(location: LocationEntity) =
        dao.deleteLocation(location)

    suspend fun deleteLocationById(id: Long) =
        dao.deleteLocationById(id)

    suspend fun insertTrack(track: TrackEntity): Long =
        dao.insertTrack(track)

    suspend fun deleteTrack(track: TrackEntity) =
        dao.deleteTrack(track)

    suspend fun deleteTrackById(id: Long) =
        dao.deleteTrackById(id)

    val allOfflineTiles: Flow<List<OfflineMapTileEntity>> = dao.getAllOfflineTiles()

    suspend fun insertOfflineTile(tile: OfflineMapTileEntity): Long =
        dao.insertOfflineTile(tile)

    suspend fun deleteOfflineTile(tile: OfflineMapTileEntity) =
        dao.deleteOfflineTile(tile)

    suspend fun deleteOfflineTileById(id: Long) =
        dao.deleteOfflineTileById(id)

    // Cached map tiles repository methods
    val cachedTilesCount: Flow<Int> = dao.getCachedTilesCount()
    val totalCacheSizeBytes: Flow<Long?> = dao.getTotalTileCacheSizeBytes()

    fun getCachedTile(zoom: Int, tileX: Int, tileY: Int, layerType: String): Flow<CachedMapTileEntity?> =
        dao.getCachedTile(zoom, tileX, tileY, layerType)

    suspend fun getDirectTile(tileKey: String, layerType: String): CachedMapTileEntity? =
        dao.getDirectTile(tileKey, layerType)

    fun getTilesForBoundingBox(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double, zoom: Int, layerType: String): Flow<List<CachedMapTileEntity>> =
        dao.getTilesForBoundingBox(minLat, maxLat, minLon, maxLon, zoom, layerType)

    suspend fun insertCachedTile(tile: CachedMapTileEntity): Long =
        dao.insertCachedTile(tile)

    suspend fun insertCachedTiles(tiles: List<CachedMapTileEntity>) =
        dao.insertCachedTiles(tiles)

    suspend fun deleteTilesByRegion(regionTag: String) =
        dao.deleteTilesByRegion(regionTag)

    suspend fun clearAllCachedTiles() =
        dao.clearAllCachedTiles()

    // Emergency Contacts
    val allEmergencyContacts: Flow<List<EmergencyContactEntity>> = dao.getAllEmergencyContacts()

    suspend fun insertEmergencyContact(contact: EmergencyContactEntity): Long =
        dao.insertEmergencyContact(contact)

    suspend fun deleteEmergencyContact(contact: EmergencyContactEntity) =
        dao.deleteEmergencyContact(contact)
}
