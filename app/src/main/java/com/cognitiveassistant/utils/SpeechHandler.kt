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
            onError("Speech recognition not available")
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(this)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Please speak your answer")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
        }

        isListening = true
        speechRecognizer?.startListening(intent)
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
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech match found"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Speech recognition error"
        }
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