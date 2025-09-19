package com.cognitiveassistant.data

import com.cognitiveassistant.model.MMSEResult
import java.util.Calendar
import java.util.Date

object TestResultsManager {
    private val mmseResults = mutableListOf<MMSEResult>()

    fun saveMMSEResult(result: MMSEResult) {
        cleanExpiredResults()
        mmseResults.add(result)
    }

    fun getMMSEResults(): List<MMSEResult> {
        cleanExpiredResults()
        return mmseResults.toList()
    }

    fun getMMSEResultsForPatient(patientId: String): List<MMSEResult> {
        cleanExpiredResults()
        return mmseResults.filter { it.patientId == patientId }
    }

    private fun cleanExpiredResults() {
        val sevenDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }.time

        mmseResults.removeAll { result ->
            result.testDate.before(sevenDaysAgo)
        }
    }

    fun calculateMMSEInterpretation(score: Int): String {
        return when {
            score >= 24 -> "Normal cognitive function (24-30 points)"
            score >= 18 -> "Mild cognitive impairment (18-23 points)"
            score >= 10 -> "Moderate cognitive impairment (10-17 points)"
            else -> "Severe cognitive impairment (0-9 points)"
        }
    }
}