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
        val orientationScore = calculateCategoryScore(result.answers, "orientation_", 10)
        val registrationScore = calculateCategoryScore(result.answers, "registration_", 3)
        val attentionScore = calculateCategoryScore(result.answers, "attention_", 5)
        val recallScore = calculateCategoryScore(result.answers, "recall_", 3)
        val languageScore = calculateCategoryScore(result.answers, "language_", 9)

        return """
            Score Breakdown:
            • Orientation: $orientationScore/10
            • Registration: $registrationScore/3
            • Attention & Calculation: $attentionScore/5
            • Recall: $recallScore/3
            • Language: $languageScore/9
        """.trimIndent()
    }

    private fun calculateCategoryScore(answers: Map<String, Any>, prefix: String, maxScore: Int): Int {
        var score = 0
        answers.entries.filter { it.key.startsWith(prefix) }.forEach { entry ->
            val answer = entry.value as? Int ?: -1
            if (answer == 0) { // Assuming 0 is correct for most questions
                score++
            }
        }
        return minOf(score, maxScore)
    }
}