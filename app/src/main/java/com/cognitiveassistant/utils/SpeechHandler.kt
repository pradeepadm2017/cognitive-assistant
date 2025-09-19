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
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition not available on this device")
            return
        }

        // Stop TTS if it's still speaking
        textToSpeech?.stop()

        // Clean up any existing recognizer
        speechRecognizer?.destroy()

        // Wait a moment for TTS to fully stop
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer?.setRecognitionListener(this)

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Please speak your answer")
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }

                isListening = true
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                onError("Failed to start speech recognition: ${e.message}")
                isListening = false
            }
        }, 500) // 500ms delay to ensure TTS has stopped
    }

    fun stopListening() {
        if (isListening) {
            speechRecognizer?.stopListening()
            isListening = false
        }
    }

    fun cleanup() {
        textToSpeech?.shutdown()
        speechRecognizer?.destroy()
    }

    // RecognitionListener implementation
    override fun onReadyForSpeech(params: Bundle?) {
        Log.d("SpeechHandler", "Ready for speech")
    }

    override fun onBeginningOfSpeech() {
        Log.d("SpeechHandler", "Beginning of speech")
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Audio level changed
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
        onError(errorMessage)
    }

    override fun onResults(results: Bundle?) {
        isListening = false
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val spokenText = matches[0]
            Log.d("SpeechHandler", "Speech result: $spokenText")
            onSpeechResult(spokenText)
        } else {
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