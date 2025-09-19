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
        MMSEQuestion("orientation_year", "What year is it?", listOf("2023", "2024", "2025", "2026"), 2, 1),
        MMSEQuestion("orientation_season", "What season is it?", listOf("Spring", "Summer", "Fall", "Winter"), -1, 1),
        MMSEQuestion("orientation_date", "What is today's date?", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("orientation_day", "What day of the week is it?", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("orientation_month", "What month is it?", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("orientation_state", "What state are we in?", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("orientation_county", "What county are we in?", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("orientation_town", "What town/city are we in?", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("orientation_hospital", "What is the name of this hospital/clinic?", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("orientation_floor", "What floor are we on?", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("registration_apple", "I will name three objects. Please repeat: APPLE", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("registration_penny", "Second object: PENNY", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("registration_table", "Third object: TABLE", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("attention_serial7_1", "Subtract 7 from 100 (93)", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("attention_serial7_2", "Subtract 7 from 93 (86)", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("attention_serial7_3", "Subtract 7 from 86 (79)", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("attention_serial7_4", "Subtract 7 from 79 (72)", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("attention_serial7_5", "Subtract 7 from 72 (65)", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("recall_apple", "What was the first object I named earlier?", listOf("Apple", "Penny", "Table", "Don't remember"), 0, 1),
        MMSEQuestion("recall_penny", "What was the second object?", listOf("Apple", "Penny", "Table", "Don't remember"), 1, 1),
        MMSEQuestion("recall_table", "What was the third object?", listOf("Apple", "Penny", "Table", "Don't remember"), 2, 1),
        MMSEQuestion("language_watch", "What is this object? (Show a watch)", listOf("Watch/Clock", "Other answer"), 0, 1),
        MMSEQuestion("language_pencil", "What is this object? (Show a pencil)", listOf("Pencil/Pen", "Other answer"), 0, 1),
        MMSEQuestion("language_repeat", "Repeat: 'No ifs, ands, or buts'", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("language_command_1", "Take the paper in your right hand", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("language_command_2", "Fold it in half", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("language_command_3", "Put it on the floor", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("language_read", "Read and obey: 'CLOSE YOUR EYES'", listOf("Correct", "Incorrect"), 0, 1),
        MMSEQuestion("language_write", "Write a sentence", listOf("Complete sentence", "Incomplete/No sentence"), 0, 1),
        MMSEQuestion("language_copy", "Copy this design (intersecting pentagons)", listOf("Correct", "Incorrect"), 0, 1)
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
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
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
        question.options.forEachIndexed { index, option ->
            val radioButton = RadioButton(this)
            radioButton.text = option
            radioButton.id = index
            answerGroup.addView(radioButton)
        }

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