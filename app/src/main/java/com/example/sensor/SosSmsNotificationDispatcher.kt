package com.example.sensor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.data.local.EmergencyContactEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SosDispatchState(
    val isDispatching: Boolean = false,
    val lastDispatchedTime: Long = 0L,
    val dispatchedCount: Int = 0,
    val totalContacts: Int = 0,
    val lastMessageText: String = "",
    val lastStatusMessage: String? = null,
    val isSuccess: Boolean = false
)

class SosSmsNotificationDispatcher(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "garmin_sos_emergency_channel"
        const val CHANNEL_NAME = "Garmin SOS Darurat"
        const val NOTIFICATION_ID = 9991
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val _dispatchState = MutableStateFlow(SosDispatchState())
    val dispatchState: StateFlow<SosDispatchState> = _dispatchState.asStateFlow()

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi Peringatan Bahaya & Koordinat GPS SOS Garmin"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                enableLights(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildEmergencyMessage(latitude: Double, longitude: Double, altitudeMeters: Double): String {
        val sdf = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault())
        val timeStr = sdf.format(Date())
        val mapsLink = "https://maps.google.com/?q=${String.format(Locale.US, "%.6f,%.6f", latitude, longitude)}"

        return """
            [GARMIN SOS DARURAT]
            SAYA MEMBUTUHKAN BANTUAN SEGERA!
            Lokasi Koordinat GPS:
            Lat: ${String.format(Locale.US, "%.6f", latitude)}
            Lon: ${String.format(Locale.US, "%.6f", longitude)}
            Ketinggian: ${String.format(Locale.US, "%.0f mdpl", altitudeMeters)}
            Peta: $mapsLink
            Waktu: $timeStr
            Dikirim via Garmin Tactical Finder SOS
        """.trimIndent()
    }

    fun dispatchSosToContacts(
        contacts: List<EmergencyContactEntity>,
        latitude: Double,
        longitude: Double,
        altitudeMeters: Double
    ) {
        val message = buildEmergencyMessage(latitude, longitude, altitudeMeters)
        _dispatchState.value = _dispatchState.value.copy(
            isDispatching = true,
            lastMessageText = message
        )

        // 1. Post High-Priority Emergency Device Notification with direct map view intent
        postEmergencyNotification(latitude, longitude, altitudeMeters, message)

        // 2. Dispatch SMS to contacts
        val hasSmsPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        var successCount = 0
        var failCount = 0

        if (hasSmsPermission && contacts.isNotEmpty()) {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            contacts.forEach { contact ->
                try {
                    val cleanPhone = contact.phoneNumber.replace(" ", "").replace("-", "")
                    if (cleanPhone.isNotBlank()) {
                        val parts = smsManager.divideMessage(message)
                        if (parts.size > 1) {
                            smsManager.sendMultipartTextMessage(cleanPhone, null, parts, null, null)
                        } else {
                            smsManager.sendTextMessage(cleanPhone, null, message, null, null)
                        }
                        successCount++
                    }
                } catch (e: Exception) {
                    failCount++
                }
            }
        }

        val status = if (hasSmsPermission) {
            "SOS Terkirim ke $successCount dari ${contacts.size} kontak darurat via SMS & Notifikasi Sistem."
        } else {
            "Notifikasi SOS aktif dengan koordinat GPS. Berikan izin SMS untuk pengiriman langsung tanpa dialog."
        }

        _dispatchState.value = SosDispatchState(
            isDispatching = false,
            lastDispatchedTime = System.currentTimeMillis(),
            dispatchedCount = successCount,
            totalContacts = contacts.size,
            lastMessageText = message,
            lastStatusMessage = status,
            isSuccess = true
        )
    }

    private fun postEmergencyNotification(
        latitude: Double,
        longitude: Double,
        altitudeMeters: Double,
        message: String
    ) {
        val mapUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(SOS+Darurat)")
        val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            mapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 PERINGATAN SOS DARURAT DIAKTIFKAN")
            .setContentText("Koordinat: ${String.format(Locale.US, "%.5f, %.5f", latitude, longitude)} (${altitudeMeters.toInt()}m)")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_mapmode, "Buka Google Maps", pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun createSmsIntent(phoneNumber: String, message: String): Intent {
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun dismissEmergencyNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
