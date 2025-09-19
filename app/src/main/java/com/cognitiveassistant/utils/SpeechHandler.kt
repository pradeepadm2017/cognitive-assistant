package com.cognitiveassistant.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class SpeechHandler(
    private val context: Context,
    private val onSpeechResult: (String) -> Unit,
    private val onError: (String) -> Unit
) : RecognitionListener {

    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var retryCount = 0
    private val maxRetries = 2

    interface SpeechCallback {
        fun onSpeechReady()
        fun onSpeechResult(spokenText: String)
        fun onSpeechError(error: String)
    }

    fun initializeTTS(callback: () -> Unit) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
                callback()
            } else {
                onError("Text-to-speech initialization failed")
            }
        }
    }

    fun speakText(text: String, onComplete: (() -> Unit)? = null) {
        textToSpeech?.let { tts ->
            if (onComplete != null) {
                tts.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        onComplete()
                    }
                    override fun onError(utteranceId: String?) {
                        onError("Speech synthesis error")
                    }
                })
                val params = Bundle()
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "question_utterance")
            } else {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    fun startListening() {
        Log.d("SpeechHandler", "Starting speech recognition...")
        retryCount = 0 // Reset retry count for new listening session

        // Detailed diagnostics
        Log.d("SpeechHandler", "Checking speech recognition availability...")
        val isAvailable = SpeechRecognizer.isRecognitionAvailable(context)
        Log.d("SpeechHandler", "Speech recognition available: $isAvailable")

        if (!isAvailable) {
            Log.e("SpeechHandler", "Speech recognition not available on this device")
            onError("Speech recognition not available on this device")
            return
        }

        // Check for available speech recognition engines
        val availableEngines = getAvailableSpeechEngines()
        Log.d("SpeechHandler", "Available speech engines: $availableEngines")

        if (availableEngines.isEmpty()) {
            Log.w("SpeechHandler", "No speech recognition engines found")
        } else {
            Log.d("SpeechHandler", "Using speech engine: ${availableEngines.first()}")
        }

        // Stop TTS if it's still speaking
        textToSpeech?.stop()

        // Clean up any existing recognizer
        speechRecognizer?.destroy()
        speechRecognizer = null

        // Wait a moment for TTS to fully stop and audio to be available
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                Log.d("SpeechHandler", "Creating new speech recognizer...")
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

                if (speechRecognizer == null) {
                    Log.e("SpeechHandler", "Failed to create SpeechRecognizer")
                    onError("Could not initialize speech recognition service")
                    return@postDelayed
                }

                speechRecognizer?.setRecognitionListener(this)

                val intent = createSpeechRecognitionIntent()

                Log.d("SpeechHandler", "Starting speech recognition with intent...")
                isListening = true
                speechRecognizer?.startListening(intent)

            } catch (e: Exception) {
                Log.e("SpeechHandler", "Exception starting speech recognition", e)
                onError("Failed to start speech recognition: ${e.message}")
                isListening = false
            }
        }, 1000) // Increased delay to 1 second for better audio device availability
    }

    fun stopListening() {
        if (isListening) {
            speechRecognizer?.stopListening()
            isListening = false
        }
    }

    private fun startListeningWithAlternativeConfig() {
        Log.d("SpeechHandler", "Trying alternative speech recognition configuration...")

        // Clean up and wait a bit longer
        speechRecognizer?.destroy()
        speechRecognizer = null

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer?.setRecognitionListener(this)

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    // Try more conservative settings
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US") // Force English US
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)

                    // Force offline if available
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)

                    // Shorter timeouts for retry
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500)
                }

                Log.d("SpeechHandler", "Starting alternative speech recognition...")
                isListening = true
                speechRecognizer?.startListening(intent)

            } catch (e: Exception) {
                Log.e("SpeechHandler", "Alternative speech recognition failed", e)
                retryCount = 0
                onError("Speech recognition service unavailable")
            }
        }, 1500) // Longer delay for alternative approach
    }

    private fun createSpeechRecognitionIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // Use the most basic, device-native approach
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

            // Force offline mode to avoid Google services dependency
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)

            // Conservative timeout settings
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 500)

            // Disable partial results to reduce complexity
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)

            // Try to specify we want device-native recognition
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-US"))
        }
    }

    private fun getAvailableSpeechEngines(): List<String> {
        val engines = mutableListOf<String>()
        val pm = context.packageManager

        // Check for common speech recognition services
        val speechServices = listOf(
            "com.google.android.googlequicksearchbox" to "Google Voice Search",
            "com.samsung.android.bixby.agent" to "Samsung Bixby",
            "com.microsoft.cortana" to "Microsoft Cortana",
            "com.amazon.dee.app" to "Amazon Alexa",
            "com.nuance.nmdp.speechkit.demo" to "Nuance Dragon",
            "android" to "Android System Speech"
        )

        for ((packageName, engineName) in speechServices) {
            try {
                if (packageName == "android") {
                    // Always include Android system speech as fallback
                    engines.add(engineName)
                } else {
                    pm.getApplicationInfo(packageName, 0)
                    engines.add(engineName)
                }
            } catch (e: Exception) {
                // Engine not found, continue
            }
        }

        return engines
    }

    fun cleanup() {
        textToSpeech?.shutdown()
        speechRecognizer?.destroy()
    }

    // RecognitionListener implementation
    override fun onReadyForSpeech(params: Bundle?) {
        Log.d("SpeechHandler", "✅ Ready for speech - microphone is ready and listening")
    }

    override fun onBeginningOfSpeech() {
        Log.d("SpeechHandler", "🎙️ Beginning of speech - user started speaking")
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Audio level changed - log significant changes
        if (rmsdB > 0) {
            Log.v("SpeechHandler", "🔊 Audio level: $rmsdB dB (sound detected)")
        }
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        // Audio buffer received
    }

    override fun onEndOfSpeech() {
        Log.d("SpeechHandler", "End of speech")
        isListening = false
    }

    override fun onError(error: Int) {
        isListening = false
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error - check microphone"
            SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission denied"
            SpeechRecognizer.ERROR_NETWORK -> "Network error - check internet connection"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout - try again"
            SpeechRecognizer.ERROR_NO_MATCH -> "Could not understand speech - try speaking clearly"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service busy - try again in a moment"
            SpeechRecognizer.ERROR_SERVER -> "Speech recognition server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected - try again"
            else -> "Speech recognition error (code: $error)"
        }
        Log.e("SpeechHandler", "Speech recognition error: $errorMessage (code: $error)")

        // Try different approach on certain errors
        if (retryCount < maxRetries && (error == SpeechRecognizer.ERROR_NETWORK ||
                                       error == SpeechRecognizer.ERROR_SERVER ||
                                       error == SpeechRecognizer.ERROR_CLIENT)) {
            retryCount++
            Log.d("SpeechHandler", "Retrying with different configuration (attempt $retryCount)")
            startListeningWithAlternativeConfig()
        } else {
            retryCount = 0
            onError(errorMessage)
        }
    }

    override fun onResults(results: Bundle?) {
        isListening = false
        retryCount = 0 // Reset retry count on successful recognition
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val spokenText = matches[0]
            Log.d("SpeechHandler", "✅ Speech result: $spokenText")
            onSpeechResult(spokenText)
        } else {
            Log.w("SpeechHandler", "No speech recognized in results")
            onError("No speech recognized")
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        // Partial results received
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        // Speech recognition event
    }
}