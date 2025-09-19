package com.cognitiveassistant

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cognitiveassistant.model.UserType

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val usernameField = findViewById<EditText>(R.id.username)
        val passwordField = findViewById<EditText>(R.id.password)
        val roleGroup = findViewById<RadioGroup>(R.id.roleGroup)
        val doctorRadio = findViewById<RadioButton>(R.id.doctorRadio)
        val patientRadio = findViewById<RadioButton>(R.id.patientRadio)
        val loginButton = findViewById<Button>(R.id.loginButton)

        // Set default selection
        patientRadio.isChecked = true

        loginButton.setOnClickListener {
            val username = usernameField.text.toString()
            val password = passwordField.text.toString()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userType = if (doctorRadio.isChecked) UserType.DOCTOR else UserType.PATIENT

            // Simple authentication
            val isValid = when (userType) {
                UserType.DOCTOR -> username == "doctor1" && password == "doctor123"
                UserType.PATIENT -> username == "patient1" && password == "patient123"
            }

            if (isValid) {
                val intent = when (userType) {
                    UserType.PATIENT -> Intent(this, PatientProfileActivity::class.java)
                    UserType.DOCTOR -> Intent(this, TestSelectionActivity::class.java).apply {
                        putExtra("is_doctor", true)
                    }
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }
}