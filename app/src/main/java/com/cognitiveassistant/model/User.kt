package com.cognitiveassistant.model

data class User(
    val username: String,
    val password: String,
    val userType: UserType
)