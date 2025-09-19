package com.cognitiveassistant.repository

import com.cognitiveassistant.model.Patient
import com.cognitiveassistant.model.User
import com.cognitiveassistant.model.UserType
import java.util.Calendar
import java.util.Date
import java.util.UUID

object InMemoryRepository {
    private val users = mutableListOf<User>().apply {
        add(User("doctor1", "doctor123", UserType.DOCTOR))
        add(User("patient1", "patient123", UserType.PATIENT))
    }

    private val patients = mutableListOf<Patient>()

    fun authenticateUser(username: String, password: String, userType: UserType): User? {
        return users.find {
            it.username == username &&
            it.password == password &&
            it.userType == userType
        }
    }

    fun generatePatientId(): String {
        return "P${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
    }

    fun savePatient(patient: Patient) {
        cleanExpiredPatients()
        patients.add(patient)
    }

    private fun cleanExpiredPatients() {
        val sevenDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }.time

        patients.removeAll { patient ->
            patient.createdAt.before(sevenDaysAgo)
        }
    }

    fun getPatients(): List<Patient> {
        cleanExpiredPatients()
        return patients.toList()
    }
}