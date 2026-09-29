package com.example.sensor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class VoiceCommandState(
    val isListening: Boolean = false,
    val spokenText: String = "",
    val lastLoggedWaypoint: String? = null,
    val errorMessage: String? = null,
    val soundLevelRms: Float = 0f,
    val isSupported: Boolean = true
)

sealed class ParsedVoiceAction {
    data class LogWaypoint(val name: String, val category: String, val notes: String) : ParsedVoiceAction()
    data class Unknown(val rawText: String) : ParsedVoiceAction()
}

class VoiceWaypointManager(private val context: Context) {
    private var speechRecognizer: SpeechRecognizer? = null

    private val _voiceState = MutableStateFlow(
        VoiceCommandState(isSupported = SpeechRecognizer.isRecognitionAvailable(context))
    )
    val voiceState: StateFlow<VoiceCommandState> = _voiceState.asStateFlow()

    private var onWaypointDetectedListener: ((name: String, category: String, notes: String) -> Unit)? = null

    fun setOnWaypointDetectedListener(listener: (name: String, category: String, notes: String) -> Unit) {
        onWaypointDetectedListener = listener
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _voiceState.value = _voiceState.value.copy(
                isSupported = false,
                errorMessage = "Fitur Speech Recognition tidak tersedia pada perangkat ini"
            )
            return
        }

        stopListening()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _voiceState.value = _voiceState.value.copy(
                            isListening = true,
                            errorMessage = null
                        )
                    }

                    override fun onBeginningOfSpeech() {
                        _voiceState.value = _voiceState.value.copy(spokenText = "Mendengarkan suara...")
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _voiceState.value = _voiceState.value.copy(soundLevelRms = rmsdB.coerceAtLeast(0f))
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _voiceState.value = _voiceState.value.copy(isListening = false)
                    }

                    override fun onError(error: Int) {
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "Tidak ada suara yang cocok. Coba bicara lebih jelas."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Waktu habis. Silakan tekan mikrofon lagi."
                            SpeechRecognizer.ERROR_AUDIO -> "Gangguan perekaman audio mikrofon."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Izin mikrofon belum diberikan."
                            else -> "Mendengarkan terhenti. Ketuk untuk mencoba lagi."
                        }
                        _voiceState.value = _voiceState.value.copy(
                            isListening = false,
                            errorMessage = msg
                        )
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        _voiceState.value = _voiceState.value.copy(
                            isListening = false,
                            spokenText = text
                        )

                        if (text.isNotBlank()) {
                            processSpokenCommand(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.firstOrNull()?.let { partial ->
                            _voiceState.value = _voiceState.value.copy(spokenText = partial)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "id-ID")
                putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayListOf("id-ID", "en-US"))
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _voiceState.value = _voiceState.value.copy(
                isListening = false,
                errorMessage = "Gagal memulai mikrofon: ${e.localizedMessage}"
            )
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        _voiceState.value = _voiceState.value.copy(isListening = false)
    }

    /**
     * Parses spoken text for hands-free waypoint creation.
     * Examples:
     * - "Tandai pos 2 sembalun" -> Name: "Pos 2 Sembalun", Category: CHECKPOINT
     * - "Simpan titik sumber air jernih" -> Name: "Sumber Air Jernih", Category: WATER_SOURCE
     * - "Catat waypoint puncak rinjani" -> Name: "Puncak Rinjani", Category: SUMMIT
     * - "Tandai bahaya jurang terjal" -> Name: "Jurang Terjal", Category: HAZARD
     * - "Log camp pelawangan" -> Name: "Pelawangan", Category: CAMP
     */
    fun processSpokenCommand(text: String) {
        val lower = text.lowercase(Locale.ROOT).trim()

        // Strip prefix command verbs
        val prefixes = listOf(
            "tandai titik", "tandai waypoint", "tandai pos", "tandai lokasi", "tandai",
            "simpan titik", "simpan waypoint", "simpan pos", "simpan lokasi", "simpan",
            "catat titik", "catat waypoint", "catat pos", "catat",
            "log waypoint", "log point", "log titik", "log",
            "tambah waypoint", "tambah titik", "tambah",
            "waypoint", "titik"
        )

        var cleanName = lower
        for (prefix in prefixes) {
            if (cleanName.startsWith(prefix)) {
                cleanName = cleanName.removePrefix(prefix).trim()
                break
            }
        }

        // Categorize based on keywords
        val category = when {
            cleanName.contains("air") || cleanName.contains("sungai") || cleanName.contains("mata air") || cleanName.contains("danau") -> "WATER_SOURCE"
            cleanName.contains("puncak") || cleanName.contains("summit") || cleanName.contains("top") || cleanName.contains("bukit") -> "SUMMIT"
            cleanName.contains("camp") || cleanName.contains("tenda") || cleanName.contains("shelter") || cleanName.contains("basecamp") || cleanName.contains("kemah") -> "CAMP"
            cleanName.contains("bahaya") || cleanName.contains("jurang") || cleanName.contains("longsor") || cleanName.contains("waspada") -> "HAZARD"
            cleanName.contains("harta") || cleanName.contains("emas") || cleanName.contains("pusaka") -> "TREASURE"
            cleanName.contains("pos") || cleanName.contains("check") || cleanName.contains("simpang") || cleanName.contains("gerbang") -> "CHECKPOINT"
            else -> "WAYPOINT"
        }

        val formattedName = if (cleanName.isNotBlank()) {
            cleanName.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            }
        } else {
            "Waypoint Suara ${System.currentTimeMillis() % 1000}"
        }

        _voiceState.value = _voiceState.value.copy(
            lastLoggedWaypoint = formattedName
        )

        onWaypointDetectedListener?.invoke(
            formattedName,
            category,
            "Dicatat via Perintah Suara Hands-Free: \"$text\""
        )
    }
}
