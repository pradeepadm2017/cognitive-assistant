package com.cognitiveassistant.model

import java.util.Date

data class Patient(
    val id: String,
    val name: String,
    val age: Int,
    val phoneNumber: String,
    val createdAt: Date = Date()
)