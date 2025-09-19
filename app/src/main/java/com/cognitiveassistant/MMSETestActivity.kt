package com.cognitiveassistant

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cognitiveassistant.data.TestResultsManager
import com.cognitiveassistant.model.MMSEResult
import java.util.UUID

class MMSETestActivity : AppCompatActivity() {

    private lateinit var patientId: String
    private lateinit var patientName: String
    private val answers = mutableMapOf<String, Any>()
    private var currentQuestionIndex = 0
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

        displayQuestion()

        nextButton.setOnClickListener {
            if (saveCurrentAnswer()) {
                currentQuestionIndex++
                if (currentQuestionIndex < questions.size) {
                    displayQuestion()
                } else {
                    showSubmitButton()
                }
            } else {
                Toast.makeText(this, "Please select an answer before continuing", Toast.LENGTH_SHORT).show()
            }
        }

        submitButton.setOnClickListener {
            submitTest()
        }
    }

    private fun displayQuestion() {
        val question = questions[currentQuestionIndex]
        questionTitle.text = "Question ${currentQuestionIndex + 1} of ${questions.size}"
        questionText.text = question.text

        answerGroup.removeAllViews()
        answerGroup.clearCheck()
        question.options.forEachIndexed { index, option ->
            val radioButton = RadioButton(this)
            radioButton.text = option
            radioButton.id = index
            answerGroup.addView(radioButton)
        }

        // Add listener to enable/disable next button
        answerGroup.setOnCheckedChangeListener { _, _ ->
            nextButton.isEnabled = answerGroup.checkedRadioButtonId != -1
            nextButton.alpha = if (answerGroup.checkedRadioButtonId != -1) 1.0f else 0.5f
        }

        // Initially disable next button until answer is selected
        nextButton.isEnabled = false
        nextButton.alpha = 0.5f

        submitButton.visibility = Button.GONE
        nextButton.visibility = Button.VISIBLE
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
        questionText.text = "You have completed all questions. Click Submit to finish the test."
        answerGroup.removeAllViews()
        nextButton.visibility = Button.GONE
        submitButton.visibility = Button.VISIBLE
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