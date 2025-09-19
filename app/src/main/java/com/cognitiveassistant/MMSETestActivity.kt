package com.cognitiveassistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cognitiveassistant.data.TestResultsManager
import com.cognitiveassistant.model.MMSEResult
import com.cognitiveassistant.utils.AnswerVerifier
import com.cognitiveassistant.utils.SpeechHandler
import java.util.UUID

class MMSETestActivity : AppCompatActivity() {

    private lateinit var patientId: String
    private lateinit var patientName: String
    private val answers = mutableMapOf<String, Any>()
    private var currentQuestionIndex = 0
    private lateinit var speechHandler: SpeechHandler
    private var isWaitingForSpeech = false

    companion object {
        private const val RECORD_AUDIO_PERMISSION_CODE = 101
    }
    private val questions = listOf(
        // ORIENTATION TO TIME (5 points)
        MMSEQuestion("orientation_year", "What year is it?", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("orientation_season", "What season is it?", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("orientation_date", "What is today's date?", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("orientation_day", "What day of the week is it?", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("orientation_month", "What month is it?", listOf("Correct", "Incorrect"), 0, 1),

        // ORIENTATION TO PLACE (5 points)
        MMSEQuestion("orientation_state", "What state are we in?", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("orientation_county", "What county/region are we in?", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("orientation_town", "What city/town are we in?", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("orientation_hospital", "What is the name of this building/hospital?", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("orientation_floor", "What floor are we on?", listOf("Correct", "Incorrect"), 0, 1),

        // REGISTRATION (3 points) - Must all be answered correctly to proceed
        MMSEQuestion("registration_1", "I will say three words. Please repeat them: APPLE", listOf("Repeated correctly", "Did not repeat correctly"), 0, 1),
        MMSEQuestion("registration_2", "Second word: PENNY", listOf("Repeated correctly", "Did not repeat correctly"), 0, 1),
        MMSEQuestion("registration_3", "Third word: TABLE", listOf("Repeated correctly", "Did not repeat correctly"), 0, 1),

        // ATTENTION/CALCULATION (5 points) - Serial 7s OR spelling WORLD backwards
        MMSEQuestion("attention_1", "Now subtract 7 from 100. What is 100 minus 7?", listOf("93 (Correct)", "Other answer"), 0, 1),
        MMSEQuestion("attention_2", "Continue subtracting 7. What is 93 minus 7?", listOf("86 (Correct)", "Other answer"), 0, 1),
        MMSEQuestion("attention_3", "What is 86 minus 7?", listOf("79 (Correct)", "Other answer"), 0, 1),
        MMSEQuestion("attention_4", "What is 79 minus 7?", listOf("72 (Correct)", "Other answer"), 0, 1),
        MMSEQuestion("attention_5", "What is 72 minus 7?", listOf("65 (Correct)", "Other answer"), 0, 1),

        // RECALL (3 points) - Remember the three words from earlier
        MMSEQuestion("recall_1", "Now, what were the three words I asked you to remember?", listOf("APPLE (1st word)", "PENNY (2nd word)", "TABLE (3rd word)", "Cannot remember"), 0, 1),
        MMSEQuestion("recall_2", "What was the second word?", listOf("PENNY (Correct)", "APPLE", "TABLE", "Cannot remember"), 0, 1),
        MMSEQuestion("recall_3", "What was the third word?", listOf("TABLE (Correct)", "APPLE", "PENNY", "Cannot remember"), 0, 1),

        // LANGUAGE (9 points)
        MMSEQuestion("language_naming_1", "What is this object called? (Point to a watch/clock)", listOf("Watch/Clock", "Other answer"), 0, 1),
        MMSEQuestion("language_naming_2", "What is this object called? (Point to a pencil/pen)", listOf("Pencil/Pen", "Other answer"), 0, 1),
        MMSEQuestion("language_repetition", "Please repeat this phrase exactly: 'No ifs, ands, or buts'", listOf("Repeated exactly", "Not repeated exactly"), 0, 1),
        MMSEQuestion("language_comprehension_1", "Follow this instruction: Take this paper in your right hand", listOf("Followed correctly", "Did not follow correctly"), 0, 1),
        MMSEQuestion("language_comprehension_2", "Fold the paper in half", listOf("Followed correctly", "Did not follow correctly"), 0, 1),
        MMSEQuestion("language_comprehension_3", "Put the paper on the floor", listOf("Followed correctly", "Did not follow correctly"), 0, 1),
        MMSEQuestion("language_reading", "Read this and do what it says: 'CLOSE YOUR EYES'", listOf("Read and followed", "Did not read/follow"), 0, 1),
        MMSEQuestion("language_writing", "Write a complete sentence (any sentence with subject and verb)", listOf("Wrote complete sentence", "Did not write complete sentence"), 0, 1),
        MMSEQuestion("language_copying", "Copy this drawing (two intersecting pentagons)", listOf("Copied correctly", "Did not copy correctly"), 0, 1)
    )

    private lateinit var questionTitle: TextView
    private lateinit var questionText: TextView
    private lateinit var answerGroup: RadioGroup
    private lateinit var nextButton: Button
    private lateinit var submitButton: Button
    private lateinit var listenButton: Button
    private lateinit var statusText: TextView
    private lateinit var debugText: TextView
    private lateinit var fallbackInput: android.widget.EditText
    private lateinit var submitTypedButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mmse_test)

        patientId = intent.getStringExtra("patient_id") ?: ""
        patientName = intent.getStringExtra("patient_name") ?: ""

        questionTitle = findViewById(R.id.questionTitle)
        questionText = findViewById(R.id.questionText)
        answerGroup = findViewById(R.id.answerGroup)
        nextButton = findViewById(R.id.nextButton)
        submitButton = findViewById(R.id.submitButton)
        listenButton = findViewById(R.id.listenButton)
        statusText = findViewById(R.id.statusText)
        debugText = findViewById(R.id.debugText)
        fallbackInput = findViewById(R.id.fallbackInput)
        submitTypedButton = findViewById(R.id.submitTypedButton)

        // Initialize speech handler
        speechHandler = SpeechHandler(
            context = this,
            onSpeechResult = { spokenText ->
                handleSpeechResult(spokenText)
            },
            onError = { error ->
                handleSpeechError(error)
            }
        )

        // Show debug info
        showDebugInfo()

        // Check for audio permission
        if (checkAudioPermission()) {
            initializeSpeech()
        } else {
            requestAudioPermission()
        }

        nextButton.setOnClickListener {
            proceedToNextQuestion()
        }

        listenButton.setOnClickListener {
            if (!isWaitingForSpeech) {
                startListeningForAnswer()
            }
        }

        submitButton.setOnClickListener {
            submitTest()
        }

        submitTypedButton.setOnClickListener {
            val typedAnswer = fallbackInput.text.toString().trim()
            if (typedAnswer.isNotEmpty()) {
                handleSpeechResult(typedAnswer)
                fallbackInput.text.clear()
            } else {
                Toast.makeText(this, "Please enter your answer", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAudioPermission(): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        Log.d("MMSETestActivity", "Audio permission granted: $hasPermission")
        return hasPermission
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeSpeech()
            } else {
                Toast.makeText(this, "Audio permission required for speech functionality", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun initializeSpeech() {
        speechHandler.initializeTTS {
            displayQuestion()
        }
    }

    private fun displayQuestion() {
        val question = questions[currentQuestionIndex]
        questionTitle.text = "Question ${currentQuestionIndex + 1} of ${questions.size}"

        // Get the actual question text for speech
        val spokenQuestionText = AnswerVerifier.getQuestionText(question.id)
        questionText.text = spokenQuestionText

        // Hide radio buttons and show speech interface
        answerGroup.visibility = RadioGroup.GONE
        listenButton.visibility = Button.VISIBLE
        statusText.visibility = TextView.VISIBLE
        statusText.text = "Tap 'Listen for Answer' to speak your response"

        // Hide fallback input initially
        fallbackInput.visibility = android.widget.EditText.GONE
        submitTypedButton.visibility = Button.GONE

        // Initially disable next button until answer is given
        nextButton.isEnabled = false
        nextButton.alpha = 0.5f
        nextButton.text = "Next (Answer Required)"

        submitButton.visibility = Button.GONE
        nextButton.visibility = Button.VISIBLE

        // Automatically speak the question
        speakQuestion(spokenQuestionText)
    }

    private fun speakQuestion(questionText: String) {
        statusText.text = "🔊 Speaking question..."
        speechHandler.speakText(questionText) {
            // After question is spoken, enable listening
            statusText.text = "Tap 'Listen for Answer' to speak your response"
        }
    }

    private fun startListeningForAnswer() {
        if (isWaitingForSpeech) return

        // Double-check audio permission before starting
        if (!checkAudioPermission()) {
            Toast.makeText(this, "Microphone permission is required for speech recognition", Toast.LENGTH_LONG).show()
            requestAudioPermission()
            return
        }

        isWaitingForSpeech = true
        statusText.text = "🎤 Listening... Please speak your answer clearly"
        listenButton.isEnabled = false
        listenButton.text = "Listening..."

        // Update debug info
        updateDebugInfo("Starting speech recognition...")

        speechHandler.startListening()
    }

    private fun handleSpeechResult(spokenText: String) {
        isWaitingForSpeech = false
        listenButton.isEnabled = true
        listenButton.text = "Listen for Answer"

        val currentQuestion = questions[currentQuestionIndex]
        val isCorrect = AnswerVerifier.verifyAnswer(currentQuestion.id, spokenText)

        statusText.text = "You said: \"$spokenText\"\nResult: ${if (isCorrect) "✓ Correct" else "✗ Incorrect"}"

        // Store the result (0 for correct, 1 for incorrect)
        answers[currentQuestion.id] = if (isCorrect) 0 else 1

        // Enable next button
        nextButton.isEnabled = true
        nextButton.alpha = 1.0f
        nextButton.text = "Next Question"

        // Auto-advance after 2 seconds
        nextButton.postDelayed({
            if (currentQuestionIndex < questions.size) {
                proceedToNextQuestion()
            }
        }, 2000)
    }

    private fun handleSpeechError(error: String) {
        isWaitingForSpeech = false
        listenButton.isEnabled = true
        listenButton.text = "🎤 Listen for Answer"

        // Update debug info with error details
        updateDebugInfo("Speech Error: $error")

        // Show user-friendly error message and fallback option
        statusText.text = when {
            error.contains("permission", ignoreCase = true) -> "❌ Microphone permission needed\nPlease grant permission and try again\n\n💡 Alternative: Use text input below"
            error.contains("network", ignoreCase = true) -> "❌ Network error\nCheck internet connection and try again\n\n💡 Alternative: Use text input below"
            error.contains("No speech", ignoreCase = true) -> "❌ No speech detected\nSpeak clearly and try again\n\n💡 Alternative: Use text input below"
            error.contains("Could not understand", ignoreCase = true) -> "❌ Speech unclear\nSpeak slowly and clearly, then try again\n\n💡 Alternative: Use text input below"
            else -> "❌ Speech error: $error\nTry speaking again or use text input below"
        }

        // Show fallback text input option after speech error
        fallbackInput.visibility = android.widget.EditText.VISIBLE
        submitTypedButton.visibility = Button.VISIBLE
        fallbackInput.hint = "Type your answer here as backup option"

        // Show helpful toast
        when {
            error.contains("permission", ignoreCase = true) -> {
                Toast.makeText(this, "Please grant microphone permission in app settings", Toast.LENGTH_LONG).show()
            }
            error.contains("network", ignoreCase = true) -> {
                Toast.makeText(this, "Speech recognition requires internet connection", Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(this, "Speech recognition failed. Try speaking clearly.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun proceedToNextQuestion() {
        if (answers.containsKey(questions[currentQuestionIndex].id)) {
            currentQuestionIndex++
            if (currentQuestionIndex < questions.size) {
                displayQuestion()
            } else {
                showSubmitButton()
            }
        } else {
            Toast.makeText(this, "Please provide an answer before continuing", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCurrentAnswer(): Boolean {
        val selectedId = answerGroup.checkedRadioButtonId
        if (selectedId == -1) return false

        val question = questions[currentQuestionIndex]
        answers[question.id] = selectedId
        return true
    }

    private fun showSubmitButton() {
        questionTitle.text = "Test Complete"
        questionText.text = "You have completed all MMSE questions. Click Submit to finish the test."

        // Hide speech interface
        answerGroup.visibility = RadioGroup.GONE
        listenButton.visibility = Button.GONE
        statusText.visibility = TextView.GONE

        nextButton.visibility = Button.GONE
        submitButton.visibility = Button.VISIBLE

        // Speak completion message
        speechHandler.speakText("Test completed. All questions answered. You may now submit your results.")
    }

    override fun onDestroy() {
        super.onDestroy()
        speechHandler.cleanup()
    }

    private fun submitTest() {
        val totalScore = calculateScore()
        val interpretation = TestResultsManager.calculateMMSEInterpretation(totalScore)

        val result = MMSEResult(
            testId = UUID.randomUUID().toString(),
            patientId = patientId,
            patientName = patientName,
            answers = answers.toMap(),
            totalScore = totalScore,
            interpretation = interpretation
        )

        TestResultsManager.saveMMSEResult(result)

        Toast.makeText(this, "Test completed! Score: $totalScore/30", Toast.LENGTH_LONG).show()

        // Redirect back to test selection
        val intent = Intent(this, TestSelectionActivity::class.java)
        intent.putExtra("patient_id", patientId)
        intent.putExtra("patient_name", patientName)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun showDebugInfo() {
        val debugInfo = StringBuilder()

        // Check speech recognition availability
        val speechAvailable = android.speech.SpeechRecognizer.isRecognitionAvailable(this)
        debugInfo.append("🔍 DEBUG INFO:\n")
        debugInfo.append("Speech Available: $speechAvailable\n")

        // Check audio permission
        val audioPermission = checkAudioPermission()
        debugInfo.append("Audio Permission: $audioPermission\n")

        // Check for Google app
        try {
            val pm = packageManager
            val googleAppInfo = pm.getApplicationInfo("com.google.android.googlequicksearchbox", 0)
            debugInfo.append("Google App: Found (${googleAppInfo.enabled})\n")
        } catch (e: Exception) {
            debugInfo.append("Google App: Not found\n")
        }

        // Device info
        debugInfo.append("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}\n")
        debugInfo.append("Android: ${android.os.Build.VERSION.RELEASE}\n")

        debugText.text = debugInfo.toString()
        debugText.visibility = TextView.VISIBLE
    }

    private fun updateDebugInfo(message: String) {
        val currentText = debugText.text.toString()
        debugText.text = "$currentText\n⚠️ $message"
    }

    private fun calculateScore(): Int {
        var score = 0
        questions.forEach { question ->
            val selectedAnswer = answers[question.id] as? Int ?: -1
            if (selectedAnswer == question.correctAnswer) {
                score += question.points
            }
        }
        return score
    }

    data class MMSEQuestion(
        val id: String,
        val text: String,
        val options: List<String>,
        val correctAnswer: Int, // -1 means any answer is valid (subjective)
        val points: Int
    )
}