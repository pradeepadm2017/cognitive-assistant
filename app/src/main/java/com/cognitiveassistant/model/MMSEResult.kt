package com.cognitiveassistant.model

import java.util.Date

data class MMSEResult(
    val testId: String,
    val patientId: String,
    val patientName: String,
    val answers: Map<String, Any>,
    val totalScore: Int,
    val maxScore: Int = 30,
    val interpretation: String,
    val testDate: Date = Date()
)