package com.cognitiveassistant

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID

class PatientProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile)

        val patientIdField = findViewById<EditText>(R.id.patientId)
        val nameField = findViewById<EditText>(R.id.patientName)
        val ageField = findViewById<EditText>(R.id.age)
        val phoneField = findViewById<EditText>(R.id.phoneNumber)
        val nextButton = findViewById<Button>(R.id.nextButton)

        // Generate and display patient ID
        val generatedId = "P${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
        patientIdField.setText(generatedId)
        patientIdField.isEnabled = false

        nextButton.setOnClickListener {
            val name = nameField.text.toString()
            val age = ageField.text.toString()
            val phone = phoneField.text.toString()

            if (name.isBlank() || age.isBlank() || phone.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ageInt = age.toIntOrNull()
            if (ageInt == null || ageInt <= 0) {
                Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, TestSelectionActivity::class.java)
            intent.putExtra("patient_name", name)
            intent.putExtra("patient_id", generatedId)
            startActivity(intent)
        }
    }
}