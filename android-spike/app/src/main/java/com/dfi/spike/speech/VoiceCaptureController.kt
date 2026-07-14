package com.dfi.spike.speech

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.dfi.spike.gesture.SpikeEvents
import com.dfi.spike.log.SpikeEventLog

/**
 * ko-KR voice capture for the short-tap interaction.
 *
 * On-device preference:
 *  - API 31+: query SpeechRecognizer.isOnDeviceRecognitionAvailable and use
 *    createOnDeviceSpeechRecognizer when available.
 *  - Otherwise: fall back to the default recognizer with
 *    RecognizerIntent.EXTRA_PREFER_OFFLINE. The default recognizer MAY
 *    require network; this limitation is logged explicitly rather than
 *    hidden (offline honesty requirement).
 *
 * The spike does NOT route intents to production actions. The recognized
 * text is logged as VOICE_RESULT and shown to the tester.
 */
class VoiceCaptureController(
    private val context: Context,
    private val log: SpikeEventLog,
) {
    private var recognizer: SpeechRecognizer? = null
    private var active = false

    fun onDeviceAvailable(): Boolean =
        Build.VERSION.SDK_INT >= 31 && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)

    fun describeAvailability(): String = when {
        !SpeechRecognizer.isRecognitionAvailable(context) ->
            "speech recognition unavailable on this device"
        Build.VERSION.SDK_INT < 31 ->
            "API < 31: on-device availability not queryable; recognition may require network"
        onDeviceAvailable() -> "on-device recognizer available (offline-capable)"
        else -> "on-device recognizer NOT available; recognition may require network"
    }

    /** Must be called on the main thread (SpeechRecognizer requirement). */
    fun start(onFinished: (text: String?) -> Unit) {
        if (active) return
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            log.log(SpikeEvents.VOICE_ERROR, mapOf("code" to "-1", "name" to "RECOGNITION_UNAVAILABLE"))
            onFinished(null)
            return
        }
        active = true
        val onDevice = onDeviceAvailable()
        log.log(
            SpikeEvents.VOICE_CAPTURE_STARTED,
            mapOf("onDevice" to onDevice.toString(), "lang" to "ko-KR"),
        )
        val r = if (onDevice && Build.VERSION.SDK_INT >= 31) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }
        recognizer = r
        r.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (text.isNullOrBlank()) {
                    log.log(SpikeEvents.VOICE_ERROR, mapOf("code" to "0", "name" to "EMPTY_RESULT"))
                    finish(null, onFinished)
                } else {
                    log.log(SpikeEvents.VOICE_RESULT, mapOf("text" to text, "onDevice" to onDevice.toString()))
                    finish(text, onFinished)
                }
            }

            override fun onError(error: Int) {
                log.log(
                    SpikeEvents.VOICE_ERROR,
                    mapOf("code" to error.toString(), "name" to errorName(error)),
                )
                finish(null, onFinished)
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        r.startListening(intent)
    }

    private fun finish(text: String?, onFinished: (String?) -> Unit) {
        active = false
        recognizer?.destroy()
        recognizer = null
        onFinished(text)
    }

    fun destroy() {
        active = false
        recognizer?.destroy()
        recognizer = null
    }

    private fun errorName(code: Int): String = when (code) {
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "NETWORK_TIMEOUT"
        SpeechRecognizer.ERROR_NETWORK -> "NETWORK"
        SpeechRecognizer.ERROR_AUDIO -> "AUDIO"
        SpeechRecognizer.ERROR_SERVER -> "SERVER"
        SpeechRecognizer.ERROR_CLIENT -> "CLIENT"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "SPEECH_TIMEOUT"
        SpeechRecognizer.ERROR_NO_MATCH -> "NO_MATCH"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RECOGNIZER_BUSY"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "INSUFFICIENT_PERMISSIONS"
        10 -> "TOO_MANY_REQUESTS"
        11 -> "SERVER_DISCONNECTED"
        12 -> "LANGUAGE_NOT_SUPPORTED"
        13 -> "LANGUAGE_UNAVAILABLE"
        14 -> "CANNOT_CHECK_SUPPORT"
        else -> "UNKNOWN_$code"
    }
}
