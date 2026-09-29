package com.example.sensor

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SosState(
    val isActive: Boolean = false,
    val isFlashlightOn: Boolean = false,
    val isAudioEnabled: Boolean = true,
    val currentLetter: String = "",
    val isLightEmitting: Boolean = false,
    val cycleCount: Int = 0
)

class SosEmergencyManager(private val context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val cameraId: String? by lazy {
        try {
            cameraManager?.cameraIdList?.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (_: Exception) {
            null
        }
    }

    private var toneGenerator: ToneGenerator? = null

    private val _sosState = MutableStateFlow(SosState())
    val sosState: StateFlow<SosState> = _sosState.asStateFlow()

    private var sosJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun startSos(useFlashlight: Boolean = true, useAudio: Boolean = true) {
        if (_sosState.value.isActive) return

        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        } catch (_: Exception) {}

        _sosState.value = SosState(
            isActive = true,
            isFlashlightOn = useFlashlight,
            isAudioEnabled = useAudio,
            currentLetter = "S",
            isLightEmitting = false
        )

        sosJob?.cancel()
        sosJob = scope.launch {
            var cycle = 0
            while (isActive) {
                cycle++
                // Letter 'S': 3 short dots (. . .)
                emitLetter("S", listOf(180L, 180L, 180L), isDash = false, cycle)
                delay(350)

                // Letter 'O': 3 long dashes (- - -)
                emitLetter("O", listOf(500L, 500L, 500L), isDash = true, cycle)
                delay(350)

                // Letter 'S': 3 short dots (. . .)
                emitLetter("S", listOf(180L, 180L, 180L), isDash = false, cycle)

                // Word pause between SOS repetitions
                _sosState.value = _sosState.value.copy(
                    currentLetter = "PAUSE",
                    isLightEmitting = false,
                    cycleCount = cycle
                )
                setTorch(false)
                delay(1200)
            }
        }
    }

    fun stopSos() {
        sosJob?.cancel()
        sosJob = null
        setTorch(false)
        try {
            toneGenerator?.stopTone()
            toneGenerator?.release()
        } catch (_: Exception) {}
        toneGenerator = null
        _sosState.value = SosState(isActive = false)
    }

    fun toggleAudio() {
        val curr = _sosState.value
        _sosState.value = curr.copy(isAudioEnabled = !curr.isAudioEnabled)
    }

    fun toggleFlashlight() {
        val curr = _sosState.value
        val newFlash = !curr.isFlashlightOn
        _sosState.value = curr.copy(isFlashlightOn = newFlash)
        if (!newFlash) {
            setTorch(false)
        }
    }

    private suspend fun emitLetter(
        letter: String,
        durations: List<Long>,
        isDash: Boolean,
        cycle: Int
    ) {
        for (duration in durations) {
            if (!scope.isActive) break

            // Signal ON
            _sosState.value = _sosState.value.copy(
                currentLetter = letter,
                isLightEmitting = true,
                cycleCount = cycle
            )

            if (_sosState.value.isFlashlightOn) {
                setTorch(true)
            }
            if (_sosState.value.isAudioEnabled) {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, duration.toInt())
                } catch (_: Exception) {}
            }
            vibrate(duration)

            delay(duration)

            // Signal OFF (interval between symbols in same letter)
            _sosState.value = _sosState.value.copy(isLightEmitting = false)
            setTorch(false)
            delay(180)
        }
    }

    private fun setTorch(on: Boolean) {
        val id = cameraId ?: return
        try {
            cameraManager?.setTorchMode(id, on)
        } catch (_: Exception) {}
    }

    private fun vibrate(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                v?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }
}
