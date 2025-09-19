package com.cognitiveassistant

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class TestSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_selection)

        val patientInfoText = findViewById<TextView>(R.id.patientInfoText)
        val viewResultsCard = findViewById<MaterialCardView>(R.id.viewResultsCard)
        val mmseCard = findViewById<MaterialCardView>(R.id.mmseCard)
        val mocaCard = findViewById<MaterialCardView>(R.id.mocaCard)
        val rorschachCard = findViewById<MaterialCardView>(R.id.rorschachCard)

        val patientName = intent.getStringExtra("patient_name") ?: "User"
        val patientId = intent.getStringExtra("patient_id") ?: ""
        val isDoctor = intent.getBooleanExtra("is_doctor", false)

        // Display patient details
        if (patientName != "User" && patientId.isNotEmpty()) {
            patientInfoText.text = "Patient: $patientName\nID: $patientId"
            patientInfoText.visibility = TextView.VISIBLE
        } else {
            patientInfoText.visibility = TextView.GONE
        }

        // Show/hide results view for doctors
        if (isDoctor) {
            viewResultsCard.visibility = MaterialCardView.VISIBLE
        } else {
            viewResultsCard.visibility = MaterialCardView.GONE
        }

        viewResultsCard.setOnClickListener {
            val intent = Intent(this, TestResultsActivity::class.java)
            startActivity(intent)
        }

        mmseCard.setOnClickListener {
            val intent = Intent(this, MMSETestActivity::class.java)
            intent.putExtra("patient_name", patientName)
            intent.putExtra("patient_id", patientId)
            startActivity(intent)
        }

        mocaCard.setOnClickListener {
            Toast.makeText(this, "MoCA test selected for $patientName (Coming soon)", Toast.LENGTH_SHORT).show()
        }

        rorschachCard.setOnClickListener {
            Toast.makeText(this, "Rorschach test selected for $patientName (Coming soon)", Toast.LENGTH_SHORT).show()
        }
    }
}