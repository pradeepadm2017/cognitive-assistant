package com.cognitiveassistant

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class TestSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_selection)

        val mmseCard = findViewById<MaterialCardView>(R.id.mmseCard)
        val mocaCard = findViewById<MaterialCardView>(R.id.mocaCard)
        val rorschachCard = findViewById<MaterialCardView>(R.id.rorschachCard)

        val patientName = intent.getStringExtra("patient_name") ?: "User"

        mmseCard.setOnClickListener {
            Toast.makeText(this, "MMSE test selected for $patientName", Toast.LENGTH_SHORT).show()
        }

        mocaCard.setOnClickListener {
            Toast.makeText(this, "MoCA test selected for $patientName", Toast.LENGTH_SHORT).show()
        }

        rorschachCard.setOnClickListener {
            Toast.makeText(this, "Rorschach test selected for $patientName", Toast.LENGTH_SHORT).show()
        }
    }
}