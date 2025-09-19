package com.cognitiveassistant

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cognitiveassistant.data.TestResultsManager
import java.text.SimpleDateFormat
import java.util.Locale

class TestResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_results)

        val resultsContainer = findViewById<LinearLayout>(R.id.resultsContainer)
        val noResultsText = findViewById<TextView>(R.id.noResultsText)

        val results = TestResultsManager.getMMSEResults()

        if (results.isEmpty()) {
            noResultsText.visibility = TextView.VISIBLE
            resultsContainer.visibility = LinearLayout.GONE
        } else {
            noResultsText.visibility = TextView.GONE
            resultsContainer.visibility = LinearLayout.VISIBLE

            results.sortedByDescending { it.testDate }.forEach { result ->
                val resultView = createResultView(result)
                resultsContainer.addView(resultView)
            }
        }
    }

    private fun createResultView(result: com.cognitiveassistant.model.MMSEResult): LinearLayout {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundColor(0xFFF5F5F5.toInt())
        }

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 16)
        }
        container.layoutParams = layoutParams

        // Patient Info
        val patientInfo = TextView(this).apply {
            text = "Patient: ${result.patientName}\nID: ${result.patientId}"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        // Test Date
        val testDate = TextView(this).apply {
            text = "Test Date: ${dateFormat.format(result.testDate)}"
            textSize = 14f
        }

        // Score
        val score = TextView(this).apply {
            text = "MMSE Score: ${result.totalScore}/${result.maxScore}"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(getScoreColor(result.totalScore))
        }

        // Interpretation
        val interpretation = TextView(this).apply {
            text = "Interpretation: ${result.interpretation}"
            textSize = 14f
        }

        // Detailed breakdown
        val breakdown = TextView(this).apply {
            text = getDetailedBreakdown(result)
            textSize = 12f
            setTextColor(0xFF666666.toInt())
        }

        container.addView(patientInfo)
        container.addView(testDate)
        container.addView(score)
        container.addView(interpretation)
        container.addView(breakdown)

        return container
    }

    private fun getScoreColor(score: Int): Int {
        return when {
            score >= 24 -> 0xFF4CAF50.toInt() // Green
            score >= 18 -> 0xFFFF9800.toInt() // Orange
            score >= 10 -> 0xFFFF5722.toInt() // Deep Orange
            else -> 0xFFF44336.toInt() // Red
        }
    }

    private fun getDetailedBreakdown(result: com.cognitiveassistant.model.MMSEResult): String {
        val orientationScore = calculateOrientationScore(result.answers)
        val registrationScore = calculateRegistrationScore(result.answers)
        val attentionScore = calculateAttentionScore(result.answers)
        val recallScore = calculateRecallScore(result.answers)
        val languageScore = calculateLanguageScore(result.answers)

        return """
            Score Breakdown:
            • Orientation (Time & Place): $orientationScore/10
            • Registration: $registrationScore/3
            • Attention & Calculation: $attentionScore/5
            • Recall: $recallScore/3
            • Language: $languageScore/9

            Total Score: ${result.totalScore}/30
        """.trimIndent()
    }

    private fun calculateOrientationScore(answers: Map<String, Any>): Int {
        var score = 0
        // Orientation questions (first 10) - "Correct" is option 0
        for (i in 0..9) {
            val key = when (i) {
                0 -> "orientation_year"
                1 -> "orientation_season"
                2 -> "orientation_date"
                3 -> "orientation_day"
                4 -> "orientation_month"
                5 -> "orientation_state"
                6 -> "orientation_county"
                7 -> "orientation_town"
                8 -> "orientation_hospital"
                9 -> "orientation_floor"
                else -> ""
            }
            if (answers[key] == 0) score++
        }
        return score
    }

    private fun calculateRegistrationScore(answers: Map<String, Any>): Int {
        var score = 0
        // Registration questions - "Repeated correctly" is option 0
        listOf("registration_1", "registration_2", "registration_3").forEach { key ->
            if (answers[key] == 0) score++
        }
        return score
    }

    private fun calculateAttentionScore(answers: Map<String, Any>): Int {
        var score = 0
        // Attention questions - correct answer is option 0
        listOf("attention_1", "attention_2", "attention_3", "attention_4", "attention_5").forEach { key ->
            if (answers[key] == 0) score++
        }
        return score
    }

    private fun calculateRecallScore(answers: Map<String, Any>): Int {
        var score = 0
        // Recall questions - correct answer is option 0
        listOf("recall_1", "recall_2", "recall_3").forEach { key ->
            if (answers[key] == 0) score++
        }
        return score
    }

    private fun calculateLanguageScore(answers: Map<String, Any>): Int {
        var score = 0
        // Language questions - correct answer is option 0
        listOf(
            "language_naming_1", "language_naming_2", "language_repetition",
            "language_comprehension_1", "language_comprehension_2", "language_comprehension_3",
            "language_reading", "language_writing", "language_copying"
        ).forEach { key ->
            if (answers[key] == 0) score++
        }
        return score
    }
}