package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [LocationEntity::class, TrackEntity::class, OfflineMapTileEntity::class, CachedMapTileEntity::class, EmergencyContactEntity::class],
    version = 5,
    exportSchema = false
)
abstract class GarminDatabase : RoomDatabase() {
    abstract fun garminDao(): GarminDao

    companion object {
        @Volatile
        private var INSTANCE: GarminDatabase? = null

        fun getInstance(context: Context): GarminDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GarminDatabase::class.java,
                    "garmin_finder_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = getInstance(context).garminDao()
                    dao.insertLocations(getPrepopulatedLocations())
                    dao.insertOfflineTiles(getPrepopulatedOfflineTiles())
                    dao.insertEmergencyContacts(getPrepopulatedContacts())
                }
            }
        }

        fun getPrepopulatedContacts(): List<EmergencyContactEntity> {
            return listOf(
                EmergencyContactEntity(
                    name = "BASARNAS SAR Mataram",
                    phoneNumber = "+62811390115",
                    relationship = "SAR / Evakuasi",
                    isPrimary = true
                ),
                EmergencyContactEntity(
                    name = "Posko Penyelamat Rinjani",
                    phoneNumber = "+62819077123",
                    relationship = "Posko Lapangan",
                    isPrimary = true
                ),
                EmergencyContactEntity(
                    name = "Layanan Panggilan Darurat",
                    phoneNumber = "112",
                    relationship = "Darurat Nasional",
                    isPrimary = false
                ),
                EmergencyContactEntity(
                    name = "Ranger Taman Nasional TNGR",
                    phoneNumber = "+62878641122",
                    relationship = "Ranger Hutan",
                    isPrimary = false
                )
            )
        }

        fun getPrepopulatedOfflineTiles(): List<OfflineMapTileEntity> {
            val now = System.currentTimeMillis()
            return listOf(
                OfflineMapTileEntity(
                    regionName = "Taman Nasional Gunung Rinjani & Kaldera",
                    centerLatitude = -8.4113,
                    centerLongitude = 116.4573,
                    radiusKm = 25.0,
                    zoomLevel = 15,
                    tileCount = 384,
                    dataSizeBytes = 18_450_000L, // ~18.4 MB
                    cachedTimestamp = now - 86400000 * 2,
                    contoursJson = "Contours 50m interval, Sembalun-Senaru-Segara Anak trail landmarks",
                    isDownloaded = true
                ),
                OfflineMapTileEntity(
                    regionName = "Lombok Barat (Kuripan, Jagaraga & Sekotong)",
                    centerLatitude = -8.6500,
                    centerLongitude = 116.1400,
                    radiusKm = 15.0,
                    zoomLevel = 16,
                    tileCount = 256,
                    dataSizeBytes = 12_200_000L, // ~12.2 MB
                    cachedTimestamp = now - 86400000 * 1,
                    contoursJson = "Topographic hills 25m interval, geological quartz belt, river streams",
                    isDownloaded = true
                ),
                OfflineMapTileEntity(
                    regionName = "Navigasi Selat Lombok & Pesisir Lembar",
                    centerLatitude = -8.5800,
                    centerLongitude = 115.8900,
                    radiusKm = 30.0,
                    zoomLevel = 14,
                    tileCount = 180,
                    dataSizeBytes = 8_750_000L, // ~8.7 MB
                    cachedTimestamp = now - 86400000 * 4,
                    contoursJson = "Nautical bathymetric depth soundings, harbor channels, reefs",
                    isDownloaded = true
                )
            )
        }

        fun getPrepopulatedLocations(): List<LocationEntity> {
            val now = System.currentTimeMillis()
            return listOf(
                // Harta Karun (Geocaching / Treasure coordinates)
                LocationEntity(
                    title = "Cache Lembah Jagaraga",
                    category = "TREASURE",
                    latitude = -8.6482,
                    longitude = 116.1425,
                    altitude = 85.0,
                    description = "Kotak logam tahan cuaca tersembunyi di bawah akar pohon beringin tua dekat aliran sungai kering.",
                    details = "Petunjuk: 15 langkah ke arah utara dari batu bersusun tiga. Kode kunci: 1985.",
                    priority = "TINGGI",
                    isFound = false,
                    timestamp = now - 86400000 * 3
                ),
                LocationEntity(
                    title = "Peti Tersembunyi Bukit Sasak",
                    category = "TREASURE",
                    latitude = -8.6720,
                    longitude = 116.1280,
                    altitude = 240.0,
                    description = "Kontainer geocache tertutup lumut di celah tebing batu karst.",
                    details = "Akses pendakian terjal. Koordinat presisi GPS Garmin Finder.",
                    priority = "SEDANG",
                    isFound = false,
                    timestamp = now - 86400000 * 5
                ),

                // Mineral Bumi (Geological potential sites)
                LocationEntity(
                    title = "Formasi Kuarsa & Emas Sekotong",
                    category = "MINERAL",
                    latitude = -8.7621,
                    longitude = 115.9810,
                    altitude = 310.0,
                    description = "Urat kuarsa hidrotermal dengan indikasi mineralisasi pirit dan emas sulfida tinggi.",
                    details = "Tipe: Urat Kuarsa (Quartz Vein) - Estimasi kadar: 3.5 - 5.2 g/t Au.",
                    priority = "TINGGI",
                    isFavorite = true,
                    timestamp = now - 86400000 * 7
                ),
                LocationEntity(
                    title = "Endapan Pasir Besi Pesisir Selatan",
                    category = "MINERAL",
                    latitude = -8.8450,
                    longitude = 116.1850,
                    altitude = 12.0,
                    description = "Lapisan pasir hitam kaya magnetit dan ilmenit sepanjang garis pantai purba.",
                    details = "Kandungan Fe total terdeteksi 48.6%. Potensi survei geomagnetik lanjut.",
                    priority = "SEDANG",
                    timestamp = now - 86400000 * 10
                ),
                LocationEntity(
                    title = "Singkapan Tembaga & Malakit Kuripan",
                    category = "MINERAL",
                    latitude = -8.6590,
                    longitude = 116.1580,
                    altitude = 145.0,
                    description = "Zona alterasi argilik dengan semburat mineral malakit kehijauan pada batuan volkanik.",
                    details = "Tipe: Porfiri Tembaga Skarn. Sampel batuan tercatat dalam survei geologi.",
                    priority = "TINGGI",
                    timestamp = now - 86400000 * 2
                ),

                // Benda Pusaka (Cultural Heritage & Relics)
                LocationEntity(
                    title = "Situs Fragmen Keris Sasak Kuno",
                    category = "HERITAGE",
                    latitude = -8.6395,
                    longitude = 116.1360,
                    altitude = 92.0,
                    description = "Titik penemuan bilah pusaka pamor beras wutah peninggalan era kedatuan Lombok purba.",
                    details = "Era: Abad ke-16 Masehi. Ditemukan pada kedalaman 1.2 meter dekat pondasi batu bata kuno.",
                    priority = "TINGGI",
                    isFavorite = true,
                    timestamp = now - 86400000 * 12
                ),
                LocationEntity(
                    title = "Batu Prasasti Tapak Kuno Beremi",
                    category = "HERITAGE",
                    latitude = -8.6515,
                    longitude = 116.1450,
                    altitude = 110.0,
                    description = "Monolit batu andesit berukir aksara Kawi/Jawa Kuno terlindungi kanopi bambu.",
                    details = "Warisan Cagar Budaya. Koordinat dijaga untuk pemantauan konservasi cagar arkeologi.",
                    priority = "TINGGI",
                    timestamp = now - 86400000 * 15
                ),
                LocationEntity(
                    title = "Fragmen Gerabah Kuno Dinasti",
                    category = "HERITAGE",
                    latitude = -8.6650,
                    longitude = 116.1150,
                    altitude = 65.0,
                    description = "Pecahan mangkuk keramik glasir biru putih seladon peninggalan rute perdagangan maritim.",
                    details = "Era: Estimasi Dinasti Ming akhir. Terdata dalam inventaris pusaka daerah.",
                    priority = "SEDANG",
                    timestamp = now - 86400000 * 20
                ),

                // Prioritas Wilayah (Emergency, SAR, Water source, Basecamp)
                LocationEntity(
                    title = "Pos Pantau & SAR Kuripan",
                    category = "PRIORITY_ZONE",
                    latitude = -8.6470,
                    longitude = 116.1410,
                    altitude = 78.0,
                    description = "Posko siaga komunikasi radio darurat repeater VHF/UHF dan logistik SAR petualang.",
                    details = "Prioritas Wilayah: Kategori Pos Darurat Utama. Dilengkapi helipad darurat.",
                    priority = "TINGGI",
                    timestamp = now - 86400000 * 1
                ),
                LocationEntity(
                    title = "Mata Air Alami Lembah Jagaraga",
                    category = "PRIORITY_ZONE",
                    latitude = -8.6530,
                    longitude = 116.1390,
                    altitude = 125.0,
                    description = "Sumber air tawar bersih mengalir sepanjang tahun, aman diminum langsung oleh pendaki.",
                    details = "Debit: 4.5 liter/detik. Titik prioritas logistik air minum tim ekspedisi.",
                    priority = "TINGGI",
                    isFavorite = true,
                    timestamp = now - 86400000 * 4
                ),
                LocationEntity(
                    title = "Shelter Camp Bukit Kuripan",
                    category = "PRIORITY_ZONE",
                    latitude = -8.6610,
                    longitude = 116.1510,
                    altitude = 195.0,
                    description = "Area datar berkapasitas 8 tenda dengan perlindungan angin kencang.",
                    details = "Prioritas Wilayah: Basecamp Darurat & Tempat Istirahat.",
                    priority = "SEDANG",
                    timestamp = now - 86400000 * 6
                ),
                LocationEntity(
                    title = "Zona Rawan Longsor Tebing Barat",
                    category = "PRIORITY_ZONE",
                    latitude = -8.6580,
                    longitude = 116.1320,
                    altitude = 160.0,
                    description = "Kemiringan lereng > 45 derajat dengan struktur tanah gembur. Hindari saat hujan lebat.",
                    details = "Kategori: Zona Bahaya / Danger Zone. Perlu kewaspadaan tinggi.",
                    priority = "TINGGI",
                    timestamp = now - 86400000 * 8
                ),

                // Pendakian (Hiking waypoints)
                LocationEntity(
                    title = "Puncak Rinjani (Plawangan)",
                    category = "HIKING",
                    latitude = -8.4113,
                    longitude = 116.4573,
                    altitude = 3726.0,
                    description = "Titik puncak gunung berapi tertinggi kedua di Indonesia dengan panorama kaldera Segara Anak.",
                    details = "Jalur: Sembalun / Senaru. Koordinat puncak utama untuk acuan navigasi.",
                    priority = "TINGGI",
                    timestamp = now - 86400000 * 14
                ),

                // Pelayaran (Marine Navigation)
                LocationEntity(
                    title = "Titik Labuh Selat Lombok",
                    category = "MARINE",
                    latitude = -8.5800,
                    longitude = 115.8900,
                    altitude = 0.0,
                    description = "Waypoint navigasi kapal & perahu layar di jalur lintas maritim Selat Lombok.",
                    details = "Kedalaman: 35 meter. Dasar laut pasir lumpur ideal untuk jangkar.",
                    priority = "SEDANG",
                    timestamp = now - 86400000 * 16
                )
            )
        }
    }
}
